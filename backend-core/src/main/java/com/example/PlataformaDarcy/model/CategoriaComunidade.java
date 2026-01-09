package com.example.PlataformaDarcy.model;

/**
 * Categorias da comunidade.
 */
public enum CategoriaComunidade {
    PAS_1("PAS 1", "Primeira etapa do PAS"),
    PAS_2("PAS 2", "Segunda etapa do PAS"),
    PAS_3("PAS 3", "Terceira etapa do PAS"),
    INTERVALO("Intervalo", "Off-topic, memes, desabafo");

    private final String nome;
    private final String descricao;

    CategoriaComunidade(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
