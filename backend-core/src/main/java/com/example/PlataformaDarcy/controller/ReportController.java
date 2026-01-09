package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import com.example.PlataformaDarcy.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/report")
public class ReportController {

    @Autowired private AuthService authService;
    @Autowired private ReportConteudoRepository reportRepo;
    @Autowired private BugTrackerRepository bugRepo;
    @Autowired private QuestaoRepository questaoRepo;

    // 1. MODAL DE CONTEÚDO (Abre dentro da prova)
    @GetMapping("/questao/{id}")
    public String modalReportQuestao(@PathVariable Long id, Model model) {
        Questao q = questaoRepo.findById(id).orElseThrow();
        model.addAttribute("questao", q);
        model.addAttribute("tiposErro", ReportConteudo.TipoErro.values());
        // Caminho: templates/fragments/modais-report.html
        return "fragments/modais-report :: modal-conteudo";
    }

    // 2. MODAL DE BUG (Geral)
    @GetMapping("/bug")
    public String modalBugGeral() {
        return "fragments/modais-report :: modal-bug";
    }

    // 3. SALVAR REPORT DE CONTEÚDO
    @PostMapping("/salvar-conteudo")
    @ResponseBody
    public String salvarConteudo(@RequestParam Long questaoId,
                                 @RequestParam ReportConteudo.TipoErro tipoErro,
                                 @RequestParam String descricao) {
        Usuario u = authService.getUsuarioLogado();
        Questao q = questaoRepo.findById(questaoId).orElseThrow();

        ReportConteudo r = new ReportConteudo();
        r.setUsuario(u);
        r.setQuestao(q);
        r.setProva(q.getProva());
        r.setTipoErro(tipoErro);
        r.setDescricao(descricao);

        reportRepo.save(r);

        return "<div class='bg-green-100 text-green-800 p-4 font-bold border-2 border-green-600 text-center'>✅ Report enviado!</div>";
    }

    // 4. SALVAR BUG TÉCNICO
    @PostMapping("/salvar-bug")
    @ResponseBody
    public String salvarBug(@RequestParam String titulo,
                            @RequestParam String descricao,
                            @RequestParam String urlAtual,
                            @RequestParam String userAgent,
                            @RequestParam String resolucao) {

        BugTracker bug = new BugTracker();
        try {
            bug.setUsuario(authService.getUsuarioLogado());
        } catch (Exception e) { }

        bug.setTitulo(titulo);
        bug.setDescricao(descricao);
        bug.setUrlOrigem(urlAtual);
        bug.setUserAgent(userAgent);
        bug.setResolucaoTela(resolucao);
        bug.setCategoria("GERAL");

        bugRepo.save(bug);

        return "<div class='bg-black text-white p-4 font-mono font-bold text-center'>🐛 Bug registrado.</div>";
    }
}