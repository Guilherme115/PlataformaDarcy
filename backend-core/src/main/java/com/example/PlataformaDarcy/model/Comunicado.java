package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Comunicado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    // Tipos para estilização visual:
    // "INFO" (Amarelo), "ALERTA" (Vermelho), "EVENTO" (Azul)
    private String tipo = "INFO";

    // Se true, aparece no feed do aluno. Se false, fica arquivado no admin.
    private boolean ativo = true;

    private LocalDateTime dataEnvio = LocalDateTime.now();

    // Construtor vazio (obrigatório para JPA)
    public Comunicado() {}
}