package com.example.PlataformaDarcy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço de integração com EFI Pagamentos (Mock).
 * Em produção, usar SDK oficial: github.com/efipay/sdk-java-apis-efi
 */
@Service
public class EfiService {

    @Value("${efi.sandbox:true}")
    private boolean sandbox;

    @Value("${efi.pix-key:mock-pix-key}")
    private String pixKey;

    // Armazena cobranças em memória (mock)
    private final Map<String, CobrancaPix> cobrancas = new ConcurrentHashMap<>();

    /**
     * Cria uma cobrança Pix.
     * Em produção: usar API /v2/cob do EFI
     */
    public CobrancaPix criarCobrancaPix(Double valor, String descricao, Long usuarioId) {
        String txid = gerarTxid();

        CobrancaPix cobranca = new CobrancaPix();
        cobranca.txid = txid;
        cobranca.valor = valor;
        cobranca.descricao = descricao;
        cobranca.usuarioId = usuarioId;
        cobranca.status = "ATIVA";
        cobranca.criadoEm = LocalDateTime.now();
        cobranca.expiraEm = LocalDateTime.now().plusMinutes(30);

        // Mock: gera dados fictícios
        cobranca.qrcode = gerarQrCodeMock(txid, valor);
        cobranca.copiaECola = gerarPixCopiaEColaMock(txid, valor);

        cobrancas.put(txid, cobranca);

        System.out.println("🟢 [EFI MOCK] Cobrança criada: " + txid + " - R$" + valor);

        return cobranca;
    }

    /**
     * Verifica status de uma cobrança.
     */
    public CobrancaPix consultarCobranca(String txid) {
        return cobrancas.get(txid);
    }

    /**
     * Simula confirmação de pagamento (para testes).
     */
    public boolean simularPagamento(String txid) {
        CobrancaPix cobranca = cobrancas.get(txid);
        if (cobranca == null)
            return false;

        cobranca.status = "CONCLUIDA";
        cobranca.pagaEm = LocalDateTime.now();

        System.out.println("✅ [EFI MOCK] Pagamento confirmado: " + txid);

        return true;
    }

    /**
     * Gera um txid único.
     */
    private String gerarTxid() {
        return "darcy" + UUID.randomUUID().toString().replace("-", "").substring(0, 26);
    }

    /**
     * Gera URL do QR Code (mock).
     */
    private String gerarQrCodeMock(String txid, Double valor) {
        // Em produção, EFI retorna a imagem do QR Code
        return "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" +
                "PIX_MOCK_" + txid + "_" + valor;
    }

    /**
     * Gera código Pix Copia e Cola (mock).
     */
    private String gerarPixCopiaEColaMock(String txid, Double valor) {
        // Em produção, EFI retorna o payload EMV
        return "00020126580014br.gov.bcb.pix0136" + pixKey +
                "52040000530398654" + String.format("%.2f", valor) +
                "5802BR5925PLATAFORMA DARCY6009BRASILIA62" + txid + "6304MOCK";
    }

    /**
     * DTO de cobrança Pix.
     */
    public static class CobrancaPix {
        public String txid;
        public Double valor;
        public String descricao;
        public Long usuarioId;
        public String status; // ATIVA, CONCLUIDA, EXPIRADA
        public String qrcode;
        public String copiaECola;
        public LocalDateTime criadoEm;
        public LocalDateTime expiraEm;
        public LocalDateTime pagaEm;
    }
}
