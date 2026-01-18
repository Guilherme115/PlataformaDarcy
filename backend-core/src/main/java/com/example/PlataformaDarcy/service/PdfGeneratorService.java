package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.Questao;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfGeneratorService {

    @Autowired
    private ITemplateEngine templateEngine;

    /**
     * Gera PDF de lista de questões com design Neo Brutalist
     */
    public byte[] gerarListaImpressaPDF(List<Questao> questoes,
            String titulo,
            Integer etapa,
            Map<String, Object> metadados) throws DocumentException {

        // 1. Prepara contexto para Thymeleaf
        Context context = new Context();
        context.setVariable("questoes", questoes);
        context.setVariable("titulo", titulo != null ? titulo : "Lista de Questões - PAS " + etapa);
        context.setVariable("etapa", etapa);
        context.setVariable("data", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        context.setVariable("quantidade", questoes.size());
        context.setVariable("tempoSugerido", questoes.size() * 3); // 3 min por questão

        // Metadados adicionais
        if (metadados != null) {
            metadados.forEach(context::setVariable);
        }

        // 2. Renderiza HTML com Thymeleaf
        String htmlContent = templateEngine.process("pdf/lista_impressa", context);

        // 3. Converte HTML para PDF com Flying Saucer
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Gera PDF simplificado apenas com questões (sem cabeçalho)
     */
    public byte[] gerarQuestoesPDF(List<Questao> questoes) throws DocumentException {
        Map<String, Object> meta = new HashMap<>();
        meta.put("simples", true);
        return gerarListaImpressaPDF(questoes, "Questões", 1, meta);
    }

    /**
     * Gera PDF apenas com o GABARITO (respostas corretas)
     */
    public byte[] gerarGabaritoPDF(List<Questao> questoes, String titulo, Integer etapa) throws DocumentException {
        Context context = new Context();
        context.setVariable("questoes", questoes);
        context.setVariable("titulo", titulo);
        context.setVariable("etapa", etapa);
        context.setVariable("data", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        context.setVariable("quantidade", questoes.size());

        // Renderiza template de gabarito
        String htmlContent = templateEngine.process("pdf/gabarito", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }
}
