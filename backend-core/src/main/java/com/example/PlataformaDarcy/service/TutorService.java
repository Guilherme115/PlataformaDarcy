package com.example.PlataformaDarcy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TutorService {

    @Autowired
    private ContextService contextService;

    @Autowired
    private GeminiService geminiService; // Reutilizamos a conexão bruta que já existe

    public String perguntarAoDarcy(String mensagemAluno) {
        // 1. O Bibliotecário busca os livros
        String contexto = contextService.recuperarContextoRelevante(mensagemAluno);

        // 2. Montamos o cenário para a IA
        String systemPrompt = """
            Você é o Darcy, um mentor virtual especializado no PAS (Programa de Avaliação Seriada) da UnB.
            Sua missão é ajudar o estudante a entender as obras e conteúdos.
            
            [DADOS DO ACERVO RECUPERADOS]
            %s
            ---------------------------------------------------
            
            DIRETRIZES:
            1. Use os dados do acervo acima para embasar sua resposta, se fizer sentido.
            2. Seja didático, encorajador e use linguagem acessível (ensino médio).
            3. Use formatação Markdown (negrito, itálico) para destacar pontos importantes.
            4. Se a pergunta for sobre algo fora do contexto do PAS/Estudos, responda educadamente que seu foco é o vestibular.
            5. Responda DIRETAMENTE ao aluno.
            
            Aluno perguntou: "%s"
            """.formatted(contexto, mensagemAluno);

        // 3. Chamamos o Gemini (usando o método genérico existente)
        // Passamos "" no contexto do método antigo pois já embutimos tudo no systemPrompt acima
        return geminiService.gerarConteudoBloco(systemPrompt, "", 1);
    }
}