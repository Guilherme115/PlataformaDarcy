package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {

    // Buscar volumes de um livro
    List<Volume> findByLivroId(Long livroId);

    // Buscar apenas volumes ativos de um livro
    List<Volume> findByLivroIdAndAtivoTrue(Long livroId);

    // Buscar volumes de um livro ordenados por número
    List<Volume> findByLivroIdOrderByNumeroAsc(Long livroId);

    // Buscar volumes ativos de um livro ordenados
    List<Volume> findByLivroIdAndAtivoTrueOrderByNumeroAsc(Long livroId);

    // Buscar volume específico por livro e número
    Optional<Volume> findByLivroIdAndNumero(Long livroId, Integer numero);

    // Contar volumes de um livro
    long countByLivroId(Long livroId);
}
