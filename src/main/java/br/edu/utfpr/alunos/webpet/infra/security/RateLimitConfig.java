package br.edu.utfpr.alunos.webpet.infra.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Configuration
public class RateLimitConfig {
    
    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Bean
    public ConcurrentHashMap<String, Bucket> rateLimitCache() {
        return cache;
    }
    
    public Bucket createAuthBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)))) // 5 tentativas a cada 15 min
            .build();
    }
    
    public Bucket createGeneralBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))) // 100 requests por minuto
            .build();
    }
}