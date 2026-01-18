package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "template_pagina")
public class TemplatePagina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LayoutPagina layout;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String htmlTemplate;

    @Column(length = 500)
    private String thumbnail; // URL da imagem de preview

    @Column(nullable = false)
    private Boolean publico = true; // Se todos podem usar

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    private Usuario criadoPor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    // Constructors
    public TemplatePagina() {
    }

    public TemplatePagina(String nome, LayoutPagina layout, String htmlTemplate) {
        this.nome = nome;
        this.layout = layout;
        this.htmlTemplate = htmlTemplate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LayoutPagina getLayout() {
        return layout;
    }

    public void setLayout(LayoutPagina layout) {
        this.layout = layout;
    }

    public String getHtmlTemplate() {
        return htmlTemplate;
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Boolean getPublico() {
        return publico;
    }

    public void setPublico(Boolean publico) {
        this.publico = publico;
    }

    public Usuario getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
