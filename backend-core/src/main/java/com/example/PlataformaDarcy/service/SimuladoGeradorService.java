package com.example.PlataformaDarcy.service;

// ✅ MANTENDO SUAS IMPORTAÇÕES ORIGINAIS
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.model.dto.SlotDefinition;
import com.example.PlataformaDarcy.repository.*;
import com.example.PlataformaDarcy.service.blueprint.BlueprintFactory;
import com.example.PlataformaDarcy.service.blueprint.BlueprintInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimuladoGeradorService {

    @Autowired private BlueprintFactory blueprintFactory;
    @Autowired private GeminiService geminiService;
    @Autowired private JdbcTemplate db;

    @Autowired private ProvaRepository provaRepository;
    @Autowired private BlocoRepository blocoRepository;
    @Autowired private QuestaoRepository questaoRepository;

    private List<Map<String, Object>> cacheObras;
    private List<Map<String, Object>> cacheTemas;
    private final ObjectMapper mapper = new ObjectMapper();

    public void carregarAcervo(int etapa) {
        try {
            if (this.cacheObras != null && !this.cacheObras.isEmpty()) return;

            String path = "data/pas" + etapa + "/";
            // Verifica existência para evitar falhas de IO
            if (new ClassPathResource(path + "acervo_obras.json").exists()) {
                this.cacheObras = mapper.readValue(new ClassPathResource(path + "acervo_obras.json").getInputStream(), new TypeReference<>() {});
            }
            if (new ClassPathResource(path + "banco_temas.json").exists()) {
                this.cacheTemas = mapper.readValue(new ClassPathResource(path + "banco_temas.json").getInputStream(), new TypeReference<>() {});
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao carregar JSONs do PAS " + etapa + ": " + e.getMessage());
        }
    }

    @Transactional
    public void gerarSimuladoOficial(int etapa) {
        carregarAcervo(etapa);
        BlueprintInterface blueprint = blueprintFactory.getBlueprint(etapa);

        Prova prova = new Prova();
        prova.setAno(2025);
        prova.setEtapa(etapa);
        prova.setTitulo("Simulado Oficial PAS " + etapa + " - Semana " + Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
        prova.setOrigem("IA_OFICIAL");
        prova.setNomeArquivoPdf("GERADO_AUTOMATICAMENTE");

        prova = provaRepository.save(prova);

        System.out.println("🚀 Iniciando geração da Prova ID: " + prova.getId());

        int contadorItens = 1;

        for (SlotDefinition slot : blueprint.getReceita()) {
            System.out.println("   -> Processando: " + slot.getNomeSlot());

            Map<String, Object> candidato = selecionarCandidato(slot);
            if (candidato == null) {
                System.out.println("⚠️ Nenhum candidato encontrado para o slot: " + slot.getNomeSlot());
                continue;
            }

            registrarUso((String) candidato.get("id"), prova.getId());

            String contexto = slot.getFonteDados().equals("OBRAS") ?
                    (String) candidato.get("texto_contexto") :
                    (String) candidato.get("prompt_tecnico");

            String jsonResposta = geminiService.gerarConteudoBloco(contexto, slot.getPromptInstrucao(), slot.getQuantidadeItens());

            try {
                String jsonLimpo = limparJson(jsonResposta);
                JsonNode root = mapper.readTree(jsonLimpo);

                Bloco bloco = new Bloco();
                bloco.setProva(prova);

                // 👇 AQUI MUDOU: Usamos o método seguro para não quebrar com MissingNode
                bloco.setTextoBase(lerTextoSeguro(root, "texto_base_gerado", "Texto indisponível"));

                if (candidato.containsKey("url_midia")) {
                    bloco.setCaminhoImagem((String) candidato.get("url_midia"));
                }
                bloco = blocoRepository.save(bloco);

                JsonNode itensNode = root.path("itens");
                if (!itensNode.isMissingNode() && itensNode.isArray()) {
                    for (JsonNode itemNode : itensNode) {
                        Questao q = new Questao();
                        q.setBloco(bloco);
                        q.setProva(prova);
                        q.setNumero(contadorItens++);

                        // 👇 LEITURA SEGURA TAMBÉM NAS QUESTÕES
                        q.setEnunciado(lerTextoSeguro(itemNode, "enunciado", "Enunciado não gerado"));
                        q.setGabarito(lerTextoSeguro(itemNode, "gabarito", "A"));

                        try {
                            String tipoStr = lerTextoSeguro(itemNode, "tipo", "A");
                            q.setTipo(TipoQuestao.valueOf(tipoStr));
                        } catch (IllegalArgumentException ex) {
                            q.setTipo(TipoQuestao.A);
                        }

                        q.setTags(slot.getTagsObrigatorias().toString());
                        questaoRepository.save(q);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ Erro ao processar resposta da IA para o slot " + slot.getNomeSlot() + ": " + e.getMessage());
            }
        }
        System.out.println("🏁 Prova Gerada com Sucesso!");
    }

    // --- MÉTODOS AUXILIARES ---

    /**
     * Método de segurança para a biblioteca tools.jackson.
     * Evita a exceção 'MissingNode' cannot be converted to String.
     */
    private String lerTextoSeguro(JsonNode parent, String campo, String padrao) {
        JsonNode node = parent.path(campo);
        if (node.isMissingNode() || node.isNull()) {
            return padrao;
        }
        return node.asText(padrao);
    }

    private String limparJson(String respostaIa) {
        if (respostaIa == null) return "{}";
        String limpa = respostaIa.trim();
        if (limpa.startsWith("```json")) {
            limpa = limpa.substring(7);
        }
        if (limpa.startsWith("```")) {
            limpa = limpa.substring(3);
        }
        if (limpa.endsWith("```")) {
            limpa = limpa.substring(0, limpa.length() - 3);
        }
        return limpa.trim();
    }

    private Map<String, Object> selecionarCandidato(SlotDefinition slot) {
        List<Map<String, Object>> fonte = "OBRAS".equals(slot.getFonteDados()) ? cacheObras : cacheTemas;
        if (fonte == null) return null;

        List<Map<String, Object>> filtrados = fonte.stream().filter(item -> {
            List<String> tags = (List<String>) item.get("tags");
            if (tags == null) return false;
            return new HashSet<>(tags).containsAll(slot.getTagsObrigatorias());
        }).collect(Collectors.toList());

        try {
            List<String> usados = db.queryForList("SELECT obra_id_json FROM controle_uso_obras", String.class);
            filtrados.removeIf(i -> usados.contains(i.get("id")));
        } catch (Exception ignored) { }

        if (filtrados.isEmpty()) return null;
        return filtrados.get(new Random().nextInt(filtrados.size()));
    }

    private void registrarUso(String idJson, Long provaId) {
        try {
            db.update("INSERT INTO controle_uso_obras (obra_id_json, simulado_id, data_uso) VALUES (?, ?, NOW())", idJson, provaId);
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível registrar o uso da obra.");
        }
    }
}