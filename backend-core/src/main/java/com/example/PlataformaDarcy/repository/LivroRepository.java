package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    // Buscar livros de uma coleção
    List<Livro> findByColecaoId(Long colecaoId);

    // Buscar apenas livros ativos de uma coleção
    List<Livro> findByColecaoIdAndAtivoTrue(Long colecaoId);

    // Buscar livros de uma coleção ordenados
    List<Livro> findByColecaoIdOrderByOrdemAsc(Long colecaoId);

    // Buscar livros ativos de uma coleção ordenados
    List<Livro> findByColecaoIdAndAtivoTrueOrderByOrdemAsc(Long colecaoId);

    // Contar livros de uma coleção
    long countByColecaoId(Long colecaoId);
}
