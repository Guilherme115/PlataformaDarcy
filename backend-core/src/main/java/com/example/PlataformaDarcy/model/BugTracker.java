package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class BugTracker {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    private String categoria; // UI, PERFORMANCE, CRASH
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    // Dados automáticos
    private String urlOrigem;
    private String userAgent;
    private String resolucaoTela;

    private boolean resolvido = false;
    private LocalDateTime dataReport = LocalDateTime.now();
}