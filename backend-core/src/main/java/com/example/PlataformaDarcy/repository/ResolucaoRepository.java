package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Resolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResolucaoRepository extends JpaRepository<Resolucao, Long> {

    List<Resolucao> findBySimuladoIdOrderByIdAsc(Long simuladoId);

}