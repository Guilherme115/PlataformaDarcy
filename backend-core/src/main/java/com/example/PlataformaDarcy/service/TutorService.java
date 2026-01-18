package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Tutor IA 2.0 - Totalmente conectado à Plataforma.
 * Integra contexto do estudante, obras do PAS e análise de desempenho.
 */
@Service
public class TutorService {

    @Autowired
    private ContextService contextService;
    @Autowired
    private StudentContextService studentContextService;
    @Autowired
    private GeminiService geminiService;

    // ==================== CHAT GERAL ====================

    /**
     * Pergunta geral com contexto completo do aluno.
     */
    public String perguntarAoDarcy(Usuario usuario, String mensagem) {
        String contextoEstudante = studentContextService.getResumoCompletoParaIA(usuario);

        // Agora passa a etapa do aluno para priorizar obras relevantes
        Integer etapaAlvo = (usuario.getEtapaAlvo() != null) ? usuario.getEtapaAlvo() : 1;
        String contextoObras = contextService.recuperarContextoRelevante(mensagem, etapaAlvo);

        String systemPrompt = buildSystemPrompt(contextoEstudante, contextoObras, null, mensagem);
        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }

    /**
     * Versão simplificada sem usuário (compatibilidade).
     */
    public String perguntarAoDarcy(String mensagem) {
        String contexto = contextService.recuperarContextoRelevante(mensagem);
        String systemPrompt = """
                Você é o Darcy, mentor virtual do PAS/UnB.

                [ACERVO RECUPERADO]
                %s

                DIRETRIZES:
                1. Seja didático e use linguagem de ensino médio
                2. Use Markdown (negrito, listas)
                3. Foque no PAS/vestibular

                Aluno perguntou: "%s"
                """.formatted(contexto, mensagem);

        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }

    // ==================== ANÁLISE DE ERROS ====================

    /**
     * Analisa erros com filtros opcionais.
     */
    public String analisarErros(Usuario usuario, String materia, Integer tempMin) {
        String contextoErros = studentContextService.getAnaliseErrosParaIA(usuario, materia, tempMin);
        String contextoEstudante = studentContextService.getResumoCompletoParaIA(usuario);

        String systemPrompt = """
                Você é o Darcy, tutor IA do PAS/UnB.

                O aluno pediu para você analisar seus erros%s.

                %s

                %s

                TAREFA:
                1. Faça uma análise clara dos erros apresentados
                2. Identifique padrões (matérias, tipos de questão)
                3. Sugira 3 ações práticas para melhorar
                4. Seja encorajador mas realista
                5. Use Markdown para formatar
                """.formatted(
                materia != null ? " de " + materia : "",
                contextoEstudante,
                contextoErros);

        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }

    /**
     * Analisa todos os erros (geral).
     */
    public String analisarErrosGeral(Usuario usuario) {
        return analisarErros(usuario, null, null);
    }

    /**
     * Analisa apenas erros críticos.
     */
    public String analisarErrosCriticos(Usuario usuario) {
        return analisarErros(usuario, null, 70);
    }

    // ==================== ANÁLISE DE DESEMPENHO ====================

    /**
     * Analisa desempenho por matéria.
     */
    public String analisarDesempenho(Usuario usuario) {
        String contexto = studentContextService.getResumoCompletoParaIA(usuario);

        // Monta texto adicional de desempenho
        var fracas = studentContextService.getMateriasMaisFracas(usuario);
        var fortes = studentContextService.getMateriasMaisFortes(usuario);

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== MATÉRIAS FRACAS (priorizar) ===\n");
        fracas.stream().limit(5).forEach(dm -> sb.append(String.format("- %s: %.1f%% (basado em %d questões)\n",
                dm.materia, dm.taxaAcerto, dm.totalQuestoes)));

        sb.append("\n=== MATÉRIAS FORTES ===\n");
        fortes.stream().limit(3).forEach(dm -> sb.append(String.format("- %s: %.1f%%\n", dm.materia, dm.taxaAcerto)));

        String systemPrompt = """
                Você é o Darcy, tutor IA do PAS/UnB.

                O aluno quer uma análise de seu desempenho por matéria.

                %s
                %s

                TAREFA:
                1. Faça um diagnóstico claro do nível do aluno
                2. Indique qual matéria deve ser PRIORIDADE máxima
                3. Dê 3 dicas práticas para as matérias fracas
                4. Parabenize os pontos fortes
                5. Termine com uma mensagem motivacional
                6. Use emojis e Markdown
                """.formatted(contexto, sb.toString());

        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }

    /**
     * Sugere qual matéria priorizar.
     */
    public String sugerirPrioridade(Usuario usuario) {
        var fracas = studentContextService.getMateriasMaisFracas(usuario);

        if (fracas.isEmpty()) {
            return "Ainda não temos dados suficientes para sugerir prioridades. Continue fazendo simulados!";
        }

        var pior = fracas.get(0);
        var criticos = studentContextService.getErrosPorMateria(usuario, pior.materia);

        String systemPrompt = """
                Você é o Darcy, tutor IA do PAS/UnB.

                O aluno perguntou qual matéria deve priorizar.

                DADOS:
                - Matéria mais fraca: %s (%.1f%% de acerto em %d questões)
                - Erros ativos nessa matéria: %d
                - Etapa alvo: PAS %d

                TAREFA:
                1. Confirme que %s é a prioridade
                2. Explique brevemente por quê (dados concretos)
                3. Dê 2-3 dicas de estudo específicas para essa matéria
                4. Sugira recursos (livros, vídeos, exercícios)
                5. Use Markdown e seja direto
                """.formatted(
                pior.materia, pior.taxaAcerto, pior.totalQuestoes,
                criticos.size(),
                usuario.getEtapaAlvo() != null ? usuario.getEtapaAlvo() : 1,
                pior.materia);

        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }

    // ==================== OBRAS DO PAS ====================

    /**
     * Pergunta focada em uma obra específica.
     */
    public String perguntarSobreObra(Usuario usuario, String obraId, String pergunta) {
        String contextoObra = contextService.formatarObraParaIA(obraId);
        String contextoEstudante = studentContextService.getResumoCompletoParaIA(usuario);

        if (contextoObra.isEmpty()) {
            return "Não encontrei essa obra no acervo. Tente selecionar outra.";
        }

        String systemPrompt = buildSystemPrompt(contextoEstudante, "", contextoObra, pergunta);
        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }

    /**
     * Explica como uma obra pode cair na prova.
     */
    public String explicarComoObraCai(String obraId) {
        String contextoObra = contextService.formatarObraParaIA(obraId);

        if (contextoObra.isEmpty()) {
            return "Obra não encontrada.";
        }

        String systemPrompt = """
                Você é o Darcy, especialista no PAS/UnB.

                %s

                TAREFA:
                1. Explique como essa obra costuma cair nas provas do PAS
                2. Dê exemplos de tipos de questões (interpretação, relação com matérias)
                3. Liste 3 pontos principais para memorizar
                4. Sugira como relacionar com outras obras/matérias
                5. Use Markdown, bullet points e seja didático
                """.formatted(contextoObra);

        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }

    // ==================== ÚLTIMO SIMULADO ====================

    /**
     * Analisa o último simulado do aluno.
     */
    public String analisarUltimoSimulado(Usuario usuario) {
        var stats = studentContextService.getAnaliseUltimoSimulado(usuario);

        if (stats == null) {
            return "Você ainda não fez nenhum simulado. Que tal começar agora?";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ÚLTIMO SIMULADO ===\n");
        sb.append(String.format("Título: %s\n", stats.titulo));
        sb.append(String.format("Resultado: %d/%d (%.1f%%)\n", stats.acertos, stats.totalQuestoes, stats.taxaAcerto));
        sb.append("\nPor matéria:\n");
        stats.desempenhoPorMateria
                .forEach((mat, arr) -> sb.append(String.format("- %s: %d/%d\n", mat, arr[0], arr[1])));

        String systemPrompt = """
                Você é o Darcy, tutor IA do PAS/UnB.

                Analise o último simulado do aluno:

                %s

                TAREFA:
                1. Comente o resultado geral
                2. Destaque matérias que foram bem e mal
                3. Compare com a média esperada para o PAS
                4. Dê 3 sugestões de melhoria específicas
                5. Termine com motivação
                6. Use Markdown e emojis
                """.formatted(sb.toString());

        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String buildSystemPrompt(String contextoEstudante, String contextoObras,
            String obraEspecifica, String pergunta) {
        return """
                Você é o Darcy, tutor IA do PAS/UnB. Você conhece profundamente este aluno.

                %s

                %s

                %s

                DIRETRIZES:
                1. Personalize a resposta para o nível do aluno
                2. Se ele tem dificuldade em alguma matéria, reforce conceitos básicos
                3. Relacione com obras do PAS quando fizer sentido
                4. Use Markdown (negrito, listas, emojis)
                5. Seja encorajador mas honesto
                6. Responda DIRETAMENTE ao aluno

                Aluno perguntou: "%s"
                """.formatted(
                contextoEstudante,
                obraEspecifica != null && !obraEspecifica.isEmpty() ? obraEspecifica : "",
                contextoObras != null && !contextoObras.isEmpty() ? "[OBRAS RELACIONADAS]\n" + contextoObras : "",
                pergunta);
    }

    // ==================== DADOS PARA FRONTEND ====================

    /**
     * Retorna dados do contexto do aluno para exibir no painel.
     */
    public Map<String, Object> getDadosContexto(Usuario usuario) {
        var desempenho = studentContextService.getDesempenhoPorMateria(usuario);
        var fracas = studentContextService.getMateriasMaisFracas(usuario);
        var criticos = studentContextService.getErrosCriticos(usuario);
        var ultimoSimulado = studentContextService.getAnaliseUltimoSimulado(usuario);

        int totalAcertos = desempenho.values().stream().mapToInt(d -> d.acertos).sum();
        int totalQuestoes = desempenho.values().stream().mapToInt(d -> d.totalQuestoes).sum();

        return Map.of(
                "nome", usuario.getNome(),
                "etapaAlvo", usuario.getEtapaAlvo() != null ? usuario.getEtapaAlvo() : 1,
                "taxaAcertoGeral", totalQuestoes > 0 ? Math.round(totalAcertos * 100.0 / totalQuestoes) : 0,
                "totalQuestoes", totalQuestoes,
                "errosCriticos", criticos.size(),
                "materiaFraca", fracas.isEmpty() ? "N/A" : fracas.get(0).materia,
                "ultimoSimulado", ultimoSimulado != null ? ultimoSimulado.titulo : "Nenhum");
    }

    /**
     * Retorna obras agrupadas para o seletor.
     */
    public Map<String, List<Map<String, Object>>> getObrasAgrupadas() {
        return contextService.listarObrasAgrupadas();
    }
}