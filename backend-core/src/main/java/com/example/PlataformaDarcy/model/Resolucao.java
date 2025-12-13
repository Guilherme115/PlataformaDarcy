package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "resolucoes")
@Data
public class Resolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "simulado_id", nullable = false)
    private Simulado simulado;

    @ManyToOne
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    // O que o aluno marcou: "C", "E", "A", "B", etc ou NULL em branco)
    private String respostaAluno;

    private Boolean correta;

    private LocalDateTime dataResposta = LocalDateTime.now();
}