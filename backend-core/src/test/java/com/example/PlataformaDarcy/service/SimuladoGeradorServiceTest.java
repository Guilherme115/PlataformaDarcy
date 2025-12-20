package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.*;
import com.example.PlataformaDarcy.model.dto.SlotDefinition;
import com.example.PlataformaDarcy.repository.*;
import com.example.PlataformaDarcy.service.blueprint.BlueprintFactory;
import com.example.PlataformaDarcy.service.blueprint.BlueprintInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SimuladoGeradorServiceTest {

    @Autowired private SimuladoGeradorService simuladoGeradorService;
    @Autowired private ProvaRepository provaRepository;
    @Autowired private BlocoRepository blocoRepository;
    @Autowired private QuestaoRepository questaoRepository;
    @Autowired private JdbcTemplate db;

    @MockitoBean private GeminiService geminiServiceMock;
    @MockitoBean private BlueprintFactory blueprintFactoryMock;

    @Test
    @DisplayName("Cenário Complexo: Fluxo completo com múltiplos Slots e Limpeza de Markdown")
    void testFluxoCompletoComSanitizacao() {
        // 1. LIMPEZA TOTAL (Garante estado zero)
        questaoRepository.deleteAll();
        blocoRepository.deleteAll();
        provaRepository.deleteAll();
        db.update("DELETE FROM controle_uso_obras");

        // 2. PREPARAÇÃO (GIVEN)
        BlueprintInterface blueprintFake = Mockito.mock(BlueprintInterface.class);

        // SLOT 1: Obras
        SlotDefinition slotObra = new SlotDefinition();
        slotObra.setNomeSlot("Analise de Obra Literaria");
        slotObra.setFonteDados("OBRAS");
        slotObra.setTagsObrigatorias(List.of("LITERATURA"));
        slotObra.setQuantidadeItens(2);
        slotObra.setPromptInstrucao("Analise esta obra..."); // <--- OBRIGATÓRIO PARA O MOCK FUNCIONAR

        // SLOT 2: Temas
        SlotDefinition slotGramatica = new SlotDefinition();
        slotGramatica.setNomeSlot("Analise Gramatical");
        slotGramatica.setFonteDados("TEMAS");
        slotGramatica.setTagsObrigatorias(List.of("PORTUGUES"));
        slotGramatica.setQuantidadeItens(1);
        slotGramatica.setPromptInstrucao("Crie uma questao..."); // <--- OBRIGATÓRIO

        Mockito.when(blueprintFake.getReceita()).thenReturn(List.of(slotObra, slotGramatica));
        Mockito.when(blueprintFactoryMock.getBlueprint(anyInt())).thenReturn(blueprintFake);

        // JSONs de Resposta (Simulando IA)
        String jsonSujoObra = """
            ```json
            {
              "texto_base_gerado": "Texto sobre Obra Literaria...",
              "itens": [
                { "tipo": "C", "enunciado": "Item 1", "gabarito": "C" },
                { "tipo": "C", "enunciado": "Item 2", "gabarito": "E" }
              ]
            }
            ```
            """;

        String jsonLimpoGramatica = """
            {
              "texto_base_gerado": "Texto sobre Gramatica...",
              "itens": [
                { "tipo": "A", "enunciado": "Item 3", "gabarito": "B" }
              ]
            }
            """;

        // IMPORTANTE: O Mockito só responde se os argumentos não forem nulos.
        // Como agora preenchemos 'promptInstrucao' e corrigimos o 'contexto', vai funcionar.
        Mockito.when(geminiServiceMock.gerarConteudoBloco(anyString(), anyString(), anyInt()))
                .thenReturn(jsonSujoObra)
                .thenReturn(jsonLimpoGramatica);

        // 3. EXECUÇÃO (WHEN)
        simuladoGeradorService.gerarSimuladoOficial(1);

        // 4. VALIDAÇÕES (THEN)
        List<Prova> provas = provaRepository.findAll();
        assertThat(provas).hasSize(1);

        List<Bloco> blocos = blocoRepository.findAll();
        assertThat(blocos).hasSize(2); // 1 de Obra + 1 de Gramática

        List<Questao> questoes = questaoRepository.findAll();
        assertThat(questoes).hasSize(3); // 2 + 1

        // Verifica se o texto base foi salvo corretamente (sem o markdown)
        boolean textoSalvo = blocos.stream().anyMatch(b -> b.getTextoBase().contains("Obra Literaria"));
        assertThat(textoSalvo).isTrue();

        System.out.println("✅ TESTE COMPLEXO PASSOU COM SUCESSO!");
    }
}