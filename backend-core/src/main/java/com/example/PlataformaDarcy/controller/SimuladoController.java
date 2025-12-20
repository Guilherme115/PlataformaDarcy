package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import com.example.PlataformaDarcy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/simulado")
public class SimuladoController {

    @Autowired private SimuladoRepository simuladoRepo;
    @Autowired private ResolucaoRepository resolucaoRepo;
    @Autowired private ProvaRepository provaRepo;
    @Autowired private RegistroErroRepository erroRepo;
    @Autowired private QuestaoRepository questaoRepo;
    @Autowired private AlgoritmoService algoritmoService;
    @Autowired private AuthService authService;
    @Autowired private TaxonomyService taxonomyService;

    // =========================================================
    // 1. ENDPOINTS DE NAVEGAÇÃO (URLs fixas primeiro)
    // =========================================================

    @GetMapping
    public String menuPrincipal(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario == null) usuario = authService.getUsuarioLogado();

        List<Simulado> emAberto = simuladoRepo.findByUsuarioOrderByDataInicioDesc(usuario).stream()
                .filter(s -> s.getDataFim() == null).limit(3).toList();

        long pendentes = 0;
        try {
            pendentes = erroRepo.findByUsuarioAndStatus(usuario, RegistroErro.StatusCiclo.PENDENTE_TRIAGEM).size();
        } catch (Exception e) { /* ignora */ }

        model.addAttribute("simuladosEmAberto", emAberto);
        model.addAttribute("errosPendentes", pendentes);
        return "simulado-menu";
    }

    @GetMapping("/personalizado")
    public String paginaConfiguracao(Model model) {
        try {
            model.addAttribute("taxonomyJson", new ObjectMapper().writeValueAsString(taxonomyService.getTaxonomy()));
        } catch (Exception e) {
            model.addAttribute("taxonomyJson", "{}");
        }
        return "simulado-config";
    }

    // --- CORREÇÃO AQUI: Usa o método exato do seu ProvaRepository ---
    @GetMapping("/arquivo")
    public String arquivoCentral(@RequestParam(required = false, defaultValue = "1") Integer etapa, Model model) {
        // CORRIGIDO: De 'findByEtapa' para 'findAllByEtapaOrderByAnoDesc'
        List<Prova> provas = provaRepo.findAllByEtapaOrderByAnoDesc(etapa);

        model.addAttribute("etapaAtiva", etapa);
        model.addAttribute("provas", provas);
        return "simulado-arquivo";
    }

    @GetMapping("/erros")
    public String abrirCadernoErros(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario == null) usuario = authService.getUsuarioLogado();

        List<RegistroErro> todos = erroRepo.findByUsuarioOrderByTemperaturaDesc(usuario);
        List<RegistroErro> triagem = todos.stream().filter(e -> e.getStatus() == RegistroErro.StatusCiclo.PENDENTE_TRIAGEM).toList();

        long tipoC = todos.stream().filter(e -> e.getQuestaoOriginal().getTipo().name().equals("C")).count();

        model.addAttribute("triagem", triagem);
        model.addAttribute("statTotal", todos.size());
        model.addAttribute("statTipoC", tipoC);

        return "caderno-erros";
    }

    // =========================================================
    // 2. MÉTODOS DE AÇÃO (POST)
    // =========================================================

    @PostMapping("/gerar")
    public String gerarBateria(@AuthenticationPrincipal Usuario usuario,
                               @RequestParam(required = false) Integer etapa,
                               @RequestParam(required = false) String materia,
                               @RequestParam(defaultValue = "10") Integer quantidade) {
        if (usuario == null) usuario = authService.getUsuarioLogado();

        Simulado s = new Simulado();
        s.setUsuario(usuario);
        s.setTitulo("TREINO: " + (materia != null ? materia : "GERAL"));
        s.setModo(Simulado.ModoExecucao.APRENDIZADO);
        s.setDataInicio(LocalDateTime.now());
        s = simuladoRepo.save(s);

        List<Questao> questoes = questaoRepo.findAll().stream()
                .filter(q -> etapa == null || (q.getProva() != null && q.getProva().getEtapa().equals(etapa)))
                .limit(quantidade)
                .collect(Collectors.toList());

        for (Questao q : questoes) {
            Resolucao r = new Resolucao();
            r.setSimulado(s);
            r.setQuestao(q);
            resolucaoRepo.save(r);
        }
        return "redirect:/simulado/" + s.getId();
    }

    @PostMapping("/gerar-prova-completa/{id}")
    public String gerarProvaCompleta(@PathVariable Long id) {
        Usuario usuario = authService.getUsuarioLogado();
        Prova provaOriginal = provaRepo.findById(id).orElseThrow();

        Simulado s = new Simulado();
        s.setUsuario(usuario);
        s.setTitulo("PROVA: " + provaOriginal.getAno() + " (PAS " + provaOriginal.getEtapa() + ")");
        s.setModo(Simulado.ModoExecucao.LIVRE);
        s.setDataInicio(LocalDateTime.now());
        s = simuladoRepo.save(s);

        for (Questao q : provaOriginal.getQuestoes()) {
            Resolucao r = new Resolucao();
            r.setSimulado(s);
            r.setQuestao(q);
            resolucaoRepo.save(r);
        }
        return "redirect:/simulado/" + s.getId();
    }

    @PostMapping("/erros/classificar/{id}")
    public String classificarErro(@PathVariable Long id, @RequestParam RegistroErro.CausaErro causa) {
        RegistroErro erro = erroRepo.findById(id).orElseThrow();
        erro.setCausa(causa);
        erro.setStatus(RegistroErro.StatusCiclo.PROTOCOLO_DIARIO);
        erro.setTemperatura(causa == RegistroErro.CausaErro.LACUNA_CONTEUDO ? 70 : 30);
        erroRepo.save(erro);
        return "redirect:/simulado/erros";
    }

    // =========================================================
    // 3. AMBIENTE DE PROVA (ID DINÂMICO)
    // O REGEX ":\\d+" IMPEDE QUE "arquivo" CAIA AQUI
    // =========================================================

    @GetMapping("/{id:\\d+}")
    public String ambienteProva(@PathVariable Long id, Model model) {
        Simulado s = simuladoRepo.findById(id).orElseThrow();
        if (s.getDataFim() != null) return "redirect:/simulado/" + id + "/resultado";

        List<Resolucao> res = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        for (Resolucao r : res) {
            if (r.getEnunciadoDinamico() != null) {
                r.getQuestao().setEnunciado(r.getEnunciadoDinamico());
                r.getQuestao().setGabarito(r.getGabaritoDinamico());
            }
        }

        int indice = 0;
        for(int i=0; i<res.size(); i++) if(res.get(i).getRespostaAluno() == null) { indice = i; break; }

        prepararModelQuestao(model, res, indice);
        return "prova-real";
    }

    @GetMapping("/{id:\\d+}/questao/{indice}")
    public String carregarQuestao(@PathVariable Long id, @PathVariable Integer indice, Model model) {
        List<Resolucao> res = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        Resolucao rAtual = res.get(indice);
        if (rAtual.getEnunciadoDinamico() != null) {
            rAtual.getQuestao().setEnunciado(rAtual.getEnunciadoDinamico());
            rAtual.getQuestao().setGabarito(rAtual.getGabaritoDinamico());
        }

        prepararModelQuestao(model, res, indice);
        return "prova-real :: conteudoQuestao";
    }

    @PostMapping("/responder/{resolucaoId}")
    public String responderQuestao(@PathVariable Long resolucaoId,
                                   @RequestParam(required = false) String resposta,
                                   @RequestParam(required = false) Long tempo,
                                   @RequestParam(required = false) Resolucao.NivelDificuldade feedback,
                                   Model model) {
        Resolucao r = resolucaoRepo.findById(resolucaoId).orElseThrow();

        if (r.getEnunciadoDinamico() != null) {
            r.getQuestao().setEnunciado(r.getEnunciadoDinamico());
            r.getQuestao().setGabarito(r.getGabaritoDinamico());
        }

        if (r.getSimulado().getDataFim() == null) {
            if (resposta != null) {
                r.setRespostaAluno(resposta);
                r.setDataResposta(LocalDateTime.now());
                if(tempo != null && tempo > 0) r.setTempoSegundos(tempo);

                String gabaritoOficial = r.getQuestao().getGabarito();
                if (r.getGabaritoDinamico() != null) gabaritoOficial = r.getGabaritoDinamico();

                if (gabaritoOficial != null) {
                    r.setCorreta(gabaritoOficial.equalsIgnoreCase(resposta));
                }
            }
            if (feedback != null) r.setFeedbackUsuario(feedback);
            resolucaoRepo.save(r);
        }

        List<Resolucao> todas = resolucaoRepo.findBySimuladoIdOrderByIdAsc(r.getSimulado().getId());
        prepararModelQuestao(model, todas, todas.indexOf(r));
        return "prova-real :: conteudoQuestao";
    }

    @PostMapping("/{id:\\d+}/finalizar")
    public String finalizarSimulado(@PathVariable Long id) {
        Simulado s = simuladoRepo.findById(id).orElseThrow();
        List<Resolucao> res = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        double nota = 0.0;

        for (Resolucao r : res) {
            boolean acertou = Boolean.TRUE.equals(r.getCorreta());

            if (r.getRespostaAluno() != null && acertou) nota += 1.0;
            else if (r.getRespostaAluno() != null) nota -= 1.0;

            if (s.getTipo() != null && (s.getTipo().contains("PROTOCOLO") || s.getTipo().contains("CENTRAL"))) {
                Optional<RegistroErro> erroOpt = erroRepo.findByUsuarioAndQuestaoOriginal(s.getUsuario(), r.getQuestao());
                erroOpt.ifPresent(re -> algoritmoService.processarResultadoExpurgo(re, acertou));
            } else {
                algoritmoService.processarErro(s.getUsuario(), r.getQuestao(), r.getCorreta(), r.getTempoSegundos(), r.getFeedbackUsuario());
            }
        }

        resolucaoRepo.saveAll(res);
        s.setNotaFinal(nota);
        s.setDataFim(LocalDateTime.now());
        simuladoRepo.save(s);
        return "redirect:/simulado/" + id + "/resultado";
    }

    @GetMapping("/{id:\\d+}/resultado")
    public String resultadoProva(@PathVariable Long id, Model model) {
        Simulado s = simuladoRepo.findById(id).orElseThrow();
        List<Resolucao> res = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        long acertos = res.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        long erros = res.stream().filter(r -> Boolean.FALSE.equals(r.getCorreta()) && r.getRespostaAluno() != null).count();
        long branco = res.stream().filter(r -> r.getRespostaAluno() == null).count();

        model.addAttribute("simulado", s);
        model.addAttribute("resolucoes", res);
        model.addAttribute("statsAcertos", acertos);
        model.addAttribute("statsErros", erros);
        model.addAttribute("statsBranco", branco);

        return "simulado-resultado";
    }

    private void prepararModelQuestao(Model model, List<Resolucao> resolucoes, int indice) {
        if (indice < 0) indice = 0;
        if (indice >= resolucoes.size()) indice = resolucoes.size() - 1;
        model.addAttribute("r", resolucoes.get(indice));
        model.addAttribute("indiceAtual", indice);
        model.addAttribute("total", resolucoes.size());
        model.addAttribute("simulado", resolucoes.get(indice).getSimulado());
    }
}