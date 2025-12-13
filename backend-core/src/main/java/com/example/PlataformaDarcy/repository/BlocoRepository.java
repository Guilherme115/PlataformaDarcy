package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Bloco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlocoRepository extends JpaRepository<Bloco, Long> {

    @Query("SELECT DISTINCT b FROM Bloco b JOIN b.prova p WHERE p.etapa = :etapa AND p.ano = :ano ORDER BY b.id ASC")
    List<Bloco> findAllByProvaEtapaAndAno(@Param("etapa") Integer etapa, @Param("ano") Integer ano);

    @Query("SELECT DISTINCT b FROM Bloco b JOIN b.questoes q WHERE q.status = 'PENDENTE' ORDER BY b.id ASC LIMIT 1")
    Optional<Bloco> findFirstBlocoComPendencias();
}