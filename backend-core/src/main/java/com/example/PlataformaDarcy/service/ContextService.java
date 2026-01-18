package com.example.PlataformaDarcy.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContextService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Map<String, Object>> acervoMemoria = new ArrayList<>();

    // Carrega obras de TODAS as etapas do PAS para a memória RAM
    @PostConstruct
    public void carregarAcervo() {
        this.acervoMemoria = new ArrayList<>();
        int[] etapas = { 1, 2, 3 };

        for (int etapa : etapas) {
            try {
                String path = "data/pas" + etapa + "/acervo_obras.json";
                ClassPathResource resource = new ClassPathResource(path);
                if (resource.exists()) {
                    List<Map<String, Object>> obrasEtapa = objectMapper.readValue(
                            resource.getInputStream(), new TypeReference<>() {
                            });
                    // Adiciona a etapa como metadado em cada obra
                    for (Map<String, Object> obra : obrasEtapa) {
                        obra.put("etapa", etapa);
                    }
                    this.acervoMemoria.addAll(obrasEtapa);
                    System.out.println(
                            "📚 ContextService: PAS " + etapa + " - " + obrasEtapa.size() + " obras carregadas.");
                }
            } catch (IOException e) {
                System.err.println("⚠️ Erro ao carregar acervo PAS " + etapa + ": " + e.getMessage());
            }
        }
        System.out.println("📚 ContextService: TOTAL de " + acervoMemoria.size() + " obras na memória.");
    }

    /**
     * Busca obras que tenham relação com a pergunta do aluno.
     * Prioriza obras da etapa do aluno, mas encontra obras de outras etapas se
     * forem específicas.
     */
    public String recuperarContextoRelevante(String perguntaUsuario) {
        return recuperarContextoRelevante(perguntaUsuario, null);
    }

    public String recuperarContextoRelevante(String perguntaUsuario, Integer etapaAlvo) {
        if (acervoMemoria.isEmpty())
            return "";

        String termo = normalizar(perguntaUsuario);

        // Classe auxiliar para ordenação
        record Match(Map<String, Object> obra, int score) {
        }

        List<Match> matches = acervoMemoria.stream()
                .map(obra -> {
                    int score = 0;
                    String titulo = normalizar((String) obra.getOrDefault("titulo", ""));
                    String tags = normalizar(obra.getOrDefault("tags", "").toString());

                    // Pontuação por palavra-chave (Peso maior)
                    if (contemPalavraChave(termo, titulo))
                        score += 20;
                    else if (contemPalavraChave(termo, tags))
                        score += 10;

                    // Se não tem match de texto, score é 0 e será descartado
                    if (score == 0)
                        return null;

                    // Pontuação por etapa (Desempate)
                    Integer etapaObra = (Integer) obra.get("etapa");
                    if (etapaAlvo != null && etapaObra != null && etapaObra.equals(etapaAlvo)) {
                        score += 5;
                    }

                    return new Match(obra, score);
                })
                .filter(Objects::nonNull)
                .sorted((m1, m2) -> Integer.compare(m2.score, m1.score)) // Decrescente
                .limit(3)
                .collect(Collectors.toList());

        String contexto = matches.stream()
                .map(m -> String.format(
                        "--- OBRA ENCONTRADA (PAS %d) ---\nTITULO: %s\nRESUMO TÉCNICO: %s\nTAGS: %s\n",
                        m.obra.get("etapa"),
                        m.obra.get("titulo"),
                        m.obra.get("texto_contexto"),
                        m.obra.get("tags")))
                .collect(Collectors.joining("\n"));

        if (contexto.isEmpty()) {
            return "Nenhuma obra específica do acervo foi citada diretamente na pergunta, use seu conhecimento geral sobre o PAS.";
        }

        return contexto;
    }

    // Remove acentos e deixa minúsculo para busca funcionar melhor
    private String normalizar(String texto) {
        if (texto == null)
            return "";
        return Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private boolean contemPalavraChave(String pergunta, String textoAlvo) {
        // Lógica simples: se qualquer palavra grande da pergunta estiver no texto
        String[] palavras = pergunta.split("\\s+");
        for (String p : palavras) {
            if (p.length() > 3 && textoAlvo.contains(p))
                return true;
        }
        return false;
    }

    // ==================== MÉTODOS PARA O TUTOR IA ====================

    /**
     * Retorna uma obra específica por ID.
     */
    public Map<String, Object> getObraPorId(String id) {
        return acervoMemoria.stream()
                .filter(obra -> id.equals(obra.get("id")))
                .findFirst()
                .orElse(null);
    }

    /**
     * Lista todas as obras.
     */
    public List<Map<String, Object>> listarObras() {
        return new ArrayList<>(acervoMemoria);
    }

    /**
     * Lista obras por tipo (MUSICAL, VISUAL, TEXTO, etc).
     */
    public List<Map<String, Object>> listarObrasPorTipo(String tipo) {
        return acervoMemoria.stream()
                .filter(obra -> tipo.equalsIgnoreCase((String) obra.get("tipo")))
                .collect(Collectors.toList());
    }

    /**
     * Retorna obras agrupadas por tipo para o seletor.
     */
    public Map<String, List<Map<String, Object>>> listarObrasAgrupadas() {
        return acervoMemoria.stream()
                .collect(Collectors.groupingBy(
                        obra -> (String) obra.getOrDefault("tipo", "OUTROS")));
    }

    /**
     * Formata uma obra para contexto da IA.
     */
    public String formatarObraParaIA(String obraId) {
        Map<String, Object> obra = getObraPorId(obraId);
        if (obra == null)
            return "";

        return String.format(
                "=== OBRA SELECIONADA ===\nTÍTULO: %s\nTIPO: %s\nCONTEXTO TÉCNICO: %s\nTAGS: %s\n",
                obra.get("titulo"),
                obra.get("tipo"),
                obra.get("texto_contexto"),
                obra.get("tags"));
    }
}