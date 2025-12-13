package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Override
    public UserDetails loadUserByUsername(String matricula) throws UsernameNotFoundException {
        return usuarioRepo.findByMatricula(matricula)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + matricula));
    }
}