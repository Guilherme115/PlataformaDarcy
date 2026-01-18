package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.TemplatePagina;
import com.example.PlataformaDarcy.model.LayoutPagina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplatePaginaRepository extends JpaRepository<TemplatePagina, Long> {

    // Buscar templates públicos
    List<TemplatePagina> findByPublicoTrue();

    // Buscar templates por layout
    List<TemplatePagina> findByLayout(LayoutPagina layout);

    // Buscar templates públicos por layout
    List<TemplatePagina> findByPublicoTrueAndLayout(LayoutPagina layout);

    // Buscar templates criados por um usuário
    List<TemplatePagina> findByCriadoPorId(Long usuarioId);

    // Buscar templates públicos OU criados por um usuário específico
    @Query("SELECT t FROM TemplatePagina t WHERE t.publico = true OR t.criadoPor.id = :usuarioId")
    List<TemplatePagina> findPublicosOuDoUsuario(@Param("usuarioId") Long usuarioId);
}
