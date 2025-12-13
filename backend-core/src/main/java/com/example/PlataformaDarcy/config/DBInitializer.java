package com.example.PlataformaDarcy.config;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DBInitializer implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepo.findByMatricula("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setMatricula("admin");
            admin.setSenha(passwordEncoder.encode("123456"));
            admin.setNome("Administrador Darcy");

            admin.setEmail("admin@darcy.unb.br");

            admin.setPerfil("ADMIN");
            usuarioRepo.save(admin);
            System.out.println("✅ USUÁRIO ADMIN CRIADO: Login: admin / Senha: 123456");
        }
    }
}