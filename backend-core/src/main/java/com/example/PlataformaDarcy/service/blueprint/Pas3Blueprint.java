package com.example.PlataformaDarcy.service.blueprint;

import com.example.PlataformaDarcy.model.dto.SlotDefinition;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class Pas3Blueprint implements BlueprintInterface {
    @Override
    public int getTotalItens() { return 120; } // PAS 3 tem 120 itens

    @Override
    public List<SlotDefinition> getReceita() {
        List<SlotDefinition> slots = new ArrayList<>();
        slots.add(new SlotDefinition("Línguas", "OBRAS", List.of("LINGUAS"), "Interpretação", 10));
        slots.add(new SlotDefinition("Visual & Exatas", "OBRAS", List.of("VISUAL", "FISICA"), "Física Moderna e Matemática", 25));
        slots.add(new SlotDefinition("Literatura & Humanas", "OBRAS", List.of("LITERATURA"), "Filosofia e Modernismo", 30));
        slots.add(new SlotDefinition("Sociologia & Cultura", "OBRAS", List.of("MUSICAL", "SOCIOLOGIA"), "Conflitos Sociais", 25));
        slots.add(new SlotDefinition("Ciências Puras", "TEMAS", List.of("NATUREZA_AVANCADA"), "Genética e Eletromagnetismo", 30));
        return slots;
    }
}