package com.example.PlataformaDarcy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controller para Central de Gerenciamento de PDFs.
 * Proxya chamadas para a API Python (pdf_api.py).
 */
@Controller
@RequestMapping("/admin/pdfs")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AdminPdfController {

    @Value("${pdf.api.url:http://localhost:8001}")
    private String pdfApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private com.example.PlataformaDarcy.repository.ProvaRepository provaRepository;

    /**
     * Página principal da Central de PDFs.
     */
    @GetMapping
    public String paginaCentral(Model model) {
        try {
            // Lista PDFs via API Python
            ResponseEntity<List> response = restTemplate.getForEntity(
                    pdfApiUrl + "/api/pdfs", List.class);
            List<Map<String, Object>> pdfs = response.getBody();

            // Busca todas as provas existentes para marcar quais PDFs já foram importados
            List<String> provasExistentes = provaRepository.findAll().stream()
                    .map(p -> "pas-" + p.getEtapa() + "-prova-" + p.getAno())
                    .map(String::toLowerCase)
                    .toList();

            // Agrupa PDFs por etapa
            java.util.Map<Integer, java.util.List<Map<String, Object>>> pdfsPorEtapa = new java.util.TreeMap<>();
            pdfsPorEtapa.put(1, new java.util.ArrayList<>());
            pdfsPorEtapa.put(2, new java.util.ArrayList<>());
            pdfsPorEtapa.put(3, new java.util.ArrayList<>());
            pdfsPorEtapa.put(0, new java.util.ArrayList<>()); // Outros

            if (pdfs != null) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("pas-(\\d)-prova-(\\d{4})",
                        java.util.regex.Pattern.CASE_INSENSITIVE);

                for (Map<String, Object> pdf : pdfs) {
                    String filename = ((String) pdf.get("filename")).toLowerCase().replace(".pdf", "");
                    pdf.put("jaImportado", provasExistentes.stream().anyMatch(filename::contains));

                    // Extrai etapa do nome do arquivo
                    java.util.regex.Matcher matcher = pattern.matcher(filename);
                    if (matcher.find()) {
                        int etapa = Integer.parseInt(matcher.group(1));
                        int ano = Integer.parseInt(matcher.group(2));
                        pdf.put("etapa", etapa);
                        pdf.put("ano", ano);
                        pdfsPorEtapa.getOrDefault(etapa, pdfsPorEtapa.get(0)).add(pdf);
                    } else {
                        pdf.put("etapa", 0);
                        pdfsPorEtapa.get(0).add(pdf);
                    }
                }

                // Ordena cada grupo por ano (decrescente)
                for (java.util.List<Map<String, Object>> lista : pdfsPorEtapa.values()) {
                    lista.sort((a, b) -> {
                        Integer anoA = (Integer) a.getOrDefault("ano", 0);
                        Integer anoB = (Integer) b.getOrDefault("ano", 0);
                        return anoB.compareTo(anoA);
                    });
                }
            }

            model.addAttribute("pdfsPorEtapa", pdfsPorEtapa);
            model.addAttribute("pdfs", pdfs);
            model.addAttribute("apiOnline", true);
        } catch (Exception e) {
            model.addAttribute("pdfsPorEtapa", java.util.Map.of());
            model.addAttribute("pdfs", List.of());
            model.addAttribute("apiOnline", false);
            model.addAttribute("apiError", e.getMessage());
        }
        return "admin/pdf-central";
    }

    /**
     * Lista PDFs em formato JSON (para o modal de inserir de outro PDF).
     */
    @GetMapping("/listar-json")
    @ResponseBody
    public List listarPdfsJson() {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    pdfApiUrl + "/api/pdfs", List.class);
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Página de edição de um PDF específico.
     */
    @GetMapping("/{filename}")
    public String paginaEdicao(@PathVariable String filename, Model model) {
        try {
            // Detalhes do PDF
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    pdfApiUrl + "/api/pdfs/" + filename, Map.class);

            if (response.getBody() == null) {
                return "redirect:/admin/pdfs?error=PDF não encontrado";
            }

            model.addAttribute("pdf", response.getBody());

            // Previews das páginas
            ResponseEntity<Map> previewsResponse = restTemplate.getForEntity(
                    pdfApiUrl + "/api/pdfs/" + filename + "/previews?zoom=0.4", Map.class);
            model.addAttribute("previews", previewsResponse.getBody() != null ? previewsResponse.getBody()
                    : java.util.Map.of("previews", java.util.List.of()));

            model.addAttribute("apiOnline", true);
            return "admin/pdf-editor";
        } catch (Exception e) {
            return "redirect:/admin/pdfs?error=API offline ou PDF não encontrado";
        }
    }

    /**
     * Upload de PDF (POST form).
     */
    @PostMapping("/upload")
    public String uploadPdf(@RequestParam("file") MultipartFile file, Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    pdfApiUrl + "/api/pdfs/upload", requestEntity, Map.class);

            return "redirect:/admin/pdfs?success=upload&file="
                    + java.net.URLEncoder.encode(file.getOriginalFilename(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/admin/pdfs?error=" + e.getMessage();
        }
    }

    /**
     * Deletar PDF.
     */
    @PostMapping("/{filename}/delete")
    public String deletePdf(@PathVariable String filename) {
        try {
            restTemplate.delete(pdfApiUrl + "/api/pdfs/" + filename);
            return "redirect:/admin/pdfs?success=delete&file="
                    + java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/admin/pdfs?error=" + e.getMessage();
        }
    }

    /**
     * Ações de edição de página (AJAX).
     */
    @PostMapping("/{filename}/pages")
    @ResponseBody
    public ResponseEntity<?> editPage(
            @PathVariable String filename,
            @RequestBody Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pdfApiUrl + "/api/pdfs/" + filename + "/pages", entity, Map.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Proxy para preview de página (imagem).
     */
    @GetMapping("/{filename}/preview/{pageNumber}")
    public ResponseEntity<byte[]> getPagePreview(
            @PathVariable String filename,
            @PathVariable int pageNumber,
            @RequestParam(defaultValue = "1.0") float zoom) {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    pdfApiUrl + "/api/pdfs/" + filename + "/preview/" + pageNumber + "?zoom=" + zoom,
                    byte[].class);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Processa PDF e importa para o banco de dados.
     */
    @PostMapping("/{filename}/process")
    public String processarParaBanco(@PathVariable String filename, Model model) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pdfApiUrl + "/api/pdfs/" + filename + "/process", null, Map.class);

            Map body = response.getBody();
            if (body != null && Boolean.TRUE.equals(body.get("success"))) {
                Integer etapa = (Integer) body.get("etapa");
                Integer ano = (Integer) body.get("ano");
                return "redirect:/cms?etapa=" + etapa + "&ano=" + ano + "&success=import";
            }
            return "redirect:/admin/pdfs/" + filename + "?error=Falha ao processar";
        } catch (Exception e) {
            String errorMsg = e.getMessage().contains("400") ? "Nome de arquivo inválido"
                    : e.getMessage().contains("500") ? "Erro ao extrair questões" : "API offline";
            return "redirect:/admin/pdfs/" + filename + "?error=" + errorMsg;
        }
    }

    /**
     * Reconstrói uma página do PDF com conteúdo formatado manualmente.
     */
    @PostMapping("/{filename}/reconstruct")
    @ResponseBody
    public ResponseEntity<?> reconstructPage(
            @PathVariable String filename,
            @RequestBody Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pdfApiUrl + "/api/pdfs/" + filename + "/reconstruct", entity, Map.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Helper class para enviar MultipartFile via RestTemplate.
     */
    private static class MultipartInputStreamResource extends org.springframework.core.io.InputStreamResource {
        private final String filename;
        private final long size;

        public MultipartInputStreamResource(MultipartFile file) throws java.io.IOException {
            super(file.getInputStream());
            this.filename = file.getOriginalFilename();
            this.size = file.getSize();
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() {
            return this.size;
        }
    }
}
