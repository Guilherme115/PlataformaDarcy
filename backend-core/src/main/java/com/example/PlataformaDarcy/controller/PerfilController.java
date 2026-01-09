package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.service.AuthService;
import com.example.PlataformaDarcy.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;
    @Autowired
    private AuthService authService;

    @GetMapping
    public String paginaPerfil(Model model) {
        Usuario usuario = authService.getUsuarioLogado();
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAllAttributes(perfilService.carregarDadosPerfil(usuario));
        return "perfil/perfil";
    }

    @GetMapping("/editar")
    public String paginaEditar(Model model) {
        Usuario usuario = authService.getUsuarioLogado();
        model.addAttribute("usuario", usuario);
        return "perfil/editar";
    }

    @PostMapping("/atualizar")
    public String atualizarPerfil(@RequestParam String nome,
            @RequestParam String regiao,
            @RequestParam(defaultValue = "1") Integer etapaAlvo,
            RedirectAttributes ra) {
        Usuario usuario = authService.getUsuarioLogado();
        perfilService.atualizarPerfil(usuario.getId(), nome, regiao, etapaAlvo);
        ra.addFlashAttribute("sucesso", "Perfil atualizado com sucesso!");
        return "redirect:/perfil";
    }

    @GetMapping("/simulados")
    public String historicoSimulados(Model model) {
        Usuario usuario = authService.getUsuarioLogado();
        model.addAllAttributes(perfilService.carregarDadosPerfil(usuario));
        return "perfil/historico-simulados";
    }

    @GetMapping("/conquistas")
    public String conquistas(Model model) {
        Usuario usuario = authService.getUsuarioLogado();
        model.addAllAttributes(perfilService.carregarDadosPerfil(usuario));
        return "perfil/conquistas";
    }
}
