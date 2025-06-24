package br.edu.utfpr.alunos.webpet.infra.cors;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class CorsEnvironmentConfig {
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;
    
    private static final List<String> UNSAFE_ORIGINS = List.of(
        "*", 
        "http://localhost:*",
        "file://"
    );
    
    @PostConstruct
    public void validateCorsConfig() {
        if ("prod".equals(activeProfile) && hasUnsafeOrigins()) {
            throw new IllegalStateException("Production environment cannot use wildcard CORS origins");
        }
        
        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException("CORS origins must be explicitly configured");
        }
    }
    
    private boolean hasUnsafeOrigins() {
        return UNSAFE_ORIGINS.stream().anyMatch(allowedOrigins::contains);
    }
}