package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.ListaImpressao;
import com.example.PlataformaDarcy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListaImpressaoRepository extends JpaRepository<ListaImpressao, Long> {

    List<ListaImpressao> findByUsuarioOrderByGeradoEmDesc(Usuario usuario);

    List<ListaImpressao> findByUsuarioAndEtapaOrderByGeradoEmDesc(Usuario usuario, Integer etapa);

    long countByUsuario(Usuario usuario);
}
