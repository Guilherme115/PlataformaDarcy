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

import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private PasswordResetTokenRepository tokenRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Recupera o usuário atualmente logado na sessão.
     */
    public Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // LOG 1: Verificar se existe algo no contexto
        if (authentication == null) {
            logger.error("❌ getUsuarioLogado: Contexto de autenticação está NULO.");
            throw new UsernameNotFoundException("Nenhum usuário autenticado.");
        }

        // logger.info("🔍 getUsuarioLogado: Verificando sessão para Principal: [{}]",
        // authentication.getPrincipal());

        if (!authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("⚠️ getUsuarioLogado: Usuário anônimo ou não autenticado.");
            throw new UsernameNotFoundException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        String loginBusca;

        if (principal instanceof UserDetails) {
            loginBusca = ((UserDetails) principal).getUsername();
        } else if (principal instanceof Usuario) {
            return (Usuario) principal;
        } else {
            loginBusca = principal.toString();
        }

        // Busca no banco pela MATRÍCULA
        return usuarioRepo.findByMatricula(loginBusca)
                .orElseGet(() -> {
                    logger.error("❌ ERRO FATAL: Usuário '{}' autenticado na memória, mas NÃO existe no banco.",
                            loginBusca);
                    throw new UsernameNotFoundException("Sessão inconsistente: Usuário não encontrado.");
                });
    }

    // --- MÉTODOS DE REGISTRO (ATUALIZADO PARA AUTOMÁTICO + REGIÃO) ---

    /**
     * Registra o estudante gerando matrícula automática e retornando-a.
     */
    public String registrarEstudante(String nome, String email, String senha, String regiao) throws Exception {
        logger.info("📝 Tentativa de registro para Email '{}', Região '{}'", email, regiao);

        // 1. Validação de Email Único
        if (usuarioRepo.findByEmail(email).isPresent()) {
            throw new Exception("E-mail já cadastrado.");
        }

        // 2. Gerar Matrícula Única (4 dígitos)
        String matriculaGerada = gerarMatriculaUnica();
        logger.info("🎲 Matrícula gerada automaticamente: {}", matriculaGerada);

        // 3. Criar e Salvar Usuário
        Usuario u = new Usuario();
        u.setNome(nome.toUpperCase()); // Caixa alta para padronizar
        u.setMatricula(matriculaGerada);
        u.setEmail(email.trim());
        u.setRegiao(regiao); // Salva a RA
        u.setSenha(passwordEncoder.encode(senha));
        u.setPerfil("ESTUDANTE");
        u.setAtivo(true);
        u.setProvider(Usuario.Provider.LOCAL);

        usuarioRepo.save(u);
        logger.info("✅ Usuário '{}' registrado com sucesso.", matriculaGerada);

        return matriculaGerada; // Retorna para exibir na tela de login
    }

    /**
     * Registra ou retorna usuário via Google
     */
    public Usuario processarLoginGoogle(String email, String nome) {
        return usuarioRepo.findByEmail(email).orElseGet(() -> {
            String matricula = gerarMatriculaUnica();
            Usuario u = new Usuario();
            u.setNome(nome.toUpperCase());
            u.setMatricula(matricula);
            u.setEmail(email);
            u.setSenha(passwordEncoder.encode("GOOGLE_AUTH_NO_PASS_" + UUID.randomUUID())); // Senha dummy
            u.setPerfil("ESTUDANTE");
            u.setRegiao("NÃO INFORMADA"); // Google não retorna região por padrão
            u.setAtivo(true);
            u.setProvider(Usuario.Provider.GOOGLE);

            logger.info("✅ Usuário Google '{}' registrado com sucesso. Matrícula: {}", email, matricula);
            return usuarioRepo.save(u);
        });
    }

    /**
     * Gera um número de 4 dígitos (1000 a 9999) que não exista no banco.
     */
    private String gerarMatriculaUnica() {
        Random random = new Random();
        String matricula;
        do {
            int numero = 1000 + random.nextInt(9000);
            matricula = String.valueOf(numero);
        } while (usuarioRepo.findByMatricula(matricula).isPresent());

        return matricula;
    }

    // --- MÉTODOS DE RECUPERAÇÃO DE SENHA (MANTIDOS) ---

    public void processarEsqueciSenha(String email) {
        logger.info("🔑 Processando esqueci senha para: {}", email);
        usuarioRepo.findByEmail(email).ifPresentOrElse(user -> {
            // Cria token
            String token = UUID.randomUUID().toString();
            PasswordResetToken myToken = new PasswordResetToken(token, user);
            tokenRepo.save(myToken);

            // Em produção, envie email. Aqui mostramos no console.
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
        tokenRepo.delete(resetToken); // Apaga o token após uso

        logger.info("🔒 Senha atualizada via token para matrícula: {}", user.getMatricula());
    }
}