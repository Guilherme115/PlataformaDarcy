package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.PaginaLivro;
import com.example.PlataformaDarcy.model.LayoutPagina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaginaLivroRepository extends JpaRepository<PaginaLivro, Long> {

    // Buscar páginas de um volume ordenadas por número
    List<PaginaLivro> findByVolumeIdOrderByNumeroPaginaAsc(Long volumeId);

    // Buscar apenas páginas ativas de um volume
    List<PaginaLivro> findByVolumeIdAndAtivaTrue(Long volumeId);

    // Buscar páginas ativas ordenadas
    List<PaginaLivro> findByVolumeIdAndAtivaTrueOrderByNumeroPaginaAsc(Long volumeId);

    // Buscar página específica por volume e número
    Optional<PaginaLivro> findByVolumeIdAndNumeroPagina(Long volumeId, Integer numeroPagina);

    // Buscar páginas por layout
    List<PaginaLivro> findByVolumeIdAndLayout(Long volumeId, LayoutPagina layout);

    // Contar páginas de um volume
    long countByVolumeId(Long volumeId);

    // Buscar última página de um volume (maior número)
    @Query("SELECT MAX(p.numeroPagina) FROM PaginaLivro p WHERE p.volume.id = :volumeId")
    Integer findMaxNumeroPaginaByVolumeId(Long volumeId);

    // Buscar páginas editadas por um usuário
    List<PaginaLivro> findByUltimaEdicaoPorId(Long usuarioId);
}
