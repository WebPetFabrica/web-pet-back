package br.edu.utfpr.alunos.webpet.services.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheManagerService {
    
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserCacheService userCacheService;
    private final QueryCacheService queryCacheService;
    private final SessionCacheService sessionCacheService;

    public void clearAllCaches() {
        log.info("Clearing all caches...");
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            try {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.debug("Cleared cache: {}", cacheName);
                }
            } catch (Exception e) {
                log.error("Failed to clear cache: {}", cacheName, e);
            }
        });
        
        userCacheService.evictAllUserCache();
        queryCacheService.evictQueryCaches();
        
        log.info("All caches cleared successfully");
    }

    public void clearUserCaches() {
        log.info("Clearing user-related caches...");
        userCacheService.evictAllUserCache();
        
        var userProfilesCache = cacheManager.getCache("userProfiles");
        var authenticatedUsersCache = cacheManager.getCache("authenticatedUsers");
        
        if (userProfilesCache != null) userProfilesCache.clear();
        if (authenticatedUsersCache != null) authenticatedUsersCache.clear();
        
        log.info("User caches cleared successfully");
    }

    public void clearQueryCaches() {
        log.info("Clearing query caches...");
        queryCacheService.evictQueryCaches();
        
        var searchCache = cacheManager.getCache("searchResults");
        var statsCache = cacheManager.getCache("userStats");
        
        if (searchCache != null) searchCache.clear();
        if (statsCache != null) statsCache.clear();
        
        log.info("Query caches cleared successfully");
    }

    public void warmupCaches() {
        log.info("Starting cache warmup...");
        
        try {
            // Warmup user stats
            queryCacheService.warmupCache();
            
            // Warmup common searches
            warmupSearchCaches();
            
            log.info("Cache warmup completed successfully");
        } catch (Exception e) {
            log.error("Cache warmup failed", e);
        }
    }

    private void warmupSearchCaches() {
        // Implementation for warming up search caches
        log.debug("Warming up search caches...");
    }

    public CacheHealthInfo getCacheHealth() {
        try {
            // Test Redis connectivity
            String pingResult = redisTemplate.getConnectionFactory()
                .getConnection().ping();
            boolean redisHealthy = "PONG".equals(pingResult);
            
            // Get cache statistics
            long totalKeys = redisTemplate.getConnectionFactory()
                .getConnection().dbSize();
            
            Set<String> cacheNames = Set.copyOf(cacheManager.getCacheNames());
            
            return new CacheHealthInfo(redisHealthy, totalKeys, cacheNames);
        } catch (Exception e) {
            log.error("Failed to get cache health", e);
            return new CacheHealthInfo(false, 0, Set.of());
        }
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void cleanupExpiredSessions() {
        log.debug("Cleaning up expired sessions...");
        // Implementation for cleanup
    }

    @Scheduled(fixedRate = 6, timeUnit = TimeUnit.HOURS)
    public void refreshCriticalCaches() {
        log.debug("Refreshing critical caches...");
        clearQueryCaches();
        warmupCaches();
    }

    public record CacheHealthInfo(
        boolean redisHealthy,
        long totalKeys,
        Set<String> cacheNames
    ) {}
}