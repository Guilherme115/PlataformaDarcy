package com.example.PlataformaDarcy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço de busca inteligente de PDFs usando Gemini com Google Search.
 * O usuário digita um tema e a IA retorna links de PDFs relevantes.
 */
@Service
public class PdfSearchService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Busca PDFs sobre um tema usando Gemini com grounding do Google.
     * 
     * @param query Tema de busca (ex: "números complexos geometria analítica")
     * @return Lista de resultados com título, descrição e URL do PDF
     */
    public List<Map<String, String>> buscarPdfs(String query) {
        List<Map<String, String>> resultados = new ArrayList<>();

        try {
            String prompt = construirPromptBusca(query);

            // Corpo da requisição para o Gemini
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "maxOutputTokens", 4096),
                    // Habilita grounding com Google Search
                    "tools", List.of(
                            Map.of("googleSearch", Map.of())));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String urlComKey = apiUrl;
            if (apiKey != null && !apiKey.isEmpty()) {
                urlComKey += "?key=" + apiKey;
            }

            // Verifica se tem API Key
            if (apiKey == null || apiKey.isEmpty() || apiKey.contains("SUA_CHAVE")) {
                return gerarResultadosMock(query);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(urlComKey, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                resultados = parsearResultados(response.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Retorna mock em caso de erro
            return gerarResultadosMock(query);
        }

        return resultados;
    }

    /**
     * Constrói o prompt para buscar PDFs.
     */
    private String construirPromptBusca(String query) {
        return """
                Você é um assistente especializado em encontrar materiais de estudo em PDF.

                TAREFA: Busque no Google por PDFs acadêmicos e materiais de estudo sobre:
                "%s"

                IMPORTANTE:
                - Foque em PDFs gratuitos e acessíveis
                - Priorize materiais de universidades, apostilas e livros
                - Inclua o link direto para o PDF quando possível
                - Retorne no máximo 8 resultados

                FORMATO DE RESPOSTA (siga exatamente):
                Para cada PDF encontrado, use este formato:

                [PDF]
                TITULO: Nome do material
                DESCRICAO: Breve descrição do conteúdo (máx 100 caracteres)
                URL: link_direto_para_o_pdf
                FONTE: Nome do site/universidade
                [/PDF]

                Retorne apenas os PDFs no formato acima, sem texto adicional.
                """.formatted(query);
    }

    /**
     * Parseia a resposta do Gemini para extrair os PDFs.
     */
    private List<Map<String, String>> parsearResultados(String jsonResponse) {
        List<Map<String, String>> resultados = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String texto = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Regex para extrair blocos [PDF]...[/PDF]
            Pattern pattern = Pattern.compile(
                    "\\[PDF\\]\\s*TITULO:\\s*(.+?)\\s*DESCRICAO:\\s*(.+?)\\s*URL:\\s*(.+?)\\s*FONTE:\\s*(.+?)\\s*\\[/PDF\\]",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

            Matcher matcher = pattern.matcher(texto);
            while (matcher.find() && resultados.size() < 8) {
                Map<String, String> pdf = new HashMap<>();
                pdf.put("titulo", matcher.group(1).trim());
                pdf.put("descricao", matcher.group(2).trim());
                pdf.put("url", matcher.group(3).trim());
                pdf.put("fonte", matcher.group(4).trim());
                resultados.add(pdf);
            }

            // Se não encontrou no formato esperado, tenta extrair URLs de PDF
            if (resultados.isEmpty()) {
                resultados = extrairUrlsPdf(texto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultados;
    }

    /**
     * Extrai URLs de PDF do texto quando não está no formato esperado.
     */
    private List<Map<String, String>> extrairUrlsPdf(String texto) {
        List<Map<String, String>> resultados = new ArrayList<>();

        // Regex para encontrar URLs terminando em .pdf
        Pattern urlPattern = Pattern.compile(
                "(https?://[^\\s]+\\.pdf)",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = urlPattern.matcher(texto);
        int count = 0;
        while (matcher.find() && count < 8) {
            String url = matcher.group(1);
            Map<String, String> pdf = new HashMap<>();
            pdf.put("titulo", "Material PDF #" + (count + 1));
            pdf.put("descricao", "PDF encontrado na busca");
            pdf.put("url", url);
            pdf.put("fonte", extrairDominio(url));
            resultados.add(pdf);
            count++;
        }

        return resultados;
    }

    /**
     * Extrai o domínio de uma URL.
     */
    private String extrairDominio(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            return u.getHost().replace("www.", "");
        } catch (Exception e) {
            return "Internet";
        }
    }

    /**
     * Gera resultados mock para desenvolvimento/testes.
     */
    private List<Map<String, String>> gerarResultadosMock(String query) {
        List<Map<String, String>> mock = new ArrayList<>();

        mock.add(Map.of(
                "titulo", "Apostila de " + query + " - UFMG",
                "descricao", "Material completo para estudo com exercícios resolvidos",
                "url", "https://exemplo.com/apostila.pdf",
                "fonte", "UFMG"));

        mock.add(Map.of(
                "titulo", "Resumo Teórico - " + query,
                "descricao", "Resumo objetivo para revisão rápida",
                "url", "https://exemplo.com/resumo.pdf",
                "fonte", "USP"));

        mock.add(Map.of(
                "titulo", "Exercícios Resolvidos - " + query,
                "descricao", "Lista de exercícios com gabarito comentado",
                "url", "https://exemplo.com/exercicios.pdf",
                "fonte", "UNICAMP"));

        return mock;
    }
}
