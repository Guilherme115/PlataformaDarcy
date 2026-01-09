package com.example.PlataformaDarcy.repository;

import com.example.PlataformaDarcy.model.RegistroErro;
import com.example.PlataformaDarcy.model.Questao;
import com.example.PlataformaDarcy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RegistroErroRepository extends JpaRepository<RegistroErro, Long> {

    // Verifica se já existe (usado no AlgoritmoService)
    Optional<RegistroErro> findByUsuarioAndQuestaoOriginal(Usuario usuario, Questao questao);

    // Filtra por status (usado para Triagem Pendente)
    List<RegistroErro> findByUsuarioAndStatus(Usuario usuario, RegistroErro.StatusCiclo status);

    // OTIMIZADO: Conta erros pendentes sem carregar tudo em memória
    long countByUsuarioAndStatus(Usuario usuario, RegistroErro.StatusCiclo status);

    // NOVO: Busca tudo ordenado pela urgência (Temperatura)
    List<RegistroErro> findByUsuarioOrderByTemperaturaDesc(Usuario usuario);

    // NOVO: Busca apenas os críticos (temperatura >= X)
    List<RegistroErro> findByUsuarioAndTemperaturaGreaterThanEqualOrderByTemperaturaDesc(Usuario usuario,
            Integer temperatura);

    // --- ADICIONE ESTE NOVO PARA O DASHBOARD ---
    // Busca tudo que NÃO foi expurgado ainda, ordenado por data (mais recente
    // primeiro)
    List<RegistroErro> findByUsuarioAndStatusNotOrderByDataUltimoErroDesc(Usuario usuario,
            RegistroErro.StatusCiclo status);
}