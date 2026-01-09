package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.ReportConteudo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportConteudoRepository extends JpaRepository<ReportConteudo, Long> {

    // Busca todos os reports pendentes (para o Admin ver o que falta moderar)
    List<ReportConteudo> findByStatusOrderByDataReportDesc(ReportConteudo.StatusReport status);

    // Conta quantos pendentes existem (para as bolinhas de notificação)
    long countByStatus(ReportConteudo.StatusReport status);

    // Busca reports de uma questão específica (para ver histórico de erro da questão)
    List<ReportConteudo> findByQuestaoId(Long questaoId);
}