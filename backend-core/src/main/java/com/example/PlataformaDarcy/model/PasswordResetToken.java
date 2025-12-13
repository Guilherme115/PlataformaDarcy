package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = Usuario.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private Usuario usuario;

    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, Usuario usuario) {
        this.token = token;
        this.usuario = usuario;
        this.expiryDate = LocalDateTime.now().plusMinutes(60); // Token vale por 1 hora
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}