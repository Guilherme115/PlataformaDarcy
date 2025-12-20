package com.example.PlataformaDarcy.service.blueprint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlueprintFactory {

    @Autowired private Pas1Blueprint pas1;
    @Autowired private Pas2Blueprint pas2;
    @Autowired private Pas3Blueprint pas3;

    public BlueprintInterface getBlueprint(int etapa) {
        return switch (etapa) {
            case 1 -> pas1;
            case 2 -> pas2;
            case 3 -> pas3;
            default -> throw new IllegalArgumentException("Etapa desconhecida ou não implementada: " + etapa);
        };
    }
}