package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.BugTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugTrackerRepository extends JpaRepository<BugTracker, Long> {

    // Busca bugs não resolvidos (A lista de tarefas do Dev)
    List<BugTracker> findByResolvidoFalseOrderByDataReportDesc();

    // Conta quantos bugs estão abertos
    long countByResolvidoFalse();

    // (Opcional) Se você quiser filtrar por severidade no futuro
    // long countBySeveridadeAndResolvidoFalse(String severidade);
}