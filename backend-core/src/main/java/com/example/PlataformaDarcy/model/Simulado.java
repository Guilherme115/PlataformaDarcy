package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "simulados")
@Data
public class Simulado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private LocalDateTime dataInicio = LocalDateTime.now();

    private LocalDateTime dataFim; // Se estiver NULL, o simulado está "Em Aberto"

    private Double notaFinal;

    @Column(columnDefinition = "TEXT")
    private String titulo;

    @OneToMany(mappedBy = "simulado", cascade = CascadeType.ALL)
    private List<Resolucao> resolucoes;

    public boolean isFinalizado() {
        return dataFim != null;
    }
}