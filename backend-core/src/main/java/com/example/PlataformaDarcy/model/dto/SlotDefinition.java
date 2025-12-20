package com.example.PlataformaDarcy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data // Gera Getters, Setters, toString, etc. automaticamente
@NoArgsConstructor // Necessário para frameworks de JSON
@AllArgsConstructor
public class SlotDefinition {
    private String nomeSlot;
    private String fonteDados; // "OBRAS" ou "TEMAS"
    private List<String> tagsObrigatorias;
    private String promptInstrucao;
    private int quantidadeItens;

    public String getNomeSlot() { return nomeSlot; }
    public String getFonteDados() { return fonteDados; }
    public List<String> getTagsObrigatorias() { return tagsObrigatorias; }
    public String getPromptInstrucao() { return promptInstrucao; }
    public int getQuantidadeItens() { return quantidadeItens; }
}