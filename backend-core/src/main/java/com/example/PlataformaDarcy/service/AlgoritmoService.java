package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.RegistroErroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AlgoritmoService {

    @Autowired private RegistroErroRepository erroRepo;

    // --- LÓGICA DO PROTOCOLO DE EXPURGO (A "UTI Cognitiva") ---
    public void processarResultadoExpurgo(RegistroErro erro, boolean acertou) {
        if (erro == null) return;

        if (acertou) {
            // CURA: O aluno entendeu a lógica.
            // Baixa a temperatura para 10º (Baixa probabilidade de erro futuro).
            erro.setTemperatura(10);
            // Opcional: Poderia mudar status para 'CURADO' ou manter monitorado
        } else {
            // PENALIDADE: Errou a revisão crítica.
            // Sobe para 100º (Urgência máxima / Reincidência grave).
            erro.setTemperatura(100);
        }

        erro.setDataUltimoErro(LocalDateTime.now());
        erroRepo.save(erro);
    }

    // --- LÓGICA PADRÃO (SIMULADOS NORMAIS) ---
    public void processarErro(Usuario u, Questao q, Boolean correta, Long tempo, Resolucao.NivelDificuldade feedback) {
        // Se acertou, não cria registro de erro novo (no modo normal).
        if (Boolean.TRUE.equals(correta)) return;

        // Busca se esse erro já existe no histórico do aluno
        RegistroErro registro = erroRepo.findByUsuarioAndQuestaoOriginal(u, q)
                .orElse(new RegistroErro());

        if (registro.getId() == null) {
            // PRIMEIRO ERRO (Trauma Inicial)
            registro.setUsuario(u);
            registro.setQuestaoOriginal(q);
            registro.setStatus(RegistroErro.StatusCiclo.PENDENTE_TRIAGEM);
            registro.setTotalErros(1);
            registro.setTemperatura(50); // Temperatura inicial padrão

            // Aqui poderíamos usar o 'feedback' para ajustar a temperatura inicial se quiser
            // Ex: Se feedback for CHUTE, talvez a temperatura devesse ser maior.
        } else {
            // REINCIDÊNCIA (O aluno errou de novo a mesma questão)
            registro.setTotalErros(registro.getTotalErros() + 1);

            // Aumenta a temperatura progressivamente até o teto de 100
            registro.setTemperatura(Math.min(100, registro.getTemperatura() + 20));
        }

        registro.setDataUltimoErro(LocalDateTime.now());
        erroRepo.save(registro);
    }
}