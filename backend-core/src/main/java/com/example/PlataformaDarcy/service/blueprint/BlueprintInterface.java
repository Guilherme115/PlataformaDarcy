package com.example.PlataformaDarcy.service.blueprint;

import com.example.PlataformaDarcy.model.dto.SlotDefinition;
import java.util.List;

public interface BlueprintInterface {
    List<SlotDefinition> getReceita();
    int getTotalItens();
}