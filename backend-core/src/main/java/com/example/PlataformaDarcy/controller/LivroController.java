package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.service.EditoraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/livros")
public class LivroController {

    @Autowired
    private EditoraService editoraService;

    // ==================== BIBLIOTECA DE LIVROS (Pública) ====================

    /**
     * Página principal da biblioteca com todas as coleções e livros
     */
    @GetMapping
    public String biblioteca(Model model) {
        List<Colecao> colecoes = editoraService.listarColecoesAtivas();
        model.addAttribute("colecoes", colecoes);
        return "livros/biblioteca";
    }

    /**
     * Visualizar um livro específico com seus volumes
     */
    @GetMapping("/{livroId}")
    public String visualizarLivro(@PathVariable Long livroId, Model model) {
        Livro livro = editoraService.buscarLivroPorId(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        List<Volume> volumes = editoraService.listarVolumesAtivosPorLivro(livroId);

        model.addAttribute("livro", livro);
        model.addAttribute("volumes", volumes);

        return "livros/livro-detalhes";
    }

    // ==================== LEITOR DE VOLUMES ====================

    /**
     * Interface de leitura de um volume
     */
    @GetMapping("/volume/{volumeId}")
    public String lerVolume(
            @PathVariable Long volumeId,
            @RequestParam(defaultValue = "1") Integer pagina,
            Model model) {

        Volume volume = editoraService.buscarVolumePorId(volumeId)
                .orElseThrow(() -> new RuntimeException("Volume não encontrado"));

        List<PaginaLivro> paginas = editoraService.listarPaginasAtivasPorVolume(volumeId);

        // Buscar página específica pelo número
        PaginaLivro paginaAtual = paginas.stream()
                .filter(p -> p.getNumeroPagina().equals(pagina))
                .findFirst()
                .orElse(paginas.isEmpty() ? null : paginas.get(0));

        model.addAttribute("volume", volume);
        model.addAttribute("paginas", paginas);
        model.addAttribute("paginaAtual", paginaAtual);
        model.addAttribute("totalPaginas", paginas.size());
        model.addAttribute("numeroPaginaAtual", pagina);

        return "livros/leitor";
    }

    // ==================== HTMX: NAVEGAÇÃO ENTRE PÁGINAS ====================

    /**
     * Carrega uma página específica via HTMX
     */
    @GetMapping("/volume/{volumeId}/pagina/{numero}")
    public String carregarPaginaLeitura(
            @PathVariable Long volumeId,
            @PathVariable Integer numero,
            Model model) {

        List<PaginaLivro> paginas = editoraService.listarPaginasAtivasPorVolume(volumeId);

        PaginaLivro paginaAtual = paginas.stream()
                .filter(p -> p.getNumeroPagina().equals(numero))
                .findFirst()
                .orElse(null);

        model.addAttribute("paginaAtual", paginaAtual);
        model.addAttribute("numeroPaginaAtual", numero);
        model.addAttribute("totalPaginas", paginas.size());

        return "livros/fragments/pagina-leitura :: pagina-leitura";
    }

    // ==================== DOWNLOAD PDF ====================

    /**
     * Download do volume em PDF
     * TODO: Implementar PDFExportService
     */
    @GetMapping("/volume/{volumeId}/download")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable Long volumeId) {
        Volume volume = editoraService.buscarVolumePorId(volumeId)
                .orElseThrow(() -> new RuntimeException("Volume não encontrado"));

        // TODO: Implementar geração de PDF
        // byte[] pdfBytes = pdfExportService.gerarPDFVolume(volumeId);

        String mensagem = "Download PDF será implementado na Sprint 4";
        byte[] pdfBytes = mensagem.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                volume.getLivro().getTitulo() + "_Vol" + volume.getNumero() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
