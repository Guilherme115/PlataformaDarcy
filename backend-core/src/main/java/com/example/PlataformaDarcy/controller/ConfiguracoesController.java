package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.UsuarioRepository;
import com.example.PlataformaDarcy.service.AuthService;
import com.example.PlataformaDarcy.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Controller
@RequestMapping("/configuracoes")
public class ConfiguracoesController {

    @Autowired
    private AuthService authService;
    @Autowired
    private PerfilService perfilService;
    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String paginaConfiguracoes(Model model) {
        Usuario usuario = authService.getUsuarioLogado();
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        // Regiões para o select
        model.addAttribute("regioes", java.util.List.of(
                "Plano Piloto", "Ceilândia", "Taguatinga", "Samambaia", "Águas Claras",
                "Gama", "Guará", "Santa Maria", "Planaltina", "Recanto das Emas",
                "Sobradinho", "Outra / Entorno"));
        return "aluno/configuracoes";
    }

    @PostMapping("/perfil")
    public String atualizarPerfil(@RequestParam String nome,
            @RequestParam String regiao,
            @RequestParam(defaultValue = "1") Integer etapaAlvo,
            RedirectAttributes ra) {
        Usuario usuario = authService.getUsuarioLogado();
        perfilService.atualizarPerfil(usuario.getId(), nome.toUpperCase(), regiao, etapaAlvo);
        ra.addFlashAttribute("sucesso", "Perfil atualizado! As mudanças já estão valendo.");
        return "redirect:/configuracoes";
    }

    @PostMapping("/senha")
    public String atualizarSenha(@RequestParam String senhaAtual,
            @RequestParam String novaSenha,
            @RequestParam String confirmacaoSenha,
            RedirectAttributes ra) {
        Usuario usuario = authService.getUsuarioLogado();

        // 1. Verifica provider
        if (usuario.getProvider() == Usuario.Provider.GOOGLE) {
            ra.addFlashAttribute("erro", "Contas Google não possuem senha para alterar.");
            return "redirect:/configuracoes";
        }

        // 2. Verifica se nova senha bate com confirmação
        if (!novaSenha.equals(confirmacaoSenha)) {
            ra.addFlashAttribute("erro", "A nova senha e a confirmação não coincidem.");
            return "redirect:/configuracoes";
        }

        // 3. Verifica senha atual
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            ra.addFlashAttribute("erro", "A senha atual está incorreta.");
            return "redirect:/configuracoes";
        }

        // 4. Salva nova senha
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepo.save(usuario);

        ra.addFlashAttribute("sucesso", "Senha de segurança atualizada com sucesso!");
        return "redirect:/configuracoes";
    }

    @PostMapping("/excluir")
    public String excluirConta(@RequestParam String confirmacao, RedirectAttributes ra) {
        Usuario usuario = authService.getUsuarioLogado();
        if (!"DELETAR".equalsIgnoreCase(confirmacao)) {
            ra.addFlashAttribute("erro", "Você deve digitar DELETAR para confirmar.");
            return "redirect:/configuracoes";
        }

        // Soft delete (desativar) ou Hard delete?
        // Vamos desativar por segurança
        usuario.setAtivo(false);
        usuarioRepo.save(usuario);

        return "redirect:/logout";
    }

    @GetMapping("/dados/exportar")
    public ResponseEntity<byte[]> exportarDados() {
        Usuario usuario = authService.getUsuarioLogado();
        java.util.Map<String, Object> dados = perfilService.exportarDados(usuario);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            byte[] content = mapper.writeValueAsBytes(dados);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"meus_dados_darcy.json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(content);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
