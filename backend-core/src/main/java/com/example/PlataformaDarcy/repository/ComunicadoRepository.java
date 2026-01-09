package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Comunicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComunicadoRepository extends JpaRepository<Comunicado, Long> {

    // Para o Feed do Aluno (Apenas ativos, do mais novo pro mais velho)
    List<Comunicado> findByAtivoTrueOrderByDataEnvioDesc();

    // Para o Admin (Histórico completo)
    List<Comunicado> findAllByOrderByDataEnvioDesc();
}