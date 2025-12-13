package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.Simulado;
import com.example.PlataformaDarcy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimuladoRepository extends JpaRepository<Simulado, Long> {
    Optional<Simulado> findFirstByUsuarioAndDataFimIsNullOrderByDataInicioDesc(Usuario usuario);

    List<Simulado> findByUsuarioOrderByDataInicioDesc(Usuario usuario);
}