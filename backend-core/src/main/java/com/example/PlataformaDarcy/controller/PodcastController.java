package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.service.PodcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller para a feature Podcastfy.
 * Permite gerar podcasts educativos a partir de PDFs de obras literárias.
 */
@Controller
@RequestMapping("/aluno/podcast")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('PRO')")
public class PodcastController {

    @Autowired
    private PodcastService podcastService;

    /**
     * Página principal do Podcastfy.
     */
    @GetMapping
    public String paginaPodcast(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario != null) {
            model.addAttribute("nome", usuario.getNome());
        }

        // Verificar se o serviço está disponível
        model.addAttribute("servicoOnline", podcastService.isServiceAvailable());

        return "aluno/podcastfy";
    }

    /**
     * Gera podcast a partir de um PDF (síncrono).
     * Retorna o arquivo de áudio diretamente.
     */
    @PostMapping("/gerar")
    @ResponseBody
    public ResponseEntity<byte[]> gerarPodcast(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            byte[] audioBytes = podcastService.gerarPodcast(arquivo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("podcast_" + arquivo.getOriginalFilename().replace(".pdf", ".mp3"))
                    .build());
            headers.setContentLength(audioBytes.length);

            return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(("Erro: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erro ao gerar podcast: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Inicia geração assíncrona de podcast.
     * Retorna um job ID para acompanhamento.
     */
    @PostMapping("/gerar-async")
    @ResponseBody
    public ResponseEntity<String> gerarPodcastAsync(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            String jobResponse = podcastService.iniciarGeracaoAsync(arquivo);
            return ResponseEntity.ok(jobResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Verifica status de um job de geração.
     */
    @GetMapping("/status/{jobId}")
    @ResponseBody
    public ResponseEntity<String> verificarStatus(@PathVariable String jobId) {
        try {
            String status = podcastService.verificarStatus(jobId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Baixa podcast de um job concluído.
     */
    @GetMapping("/download/{jobId}")
    @ResponseBody
    public ResponseEntity<byte[]> baixarPodcast(@PathVariable String jobId) {
        try {
            byte[] audioBytes = podcastService.baixarPodcast(jobId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("podcast_" + jobId + ".mp3")
                    .build());

            return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erro: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Health check do serviço.
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> healthCheck() {
        boolean available = podcastService.isServiceAvailable();
        if (available) {
            return ResponseEntity.ok("{\"status\": \"online\"}");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"status\": \"offline\"}");
        }
    }
}
