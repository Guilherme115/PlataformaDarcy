package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.ComentarioComunidade;
import com.example.PlataformaDarcy.model.PostComunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioComunidadeRepository extends JpaRepository<ComentarioComunidade, Long> {

    // Comentários de um post (mais antigos primeiro)
    List<ComentarioComunidade> findByPostAndAtivoTrueOrderByCriadoEmAsc(PostComunidade post);

    // Contagem de comentários ativos
    long countByPostAndAtivoTrue(PostComunidade post);
}
