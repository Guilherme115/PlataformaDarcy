package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.PasswordResetToken;
import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.PasswordResetTokenRepository;
import com.example.PlataformaDarcy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private PasswordResetTokenRepository tokenRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- REGISTRO DE NOVO ALUNO ---
    public void registrarEstudante(String nome, String matricula, String email, String senha) throws Exception {
        if (usuarioRepo.findByMatricula(matricula).isPresent()) {
            throw new Exception("Matrícula já cadastrada.");
        }
        if (usuarioRepo.findByEmail(email).isPresent()) {
            throw new Exception("E-mail já cadastrado.");
        }

        Usuario u = new Usuario();
        u.setNome(nome);
        u.setMatricula(matricula);
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(senha));
        u.setPerfil("ESTUDANTE");
        usuarioRepo.save(u);
    }

    public void processarEsqueciSenha(String email) {
        usuarioRepo.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken myToken = new PasswordResetToken(token, user);
            tokenRepo.save(myToken);

            System.out.println("==================================================");
            System.out.println("📨 [EMAIL SIMULADO] Para: " + email);
            System.out.println("🔗 Link de Recuperação: http://localhost:8080/reset-password?token=" + token);
            System.out.println("==================================================");
        });
    }

    public boolean validarToken(String token) {
        return tokenRepo.findByToken(token)
                .map(t -> !t.isExpired())
                .orElse(false);
    }

    @Transactional
    public void atualizarSenhaComToken(String token, String novaSenha) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        Usuario user = resetToken.getUsuario();
        user.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepo.save(user);

        tokenRepo.delete(resetToken); // Queima o token após uso
    }
}