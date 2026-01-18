package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.NotaMarginal;
import com.example.PlataformaDarcy.model.TipoNotaMarginal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaMarginalRepository extends JpaRepository<NotaMarginal, Long> {

    // Buscar notas de uma página ordenadas
    List<NotaMarginal> findByPaginaIdOrderByOrdemAsc(Long paginaId);

    // Buscar notas por tipo
    List<NotaMarginal> findByPaginaIdAndTipo(Long paginaId, TipoNotaMarginal tipo);

    // Contar notas de uma página
    long countByPaginaId(Long paginaId);

    // Deletar todas as notas de uma página
    void deleteByPaginaId(Long paginaId);
}
