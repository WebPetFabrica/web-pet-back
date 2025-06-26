package br.edu.utfpr.alunos.webpet.infra.cors;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsStr;
    
    @Value("${app.cors.max-age:3600}")
    private long maxAge;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    private List<String> allowedOrigins;
    
    @PostConstruct
    public void init() {
        if (allowedOriginsStr == null || allowedOriginsStr.trim().isEmpty()) {
            throw new IllegalStateException("CORS origins must be explicitly configured via app.cors.allowed-origins");
        }
        
        allowedOrigins = Arrays.asList(allowedOriginsStr.split(","));
        log.info("CORS configured for profile '{}' with origins: {}", activeProfile, allowedOrigins);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/auth/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("POST", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(false)
                .maxAge(maxAge);
                
        registry.addMapping("/api/v1/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(maxAge);
    }
}