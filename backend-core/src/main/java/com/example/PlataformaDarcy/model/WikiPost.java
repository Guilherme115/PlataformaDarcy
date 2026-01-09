package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "wiki_posts")
public class WikiPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(columnDefinition = "LONGTEXT")
    private String conteudo; // Texto raw do editor (JSON blocks)

    @Column(columnDefinition = "LONGTEXT")
    private String conteudoHtml; // HTML renderizado para exibição

    @Column(columnDefinition = "TEXT")
    private String thumbnail; // URL da imagem de capa

    @Enumerated(EnumType.STRING)
    private TipoConteudo tipoConteudo = TipoConteudo.ARTIGO;

    public enum TipoConteudo {
        ARTIGO, VIDEO, RESUMO, MAPA_MENTAL, PODCAST
    }

    // Organização por Etapa/Disciplina/Tópico
    private Integer etapa; // 1, 2 ou 3
    private String disciplina; // "HISTORIA", "FISICA"
    private String topico; // "BARROCO", "CINEMATICA"

    // Tags pesquisáveis
    @ElementCollection
    @CollectionTable(name = "wiki_post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    // Status e métricas
    private boolean rascunho = true; // Draft por padrão
    private int curtidas = 0;
    private int visualizacoes = 0;

    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}