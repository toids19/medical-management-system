package com.example.MedicalManagementSystem.service;

import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;

    @Autowired
    public PdfGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public ByteArrayInputStream generatePdf(String templateName, Map<String, Object> data) {
        Context context = new Context();
        if (data != null) {
            data.forEach(context::setVariable);
        }

        // Process the template with Thymeleaf
        String htmlContent = templateEngine.process(templateName, context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ITextRenderer renderer = new ITextRenderer();

            // Set document from string with proper XHTML content
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream, false);
            renderer.finishPDF();

            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}

