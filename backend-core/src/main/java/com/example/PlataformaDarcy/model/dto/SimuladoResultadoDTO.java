package com.example.PlataformaDarcy.model.dto;

import com.example.PlataformaDarcy.model.Resolucao;
import com.example.PlataformaDarcy.model.Simulado;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SimuladoResultadoDTO {
    private Simulado simulado;
    private List<Resolucao> resolucoes;

    private long statsAcertos;
    private long statsErros;
    private long statsBranco;
    private int possiveisPontos;

    private String tempoTotal;
    private String tempoMedio;

    private List<MateriaEstatistica> rankingMaterias;

    @Data
    @Builder
    public static class MateriaEstatistica {
        private String nome;
        private double pontos;
        private long acertos;
        private long erros;
        private long branco;
    }
}