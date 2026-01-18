package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.TipoPlano;
import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Gerencia assinaturas e planos dos usuários.
 */
@Service
public class SubscriptionService {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private EfiService efiService;

    // === PREÇOS DOS PLANOS (PROMOÇÃO DE LANÇAMENTO) ===

    // Plano ESTUDANTE (R$ 29,90 normal, R$ 24,90 promo)
    public static final double PRECO_ESTUDANTE_NORMAL = 29.90;
    public static final double PRECO_ESTUDANTE_PROMO = 24.90;

    // Plano PRO/PREMIUM (R$ 79,90 normal, R$ 59,90 promo)
    public static final double PRECO_PRO_NORMAL = 79.90;
    public static final double PRECO_PRO_PROMO = 59.90;

    public static final int DURACAO_MESES = 1; // Mudado para mensal (não anual)

    /**
     * Inicia processo de assinatura ESTUDANTE.
     */
    public EfiService.CobrancaPix iniciarAssinaturaEstudante(Usuario usuario) {
        String descricao = "Plataforma Darcy ESTUDANTE - Mensal";
        return efiService.criarCobrancaPix(PRECO_ESTUDANTE_PROMO, descricao, usuario.getId());
    }

    /**
     * Inicia processo de assinatura PRO/PREMIUM.
     */
    public EfiService.CobrancaPix iniciarAssinaturaPro(Usuario usuario) {
        String descricao = "Plataforma Darcy PREMIUM - Mensal";
        return efiService.criarCobrancaPix(PRECO_PRO_PROMO, descricao, usuario.getId());
    }

    /**
     * Ativa plano ESTUDANTE para o usuário.
     */
    @Transactional
    public void ativarPlanoEstudante(Usuario usuario, int duracaoMeses) {
        usuario.setPlano(TipoPlano.ESTUDANTE);
        usuario.setDataExpiracaoPlano(LocalDateTime.now().plusMonths(duracaoMeses));
        usuarioRepo.save(usuario);
        usuarioRepo.flush();

        System.out.println("🎉 Plano ESTUDANTE ativado para: " + usuario.getEmail() +
                " até " + usuario.getDataExpiracaoPlano());
    }

    /**
     * Ativa plano PRO/PREMIUM para o usuário.
     */
    @Transactional
    public void ativarPlanoPro(Usuario usuario, int duracaoMeses) {
        usuario.setPlano(TipoPlano.PRO);
        usuario.setDataExpiracaoPlano(LocalDateTime.now().plusMonths(duracaoMeses));
        usuarioRepo.save(usuario);
        usuarioRepo.flush();

        System.out.println("🎉 Plano PREMIUM ativado para: " + usuario.getEmail() +
                " até " + usuario.getDataExpiracaoPlano());
    }

    /**
     * Processa webhook de pagamento confirmado.
     */
    @Transactional
    public boolean processarPagamento(String txid) {
        EfiService.CobrancaPix cobranca = efiService.consultarCobranca(txid);

        if (cobranca == null || !"CONCLUIDA".equals(cobranca.status)) {
            return false;
        }

        Usuario usuario = usuarioRepo.findById(cobranca.usuarioId).orElse(null);
        if (usuario == null)
            return false;

        ativarPlanoPro(usuario, DURACAO_MESES);
        return true;
    }

    /**
     * Cancela plano PRO (volta para FREE).
     */
    @Transactional
    public void cancelarPlano(Usuario usuario) {
        usuario.setPlano(TipoPlano.FREE);
        usuario.setDataExpiracaoPlano(null);
        usuarioRepo.save(usuario);
    }

    /**
     * Verifica se plano expirou e atualiza.
     */
    @Transactional
    public void verificarExpiracao(Usuario usuario) {
        if (usuario.getPlano() == TipoPlano.PRO &&
                usuario.getDataExpiracaoPlano() != null &&
                usuario.getDataExpiracaoPlano().isBefore(LocalDateTime.now())) {

            cancelarPlano(usuario);
            System.out.println("⚠️ Plano expirado para: " + usuario.getEmail());
        }
    }

    /**
     * Retorna dados do plano atual.
     */
    public PlanoInfo getPlanoInfo(Usuario usuario) {
        PlanoInfo info = new PlanoInfo();
        info.planoAtual = usuario.getPlano();
        info.isPro = usuario.isPro();
        info.dataExpiracao = usuario.getDataExpiracaoPlano();

        if (info.dataExpiracao != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDateTime.now(), info.dataExpiracao);
            info.diasRestantes = (int) Math.max(0, dias);
        }

        return info;
    }

    public static class PlanoInfo {
        public TipoPlano planoAtual;
        public boolean isPro;
        public LocalDateTime dataExpiracao;
        public int diasRestantes;
    }
}
