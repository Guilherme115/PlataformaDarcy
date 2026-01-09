package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.service.PdfSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller para busca inteligente de PDFs.
 * Usa IA para encontrar materiais de estudo na internet.
 */
@Controller
@RequestMapping("/pdf-search")
public class PdfSearchController {

    @Autowired
    private PdfSearchService pdfSearchService;

    /**
     * Página principal de busca de PDFs.
     */
    @GetMapping
    public String paginaBusca(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario != null) {
            model.addAttribute("nome", usuario.getNome());
        }
        return "aluno/pdf-search";
    }

    /**
     * Endpoint de busca que retorna PDFs em JSON.
     */
    @PostMapping("/buscar")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> buscarPdfs(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        // Limita o tamanho da query
        if (query.length() > 500) {
            query = query.substring(0, 500);
        }

        List<Map<String, String>> resultados = pdfSearchService.buscarPdfs(query.trim());
        return ResponseEntity.ok(resultados);
    }

    /**
     * Sugestões rápidas para a interface.
     */
    @GetMapping("/sugestoes")
    @ResponseBody
    public ResponseEntity<List<String>> getSugestoes() {
        List<String> sugestoes = List.of(
                "Números complexos e geometria analítica",
                "Funções exponenciais e logarítmicas",
                "Mecânica clássica - Leis de Newton",
                "Química orgânica - Hidrocarbonetos",
                "Literatura brasileira - Modernismo",
                "Redação ENEM - Estrutura argumentativa",
                "Biologia celular - Mitose e meiose",
                "História do Brasil - Era Vargas");
        return ResponseEntity.ok(sugestoes);
    }
}
