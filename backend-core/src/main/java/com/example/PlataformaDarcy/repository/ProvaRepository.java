package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Prova;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProvaRepository extends JpaRepository<Prova, Long> {

    List<Prova> findAllByEtapaOrderByAnoDesc(Integer etapa);

    @Query("SELECT DISTINCT p.ano FROM Prova p WHERE p.etapa = :etapa ORDER BY p.ano DESC")
    List<Integer> findAnosByEtapa(@Param("etapa") Integer etapa);

    List<Prova> findByOrigemOrderByAnoDesc(String origem);

    // Métodos para gestão de Simulados Oficiais
    List<Prova> findByOrigemAndAtivoTrueOrderByIdDesc(String origem);

    List<Prova> findByOrigemOrderByIdDesc(String origem);
}