package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.model.RegistroErro.StatusCiclo;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço que agrega todos os dados do estudante para prover contexto à IA.
 * Conecta o Tutor IA com simulados, erros, desempenho e histórico.
 */
@Service
public class StudentContextService {

    @Autowired
    private RegistroErroRepository erroRepo;
    @Autowired
    private SimuladoRepository simuladoRepo;
    @Autowired
    private ResolucaoRepository resolucaoRepo;

    // ==================== ANÁLISE DE ERROS ====================

    /**
     * Retorna erros com filtros opcionais.
     */
    public List<RegistroErro> getErrosComFiltro(Usuario u, String materia, Integer tempMin, Integer etapa) {
        List<RegistroErro> todos = erroRepo.findByUsuarioAndStatusNotOrderByDataUltimoErroDesc(u,
                StatusCiclo.EXPURGADO);

        return todos.stream()
                .filter(e -> materia == null || contemTag(e, materia))
                .filter(e -> tempMin == null || (e.getTemperatura() != null && e.getTemperatura() >= tempMin))
                .filter(e -> etapa == null || getEtapaQuestao(e) != null && getEtapaQuestao(e).equals(etapa))
                .collect(Collectors.toList());
    }

    /**
     * Retorna erros críticos (temperatura >= 70).
     */
    public List<RegistroErro> getErrosCriticos(Usuario u) {
        return getErrosComFiltro(u, null, 70, null);
    }

    /**
     * Retorna erros por matéria específica.
     */
    public List<RegistroErro> getErrosPorMateria(Usuario u, String materia) {
        return getErrosComFiltro(u, materia, null, null);
    }

    // ==================== DESEMPENHO POR MATÉRIA ====================

    /**
     * Calcula desempenho por matéria (taxa de acerto).
     */
    public Map<String, DesempenhoMateria> getDesempenhoPorMateria(Usuario u) {
        List<Simulado> simulados = simuladoRepo.findByUsuarioOrderByDataInicioDesc(u);
        Map<String, DesempenhoMateria> mapa = new HashMap<>();

        for (Simulado s : simulados) {
            List<Resolucao> resolucoes = resolucaoRepo.findBySimuladoIdOrderByIdAsc(s.getId());
            for (Resolucao r : resolucoes) {
                if (r.getCorreta() == null || r.getQuestao() == null)
                    continue;

                String materia = extrairMateria(r.getQuestao());
                mapa.computeIfAbsent(materia, k -> new DesempenhoMateria(k));

                DesempenhoMateria dm = mapa.get(materia);
                dm.totalQuestoes++;
                if (r.getCorreta())
                    dm.acertos++;
            }
        }

        // Calcula taxas
        mapa.values().forEach(dm -> {
            dm.taxaAcerto = dm.totalQuestoes > 0 ? (dm.acertos * 100.0 / dm.totalQuestoes) : 0;
        });

        return mapa;
    }

    /**
     * Retorna matérias ordenadas da mais fraca para mais forte.
     */
    public List<DesempenhoMateria> getMateriasMaisFracas(Usuario u) {
        return getDesempenhoPorMateria(u).values().stream()
                .filter(dm -> dm.totalQuestoes >= 3) // Mínimo 3 questões
                .sorted(Comparator.comparingDouble(dm -> dm.taxaAcerto))
                .collect(Collectors.toList());
    }

    /**
     * Retorna matérias ordenadas da mais forte para mais fraca.
     */
    public List<DesempenhoMateria> getMateriasMaisFortes(Usuario u) {
        return getDesempenhoPorMateria(u).values().stream()
                .filter(dm -> dm.totalQuestoes >= 3)
                .sorted(Comparator.comparingDouble((DesempenhoMateria dm) -> dm.taxaAcerto).reversed())
                .collect(Collectors.toList());
    }

    // ==================== ANÁLISE DE SIMULADOS ====================

    /**
     * Retorna estatísticas do último simulado.
     */
    public SimuladoStats getAnaliseUltimoSimulado(Usuario u) {
        List<Simulado> simulados = simuladoRepo.findByUsuarioOrderByDataInicioDesc(u);
        if (simulados.isEmpty())
            return null;

        Simulado ultimo = simulados.get(0);
        return analisarSimulado(ultimo);
    }

    /**
     * Analisa um simulado específico.
     */
    public SimuladoStats analisarSimulado(Simulado s) {
        List<Resolucao> resolucoes = resolucaoRepo.findBySimuladoIdOrderByIdAsc(s.getId());

        SimuladoStats stats = new SimuladoStats();
        stats.simuladoId = s.getId();
        stats.titulo = s.getTitulo();
        stats.data = s.getDataInicio();
        stats.totalQuestoes = resolucoes.size();

        Map<String, int[]> porMateria = new HashMap<>(); // [acertos, total]

        for (Resolucao r : resolucoes) {
            if (r.getCorreta() == null)
                continue;

            if (r.getCorreta())
                stats.acertos++;
            else
                stats.erros++;

            String materia = r.getQuestao() != null ? extrairMateria(r.getQuestao()) : "OUTROS";
            porMateria.computeIfAbsent(materia, k -> new int[] { 0, 0 });
            porMateria.get(materia)[1]++;
            if (r.getCorreta())
                porMateria.get(materia)[0]++;
        }

        stats.taxaAcerto = stats.totalQuestoes > 0 ? (stats.acertos * 100.0 / stats.totalQuestoes) : 0;
        stats.desempenhoPorMateria = porMateria;

        return stats;
    }

    /**
     * Retorna histórico resumido dos últimos N simulados.
     */
    public List<SimuladoStats> getHistoricoSimulados(Usuario u, int limite) {
        return simuladoRepo.findByUsuarioOrderByDataInicioDesc(u).stream()
                .limit(limite)
                .map(this::analisarSimulado)
                .collect(Collectors.toList());
    }

    // ==================== RESUMO COMPLETO PARA IA ====================

    /**
     * Gera texto formatado com contexto completo do aluno para a IA.
     */
    public String getResumoCompletoParaIA(Usuario u) {
        StringBuilder sb = new StringBuilder();

        // Dados básicos
        sb.append("=== PERFIL DO ESTUDANTE ===\n");
        sb.append("Nome: ").append(u.getNome()).append("\n");
        sb.append("Etapa Alvo: PAS ").append(u.getEtapaAlvo() != null ? u.getEtapaAlvo() : 1).append("\n\n");

        // Desempenho geral
        Map<String, DesempenhoMateria> desempenho = getDesempenhoPorMateria(u);
        if (!desempenho.isEmpty()) {
            int totalAcertos = desempenho.values().stream().mapToInt(d -> d.acertos).sum();
            int totalQuestoes = desempenho.values().stream().mapToInt(d -> d.totalQuestoes).sum();
            double taxaGeral = totalQuestoes > 0 ? (totalAcertos * 100.0 / totalQuestoes) : 0;

            sb.append("=== DESEMPENHO GERAL ===\n");
            sb.append(String.format("Taxa de Acerto Global: %.1f%% (%d/%d questões)\n\n", taxaGeral, totalAcertos,
                    totalQuestoes));
        }

        // Matérias fracas
        List<DesempenhoMateria> fracas = getMateriasMaisFracas(u);
        if (!fracas.isEmpty()) {
            sb.append("=== MATÉRIAS COM DIFICULDADE ===\n");
            fracas.stream().limit(5).forEach(dm -> sb.append(
                    String.format("- %s: %.1f%% (%d questões)\n", dm.materia, dm.taxaAcerto, dm.totalQuestoes)));
            sb.append("\n");
        }

        // Erros críticos
        List<RegistroErro> criticos = getErrosCriticos(u);
        if (!criticos.isEmpty()) {
            sb.append("=== ERROS CRÍTICOS (").append(criticos.size()).append(" total) ===\n");
            criticos.stream().limit(5).forEach(e -> {
                String enunciado = e.getQuestaoOriginal() != null && e.getQuestaoOriginal().getEnunciado() != null
                        ? e.getQuestaoOriginal().getEnunciado().substring(0,
                                Math.min(100, e.getQuestaoOriginal().getEnunciado().length())) + "..."
                        : "Sem enunciado";
                sb.append(String.format("- [%d°] %s (errou %dx)\n",
                        e.getTemperatura(), enunciado, e.getTotalErros()));
            });
            sb.append("\n");
        }

        // Último simulado
        SimuladoStats ultimo = getAnaliseUltimoSimulado(u);
        if (ultimo != null) {
            sb.append("=== ÚLTIMO SIMULADO ===\n");
            sb.append(String.format("- %s (%s)\n", ultimo.titulo,
                    ultimo.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            sb.append(String.format("- Resultado: %.1f%% (%d acertos, %d erros)\n",
                    ultimo.taxaAcerto, ultimo.acertos, ultimo.erros));
        }

        return sb.toString();
    }

    /**
     * Gera análise focada em erros para a IA.
     */
    public String getAnaliseErrosParaIA(Usuario u, String materia, Integer tempMin) {
        List<RegistroErro> erros = getErrosComFiltro(u, materia, tempMin, null);

        if (erros.isEmpty()) {
            return "O aluno não possui erros registrados" +
                    (materia != null ? " em " + materia : "") +
                    (tempMin != null ? " com temperatura >= " + tempMin + "°" : "") + ".";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ANÁLISE DE ERROS ===\n");
        sb.append("Total: ").append(erros.size()).append(" erros\n");
        if (materia != null)
            sb.append("Filtro: ").append(materia).append("\n");
        if (tempMin != null)
            sb.append("Temperatura mínima: ").append(tempMin).append("°\n");
        sb.append("\n");

        // Agrupa por matéria
        Map<String, Long> porMateria = erros.stream()
                .collect(Collectors.groupingBy(this::extrairMateriaErro, Collectors.counting()));

        sb.append("Distribuição:\n");
        porMateria.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> sb.append(String.format("- %s: %d erros\n", e.getKey(), e.getValue())));

        sb.append("\nTop 5 erros mais críticos:\n");
        erros.stream()
                .sorted(Comparator.comparingInt((RegistroErro e) -> e.getTemperatura() != null ? e.getTemperatura() : 0)
                        .reversed())
                .limit(5)
                .forEach(e -> {
                    String enunciado = e.getQuestaoOriginal() != null && e.getQuestaoOriginal().getEnunciado() != null
                            ? e.getQuestaoOriginal().getEnunciado().substring(0,
                                    Math.min(80, e.getQuestaoOriginal().getEnunciado().length())) + "..."
                            : "N/A";
                    sb.append(String.format("- [%d°] %s\n", e.getTemperatura(), enunciado));
                });

        return sb.toString();
    }

    // ==================== DTOs INTERNOS ====================

    public static class DesempenhoMateria {
        public String materia;
        public int totalQuestoes;
        public int acertos;
        public double taxaAcerto;

        public DesempenhoMateria(String materia) {
            this.materia = materia;
        }
    }

    public static class SimuladoStats {
        public Long simuladoId;
        public String titulo;
        public LocalDateTime data;
        public int totalQuestoes;
        public int acertos;
        public int erros;
        public double taxaAcerto;
        public Map<String, int[]> desempenhoPorMateria;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private boolean contemTag(RegistroErro e, String termo) {
        if (e.getQuestaoOriginal() == null)
            return false;
        String tags = e.getQuestaoOriginal().getTags();
        return tags != null && tags.toUpperCase().contains(termo.toUpperCase());
    }

    private Integer getEtapaQuestao(RegistroErro e) {
        if (e.getQuestaoOriginal() == null || e.getQuestaoOriginal().getProva() == null)
            return null;
        return e.getQuestaoOriginal().getProva().getEtapa();
    }

    private String extrairMateria(Questao q) {
        if (q == null)
            return "OUTROS";
        String tags = q.getTags();
        if (tags == null || tags.isBlank()) {
            if (q.getBloco() != null && q.getBloco().getDisciplina() != null) {
                return q.getBloco().getDisciplina().toUpperCase();
            }
            return "OUTROS";
        }
        return tags.split(",")[0].trim().toUpperCase();
    }

    private String extrairMateriaErro(RegistroErro e) {
        return e.getQuestaoOriginal() != null ? extrairMateria(e.getQuestaoOriginal()) : "OUTROS";
    }
}
