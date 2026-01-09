package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private ProvaRepository provaRepo;
    @Autowired
    private BugTrackerRepository bugRepo;
    @Autowired
    private SimuladoRepository simuladoRepo;

    /**
     * GERA O DATASET COMPLETO PARA O PAINEL "MISSION CONTROL"
     */
    public Map<String, Object> carregarDadosDashboard() {
        Map<String, Object> data = new HashMap<>();

        // 1. KPIs DE TOPO (Big Numbers)
        long totalUsers = usuarioRepo.count();
        long totalSimulados = simuladoRepo.count();
        long totalBugs = bugRepo.count();
        long bugsCriticos = bugRepo.findByResolvidoFalseOrderByDataReportDesc().stream()
                .filter(b -> b.getTitulo().toUpperCase().contains("CRITICO")
                        || b.getTitulo().toUpperCase().contains("ERRO"))
                .count();

        data.put("kpi_users", totalUsers);
        data.put("kpi_simulados", totalSimulados);
        data.put("kpi_bugs", totalBugs);
        data.put("kpi_bugs_criticos", bugsCriticos);
        data.put("kpi_revenue", String.format("R$ %.2f", totalUsers * 29.90)); // Mock de Receita (MRR Potencial)
        data.put("kpi_uptime", "99.98%"); // Mock de Infra

        // 2. TENDÊNCIA DE CRESCIMENTO (GRÁFICO PRINCIPAL)
        // Simulando dados dos últimos 7 dias para o gráfico
        List<String> labels = new ArrayList<>();
        List<Integer> seriesUsers = new ArrayList<>();
        List<Integer> seriesSimulados = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            labels.add(java.time.LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("dd/MM")));
            // Mock: Randomizando levemente para parecer real
            seriesUsers.add((int) (Math.random() * 15) + 5);
            seriesSimulados.add((int) (Math.random() * 50) + 20);
        }
        data.put("chart_labels", labels);
        data.put("chart_users", seriesUsers);
        data.put("chart_simulados", seriesSimulados);

        // 3. DESEMPENHO POR MATÉRIA (GRÁFICO DE BARRAS)
        // Mockado por enquanto, idealmente viria de uma query complexa no
        // ResolucaoRepository
        Map<String, Integer> desempenho = new LinkedHashMap<>();
        desempenho.put("PORTUGUÊS", 75);
        desempenho.put("MATEMÁTICA", 45);
        desempenho.put("HISTÓRIA", 82);
        desempenho.put("FÍSICA", 30);
        desempenho.put("QUÍMICA", 55);
        desempenho.put("BIOLOGIA", 68);
        data.put("chart_desempenho_keys", desempenho.keySet());
        data.put("chart_desempenho_values", desempenho.values());

        // 4. FEED DE ATIVIDADE EM TEMPO REAL (AUDIT LOG)
        List<ActivityLog> feed = new ArrayList<>();

        // Adiciona ultimos usuarios
        usuarioRepo.findAll(org.springframework.data.domain.PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id")))
                .forEach(u -> feed
                        .add(new ActivityLog("NOVO USUÁRIO", u.getNome() + " se cadastrou.", "user", u.getId())));

        // Adiciona ultimos simulados
        simuladoRepo
                .findAll(org.springframework.data.domain.PageRequest.of(0, 5,
                        Sort.by(Sort.Direction.DESC, "dataInicio")))
                .forEach(s -> feed.add(new ActivityLog("SIMULADO INICIADO",
                        s.getUsuario().getNome() + " iniciou " + s.getTitulo(), "play", s.getId())));

        // Adiciona ultimos bugs
        bugRepo.findAll(
                org.springframework.data.domain.PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "dataReport")))
                .forEach(b -> feed.add(new ActivityLog("BUG REPORTADO", b.getTitulo(), "alert", b.getId())));

        // Embaralha para parecer um feed temporal misto
        Collections.shuffle(feed);
        data.put("feed", feed);

        // 5. LISTAS AUXILIARES
        data.put("provas", provaRepo.findAll());
        data.put("bugsPendentes", bugRepo.findByResolvidoFalseOrderByDataReportDesc());

        return data;
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
     * Busca rápida de alunos
     */
    public List<Usuario> buscarAlunos(String termo) {
        return usuarioRepo.findByNomeContainingIgnoreCaseOrMatriculaContaining(termo, termo)
                .stream()
                .limit(5)
                .toList();
    }

    /**
     * Lista todos os usuários para a página de administração
     */
    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * Busca usuários com termo (para página de usuários)
     */
    public List<Usuario> buscarUsuariosCompleto(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodosUsuarios();
        }
        return usuarioRepo.findByNomeContainingIgnoreCaseOrMatriculaContaining(termo, termo);
    }

    /**
     * Toggle ban/unban de usuário
     */
    @Transactional
    public Usuario toggleBanUsuario(Long id) {
        Usuario user = usuarioRepo.findById(id).orElseThrow();
        user.setAtivo(!user.isAtivo());
        return usuarioRepo.save(user);
    }

    /**
     * Alterar perfil de usuário
     */
    @Transactional
    public Usuario alterarPerfil(Long id, String novoPerfil) {
        Usuario user = usuarioRepo.findById(id).orElseThrow();
        user.setPerfil(novoPerfil.toUpperCase());
        return usuarioRepo.save(user);
    }

    /**
     * Resetar senha de usuário (gera senha aleatória)
     */
    @Transactional
    public String resetarSenha(Long id) {
        Usuario user = usuarioRepo.findById(id).orElseThrow();
        // Gera senha aleatória de 8 caracteres
        String novaSenha = UUID.randomUUID().toString().substring(0, 8);
        // Em produção, usar BCryptPasswordEncoder
        user.setSenha(novaSenha); // TODO: Deve usar encoder!
        usuarioRepo.save(user);
        return novaSenha;
    }

    // DTO INTERNO PARA O FEED
    public static class ActivityLog {
        public String tipo;
        public String desc;
        public String icon; // user, play, alert
        public Long refId;

        public ActivityLog(String tipo, String desc, String icon, Long refId) {
            this.tipo = tipo;
            this.desc = desc;
            this.icon = icon;
            this.refId = refId;
        }
    }
}