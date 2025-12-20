package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.RegistroErro;
import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.RegistroErroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private RegistroErroRepository erroRepo;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario != null) {
            model.addAttribute("nome", usuario.getNome());

            // Verifica pendências para o alerta no card "Caderno de Erros"
            long pendentes = erroRepo.findByUsuarioAndStatus(usuario, RegistroErro.StatusCiclo.PENDENTE_TRIAGEM).size();
            model.addAttribute("errosPendentes", pendentes);

            return "aluno-home";
        }
        return "index";
    }
}