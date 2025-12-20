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

    // Carrega tudo para a memória RAM assim que o sistema sobe (Performance Extrema)
    @PostConstruct
    public void carregarAcervo() {
        try {
            ClassPathResource resource = new ClassPathResource("data/pas1/acervo_obras.json");
            if (resource.exists()) {
                this.acervoMemoria = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
                System.out.println("📚 ContextService: " + acervoMemoria.size() + " obras carregadas na memória.");
            }
        } catch (IOException e) {
            System.err.println("❌ Erro ao carregar acervo: " + e.getMessage());
        }
    }

    /**
     * Busca obras que tenham relação com a pergunta do aluno.
     * Usa uma busca simples por palavras-chave nos títulos e tags.
     */
    public String recuperarContextoRelevante(String perguntaUsuario) {
        if (acervoMemoria.isEmpty()) return "";

        String termo = normalizar(perguntaUsuario);

        String contexto = acervoMemoria.stream()
                .filter(obra -> {
                    String titulo = normalizar((String) obra.getOrDefault("titulo", ""));
                    String tags = normalizar(obra.getOrDefault("tags", "").toString());
                    String texto = normalizar(obra.getOrDefault("texto_contexto", "").toString());

                    // Verifica se palavras-chave da pergunta aparecem na obra
                    return contemPalavraChave(termo, titulo) || contemPalavraChave(termo, tags);
                })
                .limit(3) // Pega no máximo 3 obras para não confundir a IA
                .map(obra -> String.format(
                        "--- OBRA ENCONTRADA ---\nTITULO: %s\nRESUMO TÉCNICO: %s\nTAGS: %s\n",
                        obra.get("titulo"),
                        obra.get("texto_contexto"),
                        obra.get("tags")
                ))
                .collect(Collectors.joining("\n"));

        if (contexto.isEmpty()) {
            return "Nenhuma obra específica do acervo foi citada diretamente na pergunta, use seu conhecimento geral sobre o PAS.";
        }

        return contexto;
    }

    // Remove acentos e deixa minúsculo para busca funcionar melhor
    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private boolean contemPalavraChave(String pergunta, String textoAlvo) {
        // Lógica simples: se qualquer palavra grande da pergunta estiver no texto
        String[] palavras = pergunta.split("\\s+");
        for (String p : palavras) {
            if (p.length() > 3 && textoAlvo.contains(p)) return true;
        }
        return false;
    }
}