package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.ConteudoProgramatico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ConteudoProgramaticoRepository extends JpaRepository<ConteudoProgramatico, Long> {

    /**
     * Busca todos os conteúdos de uma etapa, ordenados por matéria e ordem
     */
    List<ConteudoProgramatico> findByEtapaOrderByMateriaAscOrdemAsc(Integer etapa);

    /**
     * Busca conteúdos de uma etapa e matéria específicas
     */
    List<ConteudoProgramatico> findByEtapaAndMateriaOrderByOrdemAsc(Integer etapa, String materia);

    /**
     * Remove todos os conteúdos de uma etapa
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ConteudoProgramatico c WHERE c.etapa = ?1")
    void deleteAllByEtapa(Integer etapa);

    /**
     * Remove todos os conteúdos de uma etapa e matéria específicas
     */
    @Modifying
    @Transactional
    void deleteByEtapaAndMateria(Integer etapa, String materia);

    /**
     * Conta quantos tópicos existem em uma etapa
     */
    long countByEtapa(Integer etapa);
}
