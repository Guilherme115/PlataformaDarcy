package com.example.PlataformaDarcy.controller;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.service.EfiService;
import com.example.PlataformaDarcy.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciamento de planos e pagamentos.
 */
@Controller
@RequestMapping("/planos")
public class PlanosController {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private EfiService efiService;

    /**
     * Página de planos.
     */
    @GetMapping
    public String paginaPlanos(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("planoInfo", subscriptionService.getPlanoInfo(usuario));

        // Preços ESTUDANTE
        model.addAttribute("precoEstudanteNormal", SubscriptionService.PRECO_ESTUDANTE_NORMAL);
        model.addAttribute("precoEstudantePromo", SubscriptionService.PRECO_ESTUDANTE_PROMO);

        // Preços PREMIUM
        model.addAttribute("precoProNormal", SubscriptionService.PRECO_PRO_NORMAL);
        model.addAttribute("precoProPromo", SubscriptionService.PRECO_PRO_PROMO);

        return "pagamento/planos";
    }

    /**
     * Inicia assinatura ESTUDANTE.
     */
    @PostMapping("/assinar-estudante")
    public String iniciarAssinaturaEstudante(@AuthenticationPrincipal Usuario usuario) {
        EfiService.CobrancaPix cobranca = subscriptionService.iniciarAssinaturaEstudante(usuario);
        return "redirect:/planos/checkout/" + cobranca.txid;
    }

    /**
     * Inicia assinatura PRO/PREMIUM.
     */
    @PostMapping("/assinar-pro")
    public String iniciarAssinaturaPro(@AuthenticationPrincipal Usuario usuario) {
        EfiService.CobrancaPix cobranca = subscriptionService.iniciarAssinaturaPro(usuario);
        return "redirect:/planos/checkout/" + cobranca.txid;
    }

    /**
     * Página de checkout com QR Code Pix.
     */
    @GetMapping("/checkout/{txid}")
    public String paginaCheckout(@PathVariable String txid, Model model) {
        EfiService.CobrancaPix cobranca = efiService.consultarCobranca(txid);

        if (cobranca == null) {
            return "redirect:/planos?erro=cobranca-nao-encontrada";
        }

        model.addAttribute("cobranca", cobranca);
        return "pagamento/checkout";
    }

    /**
     * Verifica status do pagamento (AJAX polling).
     */
    @GetMapping("/status/{txid}")
    @ResponseBody
    public String verificarStatus(@PathVariable String txid) {
        EfiService.CobrancaPix cobranca = efiService.consultarCobranca(txid);
        return cobranca != null ? cobranca.status : "NAO_ENCONTRADA";
    }

    /**
     * Simula pagamento para testes (remover em produção).
     */
    @PostMapping("/simular/{txid}")
    public String simularPagamento(@PathVariable String txid,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        efiService.simularPagamento(txid);
        subscriptionService.processarPagamento(txid);

        // IMPORTANTE: Força logout para recarregar permissões Spring Security
        // Após ativar PRO, o usuário precisa fazer login novamente para que
        // o Spring Security carregue a nova ROLE_PRO
        try {
            new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler()
                    .logout(request, response, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/login?planoAtivado=true";
    }

    /**
     * Webhook EFI Pagamentos (callback).
     */
    @PostMapping("/webhook")
    @ResponseBody
    public String webhookEfi(@RequestBody String payload) {
        // Em produção: validar assinatura, extrair txid e processar
        System.out.println("📩 [EFI WEBHOOK] " + payload);
        return "OK";
    }

    /**
     * Página de sucesso.
     */
    @GetMapping("/sucesso")
    public String paginaSucesso(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("planoInfo", subscriptionService.getPlanoInfo(usuario));
        return "pagamento/sucesso";
    }
}
