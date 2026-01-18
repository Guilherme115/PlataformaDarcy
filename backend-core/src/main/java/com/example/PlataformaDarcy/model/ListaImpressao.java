package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "lista_impressao")
@Data
public class ListaImpressao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private Integer etapa;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm;

    @Column(name = "quantidade_questoes", nullable = false)
    private Integer quantidadeQuestoes;

    @Column(columnDefinition = "TEXT")
    private String materias; // CSV: "Biologia,Química,Física"

    @Column(columnDefinition = "TEXT")
    private String topicos; // CSV: "Genética,Atomística"

    @Column(name = "questoes_ids", columnDefinition = "TEXT", nullable = false)
    private String questoesIds; // JSON: "[123,456,789]"

    @Column(length = 64)
    private String checksum; // MD5/SHA para validação

    @PrePersist
    protected void onCreate() {
        geradoEm = LocalDateTime.now();
    }
}
