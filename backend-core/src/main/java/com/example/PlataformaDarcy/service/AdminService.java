package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
public class AdminService {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ProvaRepository provaRepo;
    @Autowired private BugTrackerRepository bugRepo;
    @Autowired private ReportConteudoRepository reportRepo;
    @Autowired private ComunicadoRepository comunicadoRepo;

    // @Autowired private WikiPostRepository wikiRepo; // Descomente quando tiver o módulo Wiki

    /**
     * Reúne todos os dados necessários para o Bento Grid do Dashboard
     */
    public Map<String, Object> carregarDadosDashboard() {
        Map<String, Object> stats = new HashMap<>();

        // 1. CARD USUÁRIOS
        stats.put("totalUsuarios", usuarioRepo.count());

        // 2. CARD PROVAS (Lista todas para o resumo)
        stats.put("provas", provaRepo.findAll());

        // 3. CARD BUGS (Sistema)
        // Pega os não resolvidos, ordenados por data
        List<BugTracker> bugsPendentes = bugRepo.findByResolvidoFalseOrderByDataReportDesc();
        stats.put("bugs", bugsPendentes);

        // Conta quantos são "CRITICOS" (Se você implementou a Severidade no Model)
        // Se não tiver severidade ainda, conta o total de pendentes
        long criticos = bugsPendentes.stream()
                .filter(b -> "CRITICO".equalsIgnoreCase(b.getCategoria()) || "ALTA".equalsIgnoreCase(b.getCategoria())) // Ajuste conforme seu enum/string
                .count();
        stats.put("bugsCriticos", criticos > 0 ? criticos : bugsPendentes.size());

        // 4. CARD REPORTS (Conteúdo)
        // Assume que existe um status PENDENTE. Se não, busca tudo.
        // Se der erro aqui, certifique-se que o ReportConteudoRepository tem o método findByStatus...
        // Caso contrário, usamos findAll e filtramos no Java para simplificar:
        List<ReportConteudo> todosReports = reportRepo.findAll(Sort.by(Sort.Direction.DESC, "dataReport"));
        List<ReportConteudo> reportsPendentes = todosReports.stream()
                .filter(r -> r.getStatus() == ReportConteudo.StatusReport.PENDENTE)
                .toList();

        stats.put("reports", reportsPendentes);
        stats.put("reportsNovos", reportsPendentes.size());

        // 5. CARD TRANSMISSÃO (Feed)
        stats.put("feedAtivo", comunicadoRepo.findByAtivoTrueOrderByDataEnvioDesc());

        // 6. CARD WIKI (Placeholder)
        // Como ainda não implementamos a Wiki a fundo, mandamos lista vazia para não quebrar o HTML
        stats.put("wikiEdits", Collections.emptyList());
        // stats.put("wikiEdits", wikiRepo.findByStatus("PENDENTE")); // Futuro

        return stats;
    }

    /**
     * Resolve um bug técnico (Marca como resolvido)
     */
    @Transactional
    public void resolverBug(Long id) {
        bugRepo.findById(id).ifPresent(bug -> {
            bug.setResolvido(true);
            bugRepo.save(bug);
        });
    }

    /**
     * Busca rápida de alunos para o card do Dashboard
     */
    public List<Usuario> buscarAlunos(String termo) {
        // Retorna apenas os 5 primeiros para não poluir o card pequeno
        return usuarioRepo.findByNomeContainingIgnoreCaseOrMatriculaContaining(termo, termo)
                .stream()
                .limit(5)
                .toList();
    }
}