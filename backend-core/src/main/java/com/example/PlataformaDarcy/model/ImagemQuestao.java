package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "imagens_questoes")
@Data
public class ImagemQuestao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caminho_arquivo")
    private String caminhoArquivo;

    private String tag = "ENUNCIADO"; // Valores: ENUNCIADO, A, B, C, D

    @ManyToOne
    @JoinColumn(name = "questao_id")
    private Questao questao;
}