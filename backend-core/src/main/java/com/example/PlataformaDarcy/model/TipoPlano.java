package com.example.PlataformaDarcy.model;

/**
 * Tipo de plano de assinatura do usuário.
 */
public enum TipoPlano {
    FREE, // Plano gratuito - acesso a simulados, wiki, expurgo (SEM IA)
    ESTUDANTE, // Plano básico - tudo do Free + IA limitada (Flash, 100 msgs/dia, 5
               // podcasts/mês)
    PRO // Plano premium - IA ilimitada (Pro model), podcasts ilimitados, suporte
        // prioritário
}
