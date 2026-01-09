package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.CategoriaComunidade;
import com.example.PlataformaDarcy.model.PostComunidade;
import com.example.PlataformaDarcy.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostComunidadeRepository extends JpaRepository<PostComunidade, Long> {

    // Feed geral (mais recentes primeiro)
    Page<PostComunidade> findByAtivoTrueOrderByCriadoEmDesc(Pageable pageable);

    // Feed por categoria
    Page<PostComunidade> findByCategoriaAndAtivoTrueOrderByCriadoEmDesc(
            CategoriaComunidade categoria, Pageable pageable);

    // Posts de um usuário específico
    List<PostComunidade> findByAutorAndAtivoTrueOrderByCriadoEmDesc(Usuario autor);

    // Posts mais curtidos (trending)
    @Query("SELECT p FROM PostComunidade p WHERE p.ativo = true ORDER BY p.likes DESC, p.criadoEm DESC")
    Page<PostComunidade> findTrending(Pageable pageable);

    // Busca por título ou conteúdo
    @Query("SELECT p FROM PostComunidade p WHERE p.ativo = true AND " +
            "(LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(p.conteudo) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<PostComunidade> buscar(String termo, Pageable pageable);

    // Contagem por categoria
    long countByCategoriaAndAtivoTrue(CategoriaComunidade categoria);
}
