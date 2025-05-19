package br.edu.utfpr.alunos.webpet.infra.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Configuração mais restritiva para ambientes de desenvolvimento
                .allowedOrigins("http://localhost:4200", "http://localhost:3000") // Frontend Angular e React
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache por 1 hora
        
        // TODO: Em produção, restringir para domínios específicos da aplicação
    }
}