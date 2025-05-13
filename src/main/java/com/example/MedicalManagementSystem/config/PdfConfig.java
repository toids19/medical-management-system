package com.example.MedicalManagementSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class PdfConfig {

    @Bean
    public ClassLoaderTemplateResolver pdfTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("XHTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setOrder(1);
        templateResolver.setCheckExistence(true);
        return templateResolver;
    }
}

