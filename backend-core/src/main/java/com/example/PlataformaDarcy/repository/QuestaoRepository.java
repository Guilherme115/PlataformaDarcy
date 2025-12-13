package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Questao;
import com.example.PlataformaDarcy.model.StatusRevisao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    Optional<Questao> findFirstByStatusOrderByIdAsc(StatusRevisao status);
    List<Questao> findByTagsContainingIgnoreCase(String tag);

    List<Questao> findByProvaIdOrderByNumeroAsc(Long provaId);

    @Query(value = """
        SELECT q.* FROM questoes q
        JOIN provas p ON q.prova_id = p.id
        WHERE q.status = 'REVISADO'
        AND (:etapa IS NULL OR p.etapa = :etapa)
        AND (:tipo IS NULL OR q.tipo = :tipo)
        AND (:tag IS NULL OR q.tags LIKE CONCAT('%', :tag, '%'))
        ORDER BY RAND()
        LIMIT :qtd
        """, nativeQuery = true)
    List<Questao> gerarSimuladoAvancado(@Param("etapa") Integer etapa,
                                        @Param("tipo") String tipo,
                                        @Param("tag") String tag,
                                        @Param("qtd") int qtd);
}