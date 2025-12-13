package com.example.PlataformaDarcy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.images.path}")
    private String imagesPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pathUri = Paths.get(imagesPath).toUri().toString();

        System.out.println("==================================================");
        System.out.println("📂 CONFIGURAÇÃO DE IMAGENS CORRIGIDA");
        System.out.println("👉 Caminho Físico: " + imagesPath);
        System.out.println("👉 URL Servida:    " + pathUri);
        System.out.println("==================================================");

        registry.addResourceHandler("/media/**")
                .addResourceLocations(pathUri);
    }
}