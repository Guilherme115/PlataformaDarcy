package com.example.PlataformaDarcy.service;

import com.example.PlataformaDarcy.model.Volume;
import com.example.PlataformaDarcy.model.PaginaLivro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfExportService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] gerarPdfVolume(Volume volume) throws Exception {
        // Contexto Thymeleaf
        Context context = new Context();
        context.setVariable("volume", volume);

        // Renderizar HTML usando template específico para PDF
        String htmlContent = templateEngine.process("admin/export/volume-pdf", context);

        // Gerar PDF usando Flying Saucer
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        // Ajustar base URL se necessário (para carregar imagens/css locais)
        // renderer.setDocumentFromString(htmlContent, baseUrl);
        renderer.setDocumentFromString(htmlContent);

        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }
}
