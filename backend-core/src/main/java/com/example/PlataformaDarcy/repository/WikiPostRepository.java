package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.model.WikiPost;
import com.example.PlataformaDarcy.model.WikiPost.TipoConteudo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WikiPostRepository extends JpaRepository<WikiPost, Long> {

    // --- DESTAQUES (HOME) ---
    List<WikiPost> findTop3ByEtapaAndRascunhoFalseOrderByCurtidasDesc(Integer etapa);

    List<WikiPost> findTop6ByRascunhoFalseOrderByVisualizacoesDesc(); // Populares

    List<WikiPost> findTop6ByRascunhoFalseOrderByDataCriacaoDesc(); // Recentes

    // --- EXPLORAR (FILTROS PÚBLICOS) ---
    List<WikiPost> findByEtapaAndRascunhoFalseOrderByCurtidasDesc(Integer etapa);

    List<WikiPost> findByDisciplinaAndRascunhoFalseOrderByCurtidasDesc(String disciplina);

    List<WikiPost> findByTopicoAndRascunhoFalseOrderByCurtidasDesc(String topico);

    List<WikiPost> findByTipoConteudoAndRascunhoFalseOrderByVisualizacoesDesc(TipoConteudo tipo);

    // --- CATÁLOGO PAGINADO ---
    Page<WikiPost> findByRascunhoFalseOrderByDataCriacaoDesc(Pageable pageable);

    Page<WikiPost> findByDisciplinaAndRascunhoFalseOrderByDataCriacaoDesc(String disciplina, Pageable pageable);

    // --- BUSCA ---
    @Query("SELECT w FROM WikiPost w WHERE w.rascunho = false AND " +
            "(LOWER(w.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(w.topico) LIKE LOWER(CONCAT('%', :termo, '%')))")
    List<WikiPost> buscarPorTermo(@Param("termo") String termo);

    // --- MEUS POSTS (AUTOR) ---
    List<WikiPost> findByAutorOrderByDataCriacaoDesc(Usuario autor);

    List<WikiPost> findByAutorAndRascunhoTrueOrderByDataAtualizacaoDesc(Usuario autor); // Drafts

    // --- RELACIONADOS ---
    List<WikiPost> findTop5ByDisciplinaAndIdNotAndRascunhoFalseOrderByCurtidasDesc(String disciplina, Long id);

    // --- LEGACY (mantém compatibilidade) ---
    default List<WikiPost> findTop3ByEtapaOrderByCurtidasDesc(Integer etapa) {
        return findTop3ByEtapaAndRascunhoFalseOrderByCurtidasDesc(etapa);
    }

    default List<WikiPost> findByEtapaOrderByCurtidasDesc(Integer etapa) {
        return findByEtapaAndRascunhoFalseOrderByCurtidasDesc(etapa);
    }

    default List<WikiPost> findByDisciplinaOrderByCurtidasDesc(String disciplina) {
        return findByDisciplinaAndRascunhoFalseOrderByCurtidasDesc(disciplina);
    }

    default List<WikiPost> findByTopicoOrderByCurtidasDesc(String topico) {
        return findByTopicoAndRascunhoFalseOrderByCurtidasDesc(topico);
    }
}