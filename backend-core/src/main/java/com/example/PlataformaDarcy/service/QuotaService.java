package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Serviço de gerenciamento de quotas de uso de IA.
 * PROTEÇÃO CRÍTICA: Evita que bugs ou abusos estourem o orçamento de API.
 */
@Service
public class QuotaService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    // === LIMITES GLOBAIS DE EMERGÊNCIA (BUDGET PROTECTION) ===
    private static final int MAX_MENSAGENS_DIA_GLOBAL = 10000; // Máx total do sistema por dia
    private static int mensagensUsadasHoje = 0;
    private static LocalDate dataUltimaContagem = LocalDate.now();

    /**
     * Verifica se usuário pode usar IA e incrementa quota.
     * Retorna true se permitido, false se excedeu limite.
     */
    @Transactional
    public boolean consumirQuotaMensagemIA(Usuario usuario) {
        // 1. Reset diário automático
        resetarQuotaDiariaSeNecessario(usuario);

        // 2. Verifica limite global (proteção de orçamento)
        if (!verificarLimiteGlobal()) {
            System.out.println("⚠️ [QUOTA] LIMITE GLOBAL ATINGIDO! Sistema bloqueado por hoje.");
            return false;
        }

        // 3. Verifica se usuário tem permissão de usar IA
        if (!usuario.podeUsarIA()) {
            System.out.println("⚠️ [QUOTA] Usuário " + usuario.getEmail() + " atingiu limite diário: " +
                    usuario.getQuotaIaMensagensHoje() + "/" + usuario.getLimiteMensagensIA());
            return false;
        }

        // 4. Incrementa quota
        usuario.setQuotaIaMensagensHoje(usuario.getQuotaIaMensagensHoje() + 1);
        usuario.setDataUltimoUsoIA(LocalDate.now());
        usuarioRepo.save(usuario);

        mensagensUsadasHoje++;

        System.out.println("✅ [QUOTA] Usuário " + usuario.getEmail() + " usou " +
                usuario.getQuotaIaMensagensHoje() + "/" + usuario.getLimiteMensagensIA() + " msgs");

        return true;
    }

    /**
     * Verifica se usuário pode gerar podcast e incrementa quota.
     */
    @Transactional
    public boolean consumirQuotaPodcast(Usuario usuario) {
        resetarQuotaMensalSeNecessario(usuario);

        if (usuario.getQuotaPodcastsMes() >= usuario.getLimitePodcastsMes()) {
            System.out.println("⚠️ [QUOTA] Usuário " + usuario.getEmail() + " atingiu limite mensal de podcasts: " +
                    usuario.getQuotaPodcastsMes() + "/" + usuario.getLimitePodcastsMes());
            return false;
        }

        usuario.setQuotaPodcastsMes(usuario.getQuotaPodcastsMes() + 1);
        usuarioRepo.save(usuario);

        System.out.println("✅ [QUOTA] Usuário " + usuario.getEmail() + " usou " +
                usuario.getQuotaPodcastsMes() + "/" + usuario.getLimitePodcastsMes() + " podcasts");

        return true;
    }

    /**
     * Reseta quota diária se for um novo dia.
     */
    private void resetarQuotaDiariaSeNecessario(Usuario usuario) {
        LocalDate hoje = LocalDate.now();

        if (usuario.getDataUltimoUsoIA() == null || usuario.getDataUltimoUsoIA().isBefore(hoje)) {
            usuario.setQuotaIaMensagensHoje(0);
            usuario.setDataUltimoUsoIA(hoje);
            usuarioRepo.save(usuario);
            System.out.println("🔄 [QUOTA] Reset diário para: " + usuario.getEmail());
        }
    }

    /**
     * Reseta quota mensal se for um novo mês.
     */
    private void resetarQuotaMensalSeNecessario(Usuario usuario) {
        LocalDate hoje = LocalDate.now();
        LocalDate primeiroDiaMes = hoje.withDayOfMonth(1);

        if (usuario.getDataUltimoUsoIA() == null || usuario.getDataUltimoUsoIA().isBefore(primeiroDiaMes)) {
            usuario.setQuotaPodcastsMes(0);
            usuarioRepo.save(usuario);
            System.out.println("🔄 [QUOTA] Reset mensal para: " + usuario.getEmail());
        }
    }

    /**
     * Verifica limite global de mensagens (proteção de orçamento).
     * Limite: 10.000 mensagens/dia para todo o sistema.
     */
    private boolean verificarLimiteGlobal() {
        LocalDate hoje = LocalDate.now();

        // Reset contador global de dia
        if (!dataUltimaContagem.equals(hoje)) {
            mensagensUsadasHoje = 0;
            dataUltimaContagem = hoje;
        }

        return mensagensUsadasHoje < MAX_MENSAGENS_DIA_GLOBAL;
    }

    /**
     * Retorna quantas mensagens o usuário ainda pode enviar hoje.
     */
    public int getMensagensRestantes(Usuario usuario) {
        resetarQuotaDiariaSeNecessario(usuario);
        return Math.max(0, usuario.getLimiteMensagensIA() - usuario.getQuotaIaMensagensHoje());
    }

    /**
     * Retorna quantos podcasts o usuário ainda pode gerar este mês.
     */
    public int getPodcastsRestantes(Usuario usuario) {
        resetarQuotaMensalSeNecessario(usuario);
        return Math.max(0, usuario.getLimitePodcastsMes() - usuario.getQuotaPodcastsMes());
    }
}
