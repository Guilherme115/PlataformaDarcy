package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "provas")
@Data
public class Prova {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ano;
    private Integer etapa;

    @Column(name = "nome_arquivo_pdf")
    private String nomeArquivoPdf;
}