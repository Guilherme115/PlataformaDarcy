package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import com.example.PlataformaDarcy.service.SimuladoService;
import com.example.PlataformaDarcy.service.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/simulado")
public class SimuladoController {

    @Autowired private SimuladoService simuladoService;
    @Autowired private TaxonomyService taxonomyService;
    @Autowired private SimuladoRepository simuladoRepo;
    @Autowired private ResolucaoRepository resolucaoRepo;
    @Autowired private ProvaRepository provaRepo;

    // --- . MENU PRINCIPAL ---
    @GetMapping
    public String menuPrincipal(@AuthenticationPrincipal Usuario usuario, Model model) {
        List<Simulado> emAberto = simuladoRepo.findByUsuarioOrderByDataInicioDesc(usuario).stream()
                .filter(s -> s.getDataFim() == null)
                .limit(3)
                .toList();
        model.addAttribute("simuladosEmAberto", emAberto);
        return "simulado-menu";
    }

    @GetMapping("/personalizado")
    public String configurarLista(Model model) {
        try {
            model.addAttribute("taxonomyJson", new ObjectMapper().writeValueAsString(taxonomyService.getTaxonomy()));
        } catch (Exception e) {
            model.addAttribute("taxonomyJson", "{}");
        }
        return "simulado-config";
    }

    @PostMapping("/gerar")
    public String gerarBateria(@AuthenticationPrincipal Usuario usuario,
                               @RequestParam(required = false) Integer etapa,
                               @RequestParam(required = false) String materia,
                               @RequestParam(defaultValue = "TODOS") String tipo,
                               @RequestParam(defaultValue = "10") Integer quantidade) {
        Simulado simulado = simuladoService.iniciarSimulado(usuario, etapa, materia, null, tipo, quantidade);
        return "redirect:/simulado/" + simulado.getId();
    }

    @GetMapping("/arquivo")
    public String arquivoOficial(@RequestParam(defaultValue = "1") Integer etapa, Model model) {
        List<Prova> provas = provaRepo.findAllByEtapaOrderByAnoDesc(etapa);
        model.addAttribute("etapaAtiva", etapa);
        model.addAttribute("provas", provas);
        return "simulado-arquivo";
    }

    @PostMapping("/gerar-prova-completa/{provaId}")
    public String gerarProvaCompleta(@PathVariable Long provaId, @AuthenticationPrincipal Usuario usuario) {
        Simulado simulado = simuladoService.iniciarProvaCompleta(usuario, provaId);
        return "redirect:/simulado/" + simulado.getId();
    }

    @GetMapping("/{id}")
    public String ambienteProva(@PathVariable Long id, Model model) {
        Simulado simulado = simuladoRepo.findById(id).orElseThrow();
        List<Resolucao> resolucoes = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        if (simulado.getDataFim() != null) {
            return "redirect:/simulado";
        }

        model.addAttribute("simulado", simulado);
        model.addAttribute("resolucoes", resolucoes);
        model.addAttribute("totalQuestões", resolucoes.size());

        // CHAMA O MÉTODO AUXILIAR AQUI
        prepararModelQuestao(model, resolucoes, 0);

        return "prova-real";
    }

    @GetMapping("/{id}/questao/{indice}")
    public String carregarQuestao(@PathVariable Long id, @PathVariable Integer indice, Model model) {
        List<Resolucao> resolucoes = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        prepararModelQuestao(model, resolucoes, indice);

        return "prova-real :: conteudoQuestao";
    }

    @PostMapping("/responder/{resolucaoId}")
    @ResponseBody
    public ResponseEntity<Void> responderQuestao(@PathVariable Long resolucaoId, @RequestParam String resposta) {
        Resolucao resolucao = resolucaoRepo.findById(resolucaoId).orElseThrow();

        resolucao.setRespostaAluno(resposta);
        resolucao.setDataResposta(LocalDateTime.now());

        String gabarito = resolucao.getQuestao().getGabarito();
        if (gabarito != null && !gabarito.isEmpty()) {
            resolucao.setCorreta(gabarito.equalsIgnoreCase(resposta));
        }

        resolucaoRepo.save(resolucao);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/finalizar")
    public String finalizarSimulado(@PathVariable Long id) {
        Simulado simulado = simuladoRepo.findById(id).orElseThrow();
        List<Resolucao> resolucoes = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        double nota = 0.0;
        int acertos = 0;
        int erros = 0;

        for (Resolucao res : resolucoes) {
            if (res.getRespostaAluno() != null) {
                if (Boolean.TRUE.equals(res.getCorreta())) {
                    nota += 1.0;
                    acertos++;
                } else {
                    nota -= 1.0;
                    erros++;
                }
            }
        }

        simulado.setNotaFinal(nota);
        simulado.setDataFim(LocalDateTime.now());
        simuladoRepo.save(simulado);


        return "redirect:/simulado/" + id + "/resultado";
    }

    private void prepararModelQuestao(Model model, List<Resolucao> resolucoes, int indice) {
        if (indice < 0) indice = 0;
        if (indice >= resolucoes.size()) indice = resolucoes.size() - 1;

        Resolucao atual = resolucoes.get(indice);
        model.addAttribute("r", atual);
        model.addAttribute("indiceAtual", indice);
        model.addAttribute("total", resolucoes.size());
    }
    @GetMapping("/{id}/resultado")
    public String resultadoProva(@PathVariable Long id, Model model) {
        Simulado simulado = simuladoRepo.findById(id).orElseThrow();

        if (simulado.getDataFim() == null) {
            return "redirect:/simulado/" + id; // Volta pra prova se não acabou
        }

        List<Resolucao> resolucoes = resolucaoRepo.findBySimuladoIdOrderByIdAsc(id);

        long acertos = resolucoes.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        long erros = resolucoes.stream().filter(r -> Boolean.FALSE.equals(r.getCorreta()) && r.getRespostaAluno() != null).count();
        long emBranco = resolucoes.stream().filter(r -> r.getRespostaAluno() == null).count();

        model.addAttribute("simulado", simulado);
        model.addAttribute("resolucoes", resolucoes);
        model.addAttribute("statsAcertos", acertos);
        model.addAttribute("statsErros", erros);
        model.addAttribute("statsBranco", emBranco);

        return "simulado-resultado";
    }
}