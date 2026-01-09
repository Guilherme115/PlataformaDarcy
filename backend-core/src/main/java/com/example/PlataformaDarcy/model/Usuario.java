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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
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