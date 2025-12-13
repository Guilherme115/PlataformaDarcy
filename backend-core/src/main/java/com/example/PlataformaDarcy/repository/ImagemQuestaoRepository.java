package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.ImagemQuestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagemQuestaoRepository extends JpaRepository<ImagemQuestao, Long> {
}