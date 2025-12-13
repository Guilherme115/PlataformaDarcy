package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "questoes")
@Data
public class Questao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numero;

    @Column(columnDefinition = "TEXT")
    private String enunciado;

    @Column(columnDefinition = "TEXT")
    private String alternativas; // JSON das opções A, B, C, D

    @Enumerated(EnumType.STRING)
    private StatusRevisao status = StatusRevisao.PENDENTE;

    @Enumerated(EnumType.STRING)
    private TipoQuestao tipo = TipoQuestao.A;

    private String gabarito;
    private String tags;

    @ManyToOne
    @JoinColumn(name = "bloco_id")
    private Bloco bloco;

    @ManyToOne
    @JoinColumn(name = "prova_id")
    private Prova prova;

    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ImagemQuestao> imagens;

    public String getAltTexto(String letra) {
        if (alternativas == null || alternativas.isEmpty()) return "";
        try {
            Map map = new ObjectMapper().readValue(alternativas, Map.class);
            return (String) map.getOrDefault(letra, "");
        } catch (Exception e) { return ""; }
    }
}