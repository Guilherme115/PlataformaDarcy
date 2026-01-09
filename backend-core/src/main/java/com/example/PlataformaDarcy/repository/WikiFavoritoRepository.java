package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.model.WikiFavorito;
import com.example.PlataformaDarcy.model.WikiPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WikiFavoritoRepository extends JpaRepository<WikiFavorito, Long> {

    List<WikiFavorito> findByUsuarioOrderByDataFavoritoDesc(Usuario usuario);

    Optional<WikiFavorito> findByUsuarioAndPost(Usuario usuario, WikiPost post);

    boolean existsByUsuarioAndPost(Usuario usuario, WikiPost post);

    void deleteByUsuarioAndPost(Usuario usuario, WikiPost post);

    long countByPost(WikiPost post);
}
