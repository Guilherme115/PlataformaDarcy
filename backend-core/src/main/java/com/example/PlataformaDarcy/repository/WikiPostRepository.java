package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.WikiPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WikiPostRepository extends JpaRepository<WikiPost, Long> {

    // --- MÉTODOS PARA A HOME (DESTAQUES) ---
    // Busca os 3 posts mais curtidos de uma etapa específica (ex: Destaques PAS 1)
    List<WikiPost> findTop3ByEtapaOrderByCurtidasDesc(Integer etapa);

    // --- MÉTODOS PARA A PÁGINA "EXPLORAR" (FILTROS) ---

    // Filtra só pela Etapa (ex: Tudo do PAS 3)
    List<WikiPost> findByEtapaOrderByCurtidasDesc(Integer etapa);

    // Filtra pela Matéria (ex: Todos de BIOLOGIA)
    List<WikiPost> findByDisciplinaOrderByCurtidasDesc(String disciplina);

    // Filtra pelo Tópico Específico (ex: Só sobre BARROCO)
    List<WikiPost> findByTopicoOrderByCurtidasDesc(String topico);
}