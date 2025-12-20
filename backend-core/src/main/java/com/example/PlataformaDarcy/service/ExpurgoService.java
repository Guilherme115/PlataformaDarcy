package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.model.RegistroErro.StatusCiclo;
import com.example.PlataformaDarcy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpurgoService {

    @Autowired private RegistroErroRepository erroRepo;
    @Autowired private SimuladoRepository simuladoRepo;
    @Autowired private ResolucaoRepository resolucaoRepo;
    @Autowired private GeminiService geminiService;

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
        List<RegistroErro> criticos = erroRepo.findByUsuarioAndTemperaturaGreaterThanEqualOrderByTemperaturaDesc(usuario, 50)
                .stream()
                .filter(e -> e.getQuestaoOriginal().getTipo() != TipoQuestao.D) // Ignora Discursivas
                .limit(5)
                .collect(Collectors.toList());

        if (criticos.isEmpty()) return null;

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

        if (erros.isEmpty()) return null;

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
}