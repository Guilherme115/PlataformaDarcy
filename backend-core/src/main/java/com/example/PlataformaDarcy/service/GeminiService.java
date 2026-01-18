package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.Questao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    // --- Configurações da API Real (Para o Simulado Oficial) ---
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================================================================================
    // FUNCIONALIDADE 1: EXPURGO (Mantendo sua lógica Mock/Refatoração para os
    // testes)
    // ==================================================================================
    public Questao refatorarQuestao(Questao original) {
        // Clona a questão para não alterar o objeto original na memória
        Questao mutante = new Questao();
        mutante.setId(original.getId());
        mutante.setTipo(original.getTipo());
        mutante.setTags(original.getTags());

        // LÓGICA MOCK: Inverte o gabarito para forçar raciocínio
        String novoGabarito;
        String aviso;

        if ("C".equalsIgnoreCase(original.getGabarito())) {
            novoGabarito = "E";
            aviso = "A IA reescreveu este cenário para torná-lo INCORRETO.";
        } else if ("E".equalsIgnoreCase(original.getGabarito())) {
            novoGabarito = "C";
            aviso = "A IA reescreveu este cenário para torná-lo CORRETO.";
        } else {
            // Para multipla escolha, forçamos 'A' como exemplo
            novoGabarito = "A";
            aviso = "A IA alterou as alternativas. A correta agora é a A.";
        }

        mutante.setGabarito(novoGabarito);

        // Gera o texto modificado
        String novoTexto = """
                [PROTOCOLO DE EXPURGO ATIVO]
                %s
                ----------------------------------------
                ENUNCIADO REFATORADO:

                %s
                """.formatted(aviso, original.getEnunciado());

        mutante.setEnunciado(novoTexto);

        return mutante;
    }

    // ==================================================================================
    // FUNCIONALIDADE 2: GERADOR DE SIMULADO OFICIAL (Nova Chamada API Real)
    // ==================================================================================

    /**
     * Gera um bloco completo (Texto Base + Questões) chamando a API do Google
     * Gemini.
     */
    public String gerarConteudoBloco(String contexto, String instrucao, int qtdItens) {
        try {
            String promptFinal = montarPrompt(contexto, instrucao, qtdItens);

            // Corpo da requisição para o Google Gemini
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", promptFinal)))),
                    "generationConfig", Map.of(
                            "temperature", 0.4, // Baixa temperatura para ser mais preciso
                            "response_mime_type", "application/json" // Força resposta JSON
                    ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Adiciona a API Key na URL se ela não estiver vazia (evita erro em dev sem
            // chave)
            String urlComKey = apiUrl;
            if (apiKey != null && !apiKey.isEmpty()) {
                urlComKey += "?key=" + apiKey;
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Se não tiver chave configurada, retorna um mock para não travar o
            // desenvolvimento
            if (apiKey == null || apiKey.isEmpty() || apiKey.contains("SUA_CHAVE")) {
                System.out.println("⚠️ AVISO: API Key não configurada. Retornando Mock JSON.");
                return gerarMockJson(qtdItens);
            }

            ResponseEntity<String> response = restTemplate.postForEntity(urlComKey, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return extrairTextoDaResposta(response.getBody());
            } else {
                throw new RuntimeException("Erro na API Gemini: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Retorna um JSON de fallback se falhar a conexão
            return "{\"erro\": \"Falha na geração\", \"detalhe\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Gera um texto genérico chamando a API do Google Gemini.
     */
    public String gerarTexto(String prompt) {
        try {
            // Corpo da requisição para o Google Gemini
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of(
                            "temperature", 0.2 // Baixa temperatura para ser mais preciso
                    ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Adiciona a API Key na URL
            String urlComKey = apiUrl;
            if (apiKey != null && !apiKey.isEmpty()) {
                urlComKey += "?key=" + apiKey;
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Mock se não tiver chave
            if (apiKey == null || apiKey.isEmpty() || apiKey.contains("SUA_CHAVE")) {
                return "GERAL, TESTE, MOCK";
            }

            ResponseEntity<String> response = restTemplate.postForEntity(urlComKey, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return extrairTextoDaResposta(response.getBody());
            } else {
                throw new RuntimeException("Erro na API Gemini: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "ERRO, AO, GERAR";
        }
    }

    // --- Métodos Auxiliares Privados ---

    private String montarPrompt(String contexto, String instrucao, int qtd) {
        return """
                Você é um examinador oficial da banca CEBRASPE (UnB).

                CONTEXTO DE BASE (Obra/Tema):
                %s

                SUA TAREFA:
                %s

                FORMATO DE SAÍDA OBRIGATÓRIO (JSON PURO):
                {
                  "texto_base_gerado": "Escreva aqui um texto de suporte curto e denso...",
                  "itens": [
                    {
                      "numero": 1,
                      "tipo": "A",
                      "enunciado": "Afirmação no estilo Certo/Errado...",
                      "gabarito": "C",
                      "justificativa": "Explicação breve..."
                    }
                  ]
                }

                IMPORTANTE: Gere exatamente %d itens. O JSON deve ser válido.
                """.formatted(contexto, instrucao, qtd);
    }

    private String extrairTextoDaResposta(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            // O caminho padrão da resposta do Gemini
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "{}";
        }
    }

    // Fallback para quando você estiver sem internet ou sem chave de API
    private String gerarMockJson(int qtd) {
        StringBuilder itens = new StringBuilder();
        for (int i = 1; i <= qtd; i++) {
            itens.append("""
                    {
                      "numero": %d,
                      "tipo": "A",
                      "enunciado": "Este é um item gerado via MOCK porque a API Key não foi encontrada.",
                      "gabarito": "C",
                      "justificativa": "Mock de desenvolvimento."
                    }%s
                    """.formatted(i, i < qtd ? "," : ""));
        }

        return """
                {
                  "texto_base_gerado": "Texto Base Mockado para testes locais.",
                  "itens": [
                    %s
                  ]
                }
                """.formatted(itens.toString());
    }
}