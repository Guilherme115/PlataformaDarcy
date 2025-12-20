package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.PasswordResetToken;
import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.PasswordResetTokenRepository;
import com.example.PlataformaDarcy.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private PasswordResetTokenRepository tokenRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    public Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // LOG 1: Verificar se existe algo no contexto
        if (authentication == null) {
            logger.error("❌ getUsuarioLogado: Contexto de autenticação está NULO.");
            throw new UsernameNotFoundException("Nenhum usuário autenticado.");
        }

        logger.info("🔍 getUsuarioLogado: Verificando sessão para Principal: [{}]", authentication.getPrincipal());

        if (!authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("⚠️ getUsuarioLogado: Usuário anônimo ou não autenticado.");
            throw new UsernameNotFoundException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        String loginBusca;

        if (principal instanceof UserDetails) {
            // O getUsername() do UserDetails DEVE retornar a matrícula (conforme seu Usuario.java)
            loginBusca = ((UserDetails) principal).getUsername();
            logger.info("✅ getUsuarioLogado: Principal é UserDetails. Login extraído: '{}'", loginBusca);
        } else if (principal instanceof Usuario) {
            loginBusca = ((Usuario) principal).getMatricula();
            logger.info("✅ getUsuarioLogado: Principal já é Objeto Usuario. Login: '{}'", loginBusca);
            return (Usuario) principal;
        } else {
            loginBusca = principal.toString();
            logger.info("⚠️ getUsuarioLogado: Principal é tipo desconhecido (String?). Login: '{}'", loginBusca);
        }

        // LOG CRÍTICO: Mostra qual busca será feita no banco
        logger.info("🔄 getUsuarioLogado: Buscando no banco pela MATRÍCULA: '{}'", loginBusca);

        // AQUI ESTAVA O ERRO ANTES: Buscava por email, mas 'loginBusca' era 'admin'
        return usuarioRepo.findByMatricula(loginBusca)
                .orElseGet(() -> {
                    logger.error("❌ ERRO FATAL: Usuário '{}' está autenticado na memória (Sessão), mas NÃO existe no banco com essa MATRÍCULA.", loginBusca);
                    throw new UsernameNotFoundException("Sessão inconsistente: Usuário não encontrado.");
                });
    }

    // --- MÉTODOS DE REGISTRO (Mantidos) ---
    public void registrarEstudante(String nome, String matricula, String email, String senha) throws Exception {
        logger.info("📝 Tentativa de registro: Matrícula '{}', Email '{}'", matricula, email);
        if (usuarioRepo.findByMatricula(matricula).isPresent()) throw new Exception("Matrícula já cadastrada.");
        if (usuarioRepo.findByEmail(email).isPresent()) throw new Exception("E-mail já cadastrado.");

        Usuario u = new Usuario();
        u.setNome(nome);
        u.setMatricula(matricula);
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(senha));
        u.setPerfil("ESTUDANTE");
        usuarioRepo.save(u);
        logger.info("✅ Usuário '{}' registrado com sucesso.", matricula);
    }

    public void processarEsqueciSenha(String email) {
        logger.info("🔑 Processando esqueci senha para: {}", email);
        usuarioRepo.findByEmail(email).ifPresentOrElse(user -> {
            String token = UUID.randomUUID().toString();
            tokenRepo.save(new PasswordResetToken(token, user));
            System.out.println("🔗 Link Reset: http://localhost:8080/reset-password?token=" + token);
        }, () -> logger.warn("⚠️ Esqueci senha: Email '{}' não encontrado.", email));
    }

    public boolean validarToken(String token) {
        return tokenRepo.findByToken(token).map(t -> !t.isExpired()).orElse(false);
    }

    @Transactional
    public void atualizarSenhaComToken(String token, String novaSenha) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));
        Usuario user = resetToken.getUsuario();
        user.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepo.save(user);
        tokenRepo.delete(resetToken);
        logger.info("🔒 Senha atualizada via token para: {}", user.getMatricula());
    }
}