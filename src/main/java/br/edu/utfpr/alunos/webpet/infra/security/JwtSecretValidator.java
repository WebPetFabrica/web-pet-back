package br.edu.utfpr.alunos.webpet.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class JwtSecretValidator {
    
    @Value("${api.security.token.secret}")
    private String jwtSecret;
    
    private static final int MIN_SECRET_LENGTH = 64; // 256 bits
    
    @PostConstruct
    public void validateJwtSecret() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET environment variable is required");
        }
        
        if (jwtSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                String.format("JWT secret must be at least %d characters long. Current length: %d", 
                    MIN_SECRET_LENGTH, jwtSecret.length())
            );
        }
    }
}