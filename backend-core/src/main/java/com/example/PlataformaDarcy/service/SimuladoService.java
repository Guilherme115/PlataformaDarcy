package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.Prova;
import com.example.PlataformaDarcy.model.Questao;
import com.example.PlataformaDarcy.model.Resolucao;
import com.example.PlataformaDarcy.model.Simulado;
import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.ProvaRepository;
import com.example.PlataformaDarcy.repository.QuestaoRepository;
import com.example.PlataformaDarcy.repository.ResolucaoRepository;
import com.example.PlataformaDarcy.repository.SimuladoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimuladoService {

    @Autowired private QuestaoRepository questaoRepo;
    @Autowired private SimuladoRepository simuladoRepo;
    @Autowired private ResolucaoRepository resolucaoRepo;
    @Autowired private ProvaRepository provaRepo;

    /**
     * MODO 1: Lista Personalizada (Sorteio com Filtros)
     * Cria um simulado baseado nos critérios do aluno (Matéria, Tópico, Etapa).
     */
    @Transactional
    public Simulado iniciarSimulado(Usuario aluno, Integer etapa, String materia, String topico, String tipo, Integer quantidade) {

        int qtdFinal = (quantidade != null && quantidade > 0) ? quantidade : 10;
        if (qtdFinal > 120) qtdFinal = 120; // Limite de segurança

        String tagBusca = (topico != null && !topico.isBlank()) ? topico : materia;
        String tipoFinal = (tipo != null && !tipo.equals("TODOS")) ? tipo : null;

        List<Questao> questoesSelecionadas = questaoRepo.gerarSimuladoAvancado(etapa, tipoFinal, tagBusca, qtdFinal);

        if (questoesSelecionadas.isEmpty()) {
            throw new RuntimeException("Não encontramos questões suficientes com esses filtros. Tente ampliar a busca.");
        }

        Simulado simulado = new Simulado();
        simulado.setUsuario(aluno);
        simulado.setDataInicio(LocalDateTime.now());

        String tituloGerado = (materia != null ? materia : "Conhecimentos Gerais");
        if (etapa != null) tituloGerado += " (PAS " + etapa + ")";
        simulado.setTitulo("Treino: " + tituloGerado);

        simulado = simuladoRepo.save(simulado);

        criarResolucoes(simulado, questoesSelecionadas);

        return simulado;
    }

    /**
     * MODO 2: Prova Oficial Completa (Modo Histórico)
     * Cria um simulado fiel à prova original, com todas as questões na ordem correta.
     */
    @Transactional
    public Simulado iniciarProvaCompleta(Usuario aluno, Long provaId) {

        // 1. Busca a Prova Original
        Prova prova = provaRepo.findById(provaId)
                .orElseThrow(() -> new RuntimeException("Prova não encontrada no banco de dados."));

        // 2. Pega TODAS as questões dessa prova (na ordem 1, 2, 3...)
        List<Questao> questoes = questaoRepo.findByProvaIdOrderByNumeroAsc(provaId);

        if (questoes.isEmpty()) {
            throw new RuntimeException("Esta prova ainda não tem questões cadastradas. Importe o PDF primeiro.");
        }

        Simulado simulado = new Simulado();
        simulado.setUsuario(aluno);
        simulado.setDataInicio(LocalDateTime.now());
        simulado.setTitulo("Prova Oficial: " + prova.getAno() + " - PAS " + prova.getEtapa());

        // Salva a capa
        simulado = simuladoRepo.save(simulado);

        // 4. Gera o cartão de respostas em branco
        criarResolucoes(simulado, questoes);

        return simulado;
    }

    /**
     * Método auxiliar para criar as linhas na tabela 'resolucoes'
     * Isso garante que a prova tenha "memória" do que foi perguntado.
     */
    private void criarResolucoes(Simulado simulado, List<Questao> questoes) {
        List<Resolucao> listaResolucoes = new ArrayList<>();

        for (Questao q : questoes) {
            Resolucao res = new Resolucao();
            res.setSimulado(simulado);
            res.setQuestao(q);

            // Inicializa vazio (o aluno ainda não respondeu)
            res.setCorreta(null);
            res.setRespostaAluno(null);

            listaResolucoes.add(res);
        }

        // Salva tudo de uma vez (Batch Insert)
        resolucaoRepo.saveAll(listaResolucoes);
    }
}