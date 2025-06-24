package br.edu.utfpr.alunos.webpet.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {
    
    private Redis redis = new Redis();
    private Ttl ttl = new Ttl();
    private boolean enabled = true;
    private boolean warmupOnStartup = true;
    
    @Data
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private int maxConnections = 10;
        private Duration timeout = Duration.ofSeconds(2);
    }
    
    @Data
    public static class Ttl {
        private Duration sessions = Duration.ofMinutes(30);
        private Duration authTokens = Duration.ofHours(2);
        private Duration userProfiles = Duration.ofMinutes(30);
        private Duration userPermissions = Duration.ofMinutes(10);
        private Duration userStats = Duration.ofHours(1);
        private Duration searchResults = Duration.ofMinutes(10);
        private Duration systemConfig = Duration.ofHours(24);
    }
}