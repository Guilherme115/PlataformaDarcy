package com.example.PlataformaDarcy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Serviço para geração de podcasts a partir de PDFs.
 * Comunica com o serviço Python (Podcastfy API) para processamento.
 */
@Service
public class PodcastService {

    @Value("${podcastfy.api.url:http://localhost:8001}")
    private String podcastfyApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Gera um podcast a partir de um arquivo PDF.
     * 
     * @param arquivo Arquivo PDF enviado pelo usuário
     * @return Bytes do arquivo de áudio MP3 gerado
     * @throws IOException      Se houver erro ao ler o arquivo
     * @throws RuntimeException Se houver erro na comunicação com a API
     */
    public byte[] gerarPodcast(MultipartFile arquivo) throws IOException {
        // Validação básica
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio");
        }

        String filename = arquivo.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Apenas arquivos PDF são aceitos");
        }

        // Verificar tamanho (máximo 50MB)
        if (arquivo.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Arquivo muito grande. Máximo permitido: 50MB");
        }

        // Preparar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Preparar corpo da requisição com o arquivo
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(arquivo.getBytes()) {
            @Override
            public String getFilename() {
                return arquivo.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Chamar API Python
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    podcastfyApiUrl + "/generate-podcast",
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Erro na geração do podcast: resposta inválida");
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com serviço Podcastfy: " + e.getMessage(), e);
        }
    }

    /**
     * Inicia geração assíncrona de podcast.
     * 
     * @param arquivo Arquivo PDF
     * @return ID do job para acompanhamento
     */
    public String iniciarGeracaoAsync(MultipartFile arquivo) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(arquivo.getBytes()) {
            @Override
            public String getFilename() {
                return arquivo.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    podcastfyApiUrl + "/generate-podcast-async",
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao iniciar geração: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica status de um job de geração.
     * 
     * @param jobId ID do job
     * @return JSON com status do job
     */
    public String verificarStatus(String jobId) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    podcastfyApiUrl + "/job/" + jobId,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar status: " + e.getMessage(), e);
        }
    }

    /**
     * Baixa podcast de um job concluído.
     * 
     * @param jobId ID do job
     * @return Bytes do arquivo de áudio
     */
    public byte[] baixarPodcast(String jobId) {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    podcastfyApiUrl + "/job/" + jobId + "/download",
                    byte[].class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar podcast: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se o serviço Podcastfy está disponível.
     * 
     * @return true se o serviço estiver online
     */
    public boolean isServiceAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    podcastfyApiUrl + "/health",
                    String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
}
