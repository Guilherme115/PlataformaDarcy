package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String matriculaGerada, Model model) {
        // Se o aluno acabou de se cadastrar, mostramos a matrícula dele na tela de login
        if (matriculaGerada != null) {
            model.addAttribute("matriculaRecemCriada", matriculaGerada);
        }
        return "public/login"; // Caminho: templates/public/login.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "public/register"; // Caminho: templates/public/register.html
    }

    @PostMapping("/register")
    public String registrar(@RequestParam String nome,
                            // @RequestParam String matricula, -> REMOVIDO (Gerado Auto)
                            @RequestParam String email,
                            @RequestParam String senha,
                            @RequestParam String regiao, // NOVO CAMPO
                            Model model) {
        try {
            // O serviço agora retorna a matrícula gerada
            String novaMatricula = authService.registrarEstudante(nome, email, senha, regiao);

            // Redireciona para login passando a matrícula na URL para exibir pro usuário
            return "redirect:/login?registered=true&matriculaGerada=" + novaMatricula;

        } catch (Exception e) {
            // Se der erro, volta pro formulário de registro com a mensagem
            model.addAttribute("error", e.getMessage());
            return "public/register";
        }
    }

    // --- RECUPERAÇÃO DE SENHA (MANTIDO IGUAL) ---

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "public/forgot-password";
    }

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
        return "public/change-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, @RequestParam String senha) {
        authService.atualizarSenhaComToken(token, senha);
        return "redirect:/login?resetSuccess";
    }
}