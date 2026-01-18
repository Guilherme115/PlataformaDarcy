package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Colecao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColecaoRepository extends JpaRepository<Colecao, Long> {

    // Buscar apenas coleções ativas
    List<Colecao> findByAtivaTrue();

    // Buscar todas ordenadas por ordem
    List<Colecao> findAllByOrderByOrdemAsc();

    // Buscar ativas ordenadas por ordem
    List<Colecao> findByAtivaTrueOrderByOrdemAsc();

    // Buscar por criador
    List<Colecao> findByCriadoPorId(Long usuarioId);
}
