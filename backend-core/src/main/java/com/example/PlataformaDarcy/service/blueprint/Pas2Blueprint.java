package com.example.PlataformaDarcy.service.blueprint;

import com.example.PlataformaDarcy.model.dto.SlotDefinition;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class Pas2Blueprint implements BlueprintInterface {
    @Override
    public int getTotalItens() { return 110; } // PAS 2 tem 110 itens

    @Override
    public List<SlotDefinition> getReceita() {
        List<SlotDefinition> slots = new ArrayList<>();
        slots.add(new SlotDefinition("Línguas", "OBRAS", List.of("LINGUAS"), "Interpretação", 10));
        slots.add(new SlotDefinition("Visual & História", "OBRAS", List.of("VISUAL", "HISTORIA"), "Século XIX e Brasil Império", 20));
        slots.add(new SlotDefinition("Física Ondulatória", "TEMAS", List.of("FISICA"), "Ondas e Termodinâmica", 30));
        slots.add(new SlotDefinition("Literatura", "OBRAS", List.of("LITERATURA"), "Romantismo e Realismo", 25));
        slots.add(new SlotDefinition("Biologia/Química", "TEMAS", List.of("NATUREZA_GERAL"), "Estequiometria e Fisiologia", 25));
        return slots;
    }
}