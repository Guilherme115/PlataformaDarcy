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

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
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

    // Redirect /admin to /admin/dashboard
    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }

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
    @GetMapping("/bugs")
    public String paginaBugs(Model model) {
        model.addAttribute("bugs", bugRepo.findByResolvidoFalseOrderByDataReportDesc());
        return "admin/admin-bugs";
    }

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

    // ==================== EDITORA DE LIVROS ====================

    @Autowired
    private com.example.PlataformaDarcy.service.EditoraService editoraService;

    @GetMapping("/editora")
    public String paginaEditora(Model model) {
        model.addAttribute("colecoes", editoraService.listarColecoes());
        return "admin/editora/dashboard";
    }

    @GetMapping("/editora/colecoes")
    public String listarColecoes(Model model) {
        model.addAttribute("colecoes", editoraService.listarColecoes());
        return "admin/editora/colecoes";
    }

    @PostMapping("/editora/colecoes/salvar")
    public String salvarColecao(@RequestParam String nome,
            @RequestParam String descricao,
            @RequestParam(required = false) String imagemCapa,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario,
            RedirectAttributes ra) {
        editoraService.criarColecao(nome, descricao, usuario);
        ra.addFlashAttribute("sucesso", "Coleção criada com sucesso!");
        return "redirect:/admin/editora/colecoes";
    }

    @GetMapping("/editora/colecao/{id}/livros")
    public String listarLivros(@PathVariable Long id, Model model) {
        model.addAttribute("colecao", editoraService.buscarColecaoPorId(id).orElseThrow());
        model.addAttribute("livros", editoraService.listarLivrosPorColecao(id));
        return "admin/editora/livros";
    }

    @PostMapping("/editora/livros/salvar")
    public String salvarLivro(@RequestParam Long colecaoId,
            @RequestParam String titulo,
            @RequestParam(required = false) String subtitulo,
            RedirectAttributes ra) {
        editoraService.criarLivro(colecaoId, titulo, subtitulo);
        ra.addFlashAttribute("sucesso", "Livro criado com sucesso!");
        return "redirect:/admin/editora/colecao/" + colecaoId + "/livros";
    }

    @GetMapping("/editora/livro/{id}/volumes")
    public String listarVolumes(@PathVariable Long id, Model model) {
        model.addAttribute("livro", editoraService.buscarLivroPorId(id).orElseThrow());
        model.addAttribute("volumes", editoraService.listarVolumesPorLivro(id));
        return "admin/editora/volumes";
    }

    @PostMapping("/editora/volumes/salvar")
    public String salvarVolume(@RequestParam Long livroId,
            @RequestParam Integer numero,
            @RequestParam String titulo,
            @RequestParam(required = false) String descricao,
            RedirectAttributes ra) {
        editoraService.criarVolume(livroId, numero, titulo, descricao);
        ra.addFlashAttribute("sucesso", "Volume criado com sucesso!");
        return "redirect:/admin/editora/livro/" + livroId + "/volumes";
    }

    // ==================== CENTRAL DE SIMULADOS OFICIAIS ====================

    @Autowired
    private com.example.PlataformaDarcy.service.SimuladoGeradorService simuladoGeradorService;

    @GetMapping("/simulados-oficiais")
    public String centralSimuladosOficiais(Model model) {
        List<Prova> simulados = provaRepo.findByOrigemOrderByIdDesc("IA_OFICIAL");

        // Estatísticas gerais
        long totalSimulados = simulados.size();
        long simuladosAtivos = simulados.stream().filter(p -> Boolean.TRUE.equals(p.getAtivo())).count();
        long totalAcessos = simulados.stream()
                .mapToLong(p -> p.getContadorAcessos() != null ? p.getContadorAcessos() : 0)
                .sum();

        model.addAttribute("simulados", simulados);
        model.addAttribute("totalSimulados", totalSimulados);
        model.addAttribute("simuladosAtivos", simuladosAtivos);
        model.addAttribute("totalAcessos", totalAcessos);

        return "admin/simulados-oficiais";
    }

    @PostMapping("/simulados-oficiais/gerar")
    public String gerarNovoSimulado(@RequestParam String senha,
            @RequestParam Integer etapa,
            RedirectAttributes ra) {
        if (!"2711".equals(senha)) {
            ra.addFlashAttribute("erro", "Acesso Negado: Senha incorreta.");
            return "redirect:/admin/simulados-oficiais";
        }

        try {
            simuladoGeradorService.gerarSimuladoOficial(etapa);
            ra.addFlashAttribute("sucesso", "Novo Simulado Oficial (PAS " + etapa + ") gerado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("erro", "Erro ao gerar simulado: " + e.getMessage());
        }

        return "redirect:/admin/simulados-oficiais";
    }

    @PostMapping("/simulados-oficiais/{id}/toggle-status")
    public String toggleStatusSimulado(@PathVariable Long id, RedirectAttributes ra) {
        Prova prova = provaRepo.findById(id).orElseThrow();
        prova.setAtivo(!Boolean.TRUE.equals(prova.getAtivo()));
        provaRepo.save(prova);

        String status = Boolean.TRUE.equals(prova.getAtivo()) ? "ativado" : "desativado";
        ra.addFlashAttribute("sucesso", "Simulado " + status + " com sucesso!");

        return "redirect:/admin/simulados-oficiais";
    }

    @PostMapping("/simulados-oficiais/{id}/excluir")
    public String excluirSimulado(@PathVariable Long id, RedirectAttributes ra) {
        try {
            provaRepo.deleteById(id);
            ra.addFlashAttribute("sucesso", "Simulado excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/admin/simulados-oficiais";
    }

    // ==================== GESTÃO DE TAXONOMIA ====================

    @Autowired
    private com.example.PlataformaDarcy.service.TaxonomiaService taxonomiaService;

    @GetMapping("/taxonomia")
    public String paginaTaxonomia(Model model) {
        // Carrega taxonomias existentes por etapa
        model.addAttribute("conteudosPas1", taxonomiaService.listarPorEtapa(1));
        model.addAttribute("conteudosPas2", taxonomiaService.listarPorEtapa(2));
        model.addAttribute("conteudosPas3", taxonomiaService.listarPorEtapa(3));

        // Estatísticas
        model.addAttribute("totalPas1", taxonomiaService.contarPorEtapa(1));
        model.addAttribute("totalPas2", taxonomiaService.contarPorEtapa(2));
        model.addAttribute("totalPas3", taxonomiaService.contarPorEtapa(3));

        return "admin/taxonomia";
    }

    @PostMapping("/taxonomia/parse")
    public String parseTaxonomia(@RequestParam String texto,
            @RequestParam Integer etapa,
            @RequestParam(defaultValue = "false") boolean substituir,
            RedirectAttributes ra) {
        try {
            List<com.example.PlataformaDarcy.model.ConteudoProgramatico> parsed = taxonomiaService
                    .parseTextToTaxonomia(texto);

            // Validação: verifica se todas as etapas do texto batem com a selecionada
            boolean temErro = parsed.stream().anyMatch(cp -> !cp.getEtapa().equals(etapa));
            if (temErro) {
                ra.addFlashAttribute("erro",
                        "Erro: O texto contém conteúdos de etapas diferentes da selecionada (PAS " + etapa + ")!");
                ra.addFlashAttribute("textoAntigo", texto);
                return "redirect:/admin/taxonomia";
            }

            // Salva todos
            taxonomiaService.salvarTodos(parsed, substituir);

            String msg = parsed.size() + " tópicos importados com sucesso para PAS " + etapa + "!";
            if (substituir) {
                msg += " (conteúdos anteriores foram substituídos)";
            }
            ra.addFlashAttribute("sucesso", msg);

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", "Erro no parsing: " + e.getMessage());
            ra.addFlashAttribute("textoAntigo", texto);
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("erro", "Erro inesperado: " + e.getMessage());
            ra.addFlashAttribute("textoAntigo", texto);
        }

        return "redirect:/admin/taxonomia";
    }

    @PostMapping("/taxonomia/{id}/editar")
    @ResponseBody
    public String editarTopico(@PathVariable Long id,
            @RequestParam String topico,
            @RequestParam(required = false) String observacoes) {
        try {
            com.example.PlataformaDarcy.model.ConteudoProgramatico cp = taxonomiaService.buscarPorId(id);
            cp.setTopico(topico);
            cp.setObservacoes(observacoes);
            taxonomiaService.salvar(cp);
            return "{\"status\":\"OK\"}";
        } catch (Exception e) {
            return "{\"status\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/taxonomia/{id}/excluir")
    public String excluirTopico(@PathVariable Long id, RedirectAttributes ra) {
        try {
            taxonomiaService.excluir(id);
            ra.addFlashAttribute("sucesso", "Tópico excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/admin/taxonomia";
    }
}
