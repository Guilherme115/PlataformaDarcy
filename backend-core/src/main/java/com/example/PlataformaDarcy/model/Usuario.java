package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricula; // Gerado Automaticamente (4 dígitos)

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String nome;

    private String perfil; // ESTUDANTE, ADMIN

    private String regiao; // Ex: Ceilândia, Taguatinga, etc.

    // Etapa do PAS que o aluno está se preparando (1, 2 ou 3)
    private Integer etapaAlvo = 1;

    // CAMPO ATIVO ADICIONADO
    @Column(nullable = false)
    private boolean ativo = true; // Padrão true para evitar null pointer

    @Enumerated(EnumType.STRING)
    private Provider provider;

    public enum Provider {
        LOCAL, GOOGLE
    }

    // === SISTEMA DE ASSINATURAS ===

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlano plano = TipoPlano.FREE;

    private java.time.LocalDateTime dataExpiracaoPlano;

    // === QUOTAS DE USO (PROTEÇÃO DE CUSTO) ===

    /**
     * Quantidade de mensagens IA usadas hoje.
     * Reseta automaticamente todo dia às 00:00.
     */
    @Column(nullable = false)
    private Integer quotaIaMensagensHoje = 0;

    /**
     * Quantidade de podcasts gerados este mês.
     * Reseta automaticamente todo dia 1.
     */
    @Column(nullable = false)
    private Integer quotaPodcastsMes = 0;

    /**
     * Data da última vez que usou IA (para resetar quota diária).
     */
    private java.time.LocalDate dataUltimoUsoIA;

    // === MÉTODOS DE VERIFICAÇÃO DE PLANO ===

    /**
     * Verifica se o usuário tem plano PRO ativo.
     */
    public boolean isPro() {
        if (plano != TipoPlano.PRO)
            return false;
        if (dataExpiracaoPlano == null)
            return true; // Sem expiração = vitalício
        return dataExpiracaoPlano.isAfter(java.time.LocalDateTime.now());
    }

    /**
     * Verifica se tem algum plano pago (ESTUDANTE ou PRO).
     */
    public boolean isEstudanteOuPro() {
        return plano == TipoPlano.ESTUDANTE || plano == TipoPlano.PRO;
    }

    /**
     * Retorna limite diário de mensagens IA baseado no plano.
     */
    public int getLimiteMensagensIA() {
        return switch (plano) {
            case FREE -> 0; // Sem acesso à IA
            case ESTUDANTE -> 100; // 100 mensagens/dia
            case PRO -> 999999; // "Ilimitado" (número grande)
        };
    }

    /**
     * Retorna limite mensal de podcasts baseado no plano.
     */
    public int getLimitePodcastsMes() {
        return switch (plano) {
            case FREE -> 0; // Sem acesso a podcasts
            case ESTUDANTE -> 5; // 5 podcasts/mês
            case PRO -> 999999; // "Ilimitado"
        };
    }

    /**
     * Verifica se usuário pode usar IA agora (respeitando quotas).
     */
    public boolean podeUsarIA() {
        if (!isEstudanteOuPro())
            return false;
        return quotaIaMensagensHoje < getLimiteMensagensIA();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + perfil));

        // Adiciona roles baseado no plano
        if (plano == TipoPlano.ESTUDANTE) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ESTUDANTE"));
        }
        if (isPro()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PRO"));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return matricula;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo; // Agora retorna o valor real do banco!
    }
}