package br.edu.utfpr.alunos.webpet.services.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheMetricsService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);

    public void recordCacheHit(String cacheName) {
        cacheHits.incrementAndGet();
        log.debug("Cache hit recorded for: {}", cacheName);
    }

    public void recordCacheMiss(String cacheName) {
        cacheMisses.incrementAndGet();
        log.debug("Cache miss recorded for: {}", cacheName);
    }

    public void recordCacheEviction(String cacheName) {
        cacheEvictions.incrementAndGet();
        log.debug("Cache eviction recorded for: {}", cacheName);
    }

    public CacheStats getCacheStats() {
        try {
            long totalHits = cacheHits.get();
            long totalMisses = cacheMisses.get();
            long totalEvictions = cacheEvictions.get();
            long totalRequests = totalHits + totalMisses;
            
            double hitRatio = totalRequests > 0 ? (double) totalHits / totalRequests : 0.0;
            
            Map<String, Long> keysByPattern = getKeyCountsByPattern();
            long totalKeys = redisTemplate.getConnectionFactory().getConnection().dbSize();
            
            return new CacheStats(
                totalHits, totalMisses, totalEvictions, 
                hitRatio, totalKeys, keysByPattern
            );
        } catch (Exception e) {
            log.error("Failed to get cache stats", e);
            return new CacheStats(0L, 0L, 0L, 0.0, 0L, Map.of());
        }
    }

    private Map<String, Long> getKeyCountsByPattern() {
        Map<String, Long> counts = new HashMap<>();
        
        try {
            Set<String> sessionKeys = redisTemplate.keys("session:*");
            Set<String> userKeys = redisTemplate.keys("auth_user:*");
            Set<String> tokenKeys = redisTemplate.keys("user_token:*");
            Set<String> statsKeys = redisTemplate.keys("stats:*");
            Set<String> searchKeys = redisTemplate.keys("search:*");
            
            counts.put("sessions", sessionKeys != null ? (long) sessionKeys.size() : 0L);
            counts.put("users", userKeys != null ? (long) userKeys.size() : 0L);
            counts.put("tokens", tokenKeys != null ? (long) tokenKeys.size() : 0L);
            counts.put("stats", statsKeys != null ? (long) statsKeys.size() : 0L);
            counts.put("searches", searchKeys != null ? (long) searchKeys.size() : 0L);
        } catch (Exception e) {
            log.error("Failed to count keys by pattern", e);
        }
        
        return counts;
    }

    public void resetMetrics() {
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheEvictions.set(0);
        log.info("Cache metrics reset");
    }

    public record CacheStats(
        long hits,
        long misses,
        long evictions,
        double hitRatio,
        long totalKeys,
        Map<String, Long> keysByPattern
    ) {}
}