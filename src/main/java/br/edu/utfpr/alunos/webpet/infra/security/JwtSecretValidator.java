package br.edu.utfpr.alunos.webpet.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class JwtSecretValidator {
    
    @Value("${api.security.token.secret}")
    private String jwtSecret;
    
    private static final int MIN_SECRET_LENGTH = 64; // 256 bits
    private static final String INSECURE_DEFAULT_SECRET = "minha-chave-secreta-super-segura-de-256-bits-para-jwt-que-deve-ser-alterada-em-producao";
    
    @PostConstruct
    public void validateJwtSecret() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "ERRO CRÍTICO: JWT_SECRET não foi configurado. " +
                "Configure a variável de ambiente JWT_SECRET antes de iniciar a aplicação."
            );
        }
        
        if (jwtSecret.equals(INSECURE_DEFAULT_SECRET)) {
            throw new IllegalStateException(
                "ERRO CRÍTICO: JWT_SECRET está usando um valor inseguro conhecido. " +
                "Configure uma chave secreta segura através da variável de ambiente JWT_SECRET."
            );
        }
        
        if (jwtSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                String.format("ERRO CRÍTICO: JWT secret muito curto. Deve ter pelo menos %d caracteres. Atual: %d", 
                    MIN_SECRET_LENGTH, jwtSecret.length())
            );
        }
    }
}