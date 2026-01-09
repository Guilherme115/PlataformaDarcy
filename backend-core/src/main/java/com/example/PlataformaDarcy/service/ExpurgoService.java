package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.model.RegistroErro.StatusCiclo;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpurgoService {

    @Autowired
    private RegistroErroRepository erroRepo;
    @Autowired
    private SimuladoRepository simuladoRepo;
    @Autowired
    private ResolucaoRepository resolucaoRepo;
    @Autowired
    private GeminiService geminiService;

    /**
     * Busca estatísticas e inbox para o Dashboard.
     */
    public List<RegistroErro> getInbox(Usuario usuario) {
        return erroRepo.findByUsuarioAndStatusNotOrderByDataUltimoErroDesc(usuario, StatusCiclo.EXPURGADO);
    }

    /**
     * Realiza o "Expurgo Manual" de um erro (marca como dominado/arquivado).
     */
    public void expurgarErro(Long erroId) {
        RegistroErro erro = erroRepo.findById(erroId).orElseThrow();
        erro.setStatus(StatusCiclo.EXPURGADO);
        erro.setTemperatura(0);
        erro.setAcertosConsecutivos(erro.getAcertosConsecutivos() == null ? 1 : erro.getAcertosConsecutivos() + 1);
        erroRepo.save(erro);
    }

    /**
     * Gera o Protocolo de Emergência (Botão de Pânico).
     */
    @Transactional
    public Simulado gerarProtocoloEmergencia(Usuario usuario) {
        // Lógica de Filtro Crítico
        List<RegistroErro> criticos = erroRepo
                .findByUsuarioAndTemperaturaGreaterThanEqualOrderByTemperaturaDesc(usuario, 50)
                .stream()
                .filter(e -> e.getQuestaoOriginal().getTipo() != TipoQuestao.D) // Ignora Discursivas
                .limit(5)
                .collect(Collectors.toList());

        if (criticos.isEmpty())
            return null;

        return criarSimuladoInterno(usuario, criticos, "PROTOCOLO DE EMERGÊNCIA (IA)", true);
    }

    /**
     * Gera a Bateria Personalizada com Filtros Avançados.
     */
    @Transactional
    public Simulado gerarBateriaPersonalizada(Usuario usuario, String materia, String topico,
            Integer etapa, Integer quantidade, boolean usarIA) {

        List<RegistroErro> erros = getInbox(usuario);

        // Aplicação dos Filtros (Lógica de Negócio)
        if (materia != null && !materia.isEmpty()) {
            erros = erros.stream().filter(e -> contemTag(e, materia)).collect(Collectors.toList());
        }
        if (topico != null && !topico.isEmpty()) {
            erros = erros.stream().filter(e -> contemTag(e, topico)).collect(Collectors.toList());
        }
        if (etapa != null) {
            erros = erros.stream()
                    .filter(e -> e.getQuestaoOriginal().getProva() != null &&
                            e.getQuestaoOriginal().getProva().getEtapa().equals(etapa))
                    .collect(Collectors.toList());
        }
        if (usarIA) {
            erros = erros.stream()
                    .filter(e -> e.getQuestaoOriginal().getTipo() != TipoQuestao.D)
                    .collect(Collectors.toList());
        }

        if (erros.isEmpty())
            return null;

        // Limita a quantidade
        int qtdReal = (quantidade != null) ? quantidade : 10;
        erros = erros.stream().limit(qtdReal).collect(Collectors.toList());

        String titulo = "BATERIA: " + (materia != null && !materia.isEmpty() ? materia : "GERAL");
        return criarSimuladoInterno(usuario, erros, titulo, usarIA);
    }

    // --- Métodos Auxiliares Privados ---

    private Simulado criarSimuladoInterno(Usuario usuario, List<RegistroErro> erros, String titulo, boolean usarIA) {
        Simulado s = new Simulado();
        s.setUsuario(usuario);
        s.setTitulo(titulo);
        s.setModo(Simulado.ModoExecucao.APRENDIZADO);
        s.setTipo(usarIA ? "REVISAO_IA" : "REVISAO_MANUAL");
        s.setDataInicio(LocalDateTime.now());
        s = simuladoRepo.save(s);

        for (RegistroErro erro : erros) {
            Questao original = erro.getQuestaoOriginal();
            Resolucao r = new Resolucao();
            r.setSimulado(s);
            r.setQuestao(original);

            if (usarIA) {
                try {
                    Questao mutante = geminiService.refatorarQuestao(original);
                    r.setEnunciadoDinamico(mutante.getEnunciado());
                    r.setGabaritoDinamico(mutante.getGabarito());
                } catch (Exception e) {
                    r.setEnunciadoDinamico(null); // Fallback
                }
            }
            resolucaoRepo.save(r);
        }
        return s;
    }

    private boolean contemTag(RegistroErro e, String termo) {
        return e.getQuestaoOriginal().getTags() != null &&
                e.getQuestaoOriginal().getTags().toUpperCase().contains(termo.toUpperCase());
    }

    // ==================== ANALYTICS PARA DASHBOARD ====================

    /**
     * Agrupa erros por matéria/disciplina para visualização no dashboard.
     */
    public Map<String, List<RegistroErro>> getErrosPorMateria(Usuario usuario) {
        List<RegistroErro> todos = getInbox(usuario);
        Map<String, List<RegistroErro>> agrupados = new LinkedHashMap<>();

        for (RegistroErro erro : todos) {
            String materia = extrairMateria(erro);
            agrupados.computeIfAbsent(materia, k -> new ArrayList<>()).add(erro);
        }

        // Ordena por quantidade de erros (mais críticos primeiro)
        return agrupados.entrySet().stream()
                .sorted((a, b) -> {
                    // Primeiro por quantidade, depois por temperatura média
                    int compQtd = Integer.compare(b.getValue().size(), a.getValue().size());
                    if (compQtd != 0)
                        return compQtd;
                    double tempA = a.getValue().stream()
                            .mapToInt(e -> e.getTemperatura() != null ? e.getTemperatura() : 0).average().orElse(0);
                    double tempB = b.getValue().stream()
                            .mapToInt(e -> e.getTemperatura() != null ? e.getTemperatura() : 0).average().orElse(0);
                    return Double.compare(tempB, tempA);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    /**
     * Agrupa erros por causa para análise de padrões.
     */
    public Map<String, Long> getErrosPorCausa(Usuario usuario) {
        List<RegistroErro> todos = getInbox(usuario);
        return todos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCausa() != null ? e.getCausa().name() : "NAO_CLASSIFICADO",
                        Collectors.counting()));
    }

    /**
     * Gera sugestões de revisão priorizadas para o usuário.
     */
    public List<Map<String, Object>> getSugestoesRevisao(Usuario usuario) {
        Map<String, List<RegistroErro>> porMateria = getErrosPorMateria(usuario);
        List<Map<String, Object>> sugestoes = new ArrayList<>();

        for (Map.Entry<String, List<RegistroErro>> entry : porMateria.entrySet()) {
            String materia = entry.getKey();
            List<RegistroErro> erros = entry.getValue();

            if (erros.isEmpty())
                continue;

            // Calcula métricas da matéria
            int totalErros = erros.size();
            int criticos = (int) erros.stream().filter(e -> e.getTemperatura() != null && e.getTemperatura() >= 70)
                    .count();
            double tempMedia = erros.stream()
                    .filter(e -> e.getTemperatura() != null)
                    .mapToInt(RegistroErro::getTemperatura)
                    .average().orElse(0);
            int pendentesHoje = (int) erros.stream()
                    .filter(e -> e.getDataProximaRevisao() != null
                            && !e.getDataProximaRevisao().isAfter(LocalDateTime.now()))
                    .count();

            // Determina prioridade
            String prioridade;
            if (criticos >= 3 || tempMedia >= 70) {
                prioridade = "URGENTE";
            } else if (criticos >= 1 || tempMedia >= 50) {
                prioridade = "ALTA";
            } else if (totalErros >= 5) {
                prioridade = "MEDIA";
            } else {
                prioridade = "BAIXA";
            }

            // Gera recomendação
            String recomendacao;
            if (criticos >= 3) {
                recomendacao = "🚨 Protocolo de Emergência recomendado! " + criticos + " erros críticos.";
            } else if (pendentesHoje > 0) {
                recomendacao = "📅 " + pendentesHoje + " revisões agendadas para hoje.";
            } else if (tempMedia >= 60) {
                recomendacao = "🔥 Temperatura média alta. Revise antes que vire crítico.";
            } else {
                recomendacao = "✅ Situação controlada. Continue praticando.";
            }

            Map<String, Object> sugestao = new LinkedHashMap<>();
            sugestao.put("materia", materia);
            sugestao.put("totalErros", totalErros);
            sugestao.put("criticos", criticos);
            sugestao.put("temperaturaMedia", (int) tempMedia);
            sugestao.put("pendentesHoje", pendentesHoje);
            sugestao.put("prioridade", prioridade);
            sugestao.put("recomendacao", recomendacao);
            sugestao.put("erros", erros.stream().limit(5).collect(Collectors.toList())); // Top 5

            sugestoes.add(sugestao);
        }

        return sugestoes;
    }

    /**
     * Retorna todos os dados necessários para o Dashboard do Protocolo.
     */
    public Map<String, Object> getDadosDashboard(Usuario usuario) {
        List<RegistroErro> todos = getInbox(usuario);
        Map<String, Object> dados = new LinkedHashMap<>();

        // Estatísticas Gerais
        dados.put("totalErros", todos.size());
        dados.put("criticos",
                todos.stream().filter(e -> e.getTemperatura() != null && e.getTemperatura() >= 70).count());
        dados.put("pendentesTriagem",
                todos.stream().filter(e -> e.getStatus() == StatusCiclo.PENDENTE_TRIAGEM).count());
        dados.put("emProtocolo", todos.stream().filter(
                e -> e.getStatus() == StatusCiclo.PROTOCOLO_DIARIO || e.getStatus() == StatusCiclo.CENTRAL_EXPURGO)
                .count());

        // Temperatura média geral
        double tempMedia = todos.stream()
                .filter(e -> e.getTemperatura() != null)
                .mapToInt(RegistroErro::getTemperatura)
                .average().orElse(0);
        dados.put("temperaturaMedia", (int) tempMedia);

        // Revisões pendentes hoje
        long revisoesPendentes = todos.stream()
                .filter(e -> e.getDataProximaRevisao() != null
                        && !e.getDataProximaRevisao().isAfter(LocalDateTime.now()))
                .count();
        dados.put("revisoesPendentesHoje", revisoesPendentes);

        // Sugestões organizadas por matéria
        dados.put("sugestoes", getSugestoesRevisao(usuario));

        // Distribuição por causa
        dados.put("distribuicaoCausa", getErrosPorCausa(usuario));

        // Top 10 erros mais críticos
        dados.put("errosMaisCriticos", todos.stream()
                .filter(e -> e.getTemperatura() != null)
                .sorted((a, b) -> Integer.compare(b.getTemperatura(), a.getTemperatura()))
                .limit(10)
                .collect(Collectors.toList()));

        // Etapa alvo do usuário
        dados.put("etapaAlvo", usuario.getEtapaAlvo() != null ? usuario.getEtapaAlvo() : 1);

        return dados;
    }

    private String extrairMateria(RegistroErro erro) {
        if (erro.getQuestaoOriginal() == null)
            return "OUTROS";

        String tags = erro.getQuestaoOriginal().getTags();
        if (tags == null || tags.isBlank()) {
            // Tenta extrair do bloco se existir
            if (erro.getQuestaoOriginal().getBloco() != null &&
                    erro.getQuestaoOriginal().getBloco().getDisciplina() != null) {
                return erro.getQuestaoOriginal().getBloco().getDisciplina().toUpperCase();
            }
            return "OUTROS";
        }

        // Extrai primeira tag como matéria principal
        String[] partes = tags.split(",");
        return partes[0].trim().toUpperCase();
    }
}