package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

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

    private String titulo;

    @Column(length = 50)
    private String origem;

    // --- CORREÇÃO AQUI ---
    // Adicionamos o campo que o Dashboard está pedindo
    @Transient
    private Boolean ativo = true; // Coloquei = true para as provas já nascerem ativas
    // ---------------------

    @OneToMany(mappedBy = "prova", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Questao> questoes;

    public String getTituloDisplay() {
        if (titulo != null) return titulo;
        return "Prova de " + ano + " (PDF)";


    }
}