package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pagina_livro")
public class PaginaLivro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volume_id", nullable = false)
    private Volume volume;

    @Column(nullable = false)
    private Integer numeroPagina;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 200)
    private String modulo; // Ex: "Módulo 01", usado no cabeçalho

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LayoutPagina layout = LayoutPagina.STANDARD;

    @Column(columnDefinition = "TEXT")
    private String htmlContent; // Conteúdo HTML editável

    @Column(nullable = false)
    private Integer versao = 1; // Para versionamento

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultima_edicao_por_id")
    private Usuario ultimaEdicaoPor;

    @OneToMany(mappedBy = "pagina", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotaMarginal> notasMarginais = new ArrayList<>();

    // Constructors
    public PaginaLivro() {
    }

    public PaginaLivro(Volume volume, Integer numeroPagina, String titulo) {
        this.volume = volume;
        this.numeroPagina = numeroPagina;
        this.titulo = titulo;
    }

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Volume getVolume() {
        return volume;
    }

    public void setVolume(Volume volume) {
        this.volume = volume;
    }

    public Integer getNumeroPagina() {
        return numeroPagina;
    }

    public void setNumeroPagina(Integer numeroPagina) {
        this.numeroPagina = numeroPagina;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public LayoutPagina getLayout() {
        return layout;
    }

    public void setLayout(LayoutPagina layout) {
        this.layout = layout;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Usuario getUltimaEdicaoPor() {
        return ultimaEdicaoPor;
    }

    public void setUltimaEdicaoPor(Usuario ultimaEdicaoPor) {
        this.ultimaEdicaoPor = ultimaEdicaoPor;
    }

    public List<NotaMarginal> getNotasMarginais() {
        return notasMarginais;
    }

    public void setNotasMarginais(List<NotaMarginal> notasMarginais) {
        this.notasMarginais = notasMarginais;
    }
}
