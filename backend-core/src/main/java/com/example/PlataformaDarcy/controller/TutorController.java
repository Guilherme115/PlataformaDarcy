package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Tutor IA 2.0 - Controller totalmente conectado à Plataforma.
 */
@Controller
@RequestMapping("/tutor")
public class TutorController {

    @Autowired
    private TutorService tutorService;
    @Autowired
    private ContextService contextService;

    // ==================== PÁGINA PRINCIPAL ====================

    @GetMapping
    public String abrirTutorIA(@AuthenticationPrincipal Usuario usuario, Model model) {
        // Dados do contexto do aluno para o painel
        model.addAttribute("contexto", tutorService.getDadosContexto(usuario));

        // Obras agrupadas por tipo
        model.addAttribute("obrasAgrupadas", tutorService.getObrasAgrupadas());

        return "aluno/tutor-ia";
    }

    // ==================== CHAT ENDPOINTS ====================

    /**
     * Pergunta geral com contexto do aluno.
     */
    @PostMapping("/perguntar")
    @ResponseBody
    public String processarPergunta(@AuthenticationPrincipal Usuario usuario,
            @RequestParam String mensagem) {
        try {
            return tutorService.perguntarAoDarcy(usuario, mensagem);
        } catch (Exception e) {
            return "❌ Ops! Tive um problema: " + e.getMessage();
        }
    }

    // ==================== ANÁLISE DE ERROS ====================

    /**
     * Análise geral de erros.
     */
    @PostMapping("/analisar-erros")
    @ResponseBody
    public String analisarErros(@AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) String materia,
            @RequestParam(required = false) Integer tempMin) {
        try {
            return tutorService.analisarErros(usuario, materia, tempMin);
        } catch (Exception e) {
            return "❌ Erro ao analisar: " + e.getMessage();
        }
    }

    /**
     * Análise de erros críticos (≥70°).
     */
    @PostMapping("/analisar-criticos")
    @ResponseBody
    public String analisarCriticos(@AuthenticationPrincipal Usuario usuario) {
        try {
            return tutorService.analisarErrosCriticos(usuario);
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }

    // ==================== ANÁLISE DE DESEMPENHO ====================

    /**
     * Análise de desempenho por matéria.
     */
    @PostMapping("/desempenho")
    @ResponseBody
    public String analisarDesempenho(@AuthenticationPrincipal Usuario usuario) {
        try {
            return tutorService.analisarDesempenho(usuario);
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Qual matéria priorizar.
     */
    @PostMapping("/prioridade")
    @ResponseBody
    public String sugerirPrioridade(@AuthenticationPrincipal Usuario usuario) {
        try {
            return tutorService.sugerirPrioridade(usuario);
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Análise do último simulado.
     */
    @PostMapping("/ultimo-simulado")
    @ResponseBody
    public String analisarUltimoSimulado(@AuthenticationPrincipal Usuario usuario) {
        try {
            return tutorService.analisarUltimoSimulado(usuario);
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }

    // ==================== OBRAS DO PAS ====================

    /**
     * Pergunta sobre obra específica.
     */
    @PostMapping("/obra/{obraId}")
    @ResponseBody
    public String perguntarSobreObra(@AuthenticationPrincipal Usuario usuario,
            @PathVariable String obraId,
            @RequestParam String pergunta) {
        try {
            return tutorService.perguntarSobreObra(usuario, obraId, pergunta);
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Como a obra pode cair na prova.
     */
    @PostMapping("/obra/{obraId}/como-cai")
    @ResponseBody
    public String comoObraCai(@PathVariable String obraId) {
        try {
            return tutorService.explicarComoObraCai(obraId);
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }

    /**
     * Lista obras para o seletor.
     */
    @GetMapping("/obras")
    @ResponseBody
    public Map<String, List<Map<String, Object>>> listarObras() {
        return tutorService.getObrasAgrupadas();
    }

    // ==================== DADOS DO CONTEXTO ====================

    /**
     * Retorna JSON com dados do aluno para o painel.
     */
    @GetMapping("/contexto")
    @ResponseBody
    public Map<String, Object> getContexto(@AuthenticationPrincipal Usuario usuario) {
        return tutorService.getDadosContexto(usuario);
    }
}