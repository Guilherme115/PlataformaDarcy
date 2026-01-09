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
// import java.util.Comparator; // Removido pois não é mais usado
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/simulado")
public class SimuladoController {

    @Autowired
    private SimuladoRepository simuladoRepo;
    @Autowired
    private ResolucaoRepository resolucaoRepo;
    @Autowired
    private ProvaRepository provaRepo;
    @Autowired
    private RegistroErroRepository erroRepo;
    @Autowired
    private QuestaoRepository questaoRepo;
    @Autowired
    private AlgoritmoService algoritmoService;
    @Autowired
    private AuthService authService;
    @Autowired
    private TaxonomyService taxonomyService;

    // --- NAVEGAÇÃO (Manteve igual) ---

    @GetMapping
    public String menuPrincipal(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario == null)
            usuario = authService.getUsuarioLogado();

        // OTIMIZADO: Busca apenas os 3 em aberto diretamente do banco
        List<Simulado> emAberto = simuladoRepo.findTop3ByUsuarioAndDataFimIsNullOrderByDataInicioDesc(usuario);

        // OTIMIZADO: Usa COUNT ao invés de carregar todos e contar
        long pendentes = erroRepo.countByUsuarioAndStatus(usuario, RegistroErro.StatusCiclo.PENDENTE_TRIAGEM);

        model.addAttribute("simuladosEmAberto", emAberto);
        model.addAttribute("errosPendentes", pendentes);

        return "simulado/menu";
    }

    @GetMapping("/personalizado")
    public String paginaConfiguracao(Model model) {
        try {
            model.addAttribute("taxonomyJson", new ObjectMapper().writeValueAsString(taxonomyService.getTaxonomy()));
        } catch (Exception e) {
            model.addAttribute("taxonomyJson", "{}");
        }
        return "simulado/config";
    }

    @GetMapping("/arquivo")
    public String arquivoCentral(@RequestParam(required = false, defaultValue = "1") Integer etapa, Model model) {
        List<Prova> provas = provaRepo.findAllByEtapaOrderByAnoDesc(etapa);
        model.addAttribute("etapaAtiva", etapa);
        model.addAttribute("provas", provas);
        return "simulado/arquivo";
    }

    @GetMapping("/erros")
    public String abrirCadernoErros(@AuthenticationPrincipal Usuario usuario, Model model) {
        if (usuario == null)
            usuario = authService.getUsuarioLogado();

        List<RegistroErro> todos = erroRepo.findByUsuarioOrderByTemperaturaDesc(usuario);
        List<RegistroErro> triagem = todos.stream()
                .filter(e -> e.getStatus() == RegistroErro.StatusCiclo.PENDENTE_TRIAGEM).toList();
        List<RegistroErro> revisoesPendentes = algoritmoService.buscarRevisoesPendentesHoje(usuario);
        long tipoC = todos.stream().filter(e -> e.getQuestaoOriginal().getTipo().name().equals("C")).count();

        // Estatísticas completas
        AlgoritmoService.EstatisticasErros stats = algoritmoService.calcularEstatisticas(usuario);

        model.addAttribute("triagem", triagem);
        model.addAttribute("revisoesPendentes", revisoesPendentes);
        model.addAttribute("statTotal", stats.total());
        model.addAttribute("statPendentes", stats.pendentesTriagem());
        model.addAttribute("statProtocolo", stats.emProtocolo());
        model.addAttribute("statDominados", stats.dominados());
        model.addAttribute("statCriticos", stats.criticos());
        model.addAttribute("statTempMedia", (int) stats.temperaturaMedia());
        model.addAttribute("statTipoC", tipoC);

        return "simulado/caderno-erros";
    }

    // --- AÇÕES ---

    @PostMapping("/gerar")
    public String gerarBateria(@AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) Integer etapa,
            @RequestParam(required = false) String materia,
            @RequestParam(defaultValue = "10") Integer quantidade) {
        if (usuario == null)
            usuario = authService.getUsuarioLogado();

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

    // ================================================================
    // AQUI ESTÁ A ALTERAÇÃO PRINCIPAL
    // ================================================================
    @PostMapping("/gerar-prova-completa/{id}")
    public String gerarProvaCompleta(@PathVariable Long id,
            @RequestParam String idioma) { // Adicionado parametro idioma

        Usuario usuario = authService.getUsuarioLogado();
        Prova provaOriginal = provaRepo.findById(id).orElseThrow();

        Simulado s = new Simulado();
        s.setUsuario(usuario);
        s.setTitulo("PROVA: " + provaOriginal.getAno() + " (PAS " + provaOriginal.getEtapa() + ")");
        s.setModo(Simulado.ModoExecucao.LIVRE); // Ou APRENDIZADO, dependendo da sua regra
        s.setDataInicio(LocalDateTime.now());
        s = simuladoRepo.save(s);

        // Filtra as questões removendo os idiomas não selecionados
        List<Questao> questoesFiltradas = provaOriginal.getQuestoes().stream()
                .filter(q -> filtrarIdioma(q, idioma))
                .collect(Collectors.toList());

        // DEDUPLICAÇÃO: Garante apenas uma questão por número (para limpar banco sujo)
        // Usamos um Map para sobrescrever duplicatas (mantendo a última encontrada ou
        // primeira, tanto faz neste caso)
        // TreeMap para já manter ordenado pelo número
        java.util.Map<Integer, Questao> questoesUnicas = new java.util.TreeMap<>();
        for (Questao q : questoesFiltradas) {
            // Se já tem a questão, não sobrescreve (ou sobrescreve, depende da estratégia.
            // Aqui mantemos a primeira)
            questoesUnicas.putIfAbsent(q.getNumero(), q);
        }

        List<Questao> questoesFinais = new java.util.ArrayList<>(questoesUnicas.values());

        for (Questao q : questoesFinais) {
            Resolucao r = new Resolucao();
            r.setSimulado(s);
            r.setQuestao(q);
            resolucaoRepo.save(r);
        }
        return "redirect:/simulado/" + s.getId();
    }

    /**
     * Lógica auxiliar para decidir se a questão entra no simulado baseado no
     * idioma.
     * Assume que q.getMateria() retorna algo como "Inglês", "Espanhol",
     * "Matemática", etc.
     */
    private boolean filtrarIdioma(Questao q, String idiomaSelecionado) {
        // 1. Tenta identificar pelo Bloco (Mais confiável)
        if (q.getBloco() != null && q.getBloco().getDisciplina() != null) {
            String disciplina = q.getBloco().getDisciplina().toUpperCase();

            // Se for GERAL, entra sempre
            if (disciplina.contains("GERAL"))
                return true;

            // Se for língua estrangeira, verifica se bate com a seleção
            boolean isIngles = disciplina.contains("INGLÊS") || disciplina.contains("INGLES");
            boolean isEspanhol = disciplina.contains("ESPANHOL");
            boolean isFrances = disciplina.contains("FRANCÊS") || disciplina.contains("FRANCES");

            if (isIngles || isEspanhol || isFrances) {
                switch (idiomaSelecionado) {
                    case "INGLES":
                        return isIngles;
                    case "ESPANHOL":
                        return isEspanhol;
                    case "FRANCES":
                        return isFrances;
                    default:
                        return true;
                }
            }
            // Se não for nenhuma das 3 (o que não deve ocorrer se for GERAL, mas por
            // segurança), aprova.
            return true;
        }

        // 2. Fallback: Tenta pelas TAGS (Lógica antiga)
        String tags = q.getTags();
        if (tags == null)
            return true;

        String tagsUpper = tags.toUpperCase();
        boolean isIngles = tagsUpper.contains("INGLÊS") || tagsUpper.contains("INGLES");
        boolean isEspanhol = tagsUpper.contains("ESPANHOL");
        boolean isFrances = tagsUpper.contains("FRANCÊS") || tagsUpper.contains("FRANCES");

        if (!isIngles && !isEspanhol && !isFrances)
            return true;

        switch (idiomaSelecionado) {
            case "INGLES":
                return isIngles;
            case "ESPANHOL":
                return isEspanhol;
            case "FRANCES":
                return isFrances;
            default:
                return true;
        }
    }
    // ================================================================

    @PostMapping("/erros/classificar/{id}")
    public String classificarErro(@PathVariable Long id, @RequestParam RegistroErro.CausaErro causa) {
        algoritmoService.classificarErro(id, causa);
        return "redirect:/simulado/erros";
    }

    // --- PROVA REAL ---

    @GetMapping("/{id:\\d+}")
    public String ambienteProva(@PathVariable Long id, Model model) {
        Simulado s = simuladoRepo.findById(id).orElseThrow();
        if (s.getDataFim() != null)
            return "redirect:/simulado/" + id + "/resultado";

        List<Resolucao> res = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);
        int indice = 0;
        for (int i = 0; i < res.size(); i++)
            if (res.get(i).getRespostaAluno() == null) {
                indice = i;
                break;
            }

        prepararModelQuestao(model, res, indice);

        return "simulado/ambiente-prova";
    }

    @GetMapping("/{id:\\d+}/questao/{indice}")
    public String carregarQuestao(@PathVariable Long id, @PathVariable Integer indice, Model model) {
        List<Resolucao> res = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);
        prepararModelQuestao(model, res, indice);

        return "simulado/ambiente-prova :: conteudoQuestao";
    }

    @PostMapping("/responder/{resolucaoId}")
    public String responderQuestao(@PathVariable Long resolucaoId,
            @RequestParam(required = false) String resposta,
            @RequestParam(required = false) Long tempo,
            @RequestParam(required = false) Resolucao.NivelDificuldade feedback,
            Model model) {
        Resolucao r = resolucaoRepo.findById(resolucaoId).orElseThrow();

        if (r.getSimulado().getDataFim() == null) {
            if (resposta != null) {
                r.setRespostaAluno(resposta);
                r.setDataResposta(LocalDateTime.now());
                if (tempo != null && tempo > 0)
                    r.setTempoSegundos(tempo);

                String gabaritoOficial = r.getQuestao().getGabarito();
                if (r.getGabaritoDinamico() != null)
                    gabaritoOficial = r.getGabaritoDinamico();

                if (gabaritoOficial != null) {
                    r.setCorreta(gabaritoOficial.equalsIgnoreCase(resposta));
                }
            }
            if (feedback != null)
                r.setFeedbackUsuario(feedback);
            resolucaoRepo.save(r);
        }

        List<Resolucao> todas = resolucaoRepo.findBySimuladoIdOrderByIdAsc(r.getSimulado().getId());
        prepararModelQuestao(model, todas, todas.indexOf(r));
        return "simulado/ambiente-prova :: conteudoQuestao";
    }

    @PostMapping("/{id:\\d+}/finalizar")
    public String finalizarSimulado(@PathVariable Long id) {
        Simulado s = simuladoRepo.findById(id).orElseThrow();
        List<Resolucao> res = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        double nota = 0.0;
        for (Resolucao r : res) {
            boolean acertou = Boolean.TRUE.equals(r.getCorreta());
            if (r.getRespostaAluno() != null && acertou)
                nota += 1.0;
            else if (r.getRespostaAluno() != null)
                nota -= 1.0;

            if (s.getTipo() != null && (s.getTipo().contains("PROTOCOLO") || s.getTipo().contains("CENTRAL"))) {
                Optional<RegistroErro> erroOpt = erroRepo.findByUsuarioAndQuestaoOriginal(s.getUsuario(),
                        r.getQuestao());
                erroOpt.ifPresent(re -> algoritmoService.processarResultadoExpurgo(re, acertou));
            } else {
                algoritmoService.processarErro(s.getUsuario(), r.getQuestao(), r.getCorreta(), r.getTempoSegundos(),
                        r.getFeedbackUsuario());
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
        long erros = res.stream().filter(r -> Boolean.FALSE.equals(r.getCorreta()) && r.getRespostaAluno() != null)
                .count();
        long branco = res.stream().filter(r -> r.getRespostaAluno() == null).count();

        // 1. Possíveis Pontos
        double possiveisPontos = res.size() * 1.0;

        // 2. Estatísticas de Tempo
        long totalSegundos = res.stream().mapToLong(r -> r.getTempoSegundos() != null ? r.getTempoSegundos() : 0).sum();
        String tempoTotal = String.format("%02d:%02d:%02d", totalSegundos / 3600, (totalSegundos % 3600) / 60,
                totalSegundos % 60);

        long medio = res.isEmpty() ? 0 : totalSegundos / res.size();
        String tempoMedio = String.format("%dm %02ds", medio / 60, medio % 60);

        // 3. Ranking de Matérias (Simplificado por Disciplina do Bloco ou Tag)
        // Agrupando...
        java.util.Map<String, java.util.Map<String, Object>> statsPorMateria = new java.util.HashMap<>();

        for (Resolucao r : res) {
            String materia = "GERAL";
            if (r.getQuestao().getBloco() != null && r.getQuestao().getBloco().getDisciplina() != null) {
                materia = r.getQuestao().getBloco().getDisciplina();
            } else if (r.getQuestao().getTags() != null && !r.getQuestao().getTags().isEmpty()) {
                // Tenta pegar a primeira tag como matéria se não tiver disciplina
                materia = r.getQuestao().getTags().split(",")[0].trim().replace("[", "").replace("]", "").replace("\"",
                        "");
            }

            statsPorMateria.putIfAbsent(materia, new java.util.HashMap<>());
            java.util.Map<String, Object> stat = statsPorMateria.get(materia);

            stat.put("nome", materia);
            stat.putIfAbsent("pontos", 0.0);
            stat.putIfAbsent("acertos", 0);
            stat.putIfAbsent("erros", 0);
            stat.putIfAbsent("branco", 0);

            if (r.getRespostaAluno() == null) {
                stat.put("branco", (Integer) stat.get("branco") + 1);
            } else if (Boolean.TRUE.equals(r.getCorreta())) {
                stat.put("acertos", (Integer) stat.get("acertos") + 1);
                stat.put("pontos", (Double) stat.get("pontos") + 1.0);
            } else {
                stat.put("erros", (Integer) stat.get("erros") + 1);
                stat.put("pontos", (Double) stat.get("pontos") - 1.0);
            }
        }

        model.addAttribute("simulado", s);
        model.addAttribute("resolucoes", res);
        model.addAttribute("statsAcertos", acertos);
        model.addAttribute("statsErros", erros);
        model.addAttribute("statsBranco", branco);

        // Atributos adicionados para corrigir o erro do Thymeleaf
        model.addAttribute("possiveisPontos", possiveisPontos);
        model.addAttribute("tempoTotal", tempoTotal);
        model.addAttribute("tempoMedio", tempoMedio);
        model.addAttribute("rankingMaterias", new java.util.ArrayList<>(statsPorMateria.values()));

        return "simulado/resultado";
    }

    private void prepararModelQuestao(Model model, List<Resolucao> resolucoes, int indice) {
        if (indice < 0)
            indice = 0;
        if (indice >= resolucoes.size())
            indice = resolucoes.size() - 1;

        model.addAttribute("r", resolucoes.get(indice));
        model.addAttribute("resolucoes", resolucoes);
        model.addAttribute("indiceAtual", indice);
        model.addAttribute("total", resolucoes.size());
        model.addAttribute("simulado", resolucoes.get(indice).getSimulado());
    }
}