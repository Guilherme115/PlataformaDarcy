package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.dto.SimuladoResultadoDTO;
import com.example.PlataformaDarcy.model.Resolucao;
import com.example.PlataformaDarcy.model.Simulado;
import com.example.PlataformaDarcy.repository.ResolucaoRepository;
import com.example.PlataformaDarcy.repository.SimuladoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuditoriaService {

    @Autowired private SimuladoRepository simuladoRepo;
    @Autowired private ResolucaoRepository resolucaoRepo;

    public SimuladoResultadoDTO gerarRelatorio(Long simuladoId) {
        Simulado s = simuladoRepo.findById(simuladoId).orElseThrow();
        List<Resolucao> res = resolucaoRepo.findBySimuladoIdOrderByIdAsc(simuladoId);

        long acertos = res.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        long erros = res.stream().filter(r -> Boolean.FALSE.equals(r.getCorreta()) && r.getRespostaAluno() != null).count();
        long brancos = res.stream().filter(r -> r.getRespostaAluno() == null).count();

        // Soma segura do tempo (evita NullPointerException)
        long tempoTotalSeg = res.stream()
                .mapToLong(r -> r.getTempoSegundos() != null ? r.getTempoSegundos() : 0L)
                .sum();

        // AGRUPAMENTO INTELIGENTE (SEM CAMPO 'NOME' NO BLOCO)
        Map<String, List<Resolucao>> porMateria = res.stream()
                .collect(Collectors.groupingBy(r -> identificarMateria(r)));

        List<SimuladoResultadoDTO.MateriaEstatistica> ranking = new ArrayList<>();

        porMateria.forEach((nome, lista) -> {
            double pts = 0;
            long mAcertos = 0, mErros = 0, mBranco = 0;
            for (Resolucao r : lista) {
                if (r.getRespostaAluno() == null) {
                    mBranco++;
                } else if (Boolean.TRUE.equals(r.getCorreta())) {
                    pts += 1.0;
                    mAcertos++;
                } else {
                    pts -= 1.0; // Penalidade CESPE
                    mErros++;
                }
            }
            ranking.add(SimuladoResultadoDTO.MateriaEstatistica.builder()
                    .nome(nome)
                    .pontos(pts)
                    .acertos(mAcertos)
                    .erros(mErros)
                    .branco(mBranco)
                    .build());
        });

        // Ordena: matérias com pior desempenho (mais erros) primeiro
        ranking.sort((a, b) -> Long.compare(b.getErros(), a.getErros()));

        return SimuladoResultadoDTO.builder()
                .simulado(s)
                .resolucoes(res)
                .statsAcertos(acertos)
                .statsErros(erros)
                .statsBranco(brancos)
                .possiveisPontos(res.size())
                .tempoTotal(formatar(tempoTotalSeg))
                .tempoMedio(formatar(res.isEmpty() ? 0 : tempoTotalSeg / res.size()))
                .rankingMaterias(ranking)
                .build();
    }

    // Lógica para descobrir a matéria baseada nas Tags
    private String identificarMateria(Resolucao r) {
        String tags = r.getQuestao().getTags();
        if (tags == null || tags.isBlank()) return "GERAL";

        // Exemplo de tag: "FÍSICA, DINÂMICA, LEIS DE NEWTON"
        // Pega a primeira palavra antes da vírgula
        String primeiraTag = tags.split(",")[0].trim().toUpperCase();

        // Opcional: Normalizar nomes comuns
        if (primeiraTag.contains("MATEM")) return "MATEMÁTICA";
        if (primeiraTag.contains("FIS") || primeiraTag.contains("FÍS")) return "FÍSICA";
        if (primeiraTag.contains("QUIM") || primeiraTag.contains("QUÍ")) return "QUÍMICA";
        if (primeiraTag.contains("BIO")) return "BIOLOGIA";
        if (primeiraTag.contains("HIST")) return "HISTÓRIA";
        if (primeiraTag.contains("GEO")) return "GEOGRAFIA";
        if (primeiraTag.contains("FILO")) return "FILOSOFIA";
        if (primeiraTag.contains("SOCIO")) return "SOCIOLOGIA";
        if (primeiraTag.contains("PORT") || primeiraTag.contains("LIT")) return "PORTUGUÊS";
        if (primeiraTag.contains("ART")) return "ARTES";
        if (primeiraTag.contains("ING") || primeiraTag.contains("ESP") || primeiraTag.contains("FRAN")) return "LÍNGUA ESTRANGEIRA";

        return primeiraTag;
    }

    private String formatar(long seg) {
        return String.format("%02d:%02d:%02d", seg / 3600, (seg % 3600) / 60, seg % 60);
    }
}