package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import com.example.PlataformaDarcy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
// import java.util.Comparator; // Removido pois não é mais usado
import java.util.ArrayList;
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
    @Autowired
    private com.example.PlataformaDarcy.service.TaxonomiaService taxonomiaService;
    @Autowired
    private QuestaoIAService questaoIAService;
    @Autowired
    private SimuladoGeradorService simuladoGeradorService;

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
    public String paginaConfiguracao(@RequestParam(defaultValue = "1") Integer etapa, Model model) {
        try {
            // Busca taxonomia da etapa selecionada do banco de dados
            java.util.Map<String, java.util.Map<String, java.util.List<String>>> taxonomy = taxonomiaService
                    .getTaxonomyByEtapa(etapa);

            model.addAttribute("taxonomyJson", new ObjectMapper().writeValueAsString(taxonomy));
            model.addAttribute("etapaAtual", etapa);
        } catch (Exception e) {
            model.addAttribute("taxonomyJson", "{}");
            model.addAttribute("etapaAtual", etapa);
        }
        return "simulado/config";
    }

    // API REST para carregar taxonomia dinamicamente por etapa
    @GetMapping("/api/taxonomy")
    @ResponseBody
    public java.util.Map<String, java.util.Map<String, java.util.List<String>>> getTaxonomyAPI(
            @RequestParam Integer etapa) {
        try {
            return taxonomiaService.getTaxonomyByEtapa(etapa);
        } catch (Exception e) {
            return new java.util.LinkedHashMap<>();
        }
    }

    // GERAR PDF PARA IMPRESSÃO
    @Autowired
    private com.example.PlataformaDarcy.service.PdfGeneratorService pdfGeneratorService;

    @Autowired
    private com.example.PlataformaDarcy.repository.ListaImpressaoRepository listaImpressaoRepo;

    @GetMapping("/gerar-pdf")
    public org.springframework.http.ResponseEntity<byte[]> gerarPDF(
            @RequestParam Integer etapa,
            @RequestParam(required = false) String materias,
            @RequestParam(required = false) String topicos,
            @RequestParam(defaultValue = "20") Integer quantidade,
            Authentication authentication) {

        try {
            // Busca usuário logado
            Usuario usuario = authService.getUsuarioLogado();

            // Busca questões do banco
            java.util.List<Questao> questoesBanco = questaoRepo.findByProva_Etapa(etapa);

            // Filtra por matérias se especificado
            if (materias != null && !materias.trim().isEmpty()) {
                String[] materiasList = materias.split(",");
                questoesBanco = questoesBanco.stream()
                        .filter(q -> {
                            if (q.getTags() == null)
                                return false;
                            for (String mat : materiasList) {
                                if (q.getTags().toUpperCase().contains(mat.trim().toUpperCase())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }

            // Filtra por tópicos se especificado
            if (topicos != null && !topicos.trim().isEmpty()) {
                String[] topicosList = topicos.split(",");
                questoesBanco = questoesBanco.stream()
                        .filter(q -> {
                            if (q.getTags() == null)
                                return false;
                            for (String top : topicosList) {
                                if (q.getTags().toUpperCase().contains(top.trim().toUpperCase())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(java.util.stream.Collectors.toList());
            }

            // Randomiza e limita quantidade
            java.util.Collections.shuffle(questoesBanco);
            questoesBanco = questoesBanco.stream()
                    .limit(quantidade)
                    .collect(java.util.stream.Collectors.toList());

            // Salva lista no banco
            com.example.PlataformaDarcy.model.ListaImpressao lista = new com.example.PlataformaDarcy.model.ListaImpressao();
            lista.setUsuario(usuario);
            lista.setEtapa(etapa);
            lista.setTitulo("Lista PAS " + etapa + " - "
                    + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            lista.setQuantidadeQuestoes(questoesBanco.size());
            lista.setMaterias(materias != null ? materias : "Todas");
            lista.setTopicos(topicos != null ? topicos : "Todos");

            // Salva IDs das questões em JSON
            String questoesIds = "[" + questoesBanco.stream()
                    .map(q -> q.getId().toString())
                    .collect(java.util.stream.Collectors.joining(",")) + "]";
            lista.setQuestoesIds(questoesIds);

            // Gera checksum
            String checksum = java.security.MessageDigest.getInstance("MD5")
                    .digest(questoesIds.getBytes())
                    .toString();
            lista.setChecksum(checksum);

            lista = listaImpressaoRepo.save(lista);

            // Prepara metadados (SEM gabarito)
            java.util.Map<String, Object> metadados = new java.util.HashMap<>();
            metadados.put("materias", materias != null ? materias : "Todas");
            metadados.put("topicos", topicos != null ? topicos : "Todos");
            metadados.put("semGabarito", true); // Flag para NÃO incluir gabarito

            // Gera PDF sem gabarito
            String titulo = "Lista PAS " + etapa;
            byte[] pdfBytes = pdfGeneratorService.gerarListaImpressaPDF(questoesBanco, titulo, etapa, metadados);

            // Configura headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "lista_pas_" + etapa + "_" + lista.getId() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new org.springframework.http.ResponseEntity<>(pdfBytes, headers,
                    org.springframework.http.HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // MINHAS LISTAS (Histórico)
    @GetMapping("/minhas-listas")
    public String minhasListas(@RequestParam(required = false, defaultValue = "0") Integer etapa,
            Authentication authentication,
            Model model) {
        Usuario usuario = authService.getUsuarioLogado();

        java.util.List<com.example.PlataformaDarcy.model.ListaImpressao> listas;
        if (etapa > 0) {
            listas = listaImpressaoRepo.findByUsuarioAndEtapaOrderByGeradoEmDesc(usuario, etapa);
        } else {
            listas = listaImpressaoRepo.findByUsuarioOrderByGeradoEmDesc(usuario);
        }

        model.addAttribute("listas", listas);
        model.addAttribute("etapaFiltro", etapa);
        model.addAttribute("totalListas", listaImpressaoRepo.countByUsuario(usuario));

        return "simulado/minhas-listas";
    }

    // BAIXAR GABARITO
    @GetMapping("/gabarito/{listaId}")
    public org.springframework.http.ResponseEntity<byte[]> baixarGabarito(@PathVariable Long listaId,
            Authentication authentication) {
        try {
            Usuario usuario = authService.getUsuarioLogado();
            com.example.PlataformaDarcy.model.ListaImpressao lista = listaImpressaoRepo.findById(listaId)
                    .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

            // Verifica se a lista pertence ao usuário
            if (!lista.getUsuario().getId().equals(usuario.getId())) {
                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body(null);
            }

            // Busca questões pelos IDs salvos
            String questoesIdsStr = lista.getQuestoesIds().replace("[", "").replace("]", "");
            String[] idsArray = questoesIdsStr.split(",");
            java.util.List<Questao> questoes = new java.util.ArrayList<>();
            for (String idStr : idsArray) {
                Long id = Long.parseLong(idStr.trim());
                questaoRepo.findById(id).ifPresent(questoes::add);
            }

            // Gera PDF do gabarito
            byte[] pdfBytes = pdfGeneratorService.gerarGabaritoPDF(questoes, lista.getTitulo(), lista.getEtapa());

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "gabarito_" + listaId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new org.springframework.http.ResponseEntity<>(pdfBytes, headers,
                    org.springframework.http.HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // BAIXAR PROVA NOVAMENTE
    @GetMapping("/baixar-prova/{listaId}")
    public org.springframework.http.ResponseEntity<byte[]> baixarProva(@PathVariable Long listaId,
            Authentication authentication) {
        try {
            Usuario usuario = authService.getUsuarioLogado();
            com.example.PlataformaDarcy.model.ListaImpressao lista = listaImpressaoRepo.findById(listaId)
                    .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

            if (!lista.getUsuario().getId().equals(usuario.getId())) {
                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body(null);
            }

            // Busca questões
            String questoesIdsStr = lista.getQuestoesIds().replace("[", "").replace("]", "");
            String[] idsArray = questoesIdsStr.split(",");
            java.util.List<Questao> questoes = new java.util.ArrayList<>();
            for (String idStr : idsArray) {
                Long id = Long.parseLong(idStr.trim());
                questaoRepo.findById(id).ifPresent(questoes::add);
            }

            // Gera PDF sem gabarito
            java.util.Map<String, Object> metadados = new java.util.HashMap<>();
            metadados.put("semGabarito", true);
            byte[] pdfBytes = pdfGeneratorService.gerarListaImpressaPDF(questoes, lista.getTitulo(), lista.getEtapa(),
                    metadados);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "lista_" + listaId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new org.springframework.http.ResponseEntity<>(pdfBytes, headers,
                    org.springframework.http.HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
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

    @GetMapping("/oficiais")
    public String simuladosOficiaisIneditos(Model model) {
        List<Prova> provasIneditas = provaRepo.findByOrigemAndAtivoTrueOrderByIdDesc("IA_OFICIAL");

        // Incrementa contador de acessos para cada prova exibida
        for (Prova prova : provasIneditas) {
            if (prova.getContadorAcessos() == null) {
                prova.setContadorAcessos(0);
            }
            prova.setContadorAcessos(prova.getContadorAcessos() + 1);
            prova.setDataUltimoAcesso(LocalDateTime.now());
            provaRepo.save(prova);
        }

        model.addAttribute("provas", provasIneditas);
        return "simulado/oficiais";
    }

    // --- AREA RESTRITA (ADMIN) ---
    @PostMapping("/oficiais/gerar-novo")
    public String gerarSimuladoAdmin(@RequestParam String senha, @RequestParam Integer etapa,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (!"2711".equals(senha)) {
            redirectAttributes.addFlashAttribute("erro", "Acesso Negado: Senha incorreta.");
            return "redirect:/admin/dashboard";
        }

        try {
            // Chama o serviço existente que já contém a lógica de blueprint + IA
            // Precisamos injetar o SimuladoGeradorService (assumindo que já existe bean
            // dele, mas não estava autowired aqui, vamos checar)
            // Se não estiver, vou adicionar o Autowired no topo.
            simuladoGeradorService.gerarSimuladoOficial(etapa);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Novo Simulado Oficial (PAS " + etapa + ") gerado e adicionado à lista!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Erro ao gerar simulado: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    // ==================== GERAÇÃO DE LISTA COM IA ====================

    @GetMapping("/gerar-ia")
    public String paginaGerarIA(@AuthenticationPrincipal Usuario usuario, Model model) {
        Integer etapa = usuario != null && usuario.getEtapaAlvo() != null ? usuario.getEtapaAlvo() : 1;
        List<String> tags = questaoIAService.listarTagsDisponiveis(etapa);
        model.addAttribute("tagsDisponiveis", tags);
        model.addAttribute("etapaAtual", etapa);
        return "simulado/gerar-ia";
    }

    // API REST para obter tags IA por etapa (usado pela página unificada)
    @GetMapping("/api/questao-ia/tags")
    @ResponseBody
    public List<String> getTagsIA(@RequestParam Integer etapa) {
        return questaoIAService.listarTagsDisponiveis(etapa);
    }

    // ==================== GERAÇÃO HÍBRIDA (BANCO + IA) ====================
    @PostMapping("/gerar-hibrido")
    public String gerarHibrido(@AuthenticationPrincipal Usuario usuario,
            @RequestParam Integer etapa,
            @RequestParam(required = false) String materias,
            @RequestParam(required = false) List<String> topicos,
            @RequestParam Integer quantidadeTotal,
            @RequestParam Integer percentualIA,
            @RequestParam(defaultValue = "TODOS") String tipo,
            @RequestParam String modo,
            RedirectAttributes redirectAttributes) {

        if (usuario == null)
            usuario = authService.getUsuarioLogado();

        // Calcula quantidade de cada fonte
        int qtdIA = Math.round(quantidadeTotal * percentualIA / 100f);
        int qtdBanco = quantidadeTotal - qtdIA;

        List<Questao> questoesBanco = new ArrayList<>();
        List<Questao> questoesIA = new ArrayList<>();

        // 1. Buscar questões do banco (se qtdBanco > 0)
        if (qtdBanco > 0) {
            String[] materiasList = materias != null && !materias.isEmpty() ? materias.split(",") : null;

            if (topicos != null && !topicos.isEmpty()) {
                // Com filtro de tópicos específicos - usa o campo 'tags'
                for (String topicoCompleto : topicos) {
                    String[] parts = topicoCompleto.split(":");
                    if (parts.length > 1) {
                        String topicoFiltro = parts[1];
                        List<Questao> todasEtapa = questaoRepo.findByProva_Etapa(etapa);
                        for (Questao q : todasEtapa) {
                            if (q.getTags() != null && q.getTags().toLowerCase().contains(topicoFiltro.toLowerCase())) {
                                questoesBanco.add(q);
                            }
                        }
                    }
                }
            } else if (materiasList != null && materiasList.length > 0) {
                // Por matéria - busca todas e filtra por tags
                List<Questao> todasEtapa = questaoRepo.findByProva_Etapa(etapa);
                for (String mat : materiasList) {
                    for (Questao q : todasEtapa) {
                        if (q.getTags() != null && q.getTags().toLowerCase().contains(mat.trim().toLowerCase())) {
                            questoesBanco.add(q);
                        }
                    }
                }
            } else {
                // Geral
                questoesBanco = questaoRepo.findByProva_Etapa(etapa);
            }

            // Filtrar por tipo se necessário
            if (!"TODOS".equals(tipo)) {
                List<Questao> filtradas = new ArrayList<>();
                for (Questao q : questoesBanco) {
                    if (q.getTipo() != null && tipo.equals(q.getTipo().toString())) {
                        filtradas.add(q);
                    }
                }
                questoesBanco = filtradas;
            }

            // Randomizar e limitar
            java.util.Collections.shuffle(questoesBanco);
            if (questoesBanco.size() > qtdBanco) {
                questoesBanco = questoesBanco.subList(0, qtdBanco);
            }
        }

        // 2. Gerar questões com IA (se qtdIA > 0)
        if (qtdIA > 0 && materias != null && !materias.isEmpty()) {
            String[] materiasList = materias.split(",");
            int qtdPorMateria = Math.max(1, qtdIA / materiasList.length);

            for (String mat : materiasList) {
                List<Questao> qs = questaoIAService.gerarQuestoesComIA(mat.trim(), qtdPorMateria, etapa);
                questoesIA.addAll(qs);
            }

            // Ajusta para quantidade exata
            if (questoesIA.size() > qtdIA) {
                java.util.Collections.shuffle(questoesIA);
                questoesIA = questoesIA.subList(0, qtdIA);
            }
        }

        // 3. Combinar e embaralhar
        List<Questao> todasQuestoes = new ArrayList<>();
        todasQuestoes.addAll(questoesBanco);
        todasQuestoes.addAll(questoesIA);
        java.util.Collections.shuffle(todasQuestoes);

        if (todasQuestoes.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Nenhuma questão encontrada com os filtros selecionados.");
            return "redirect:/simulado/personalizado";
        }

        // 4. Criar simulado
        Simulado s = new Simulado();
        s.setUsuario(usuario);
        s.setTitulo("LISTA HÍBRIDA (" + questoesBanco.size() + " banco + " + questoesIA.size() + " IA)");
        s.setTipo("HIBRIDO");
        s.setModo("LIVRE".equals(modo) ? Simulado.ModoExecucao.LIVRE : Simulado.ModoExecucao.APRENDIZADO);
        s.setDataInicio(LocalDateTime.now());
        s = simuladoRepo.save(s);

        // 5. Criar resoluções
        int num = 1;
        for (Questao q : todasQuestoes) {
            // Salva questão IA se necessário
            if (q.getId() == null) {
                q.setNumero(num);
                q = questaoRepo.save(q);
            }

            Resolucao r = new Resolucao();
            r.setSimulado(s);
            r.setQuestao(q);
            resolucaoRepo.save(r);
            num++;
        }

        return "redirect:/simulado/" + s.getId();
    }

    @PostMapping("/gerar-ia")
    public String processarGeracaoIA(@AuthenticationPrincipal Usuario usuario,
            @RequestParam String tag,
            @RequestParam(defaultValue = "5") Integer quantidade) {

        if (usuario == null)
            usuario = authService.getUsuarioLogado();
        Integer etapa = usuario.getEtapaAlvo() != null ? usuario.getEtapaAlvo() : 1;

        // Gera questões via IA
        List<Questao> questoesGeradas = questaoIAService.gerarQuestoesComIA(tag, quantidade, etapa);

        if (questoesGeradas.isEmpty()) {
            return "redirect:/simulado/gerar-ia?erro=vazio";
        }

        // Cria simulado com as questões geradas
        Simulado s = new Simulado();
        s.setUsuario(usuario);
        s.setTitulo("TREINO IA: " + tag);
        s.setTipo("IA_GERADO");
        s.setModo(Simulado.ModoExecucao.APRENDIZADO);
        s.setDataInicio(LocalDateTime.now());
        s = simuladoRepo.save(s);

        // Cria resoluções para cada questão (questões não são persistidas, apenas em
        // memória)
        int num = 1;
        for (Questao q : questoesGeradas) {
            q.setNumero(num++);
            // Salva a questão temporariamente para ter ID
            q = questaoRepo.save(q);

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

    @GetMapping("/{id}/mapa")
    public String carregarMapa(@PathVariable Long id, Model model) {
        Simulado simulado = simuladoRepo.findById(id).orElseThrow();
        // Ordena por ID ou Ordem se houver. Assumindo ID por enquanto que segue a ordem
        // de inserção.
        List<Resolucao> resolucoes = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        model.addAttribute("simulado", simulado);
        model.addAttribute("resolucoes", resolucoes);

        return "simulado/ambiente-prova :: mapaGrid";
    }

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