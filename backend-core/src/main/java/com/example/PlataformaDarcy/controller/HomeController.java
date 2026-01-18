package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.RegistroErro;
import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.ComunicadoRepository;
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

    @Autowired
    private ComunicadoRepository comunicadoRepo;

    @GetMapping("/500")
    public String triggerError() {
        throw new RuntimeException("Teste de Explosão Controlada 💥");
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario != null) {
            model.addAttribute("nome", usuario.getNome());

            // Verifica pendências para o alerta no card "Caderno de Erros"
            long pendentes = 0;
            try {
                pendentes = erroRepo.findByUsuarioAndStatus(usuario, RegistroErro.StatusCiclo.PENDENTE_TRIAGEM).size();
            } catch (Exception e) {
                /* Ignora se der erro no banco vazio */ }

            model.addAttribute("errosPendentes", pendentes);

            // Feed do Subsolo - Comunicados ativos
            model.addAttribute("feed", comunicadoRepo.findByAtivoTrueOrderByDataEnvioDesc());

            return "aluno/home"; // Caminho: templates/aluno/home.html
        }
        return "public/index"; // Caminho: templates/public/index.html
    }

    // Redirect /home to /
    @GetMapping("/home")
    public String redirectHome() {
        return "redirect:/";
    }

    // Método Darcy - Página institucional
    @GetMapping("/metodo")
    public String metodo() {
        return "public/metodo";
    }
}