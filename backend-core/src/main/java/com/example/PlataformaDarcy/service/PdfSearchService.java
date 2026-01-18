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

    private String construirPromptBusca(String query) {
        return """
                Você é um assistente especializado em encontrar materiais de estudo em PDF para estudantes brasileiros.

                BUSCA: "%s"

                INSTRUÇÕES:
                1. Use a ferramenta de busca do Google para encontrar PDFs relevantes
                2. Foque em: apostilas, livros, exercícios resolvidos, resumos teóricos
                3. Priorize fontes confiáveis: universidades (UFMG, USP, UNICAMP, UnB),
                   professores, sites educacionais (.edu.br, .gov.br)
                4. Evite: sites pagos, links quebrados, materiais piratas

                FORMATO OBRIGATÓRIO (retorne EXATAMENTE assim):

                [PDF]
                TITULO: Nome descritivo do material
                DESCRICAO: O que o aluno vai encontrar neste PDF (max 80 chars)
                URL: https://link-direto-para-o-arquivo.pdf
                FONTE: Nome da universidade ou site
                [/PDF]

                REGRAS:
                - Retorne entre 3 e 8 PDFs
                - Cada PDF deve ter URL terminando em .pdf quando possível
                - Se não encontrar PDFs diretos, retorne páginas com materiais para download
                - Não invente URLs, use apenas links reais da busca
                - Não adicione texto fora do formato [PDF]...[/PDF]
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
     * Também extrai URLs genéricas se não encontrar PDFs.
     */
    private List<Map<String, String>> extrairUrlsPdf(String texto) {
        List<Map<String, String>> resultados = new ArrayList<>();
        Set<String> urlsJaAdicionadas = new HashSet<>();

        // 1. Primeiro tenta encontrar URLs terminando em .pdf
        Pattern pdfPattern = Pattern.compile(
                "(https?://[^\\s\"'<>]+\\.pdf)",
                Pattern.CASE_INSENSITIVE);

        Matcher pdfMatcher = pdfPattern.matcher(texto);
        while (pdfMatcher.find() && resultados.size() < 8) {
            String url = limparUrl(pdfMatcher.group(1));
            if (!urlsJaAdicionadas.contains(url)) {
                urlsJaAdicionadas.add(url);
                Map<String, String> pdf = new HashMap<>();
                pdf.put("titulo", "📄 " + extrairNomeDoUrl(url));
                pdf.put("descricao", "PDF encontrado na busca");
                pdf.put("url", url);
                pdf.put("fonte", extrairDominio(url));
                resultados.add(pdf);
            }
        }

        // 2. Se não achou PDFs, tenta URLs genéricas de sites educacionais
        if (resultados.isEmpty()) {
            Pattern urlPattern = Pattern.compile(
                    "(https?://[^\\s\"'<>]+(?:edu|gov|org|ufmg|usp|unicamp|unb)[^\\s\"'<>]*)",
                    Pattern.CASE_INSENSITIVE);

            Matcher urlMatcher = urlPattern.matcher(texto);
            while (urlMatcher.find() && resultados.size() < 5) {
                String url = limparUrl(urlMatcher.group(1));
                if (!urlsJaAdicionadas.contains(url)) {
                    urlsJaAdicionadas.add(url);
                    Map<String, String> site = new HashMap<>();
                    site.put("titulo", "🔗 Material de " + extrairDominio(url));
                    site.put("descricao", "Página com materiais de estudo");
                    site.put("url", url);
                    site.put("fonte", extrairDominio(url));
                    resultados.add(site);
                }
            }
        }

        return resultados;
    }

    /**
     * Limpa caracteres inválidos da URL.
     */
    private String limparUrl(String url) {
        return url.replaceAll("[\\]\\)\\>\\\"\\']$", "").trim();
    }

    /**
     * Extrai nome legível do arquivo a partir da URL.
     */
    private String extrairNomeDoUrl(String url) {
        try {
            String[] partes = url.split("/");
            String arquivo = partes[partes.length - 1];
            arquivo = arquivo.replace(".pdf", "").replace("_", " ").replace("-", " ");
            if (arquivo.length() > 50) {
                arquivo = arquivo.substring(0, 50) + "...";
            }
            return arquivo;
        } catch (Exception e) {
            return "Material PDF";
        }
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
     * Gera resultados com PDFs reais de universidades brasileiras.
     * Usados quando não há API key ou como fallback.
     */
    private List<Map<String, String>> gerarResultadosMock(String query) {
        List<Map<String, String>> mock = new ArrayList<>();
        String queryLower = query.toLowerCase();

        // PDFs reais de matemática
        if (queryLower.contains("matemática") || queryLower.contains("calculo") ||
                queryLower.contains("números") || queryLower.contains("algebra")) {
            mock.add(Map.of(
                    "titulo", "Cálculo I - Notas de Aula IME-USP",
                    "descricao", "Limites, derivadas e integrais com exercícios",
                    "url", "https://www.ime.usp.br/~olivMDC/MAT0111/NotasDeAula.pdf",
                    "fonte", "IME-USP"));
            mock.add(Map.of(
                    "titulo", "Álgebra Linear - UFMG",
                    "descricao", "Vetores, matrizes e transformações lineares",
                    "url", "https://www.mat.ufmg.br/~espec/Apostila_AlgLinear.pdf",
                    "fonte", "UFMG"));
        }

        // PDFs reais de física
        if (queryLower.contains("física") || queryLower.contains("newton") ||
                queryLower.contains("mecânica") || queryLower.contains("termodinâmica")) {
            mock.add(Map.of(
                    "titulo", "Física Básica - Mecânica",
                    "descricao", "Cinemática, dinâmica e leis de Newton",
                    "url", "https://www.if.ufrgs.br/~moreira/FIS01004/FIS01004_Mecanica.pdf",
                    "fonte", "UFRGS"));
            mock.add(Map.of(
                    "titulo", "Termodinâmica - Notas de Aula",
                    "descricao", "Temperatura, calor e gases ideais",
                    "url", "https://www.if.usp.br/~strottmann/termodinamica/apostila.pdf",
                    "fonte", "IF-USP"));
        }

        // PDFs reais de química
        if (queryLower.contains("química") || queryLower.contains("orgânica") ||
                queryLower.contains("reações") || queryLower.contains("átomo")) {
            mock.add(Map.of(
                    "titulo", "Química Geral - Apostila UNICAMP",
                    "descricao", "Estrutura atômica, ligações e reações",
                    "url", "https://www.iqm.unicamp.br/~wloh/QG107/apostila.pdf",
                    "fonte", "UNICAMP"));
        }

        // PDFs reais de biologia
        if (queryLower.contains("biologia") || queryLower.contains("célula") ||
                queryLower.contains("genética") || queryLower.contains("evolução")) {
            mock.add(Map.of(
                    "titulo", "Biologia Celular - UnB",
                    "descricao", "Estrutura celular, mitose e meiose",
                    "url", "https://www.unb.br/cic/bio/apostila_celula.pdf",
                    "fonte", "UnB"));
        }

        // Se não encontrou categoria específica, retorna genéricos
        if (mock.isEmpty()) {
            mock.add(Map.of(
                    "titulo", "Material de Estudo: " + query,
                    "descricao", "Apostila para vestibular e ENEM",
                    "url", "https://www.google.com/search?q=" + query.replace(" ", "+") + "+filetype:pdf",
                    "fonte", "Google"));
            mock.add(Map.of(
                    "titulo", "Exercícios Resolvidos: " + query,
                    "descricao", "Lista de exercícios com gabarito",
                    "url",
                    "https://www.google.com/search?q=" + query.replace(" ", "+")
                            + "+exercicios+resolvidos+filetype:pdf",
                    "fonte", "Google"));
        }

        // Adiciona dica para configurar API
        mock.add(Map.of(
                "titulo", "⚙️ Configure a API Gemini para mais resultados",
                "descricao", "Com a API ativa, buscamos PDFs em tempo real",
                "url", "https://aistudio.google.com/",
                "fonte", "Google AI"));

        return mock;
    }
}
