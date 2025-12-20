package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AutenticacaoService.class);

    @Autowired
    private UsuarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(String matricula) throws UsernameNotFoundException {
        logger.info("🔐 LOGIN: Recebida tentativa de login para matrícula: '{}'", matricula);

        return repository.findByMatricula(matricula)
                .map(usuario -> {
                    logger.info("✅ LOGIN: Usuário encontrado no banco! ID: {}, Perfil: {}", usuario.getId(), usuario.getPerfil());
                    return usuario;
                })
                .orElseThrow(() -> {
                    logger.warn("❌ LOGIN FALHOU: Matrícula '{}' não existe no banco de dados.", matricula);
                    return new UsernameNotFoundException("Usuário não encontrado: " + matricula);
                });
    }
}