package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired private AuthService authService;

    // LOGIN (Página padrão)
    @GetMapping("/login")
    public String login() { return "login"; }

    // REGISTRO
    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String registrar(@RequestParam String nome,
                            @RequestParam String matricula,
                            @RequestParam String email,
                            @RequestParam String senha,
                            Model model) {
        try {
            authService.registrarEstudante(nome, matricula, email, senha);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // ESQUECI A SENHA (Solicitar)
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() { return "forgot-password"; }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email) {
        authService.processarEsqueciSenha(email);
        return "redirect:/login?resetSent";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        if (!authService.validarToken(token)) {
            return "redirect:/login?invalidToken";
        }
        model.addAttribute("token", token);
        return "change-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, @RequestParam String senha) {
        authService.atualizarSenhaComToken(token, senha);
        return "redirect:/login?resetSuccess";
    }
}