package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "wiki_posts")
public class WikiPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(columnDefinition = "LONGTEXT")
    private String conteudo; // O texto explicativo do aluno

    // A ORGANIZAÇÃO "REDE SOCIAL + BIBLIOTECA"
    private Integer etapa;   // 1, 2 ou 3
    private String disciplina; // "HISTORIA", "FISICA"
    private String topico;     // "BARROCO", "CINEMATICA"

    private int curtidas = 0;
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;
}