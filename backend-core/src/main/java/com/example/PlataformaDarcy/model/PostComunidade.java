package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Post da comunidade.
 */
@Entity
@Table(name = "posts_comunidade")
@Data
public class PostComunidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaComunidade categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime editadoEm;

    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private int totalComentarios = 0;

    @Column(nullable = false)
    private boolean ativo = true;

    // Tags opcionais (ex: "FISICA, QUIMICA")
    private String tags;

    // Comentários (lazy para performance)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComentarioComunidade> comentarios = new ArrayList<>();

    // Usuários que deram like
    @ManyToMany
    @JoinTable(name = "likes_posts", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private List<Usuario> usuariosLike = new ArrayList<>();

    public void incrementarLikes() {
        this.likes++;
    }

    public void decrementarLikes() {
        if (this.likes > 0)
            this.likes--;
    }

    public void incrementarComentarios() {
        this.totalComentarios++;
    }
}
