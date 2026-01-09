package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comentário em um post da comunidade.
 */
@Entity
@Table(name = "comentarios_comunidade")
@Data
public class ComentarioComunidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String conteudo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostComunidade post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private boolean ativo = true;

    // Usuários que deram like
    @ManyToMany
    @JoinTable(name = "likes_comentarios", joinColumns = @JoinColumn(name = "comentario_id"), inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private List<Usuario> usuariosLike = new ArrayList<>();

    public void incrementarLikes() {
        this.likes++;
    }

    public void decrementarLikes() {
        if (this.likes > 0)
            this.likes--;
    }
}
