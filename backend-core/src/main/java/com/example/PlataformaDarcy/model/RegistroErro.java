package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "registros_erros")
public class RegistroErro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "questao_id")
    private Questao questaoOriginal;

    @Enumerated(EnumType.STRING)
    private CausaErro causa;

    @Enumerated(EnumType.STRING)
    private StatusCiclo status;

    private Integer temperatura;
    private Integer totalErros = 0;
    private Integer acertosConsecutivos = 0;

    private boolean necessitaRefatoracaoIA;

    @Column(columnDefinition = "TEXT")
    private String ultimaVersaoRefatoradaIA;

    private LocalDateTime dataUltimoErro;
    private LocalDateTime dataProximaRevisao;

    public enum CausaErro {
        LACUNA_CONTEUDO, INTERPRETACAO, DESATENCAO, CHUTE, NAO_CLASSIFICADO
    }

    public enum StatusCiclo {
        PENDENTE_TRIAGEM,
        PROTOCOLO_DIARIO,
        CENTRAL_EXPURGO,
        NECROTERIO,
        DOMINADA,
        EXPURGADO // <--- ADICIONADO PARA CORRIGIR O ERRO
    }
}