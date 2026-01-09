package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.model.RegistroErro.StatusCiclo;
import com.example.PlataformaDarcy.model.RegistroErro.CausaErro;
import com.example.PlataformaDarcy.repository.RegistroErroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * ALGORITMO DE TEMPERATURA v2.0 - "UTI Cognitiva"
 * 
 * Baseado na Curva de Esquecimento de Ebbinghaus com melhorias:
 * - Temperatura: 0-100° (urgência de revisão)
 * - Reincidência exponencial (não linear)
 * - Bonus por tempo de resposta
 * - Multiplicador por etapa do PAS
 * - Threshold unificado para "dominado" (5 acertos)
 * - Decaimento inteligente (não afeta críticos)
 * 
 * FÓRMULA:
 * T = BaseTemp × ReincidênciaFator × EtapaMultiplier - TempoBonus + CausaAjuste
 */
@Service
public class AlgoritmoService {

    @Autowired
    private RegistroErroRepository erroRepo;

    // ==================== CONSTANTES DO ALGORITMO ====================

    // Intervalos de revisão Ebbinghaus (em dias)
    private static final int[] INTERVALOS_EBBINGHAUS = { 1, 3, 7, 14, 30, 60 };

    // Threshold único para considerar erro dominado
    private static final int THRESHOLD_DOMINADO = 5;

    // Curva exponencial de reincidência (1º=50, 2º=70, 3º=85, 4º=95, 5º+=100)
    private static final int[] CURVA_REINCIDENCIA = { 50, 70, 85, 95, 100 };

    // Multiplicadores por CONTEXTO (compara etapa do aluno vs etapa da questão)
    // Questão da mesma etapa que o aluno está estudando = MÁXIMA PRIORIDADE
    // Questão de etapa anterior = importante (base de conhecimento)
    // Questão de etapa futura = menos urgente (ainda vai estudar)
    private static final double MULT_MESMA_ETAPA = 1.3; // Ex: Aluno PAS2, Questão PAS2
    private static final double MULT_ETAPA_ANTERIOR = 1.0; // Ex: Aluno PAS2, Questão PAS1
    private static final double MULT_ETAPA_FUTURA = 0.7; // Ex: Aluno PAS2, Questão PAS3

    // Bonus/penalidade por tempo de resposta (segundos)
    private static final int TEMPO_RAPIDO = 30; // < 30s = resposta rápida
    private static final int TEMPO_NORMAL = 60; // 30-60s = normal
    private static final int TEMPO_LENTO = 120; // > 120s = muito lento

    // ==================== PROCESSAMENTO DE ERROS EM SIMULADOS ====================

    /**
     * Processa resultado de uma questão respondida em simulado normal.
     */
    @Transactional
    public void processarRespostaSimulado(Usuario u, Questao q, boolean acertou,
            Long tempoSegundos, Resolucao.NivelDificuldade feedback) {
        Optional<RegistroErro> existente = erroRepo.findByUsuarioAndQuestaoOriginal(u, q);

        if (acertou) {
            if (existente.isPresent()) {
                processarAcerto(existente.get(), tempoSegundos);
            }
            return;
        }

        // ERRO
        RegistroErro erro = existente.orElse(criarNovoRegistroErro(u, q));

        if (existente.isPresent()) {
            processarReincidencia(erro, q, tempoSegundos, feedback);
        } else {
            definirTemperaturaInicial(erro, q, tempoSegundos, feedback);
            erroRepo.save(erro);
        }
    }

    /**
     * Processa resultado do Protocolo de Expurgo (revisão crítica).
     */
    @Transactional
    public void processarResultadoExpurgo(RegistroErro erro, boolean acertou, Long tempoSegundos) {
        if (erro == null)
            return;

        if (acertou) {
            processarAcertoExpurgo(erro, tempoSegundos);
        } else {
            processarErroExpurgo(erro);
        }

        erro.setDataUltimoErro(LocalDateTime.now());
        erroRepo.save(erro);
    }

    // Overload para compatibilidade
    public void processarResultadoExpurgo(RegistroErro erro, boolean acertou) {
        processarResultadoExpurgo(erro, acertou, null);
    }

    // ==================== LÓGICA DE ACERTO ====================

    private void processarAcerto(RegistroErro erro, Long tempoSegundos) {
        int acertos = erro.getAcertosConsecutivos() + 1;
        erro.setAcertosConsecutivos(acertos);

        // Redução de temperatura baseada em acertos + bonus tempo
        int reducaoBase = 15 + (acertos * 8);
        int bonusTempo = calcularBonusTempo(tempoSegundos);
        int reducaoTotal = reducaoBase + bonusTempo;

        erro.setTemperatura(Math.max(0, erro.getTemperatura() - reducaoTotal));

        // Agenda próxima revisão (Ebbinghaus)
        agendarProximaRevisao(erro, acertos);

        // Verifica se dominou (threshold unificado)
        if (acertos >= THRESHOLD_DOMINADO) {
            erro.setStatus(StatusCiclo.DOMINADA);
            erro.setTemperatura(0);
        } else if (erro.getTemperatura() < 30) {
            erro.setStatus(StatusCiclo.PROTOCOLO_DIARIO);
        }

        erroRepo.save(erro);
    }

    private void processarAcertoExpurgo(RegistroErro erro, Long tempoSegundos) {
        int acertos = erro.getAcertosConsecutivos() + 1;
        erro.setAcertosConsecutivos(acertos);

        // No expurgo, redução é mais agressiva (questão foi refatorada)
        int reducaoBase = 20 + (acertos * 10);
        int bonusTempo = calcularBonusTempo(tempoSegundos);
        erro.setTemperatura(Math.max(0, erro.getTemperatura() - reducaoBase - bonusTempo));

        agendarProximaRevisao(erro, acertos);

        if (acertos >= THRESHOLD_DOMINADO) {
            erro.setStatus(StatusCiclo.DOMINADA);
            erro.setTemperatura(0);
        } else {
            erro.setStatus(StatusCiclo.PROTOCOLO_DIARIO);
        }
    }

    private void processarErroExpurgo(RegistroErro erro) {
        erro.setAcertosConsecutivos(0);
        erro.setTotalErros(erro.getTotalErros() + 1);

        // Penalidade severa: errou revisão crítica
        int penalidade = 35;
        erro.setTemperatura(Math.min(100, erro.getTemperatura() + penalidade));
        erro.setStatus(StatusCiclo.CENTRAL_EXPURGO);
        erro.setDataProximaRevisao(LocalDateTime.now().plusDays(1));
    }

    // ==================== LÓGICA DE REINCIDÊNCIA (CURVA EXPONENCIAL)
    // ====================

    private void processarReincidencia(RegistroErro erro, Questao q,
            Long tempoSegundos, Resolucao.NivelDificuldade feedback) {
        int totalErros = erro.getTotalErros() + 1;
        erro.setTotalErros(totalErros);
        erro.setAcertosConsecutivos(0);

        // Temperatura baseada na curva exponencial
        int indexCurva = Math.min(totalErros - 1, CURVA_REINCIDENCIA.length - 1);
        int tempBase = CURVA_REINCIDENCIA[indexCurva];

        // Aplica multiplicador de etapa
        double multEtapa = getMultiplicadorEtapa(q);
        int tempComEtapa = (int) Math.round(tempBase * multEtapa);

        // Penalidade por tempo lento (demorou e ainda errou)
        int penalidadeTempo = calcularPenalidadeTempo(tempoSegundos);

        int tempFinal = Math.min(100, tempComEtapa + penalidadeTempo);
        erro.setTemperatura(tempFinal);

        // Status baseado na temperatura
        if (tempFinal >= 70) {
            erro.setStatus(StatusCiclo.CENTRAL_EXPURGO);
        } else {
            erro.setStatus(StatusCiclo.PROTOCOLO_DIARIO);
        }

        erro.setDataUltimoErro(LocalDateTime.now());
        erro.setDataProximaRevisao(LocalDateTime.now().plusDays(1));
        erroRepo.save(erro);
    }

    // ==================== TEMPERATURA INICIAL ====================

    private void definirTemperaturaInicial(RegistroErro erro, Questao q,
            Long tempoSegundos, Resolucao.NivelDificuldade feedback) {
        // Base por dificuldade percebida
        int tempBase;
        if (feedback == null) {
            tempBase = 50;
        } else {
            // Enum real: FACIL, MEDIO, DIFICIL, CHUTE, ADIAR
            tempBase = switch (feedback) {
                case FACIL -> 35; // Errou questão fácil = desatenção
                case MEDIO -> 50; // Normal
                case DIFICIL -> 70; // Lacuna séria
                case CHUTE -> 60; // Chutou = não sabia
                case ADIAR -> 45; // Adiou = incerteza
            };
        }

        // Multiplicador por etapa
        double multEtapa = getMultiplicadorEtapa(q);
        int tempComEtapa = (int) Math.round(tempBase * multEtapa);

        // Bonus/penalidade por tempo
        int ajusteTempo = 0;
        if (tempoSegundos != null) {
            if (tempoSegundos < TEMPO_RAPIDO) {
                ajusteTempo = -5; // Errou rápido = desatenção
            } else if (tempoSegundos > TEMPO_LENTO) {
                ajusteTempo = +5; // Pensou muito e errou = lacuna
            }
        }

        erro.setTemperatura(Math.max(20, Math.min(100, tempComEtapa + ajusteTempo)));
    }

    // ==================== CÁLCULOS DE MULTIPLICADORES ====================

    /**
     * Calcula multiplicador baseado no contexto do aluno.
     * - Se questão é da MESMA etapa que o aluno estuda → prioridade MÁXIMA
     * - Se questão é de etapa ANTERIOR → importante (conhecimento base)
     * - Se questão é de etapa FUTURA → menos urgente (ainda vai estudar)
     */
    private double getMultiplicadorEtapa(Usuario u, Questao q) {
        // Se não temos informação completa, retorna neutro
        Integer etapaAluno = (u != null && u.getEtapaAlvo() != null) ? u.getEtapaAlvo() : 1;
        Integer etapaQuestao = null;

        if (q != null && q.getProva() != null && q.getProva().getEtapa() != null) {
            etapaQuestao = q.getProva().getEtapa();
        }

        if (etapaQuestao == null) {
            return MULT_ETAPA_ANTERIOR; // Default neutro
        }

        // Compara etapa da questão com etapa alvo do aluno
        if (etapaQuestao.equals(etapaAluno)) {
            // Mesma etapa = prioridade máxima (vai cair na prova!)
            return MULT_MESMA_ETAPA;
        } else if (etapaQuestao < etapaAluno) {
            // Etapa anterior = importante (conhecimento base)
            return MULT_ETAPA_ANTERIOR;
        } else {
            // Etapa futura = menos urgente (ainda vai estudar)
            return MULT_ETAPA_FUTURA;
        }
    }

    // Overload para compatibilidade (sem usuário)
    private double getMultiplicadorEtapa(Questao q) {
        return getMultiplicadorEtapa(null, q);
    }

    private int calcularBonusTempo(Long tempoSegundos) {
        if (tempoSegundos == null)
            return 0;
        if (tempoSegundos < TEMPO_RAPIDO)
            return 10; // Resposta rápida e certa = confiança
        if (tempoSegundos < TEMPO_NORMAL)
            return 5;
        return 0;
    }

    private int calcularPenalidadeTempo(Long tempoSegundos) {
        if (tempoSegundos == null)
            return 0;
        if (tempoSegundos > TEMPO_LENTO)
            return 10; // Demorou muito e errou
        return 0;
    }

    private void agendarProximaRevisao(RegistroErro erro, int acertosConsecutivos) {
        int index = Math.min(acertosConsecutivos - 1, INTERVALOS_EBBINGHAUS.length - 1);
        if (index < 0)
            index = 0;
        int dias = INTERVALOS_EBBINGHAUS[index];
        erro.setDataProximaRevisao(LocalDateTime.now().plusDays(dias));
    }

    // ==================== DECAIMENTO INTELIGENTE ====================

    /**
     * Decaimento de temperatura baseado no tempo.
     * NÃO afeta erros em CENTRAL_EXPURGO (críticos precisam ser revisados).
     * Decai mais lento para LACUNA_CONTEUDO.
     */
    @Transactional
    public void aplicarDecaimentoTemperatura(Usuario usuario) {
        List<RegistroErro> erros = erroRepo.findByUsuarioOrderByTemperaturaDesc(usuario);
        LocalDateTime agora = LocalDateTime.now();

        for (RegistroErro erro : erros) {
            // NÃO decai erros críticos - eles precisam ser revisados
            if (erro.getStatus() == StatusCiclo.CENTRAL_EXPURGO)
                continue;
            if (erro.getDataUltimoErro() == null)
                continue;

            long diasSemErro = ChronoUnit.DAYS.between(erro.getDataUltimoErro(), agora);
            if (diasSemErro < 7 || erro.getTemperatura() <= 0)
                continue;

            // Decaimento base: 5° por semana
            // Decaimento reduzido para lacunas de conteúdo (3° por semana)
            int decaimentoBase = (erro.getCausa() == CausaErro.LACUNA_CONTEUDO) ? 3 : 5;
            int decaimentoTotal = (int) (diasSemErro / 7) * decaimentoBase;
            int novaTemp = Math.max(0, erro.getTemperatura() - decaimentoTotal);

            // Se temperatura zerou e estava DOMINADA -> EXPURGADO
            if (novaTemp == 0 && erro.getStatus() == StatusCiclo.DOMINADA) {
                erro.setStatus(StatusCiclo.EXPURGADO);
            }

            erro.setTemperatura(novaTemp);
            erroRepo.save(erro);
        }
    }

    // ==================== CLASSIFICAÇÃO MANUAL ====================

    @Transactional
    public void classificarErro(Long erroId, CausaErro causa) {
        RegistroErro erro = erroRepo.findById(erroId).orElseThrow();
        erro.setCausa(causa);

        // Ajuste de temperatura por causa
        int ajuste = switch (causa) {
            case LACUNA_CONTEUDO -> {
                erro.setNecessitaRefatoracaoIA(true);
                yield +15; // Lacuna é grave
            }
            case INTERPRETACAO -> {
                erro.setNecessitaRefatoracaoIA(true);
                yield +5;
            }
            case DESATENCAO -> -10; // Menos grave
            case CHUTE -> -5; // Menos grave, mas não tanto
            default -> 0;
        };

        erro.setTemperatura(Math.max(20, Math.min(100, erro.getTemperatura() + ajuste)));
        erro.setStatus(StatusCiclo.PROTOCOLO_DIARIO);
        erroRepo.save(erro);
    }

    // ==================== BUSCAS ====================

    public List<RegistroErro> buscarRevisoesPendentesHoje(Usuario usuario) {
        LocalDateTime agora = LocalDateTime.now();
        return erroRepo.findByUsuarioOrderByTemperaturaDesc(usuario).stream()
                .filter(e -> e.getDataProximaRevisao() != null &&
                        !e.getDataProximaRevisao().isAfter(agora) &&
                        e.getStatus() != StatusCiclo.EXPURGADO &&
                        e.getStatus() != StatusCiclo.DOMINADA)
                .toList();
    }

    public List<RegistroErro> buscarErrosCriticos(Usuario usuario, int limiteTemperatura) {
        return erroRepo.findByUsuarioAndTemperaturaGreaterThanEqualOrderByTemperaturaDesc(usuario, limiteTemperatura);
    }

    // ==================== HELPERS ====================

    private RegistroErro criarNovoRegistroErro(Usuario u, Questao q) {
        RegistroErro erro = new RegistroErro();
        erro.setUsuario(u);
        erro.setQuestaoOriginal(q);
        erro.setStatus(StatusCiclo.PENDENTE_TRIAGEM);
        erro.setTotalErros(1);
        erro.setAcertosConsecutivos(0);
        erro.setDataUltimoErro(LocalDateTime.now());
        erro.setDataProximaRevisao(LocalDateTime.now().plusDays(1));
        return erro;
    }

    // ==================== ESTATÍSTICAS ====================

    public record EstatisticasErros(
            int total,
            int pendentesTriagem,
            int emProtocolo,
            int dominados,
            int expurgados,
            int criticos,
            double temperaturaMedia,
            int revisoesPendentesHoje) {
    }

    public EstatisticasErros calcularEstatisticas(Usuario usuario) {
        List<RegistroErro> todos = erroRepo.findByUsuarioOrderByTemperaturaDesc(usuario);
        List<RegistroErro> revisoesPendentes = buscarRevisoesPendentesHoje(usuario);

        int total = todos.size();
        int pendentes = (int) todos.stream().filter(e -> e.getStatus() == StatusCiclo.PENDENTE_TRIAGEM).count();
        int protocolo = (int) todos.stream().filter(e -> e.getStatus() == StatusCiclo.PROTOCOLO_DIARIO
                || e.getStatus() == StatusCiclo.CENTRAL_EXPURGO).count();
        int dominados = (int) todos.stream().filter(e -> e.getStatus() == StatusCiclo.DOMINADA).count();
        int expurgados = (int) todos.stream().filter(e -> e.getStatus() == StatusCiclo.EXPURGADO).count();
        int criticos = (int) todos.stream().filter(e -> e.getTemperatura() != null && e.getTemperatura() >= 70).count();
        double tempMedia = todos.stream()
                .filter(e -> e.getTemperatura() != null)
                .mapToInt(RegistroErro::getTemperatura)
                .average().orElse(0);

        return new EstatisticasErros(total, pendentes, protocolo, dominados, expurgados,
                criticos, tempMedia, revisoesPendentes.size());
    }

    // ==================== LEGACY (Compatibilidade) ====================

    public void processarErro(Usuario u, Questao q, Boolean correta, Long tempo, Resolucao.NivelDificuldade feedback) {
        processarRespostaSimulado(u, q, Boolean.TRUE.equals(correta), tempo, feedback);
    }
}