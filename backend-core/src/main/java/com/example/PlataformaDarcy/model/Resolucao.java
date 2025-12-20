package com.example.PlataformaDarcy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resolucoes")
public class Resolucao {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "simulado_id")
    private Simulado simulado;

    @ManyToOne @JoinColumn(name = "questao_id")
    private Questao questao;

    @Column(columnDefinition = "TEXT")
    private String respostaAluno;

    private Boolean correta;

    // TEMPO GASTO (Importante para o Algoritmo)
    private Long tempoSegundos = 0L;

    @Enumerated(EnumType.STRING)
    private NivelDificuldade feedbackUsuario;

    private LocalDateTime dataResposta;

    // --- NOVOS CAMPOS PARA O EXPURGO (IA) ---
    @Column(name = "gabarito_dinamico")
    private String gabaritoDinamico; // Guarda o novo gabarito da IA (Ex: "E")

    @Column(name = "enunciado_dinamico", columnDefinition = "TEXT")
    private String enunciadoDinamico; // Guarda o novo texto da IA

    public enum NivelDificuldade { FACIL, MEDIO, DIFICIL, CHUTE, ADIAR }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Simulado getSimulado() { return simulado; }
    public void setSimulado(Simulado simulado) { this.simulado = simulado; }
    public Questao getQuestao() { return questao; }
    public void setQuestao(Questao questao) { this.questao = questao; }
    public String getRespostaAluno() { return respostaAluno; }
    public void setRespostaAluno(String respostaAluno) { this.respostaAluno = respostaAluno; }
    public Boolean getCorreta() { return correta; }
    public void setCorreta(Boolean correta) { this.correta = correta; }
    public Long getTempoSegundos() { return tempoSegundos; }
    public void setTempoSegundos(Long tempoSegundos) { this.tempoSegundos = tempoSegundos; }
    public NivelDificuldade getFeedbackUsuario() { return feedbackUsuario; }
    public void setFeedbackUsuario(NivelDificuldade feedbackUsuario) { this.feedbackUsuario = feedbackUsuario; }
    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }

    // Novos Getters/Setters
    public String getGabaritoDinamico() { return gabaritoDinamico; }
    public void setGabaritoDinamico(String gabaritoDinamico) { this.gabaritoDinamico = gabaritoDinamico; }
    public String getEnunciadoDinamico() { return enunciadoDinamico; }
    public void setEnunciadoDinamico(String enunciadoDinamico) { this.enunciadoDinamico = enunciadoDinamico; }
}