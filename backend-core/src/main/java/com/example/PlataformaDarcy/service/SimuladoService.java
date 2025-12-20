package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SimuladoService {

    @Autowired private QuestaoRepository questaoRepo;
    @Autowired private SimuladoRepository simuladoRepo;
    @Autowired private ResolucaoRepository resolucaoRepo;
    @Autowired private ProvaRepository provaRepo;
    @Autowired private AlgoritmoService algoritmoService;
    @Autowired private RegistroErroRepository erroRepo; // Necessário para buscar o erro no Expurgo

    @Transactional
    public Simulado iniciarSimulado(Usuario aluno, Integer etapa, String materia, String topico, String tipo, Integer quantidade) {
        int qtdFinal = (quantidade != null && quantidade > 0) ? quantidade : 10;
        if (qtdFinal > 120) qtdFinal = 120;

        String tagBusca = (topico != null && !topico.isBlank()) ? topico : materia;
        String tipoFinal = (tipo != null && !tipo.equals("TODOS")) ? tipo : null;

        List<Questao> questoesSelecionadas = questaoRepo.gerarSimuladoAvancado(etapa, tipoFinal, tagBusca, qtdFinal);

        if (questoesSelecionadas.isEmpty()) {
            throw new RuntimeException("Não encontramos questões suficientes.");
        }

        Simulado simulado = new Simulado();
        simulado.setUsuario(aluno);
        simulado.setDataInicio(LocalDateTime.now());
        String titulo = (materia != null ? materia : "Geral") + (etapa != null ? " (PAS " + etapa + ")" : "");
        simulado.setTitulo("Treino: " + titulo);

        simulado = simuladoRepo.save(simulado);
        criarResolucoes(simulado, questoesSelecionadas);
        return simulado;
    }

    @Transactional
    public Simulado iniciarProvaCompleta(Usuario aluno, Long provaId) {
        Prova prova = provaRepo.findById(provaId).orElseThrow();
        List<Questao> questoes = questaoRepo.findByProvaIdOrderByNumeroAsc(provaId);

        Simulado simulado = new Simulado();
        simulado.setUsuario(aluno);
        simulado.setDataInicio(LocalDateTime.now());
        simulado.setTitulo("Prova Oficial: " + prova.getAno() + " - PAS " + prova.getEtapa());

        simulado = simuladoRepo.save(simulado);
        criarResolucoes(simulado, questoes);
        return simulado;
    }

    // --- MÉTODO NOVO: Necessário para o ExpurgoController ---
    @Transactional
    public void adicionarQuestoesAoSimulado(Simulado simulado, List<Questao> questoes) {
        criarResolucoes(simulado, questoes);
    }

    private void criarResolucoes(Simulado simulado, List<Questao> questoes) {
        List<Resolucao> lista = new ArrayList<>();
        for (Questao q : questoes) {
            Resolucao res = new Resolucao();
            res.setSimulado(simulado);
            res.setQuestao(q);
            lista.add(res);
        }
        resolucaoRepo.saveAll(lista);
    }

    // --- CORREÇÃO E ALGORITMO ---
    public void processarErrosDoSimulado(Simulado simulado, List<Resolucao> resolucoes) {

        // CENÁRIO 1: PROTOCOLO DE EXPURGO (IA ou Central)
        // Aqui o objetivo é Atualizar a temperatura (Cura ou Penalidade)
        if (simulado.getTipo() != null && (simulado.getTipo().contains("PROTOCOLO") || simulado.getTipo().contains("CENTRAL"))) {
            for (Resolucao r : resolucoes) {
                // Busca o registro de erro original usando o ID da questão
                Optional<RegistroErro> erroOpt = erroRepo.findByUsuarioAndQuestaoOriginal(simulado.getUsuario(), r.getQuestao());
                boolean acertou = Boolean.TRUE.equals(r.getCorreta());

                // Chama a lógica de Expurgo no Algoritmo
                erroOpt.ifPresent(erro -> algoritmoService.processarResultadoExpurgo(erro, acertou));
            }
        }
        // CENÁRIO 2: SIMULADO NORMAL (Aprendizado)
        // Aqui o objetivo é Criar novos registros de erro
        else if (simulado.getModo() == Simulado.ModoExecucao.APRENDIZADO) {
            for (Resolucao res : resolucoes) {
                // Chama o método correto: processarErro
                algoritmoService.processarErro(
                        simulado.getUsuario(),
                        res.getQuestao(),
                        res.getCorreta(),
                        res.getTempoSegundos(),
                        res.getFeedbackUsuario()
                );
            }
        }
    }
}