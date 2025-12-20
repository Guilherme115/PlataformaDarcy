package com.example.PlataformaDarcy.service.blueprint;

import com.example.PlataformaDarcy.model.dto.SlotDefinition;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class Pas1Blueprint implements BlueprintInterface {
    @Override
    public int getTotalItens() { return 100; }

    @Override
    public List<SlotDefinition> getReceita() {
        List<SlotDefinition> slots = new ArrayList<>();
        // 1. Línguas (10 itens)
        slots.add(new SlotDefinition("Línguas", "OBRAS", List.of("LINGUAS"), "Interpretação e Gramática", 10));
        // 2. Visual/Exatas (20 itens)
        slots.add(new SlotDefinition("Visual Interdisciplinar", "OBRAS", List.of("VISUAL", "MATEMATICA"), "Geometria e Contexto", 20));
        // 3. Humanas (20 itens)
        slots.add(new SlotDefinition("Humanas e Sociedade", "OBRAS", List.of("SOCIOLOGIA"), "Análise Social", 20));
        // 4. Linguagens/Literatura (20 itens)
        slots.add(new SlotDefinition("Literatura", "OBRAS", List.of("LITERATURA"), "Análise Literária", 20));
        // 5. Ciências Puras (30 itens)
        slots.add(new SlotDefinition("Ciências da Natureza", "TEMAS", List.of("NATUREZA_GERAL"), "Conceitos Técnicos", 30));
        return slots;
    }
}