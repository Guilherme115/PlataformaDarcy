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
        model.addAttribute("precoNormal", SubscriptionService.PRECO_PRO_NORMAL);
        model.addAttribute("precoPromo", SubscriptionService.PRECO_PRO_PROMO);
        return "pagamento/planos";
    }

    /**
     * Inicia assinatura PRO.
     */
    @PostMapping("/assinar")
    public String iniciarAssinatura(@AuthenticationPrincipal Usuario usuario) {
        EfiService.CobrancaPix cobranca = subscriptionService.iniciarAssinatura(usuario);
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
    public String simularPagamento(@PathVariable String txid) {
        efiService.simularPagamento(txid);
        subscriptionService.processarPagamento(txid);
        return "redirect:/planos/sucesso";
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
