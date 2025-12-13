package com.example.PlataformaDarcy.model;

import com.example.PlataformaDarcy.model.Prova;
import com.example.PlataformaDarcy.model.Questao;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "blocos")
@Data
public class Bloco {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String textoBase;

    private String caminhoImagem;

    @ManyToOne
    @JoinColumn(name = "prova_id")
    private Prova prova;

    @OneToMany(mappedBy = "bloco", fetch = FetchType.EAGER)
    private List<Questao> questoes;
}