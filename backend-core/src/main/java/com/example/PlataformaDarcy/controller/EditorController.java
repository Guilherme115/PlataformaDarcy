package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.service.EditoraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/editor")
@PreAuthorize("hasRole('ADMIN')")
public class EditorController {

    @Autowired
    private EditoraService editoraService;

    // ==================== INTERFACE PRINCIPAL DO EDITOR ====================

    /**
     * Carrega a interface principal do editor de 3 colunas
     */
    @GetMapping("/volume/{volumeId}")
    public String editorVolume(@PathVariable Long volumeId, Model model) {
        Volume volume = editoraService.buscarVolumePorId(volumeId)
                .orElseThrow(() -> new RuntimeException("Volume não encontrado"));

        List<PaginaLivro> paginas = editoraService.listarPaginasPorVolume(volumeId);

        model.addAttribute("volume", volume);
        model.addAttribute("paginas", paginas);

        // Se houver páginas, carregar a primeira
        if (!paginas.isEmpty()) {
            model.addAttribute("paginaAtiva", paginas.get(0));
        }

        return "admin/editor/editor-volume";
    }

    // ==================== HTMX: SIDEBAR (Lista de Páginas) ====================

    /**
     * Retorna fragment com lista de páginas para a sidebar
     */
    @GetMapping("/volume/{volumeId}/paginas")
    public String listarPaginas(@PathVariable Long volumeId, Model model) {
        List<PaginaLivro> paginas = editoraService.listarPaginasPorVolume(volumeId);
        model.addAttribute("paginas", paginas);
        return "admin/editor/fragments/lista-paginas :: lista-paginas";
    }

    // ==================== HTMX: CARREGAR PÁGINA PARA EDIÇÃO ====================

    /**
     * Carrega uma página específica no editor central
     */
    @GetMapping("/pagina/{id}")
    public String carregarPagina(@PathVariable Long id, Model model) {
        PaginaLivro pagina = editoraService.buscarPaginaPorId(id)
                .orElseThrow(() -> new RuntimeException("Página não encontrada"));

        model.addAttribute("paginaAtiva", pagina);
        return "admin/editor/fragments/editor-pagina :: editor-pagina";
    }

    // ==================== HTMX: SALVAR CONTEÚDO ====================

    /**
     * Salva o conteúdo HTML da página
     */
    @PostMapping("/pagina/{id}/salvar")
    @ResponseBody
    public String salvarPagina(
            @PathVariable Long id,
            @RequestParam String titulo,
            @RequestParam String modulo,
            @RequestParam String layout,
            @RequestParam String htmlContent,
            @AuthenticationPrincipal Usuario usuario) {

        LayoutPagina layoutEnum = LayoutPagina.valueOf(layout.toUpperCase());

        editoraService.atualizarPagina(id, titulo, modulo, layoutEnum, htmlContent, usuario);

        return "<div class='bg-green-100 border-2 border-green-600 p-2 text-center font-bold text-xs'>" +
                "✓ SALVO COM SUCESSO!</div>";
    }

    // ==================== HTMX: PREVIEW EM TEMPO REAL ====================

    /**
     * Gera preview da página em formato A4
     */
    @PostMapping("/preview")
    public String preview(
            @RequestParam String htmlContent,
            @RequestParam String layout,
            @RequestParam String modulo,
            @RequestParam Integer numeroPagina,
            @RequestParam String titulo,
            Model model) {

        // Criar objeto temporário para preview
        PaginaLivro paginaPreview = new PaginaLivro();
        paginaPreview.setHtmlContent(htmlContent);
        paginaPreview.setLayout(LayoutPagina.valueOf(layout.toUpperCase()));
        paginaPreview.setModulo(modulo);
        paginaPreview.setNumeroPagina(numeroPagina);
        paginaPreview.setTitulo(titulo);

        model.addAttribute("pagina", paginaPreview);

        return "admin/editor/fragments/preview-pagina :: preview-pagina";
    }

    // ==================== HTMX: ADICIONAR NOVA PÁGINA ====================

    /**
     * Cria uma nova página no volume
     */
    @PostMapping("/volume/{volumeId}/nova-pagina")
    public String novaPagina(
            @PathVariable Long volumeId,
            @AuthenticationPrincipal Usuario usuario,
            Model model) {

        PaginaLivro novaPagina = editoraService.criarPagina(
                volumeId,
                "Nova Página",
                "Módulo Novo",
                LayoutPagina.STANDARD,
                usuario);

        // Retornar lista atualizada de páginas
        List<PaginaLivro> paginas = editoraService.listarPaginasPorVolume(volumeId);
        model.addAttribute("paginas", paginas);
        model.addAttribute("novaPaginaId", novaPagina.getId());

        return "admin/editor/fragments/lista-paginas :: lista-paginas";
    }

    // ==================== HTMX: DUPLICAR PÁGINA ====================

    /**
     * Duplica uma página existente
     */
    @PostMapping("/pagina/{id}/duplicar")
    public String duplicarPagina(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            Model model) {

        PaginaLivro original = editoraService.buscarPaginaPorId(id)
                .orElseThrow(() -> new RuntimeException("Página não encontrada"));

        PaginaLivro copia = editoraService.duplicarPagina(id, usuario);

        // Retornar lista atualizada
        List<PaginaLivro> paginas = editoraService.listarPaginasPorVolume(original.getVolume().getId());
        model.addAttribute("paginas", paginas);
        model.addAttribute("novaPaginaId", copia.getId());

        return "admin/editor/fragments/lista-paginas :: lista-paginas";
    }

    // ==================== HTMX: DELETAR PÁGINA ====================

    /**
     * Deleta uma página
     */
    @DeleteMapping("/pagina/{id}")
    @ResponseBody
    public String deletarPagina(@PathVariable Long id) {
        PaginaLivro pagina = editoraService.buscarPaginaPorId(id)
                .orElseThrow(() -> new RuntimeException("Página não encontrada"));

        Long volumeId = pagina.getVolume().getId();

        // Verificar se não é a última página
        long totalPaginas = editoraService.contarPaginasPorVolume(volumeId);
        if (totalPaginas <= 1) {
            return "<div class='bg-red-100 border-2 border-red-600 p-2 text-center font-bold text-xs'>" +
                    "✗ Não é possível deletar a última página!</div>";
        }

        editoraService.deletarPagina(id);

        return "<div class='bg-green-100 border-2 border-green-600 p-2 text-center font-bold text-xs'>" +
                "✓ PÁGINA DELETADA!</div>";
    }

    // ==================== EXPORTAR PDF ====================

    /**
     * Exporta o volume completo para PDF
     * TODO: Implementar PDFExportService
     */
    @Autowired
    private com.example.PlataformaDarcy.service.PdfExportService pdfExportService;

    /**
     * Exporta o volume completo para PDF
     */
    @GetMapping("/volume/{volumeId}/exportar-pdf")
    public ResponseEntity<byte[]> exportarPDF(@PathVariable Long volumeId) {
        try {
            Volume volume = editoraService.buscarVolumePorId(volumeId)
                    .orElseThrow(() -> new RuntimeException("Volume não encontrado"));

            byte[] pdfBytes = pdfExportService.gerarPdfVolume(volume);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    volume.getLivro().getTitulo().replaceAll("\\s+", "_") + "_Vol" + volume.getNumero() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== TEMPLATES ====================

    /**
     * Lista templates disponíveis
     */
    @GetMapping("/templates")
    public String listarTemplates(@AuthenticationPrincipal Usuario usuario, Model model) {
        List<TemplatePagina> templates = editoraService.listarTemplatesDoUsuario(usuario.getId());
        model.addAttribute("templates", templates);
        return "admin/editor/fragments/lista-templates :: lista-templates";
    }

    /**
     * Aplica um template criando nova página
     */
    @PostMapping("/volume/{volumeId}/aplicar-template/{templateId}")
    public String aplicarTemplate(
            @PathVariable Long volumeId,
            @PathVariable Long templateId,
            @AuthenticationPrincipal Usuario usuario,
            Model model) {

        PaginaLivro novaPagina = editoraService.aplicarTemplate(templateId, volumeId, usuario);

        // Retornar lista atualizada
        List<PaginaLivro> paginas = editoraService.listarPaginasPorVolume(volumeId);
        model.addAttribute("paginas", paginas);
        model.addAttribute("novaPaginaId", novaPagina.getId());

        return "admin/editor/fragments/lista-paginas :: lista-paginas";
    }
}
