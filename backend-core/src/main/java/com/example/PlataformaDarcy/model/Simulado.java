package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulados")
public class Simulado {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    private String titulo; // Ex: "PAS 1 - 2013", "PROTOCOLO DE EXPURGO"

    private Double notaFinal;

    @Enumerated(EnumType.STRING)
    private ModoExecucao modo; // APRENDIZADO ou LIVRE (Cronômetro)

    // --- NOVO CAMPO PARA O MÓDULO DE EXPURGO ---
    // Serve para identificar se é "NORMAL", "PROTOCOLO_IA", "CENTRAL_MANUAL", etc.
    private String tipo;

    public enum ModoExecucao {
        LIVRE,         // Simula prova real (sem feedback imediato)
        APRENDIZADO    // Feedback imediato a cada questão
    }

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Double getNotaFinal() { return notaFinal; }
    public void setNotaFinal(Double notaFinal) { this.notaFinal = notaFinal; }

    public ModoExecucao getModo() { return modo; }
    public void setModo(ModoExecucao modo) { this.modo = modo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}