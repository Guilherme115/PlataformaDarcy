package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "nota_marginal")
public class NotaMarginal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagina_id", nullable = false)
    private PaginaLivro pagina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoNotaMarginal tipo = TipoNotaMarginal.NOTE;

    @Column(length = 100)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String conteudo;

    @Column(nullable = false)
    private Integer ordem = 0;

    // Constructors
    public NotaMarginal() {
    }

    public NotaMarginal(PaginaLivro pagina, TipoNotaMarginal tipo, String conteudo) {
        this.pagina = pagina;
        this.tipo = tipo;
        this.conteudo = conteudo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaginaLivro getPagina() {
        return pagina;
    }

    public void setPagina(PaginaLivro pagina) {
        this.pagina = pagina;
    }

    public TipoNotaMarginal getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotaMarginal tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
