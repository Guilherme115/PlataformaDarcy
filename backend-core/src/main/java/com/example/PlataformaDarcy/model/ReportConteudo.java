package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class ReportConteudo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Questao questao; // Contexto Automático

    @ManyToOne
    private Prova prova;     // Contexto Automático

    @Enumerated(EnumType.STRING)
    private TipoErro tipoErro;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusReport status = StatusReport.PENDENTE;

    private LocalDateTime dataReport = LocalDateTime.now();

    public enum TipoErro { GABARITO_ERRADO, IMAGEM_RUIM, ERRO_DIGITACAO, TEXTO_CONFUSO, OUTRO }
    public enum StatusReport { PENDENTE, EM_ANALISE, CORRIGIDO, RECUSADO }
}