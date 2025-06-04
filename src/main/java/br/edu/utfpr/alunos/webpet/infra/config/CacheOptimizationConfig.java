package br.edu.utfpr.alunos.webpet.infra.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheOptimizationConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            // User-related caches
            new ConcurrentMapCache("users"),
            new ConcurrentMapCache("activeUsers"),
            new ConcurrentMapCache("userProfiles"),
            new ConcurrentMapCache("userStats"),
            new ConcurrentMapCache("recentUsers"),
            
            // Projection caches
            new ConcurrentMapCache("userProjections"),
            new ConcurrentMapCache("ongProjections"),
            new ConcurrentMapCache("protetorProjections"),
            
            // Query result caches
            new ConcurrentMapCache("searchResults"),
            new ConcurrentMapCache("paginatedResults")
        ));
        return cacheManager;
    }
}