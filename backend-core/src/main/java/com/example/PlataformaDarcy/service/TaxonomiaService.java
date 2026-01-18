package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.ConteudoProgramatico;
import com.example.PlataformaDarcy.repository.ConteudoProgramaticoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaxonomiaService {

    @Autowired
    private ConteudoProgramaticoRepository repository;

    /**
     * Faz o parsing automático de texto formatado e retorna lista de
     * ConteudoProgramatico
     * 
     * Formato esperado:
     * *MATERIA no PAS [123]
     * Tópico 1
     * Tópico 2 (com observações)
     * Tópico 3
     * 
     * *OUTRA MATERIA no PAS [123]
     * ...
     */
    public List<ConteudoProgramatico> parseTextToTaxonomia(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Texto não pode ser vazio");
        }

        // Pattern para identificar cabeçalho de matéria: *QUÍMICA no PAS 1
        Pattern materiaPattern = Pattern.compile("^\\*([A-ZÁÉÍÓÚÂÊÔÃÕÇ\\s]+)\\s+no\\s+PAS\\s+([123])$",
                Pattern.CASE_INSENSITIVE);

        List<ConteudoProgramatico> resultados = new ArrayList<>();
        String[] linhas = texto.split("\\r?\\n");

        String materiaAtual = null;
        Integer etapaAtual = null;
        int ordem = 0;

        for (String linha : linhas) {
            linha = linha.trim();

            // Ignora linhas vazias
            if (linha.isEmpty()) {
                continue;
            }

            // Tenta identificar cabeçalho de matéria
            Matcher matcher = materiaPattern.matcher(linha);
            if (matcher.find()) {
                // Nova matéria detectada
                materiaAtual = matcher.group(1).trim().toUpperCase();
                etapaAtual = Integer.parseInt(matcher.group(2));
                ordem = 0;
                continue;
            }

            // Se já temos matéria e etapa definidas, linha é um tópico
            if (materiaAtual != null && etapaAtual != null) {
                ConteudoProgramatico cp = new ConteudoProgramatico();
                cp.setEtapa(etapaAtual);
                cp.setMateria(materiaAtual);
                cp.setOrdem(ordem++);

                // Extrai observações entre parênteses
                Pattern obsPattern = Pattern.compile("(.+?)\\s*\\((.+)\\)\\s*$");
                Matcher obsMatcher = obsPattern.matcher(linha);

                if (obsMatcher.find()) {
                    cp.setTopico(obsMatcher.group(1).trim());
                    cp.setObservacoes(obsMatcher.group(2).trim());
                } else {
                    cp.setTopico(linha);
                }

                resultados.add(cp);
            }
        }

        if (resultados.isEmpty()) {
            throw new IllegalArgumentException("Nenhum tópico válido encontrado no texto. Verifique o formato.");
        }

        return resultados;
    }

    /**
     * Salva uma lista de ConteudoProgramatico no banco
     * 
     * @param conteudos           Lista de conteúdos a salvar
     * @param substituirExistente Se true, remove todos os conteúdos da etapa antes
     *                            de salvar
     */
    @Transactional
    public void salvarTodos(List<ConteudoProgramatico> conteudos, boolean substituirExistente) {
        if (conteudos == null || conteudos.isEmpty()) {
            return;
        }

        if (substituirExistente) {
            // Agrupa por etapa e remove conteúdos existentes
            Set<Integer> etapas = new HashSet<>();
            for (ConteudoProgramatico cp : conteudos) {
                etapas.add(cp.getEtapa());
            }
            for (Integer etapa : etapas) {
                repository.deleteAllByEtapa(etapa);
            }
        }

        repository.saveAll(conteudos);
    }

    /**
     * Retorna a taxonomia em formato Map para uso no frontend
     * 
     * Formato de retorno:
     * {
     * "QUÍMICA": {
     * "Tópicos": ["Química Geral", "Leis Ponderais", ...]
     * },
     * "FÍSICA": {
     * "Tópicos": ["Cinemática", "Dinâmica", ...]
     * }
     * }
     */
    public Map<String, Map<String, List<String>>> getTaxonomyByEtapa(Integer etapa) {
        List<ConteudoProgramatico> conteudos = repository.findByEtapaOrderByMateriaAscOrdemAsc(etapa);

        Map<String, Map<String, List<String>>> taxonomy = new LinkedHashMap<>();

        for (ConteudoProgramatico cp : conteudos) {
            taxonomy.putIfAbsent(cp.getMateria(), new LinkedHashMap<>());
            taxonomy.get(cp.getMateria()).putIfAbsent("Tópicos", new ArrayList<>());
            taxonomy.get(cp.getMateria()).get("Tópicos").add(cp.getTopico());
        }

        return taxonomy;
    }

    /**
     * Lista todos os conteúdos de uma etapa
     */
    public List<ConteudoProgramatico> listarPorEtapa(Integer etapa) {
        return repository.findByEtapaOrderByMateriaAscOrdemAsc(etapa);
    }

    /**
     * Lista apenas os nomes dos tópicos de uma etapa (para sugestões no CMS)
     */
    public List<String> listarTopicosPorEtapa(Integer etapa) {
        List<ConteudoProgramatico> conteudos = repository.findByEtapaOrderByMateriaAscOrdemAsc(etapa);
        List<String> topicos = new ArrayList<>();
        for (ConteudoProgramatico cp : conteudos) {
            topicos.add(cp.getTopico());
        }
        return topicos;
    }

    /**
     * Busca um conteúdo por ID
     */
    public ConteudoProgramatico buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo não encontrado: " + id));
    }

    /**
     * Salva ou atualiza um único conteúdo
     */
    public ConteudoProgramatico salvar(ConteudoProgramatico conteudo) {
        return repository.save(conteudo);
    }

    /**
     * Exclui um conteúdo por ID
     */
    @Transactional
    public void excluir(Long id) {
        repository.deleteById(id);
    }

    /**
     * Conta quantos tópicos existem em uma etapa
     */
    public long contarPorEtapa(Integer etapa) {
        return repository.countByEtapa(etapa);
    }
}
