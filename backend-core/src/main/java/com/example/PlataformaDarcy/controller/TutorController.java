package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    @Autowired
    private TutorService tutorService;

    // Carrega a página HTML
    @GetMapping
    public String abrirSalaDeEstudos() {
        return "tutor-chat";
    }

    // API que recebe a pergunta e devolve a resposta limpa
    @PostMapping("/perguntar")
    @ResponseBody
    public String processarPergunta(@RequestParam String mensagem) {
        try {
            // Simples e direto: processa e devolve o texto
            return tutorService.perguntarAoDarcy(mensagem);
        } catch (Exception e) {
            return "Ops! Tive um pequeno lapso de memória. Tente perguntar de novo? (Erro: " + e.getMessage() + ")";
        }
    }
}