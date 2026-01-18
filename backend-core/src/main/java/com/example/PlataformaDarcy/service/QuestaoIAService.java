package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.Questao;
import com.example.PlataformaDarcy.model.TipoQuestao;
import com.example.PlataformaDarcy.repository.QuestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço para geração de questões via IA.
 * Gera questões baseadas em exemplos existentes com mesmas tags.
 */
@Service
public class QuestaoIAService {

    @Autowired
    private QuestaoRepository questaoRepo;

    @Autowired
    private GeminiService geminiService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Gera questões via IA baseadas em exemplos existentes.
     * As questões NÃO são salvas automaticamente - retornam para preview.
     *
     * @param tag        Tag/matéria para gerar questões
     * @param quantidade Quantidade de questões a gerar (1-10)
     * @param etapa      Etapa do PAS (1, 2 ou 3)
     * @return Lista de questões geradas (sem ID, não persistidas)
     */
    public List<Questao> gerarQuestoesComIA(String tag, int quantidade, Integer etapa) {
        // Limita quantidade
        quantidade = Math.min(Math.max(quantidade, 1), 10);

        // Busca exemplos existentes para usar como modelo
        List<Questao> exemplos = questaoRepo.findByTagsContainingIgnoreCase(tag);

        // Filtra por etapa se especificada
        if (etapa != null) {
            exemplos = exemplos.stream()
                    .filter(q -> q.getProva() != null && etapa.equals(q.getProva().getEtapa()))
                    .collect(Collectors.toList());
        }

        // Precisa de ao menos 1 exemplo
        if (exemplos.isEmpty()) {
            throw new RuntimeException("Não há questões com a tag '" + tag + "' para usar como exemplo.");
        }

        // Pega até 3 exemplos aleatórios
        Collections.shuffle(exemplos);
        List<Questao> exemplosSelecionados = exemplos.stream().limit(3).collect(Collectors.toList());

        // Monta o prompt com os exemplos
        String prompt = montarPromptGeracao(exemplosSelecionados, tag, quantidade);

        // Chama a IA
        String respostaJson = geminiService.gerarConteudoBloco(prompt, "", quantidade);

        // Parseia a resposta e converte em objetos Questao
        return parseRespostaIA(respostaJson, tag);
    }

    private String montarPromptGeracao(List<Questao> exemplos, String tag, int quantidade) {
        StringBuilder sb = new StringBuilder();

        sb.append("Você é um elaborador de questões para o PAS/UnB (Vestibular da Universidade de Brasília).\n\n");
        sb.append("Analise os exemplos de questões abaixo e gere ").append(quantidade);
        sb.append(" NOVAS questões originais seguindo EXATAMENTE o mesmo estilo, formato e nível de dificuldade.\n\n");
        sb.append("MATÉRIA/TAG: ").append(tag).append("\n\n");

        sb.append("=== EXEMPLOS DE QUESTÕES ===\n\n");

        int i = 1;
        for (Questao q : exemplos) {
            sb.append("--- Exemplo ").append(i++).append(" ---\n");
            sb.append("ENUNCIADO: ").append(q.getEnunciado()).append("\n");

            if (q.getAlternativas() != null && !q.getAlternativas().isEmpty()) {
                sb.append("ALTERNATIVAS (JSON): ").append(q.getAlternativas()).append("\n");
            }

            sb.append("GABARITO: ").append(q.getGabarito()).append("\n");
            sb.append("TIPO: ").append(q.getTipo()).append("\n\n");
        }

        sb.append("=== INSTRUÇÕES ===\n");
        sb.append("1. Gere questões ORIGINAIS (não copie os exemplos)\n");
        sb.append("2. Mantenha o mesmo nível de dificuldade\n");
        sb.append("3. Mantenha o mesmo formato de alternativas\n");
        sb.append("4. Use o formato JSON abaixo\n\n");

        sb.append("=== FORMATO DE RESPOSTA (JSON) ===\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"questoes\": [\n");
        sb.append("    {\n");
        sb.append("      \"enunciado\": \"Texto da questão...\",\n");
        sb.append("      \"alternativas\": {\"A\": \"...\", \"B\": \"...\", \"C\": \"...\", \"D\": \"...\"},\n");
        sb.append("      \"gabarito\": \"A\",\n");
        sb.append("      \"tipo\": \"A\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");
        sb.append("```\n");

        return sb.toString();
    }

    private List<Questao> parseRespostaIA(String respostaJson, String tag) {
        List<Questao> questoes = new ArrayList<>();

        try {
            // Limpa a resposta (remove markdown code blocks se houver)
            String jsonLimpo = respostaJson
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            JsonNode root = mapper.readTree(jsonLimpo);
            JsonNode questoesNode = root.path("questoes");

            if (questoesNode.isMissingNode()) {
                // Tenta buscar no root se for array
                questoesNode = root.path("itens");
            }

            if (questoesNode.isArray()) {
                for (JsonNode qNode : questoesNode) {
                    Questao q = new Questao();

                    q.setEnunciado(qNode.path("enunciado").asText(""));
                    q.setGabarito(qNode.path("gabarito").asText("A"));
                    q.setTags(tag);

                    // Alternativas
                    JsonNode altNode = qNode.path("alternativas");
                    if (!altNode.isMissingNode()) {
                        q.setAlternativas(mapper.writeValueAsString(altNode));
                    }

                    // Tipo
                    String tipoStr = qNode.path("tipo").asText("A");
                    try {
                        q.setTipo(TipoQuestao.valueOf(tipoStr));
                    } catch (Exception e) {
                        q.setTipo(TipoQuestao.A);
                    }

                    questoes.add(q);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao parsear resposta da IA: " + e.getMessage());
            e.printStackTrace();
        }

        return questoes;
    }

    /**
     * Retorna lista de tags disponíveis no banco para o dropdown.
     */
    public List<String> listarTagsDisponiveis(Integer etapa) {
        List<Questao> questoes = etapa != null
                ? questaoRepo.findByProva_Etapa(etapa)
                : questaoRepo.findAll();

        return questoes.stream()
                .filter(q -> q.getTags() != null && !q.getTags().isBlank())
                .flatMap(q -> Arrays.stream(q.getTags().split(",")))
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
