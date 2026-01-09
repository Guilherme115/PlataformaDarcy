package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import com.example.PlataformaDarcy.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private BugTrackerRepository bugRepo;
    @Autowired
    private ReportConteudoRepository reportRepo;
    @Autowired
    private ProvaRepository provaRepo;
    @Autowired
    private QuestaoRepository questaoRepo;
    @Autowired
    private ComunicadoRepository comunicadoRepo;
    @Autowired
    private UsuarioRepository usuarioRepo;

    // ==================== DASHBOARD ====================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(adminService.carregarDadosDashboard());
        return "admin/dashboard";
    }

    // ==================== PÁGINA DE USUÁRIOS ====================
    @GetMapping("/usuarios")
    public String paginaUsuarios(Model model) {
        model.addAttribute("usuarios", adminService.listarTodosUsuarios());
        return "admin/admin-usuarios";
    }

    @PostMapping("/usuarios/buscar")
    public String buscarUsuarios(@RequestParam(value = "termo", required = false) String termo, Model model) {
        model.addAttribute("usuarios", adminService.buscarUsuariosCompleto(termo));
        return "admin/admin-usuarios :: lista-tabela";
    }

    @PostMapping("/usuarios/{id}/toggle-ban")
    public String toggleBanUsuario(@PathVariable Long id, Model model) {
        Usuario user = adminService.toggleBanUsuario(id);
        model.addAttribute("u", user);
        return "admin/admin-usuarios :: linha-usuario";
    }

    @PostMapping("/usuarios/{id}/mudar-perfil")
    public String mudarPerfilUsuario(@PathVariable Long id, @RequestParam String novoPerfil, Model model) {
        Usuario user = adminService.alterarPerfil(id, novoPerfil);
        model.addAttribute("u", user);
        return "admin/admin-usuarios :: linha-usuario";
    }

    @PostMapping("/usuarios/{id}/reset-senha")
    @ResponseBody
    public String resetarSenhaUsuario(@PathVariable Long id) {
        String novaSenha = adminService.resetarSenha(id);
        return "<div class='bg-green-100 border-2 border-green-600 p-3 text-center font-mono text-xs'>" +
                "<strong>SENHA RESETADA!</strong><br>Nova senha: <code class='bg-white px-2 py-1 border'>" + novaSenha
                + "</code></div>";
    }

    // Quick actions from dashboard
    @PostMapping("/usuarios/buscar-rapido")
    public String buscarAlunoRapido(@RequestParam("termo") String termo, Model model) {
        List<Usuario> resultados = adminService.buscarAlunos(termo);
        model.addAttribute("resultados", resultados);
        return "admin/fragments/admin-fragments :: lista-busca-simples";
    }

    @GetMapping("/usuarios/detalhes/{id}")
    public String modalUsuario(@PathVariable Long id, Model model) {
        Usuario user = usuarioRepo.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "admin/fragments/admin-fragments :: modal-usuario-admin";
    }

    @PostMapping("/usuarios/banir/{id}")
    public String banirUsuario(@PathVariable Long id, Model model) {
        Usuario user = usuarioRepo.findById(id).orElseThrow();
        user.setAtivo(false);
        usuarioRepo.save(user);
        model.addAttribute("resultados", List.of(user));
        return "admin/fragments/admin-fragments :: lista-busca-simples";
    }

    @PostMapping("/usuarios/desbanir/{id}")
    public String desbanirUsuario(@PathVariable Long id, Model model) {
        Usuario user = usuarioRepo.findById(id).orElseThrow();
        user.setAtivo(true);
        usuarioRepo.save(user);
        model.addAttribute("resultados", List.of(user));
        return "admin/fragments/admin-fragments :: lista-busca-simples";
    }

    @PostMapping("/usuarios/promover/{id}")
    public String promoverUsuario(@PathVariable Long id, Model model) {
        Usuario user = usuarioRepo.findById(id).orElseThrow();
        user.setPerfil("ADMIN");
        usuarioRepo.save(user);
        model.addAttribute("resultados", List.of(user));
        return "admin/fragments/admin-fragments :: lista-busca-simples";
    }

    // ==================== PÁGINA DE TRANSMISSÕES ====================
    @GetMapping("/transmissoes")
    public String paginaTransmissoes(Model model) {
        model.addAttribute("comunicados", comunicadoRepo.findAllByOrderByDataEnvioDesc());
        return "admin/admin-transmissoes";
    }

    @PostMapping("/transmissoes/salvar")
    public String salvarTransmissao(@RequestParam String titulo,
            @RequestParam String mensagem,
            @RequestParam String tipo,
            @RequestParam(defaultValue = "false") boolean ativo,
            RedirectAttributes ra) {
        Comunicado c = new Comunicado();
        c.setTitulo(titulo);
        c.setMensagem(mensagem);
        c.setTipo(tipo.toUpperCase());
        c.setAtivo(ativo);
        comunicadoRepo.save(c);
        ra.addFlashAttribute("sucesso", "Aviso publicado com sucesso!");
        return "redirect:/admin/transmissoes";
    }

    @PostMapping("/transmissoes/{id}/toggle")
    public String toggleTransmissao(@PathVariable Long id, Model model) {
        Comunicado c = comunicadoRepo.findById(id).orElseThrow();
        c.setAtivo(!c.isAtivo());
        comunicadoRepo.save(c);
        model.addAttribute("c", c);
        return "admin/admin-transmissoes :: botao-status";
    }

    @PostMapping("/transmissoes/{id}/excluir")
    public String excluirTransmissao(@PathVariable Long id, RedirectAttributes ra) {
        comunicadoRepo.deleteById(id);
        ra.addFlashAttribute("sucesso", "Aviso excluído permanentemente!");
        return "redirect:/admin/transmissoes";
    }

    // Quick transmit from dashboard
    @PostMapping("/transmitir")
    @ResponseBody
    public String transmitir(@RequestParam String titulo, @RequestParam String mensagem,
            @RequestParam(defaultValue = "INFO") String tipo) {
        Comunicado c = new Comunicado();
        c.setTitulo(titulo);
        c.setMensagem(mensagem);
        c.setTipo(tipo.toUpperCase());
        c.setAtivo(true);
        comunicadoRepo.save(c);
        return "<div class='bg-green-100 text-green-800 p-2 font-bold border-2 border-green-600 text-center text-xs mt-2'>✓ TRANSMITIDO COM SUCESSO!</div>";
    }

    // ==================== BUGS ====================
    @PostMapping("/bug/resolver/{id}")
    @ResponseBody
    public String resolverBug(@PathVariable Long id) {
        adminService.resolverBug(id);
        return "";
    }

    // ==================== MODAIS DE DETALHES ====================
    @GetMapping("/detalhes/bug/{id}")
    public String modalBug(@PathVariable Long id, Model model) {
        BugTracker bug = bugRepo.findById(id).orElseThrow();
        model.addAttribute("bug", bug);
        model.addAttribute("logs",
                List.of("ERROR [System]: NullPointer at line 42", "WARN [Auth]: Retry limit exceeded"));
        return "fragments/modais-report :: modal-bug-admin";
    }

    @GetMapping("/detalhes/report/{id}")
    public String modalReport(@PathVariable Long id, Model model) {
        ReportConteudo rep = reportRepo.findById(id).orElseThrow();
        model.addAttribute("rep", rep);
        model.addAttribute("questao", rep.getQuestao());
        return "fragments/modais-report :: modal-report-admin";
    }

    @GetMapping("/detalhes/prova/{id}")
    public String modalProva(@PathVariable Long id, Model model) {
        Prova prova = provaRepo.findById(id).orElseThrow();
        List<Questao> questoes = questaoRepo.findByProvaIdOrderByNumeroAsc(id);
        model.addAttribute("prova", prova);
        model.addAttribute("questoes", questoes);
        return "fragments/modais-report :: modal-prova-admin";
    }
}