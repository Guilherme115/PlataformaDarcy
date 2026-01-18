package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "conteudo_programatico")
@Data
public class ConteudoProgramatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Etapa do PAS: 1, 2 ou 3
     */
    @Column(nullable = false)
    private Integer etapa;

    /**
     * Matéria (ex: "QUÍMICA", "FÍSICA", "MATEMÁTICA", "PORTUGUÊS")
     */
    @Column(nullable = false, length = 100)
    private String materia;

    /**
     * Tópico/conteúdo (ex: "Química Geral", "Cinemática")
     */
    @Column(nullable = false, length = 500)
    private String topico;

    /**
     * Observações adicionais (texto entre parênteses na entrada)
     */
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    /**
     * Ordem de exibição dentro da matéria
     */
    @Column(nullable = false)
    private Integer ordem = 0;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
