package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class   ExpurgoIntegrationTest {

    @Autowired private AlgoritmoService algoritmoService;
    @Autowired private GeminiService geminiService;
    @Autowired private SimuladoService simuladoService;

    @Autowired private RegistroErroRepository erroRepo;
    @Autowired private QuestaoRepository questaoRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ResolucaoRepository resolucaoRepo;
    @Autowired private BlocoRepository blocoRepo;
    @Autowired private ProvaRepository provaRepo;

    private Usuario aluno;
    private Questao questaoOriginal;
    private Bloco blocoTeste;
    private Prova provaTeste;

    @BeforeEach
    void setup() {
        // 1. CRIA O USUÁRIO
        aluno = new Usuario();
        aluno.setNome("Tester da Silva");
        aluno.setEmail("teste@darcy.com");
        aluno.setSenha("123");
        aluno.setMatricula("20259999");
        aluno = usuarioRepo.save(aluno);

        // 2. CRIA A PROVA
        provaTeste = new Prova();
        provaTeste.setAno(2023);
        provaTeste.setEtapa(1);
        provaTeste = provaRepo.save(provaTeste);

        // 3. CRIA O BLOCO
        blocoTeste = new Bloco();
        blocoTeste.setTextoBase("Texto base para o teste de integração.");
        blocoTeste.setProva(provaTeste);
        blocoTeste = blocoRepo.save(blocoTeste);

        // 4. CRIA A QUESTÃO
        questaoOriginal = new Questao();
        questaoOriginal.setEnunciado("O céu é verde.");
        questaoOriginal.setGabarito("E");

        // --- CORREÇÃO: ADICIONANDO O NÚMERO OBRIGATÓRIO ---
        questaoOriginal.setNumero(1);

        questaoOriginal.setTipo(TipoQuestao.A);
        questaoOriginal.setTags("NATUREZA");

        // VÍNCULOS
        questaoOriginal.setBloco(blocoTeste);
        questaoOriginal.setProva(provaTeste);

        questaoOriginal = questaoRepo.save(questaoOriginal);
    }

    @Test
    @DisplayName("1. Deve criar RegistroErro quando aluno erra no modo Normal")
    void testeCriacaoErro() {
        algoritmoService.processarErro(aluno, questaoOriginal, false, 60L, Resolucao.NivelDificuldade.DIFICIL);

        Optional<RegistroErro> erro = erroRepo.findByUsuarioAndQuestaoOriginal(aluno, questaoOriginal);

        assertTrue(erro.isPresent(), "O registro de erro deveria ter sido criado.");
        assertEquals(50, erro.get().getTemperatura());
        assertEquals(RegistroErro.StatusCiclo.PENDENTE_TRIAGEM, erro.get().getStatus());
    }

    @Test
    @DisplayName("2. GeminiService deve inverter gabarito no Mock")
    void testeMutacaoIA() {
        Questao mutante = geminiService.refatorarQuestao(questaoOriginal);

        assertNotNull(mutante);
        assertEquals("C", mutante.getGabarito(), "O Mock deveria ter invertido o gabarito de E para C.");
        assertTrue(mutante.getEnunciado().contains("PROTOCOLO"), "O texto deveria ter a marcação da IA.");
    }

    @Test
    @DisplayName("3. CURA: Temperatura deve cair para 10 se acertar no Expurgo")
    void testeCuraExpurgo() {
        RegistroErro registro = new RegistroErro();
        registro.setUsuario(aluno);
        registro.setQuestaoOriginal(questaoOriginal);
        registro.setTemperatura(50);
        erroRepo.save(registro);

        Simulado simuladoExpurgo = new Simulado();
        simuladoExpurgo.setUsuario(aluno);
        simuladoExpurgo.setModo(Simulado.ModoExecucao.APRENDIZADO);
        simuladoExpurgo.setTipo("PROTOCOLO_IA");

        Resolucao resolucao = new Resolucao();
        resolucao.setSimulado(simuladoExpurgo);
        resolucao.setQuestao(questaoOriginal);
        resolucao.setCorreta(true);

        simuladoService.processarErrosDoSimulado(simuladoExpurgo, List.of(resolucao));

        RegistroErro erroPosTeste = erroRepo.findByUsuarioAndQuestaoOriginal(aluno, questaoOriginal).orElseThrow();
        assertEquals(10, erroPosTeste.getTemperatura());
    }

    @Test
    @DisplayName("4. PENALIDADE: Temperatura deve subir para 100 se errar no Expurgo")
    void testePenalidadeExpurgo() {
        RegistroErro registro = new RegistroErro();
        registro.setUsuario(aluno);
        registro.setQuestaoOriginal(questaoOriginal);
        registro.setTemperatura(50);
        erroRepo.save(registro);

        Simulado simuladoExpurgo = new Simulado();
        simuladoExpurgo.setUsuario(aluno);
        simuladoExpurgo.setModo(Simulado.ModoExecucao.APRENDIZADO);
        simuladoExpurgo.setTipo("CENTRAL_IA");

        Resolucao resolucao = new Resolucao();
        resolucao.setSimulado(simuladoExpurgo);
        resolucao.setQuestao(questaoOriginal);
        resolucao.setCorreta(false);

        simuladoService.processarErrosDoSimulado(simuladoExpurgo, List.of(resolucao));

        RegistroErro erroPosTeste = erroRepo.findByUsuarioAndQuestaoOriginal(aluno, questaoOriginal).orElseThrow();
        assertEquals(100, erroPosTeste.getTemperatura());
    }
    @Test
    @DisplayName("5. ISOLAMENTO: Garante que a IA não mistura questões diferentes")
    void testeIsolamentoDeMultiplasQuestoes() {
        // 1. Cria Questão 1 (Matemática - Gabarito E)
        Questao q1 = new Questao();
        q1.setEnunciado("Quanto é 2 + 2?");
        q1.setGabarito("E"); // Errado (no nosso exemplo)
        q1.setTipo(TipoQuestao.A);
        q1.setNumero(10);
        q1.setBloco(blocoTeste);
        q1.setProva(provaTeste);
        q1 = questaoRepo.save(q1);

        // 2. Cria Questão 2 (História - Gabarito C)
        Questao q2 = new Questao();
        q2.setEnunciado("Quem descobriu o Brasil?");
        q2.setGabarito("C"); // Certo
        q2.setTipo(TipoQuestao.A);
        q2.setNumero(11);
        q2.setBloco(blocoTeste);
        q2.setProva(provaTeste);
        q2 = questaoRepo.save(q2);

        // 3. O Gemini processa as duas
        Questao q1Mutante = geminiService.refatorarQuestao(q1);
        Questao q2Mutante = geminiService.refatorarQuestao(q2);

        // 4. VERIFICAÇÃO DE IDENTIDADE

        // Q1 era E, o Mock deve transformar em C
        assertEquals("C", q1Mutante.getGabarito(), "A IA errou o processamento da Questão 1");

        // Q2 era C, o Mock deve transformar em E
        assertEquals("E", q2Mutante.getGabarito(), "A IA errou o processamento da Questão 2");

        // Verifica se os textos não se misturaram
        assertTrue(q1Mutante.getEnunciado().contains("2 + 2"), "O texto da Q1 sumiu!");
        assertTrue(q2Mutante.getEnunciado().contains("Brasil"), "O texto da Q2 sumiu!");

        // Garante que o ID foi preservado corretamente no retorno do objeto
        assertEquals(q1.getId(), q1Mutante.getId(), "A IA perdeu o ID da Questão 1");
        assertEquals(q2.getId(), q2Mutante.getId(), "A IA perdeu o ID da Questão 2");
    }
}