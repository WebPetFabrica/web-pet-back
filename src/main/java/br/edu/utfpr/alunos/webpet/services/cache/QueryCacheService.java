package br.edu.utfpr.alunos.webpet.services.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String STATS_PREFIX = "stats:";
    private static final String SEARCH_PREFIX = "search:";
    private static final String COUNT_PREFIX = "count:";

    @Cacheable(value = "userStats", key = "'global'")
    public Map<String, Long> getUserStats() {
        return null; // Cache miss, will be populated by calling service
    }

    public void cacheUserStats(Map<String, Long> stats) {
        try {
            String key = STATS_PREFIX + "global";
            redisTemplate.opsForValue().set(key, stats, Duration.ofHours(1));
            log.debug("Cached global user stats");
        } catch (Exception e) {
            log.error("Failed to cache user stats", e);
        }
    }

    @Cacheable(value = "searchResults", key = "#searchTerm + ':' + #page + ':' + #size")
    public List<Object> getSearchResults(String searchTerm, int page, int size) {
        return null; // Cache miss
    }

    public void cacheSearchResults(String searchTerm, int page, int size, List<Object> results) {
        try {
            String key = SEARCH_PREFIX + searchTerm + ":" + page + ":" + size;
            redisTemplate.opsForValue().set(key, results, Duration.ofMinutes(10));
            log.debug("Cached search results for: {}", searchTerm);
        } catch (Exception e) {
            log.error("Failed to cache search results for: {}", searchTerm, e);
        }
    }

    @Cacheable(value = "activeUsers", key = "'count'")
    public Long getActiveUserCount() {
        return null; // Cache miss
    }

    public void cacheActiveUserCount(Long count) {
        try {
            String key = COUNT_PREFIX + "active_users";
            redisTemplate.opsForValue().set(key, count, Duration.ofMinutes(5));
            log.debug("Cached active user count: {}", count);
        } catch (Exception e) {
            log.error("Failed to cache active user count", e);
        }
    }

    public Long getCachedActiveUserCount() {
        try {
            String key = COUNT_PREFIX + "active_users";
            Object count = redisTemplate.opsForValue().get(key);
            return count != null ? (Long) count : null;
        } catch (Exception e) {
            log.error("Failed to get cached active user count", e);
            return null;
        }
    }

    @CacheEvict(value = {"userStats", "searchResults", "activeUsers"}, allEntries = true)
    public void evictQueryCaches() {
        try {
            redisTemplate.delete(
                redisTemplate.keys(STATS_PREFIX + "*"),
                redisTemplate.keys(SEARCH_PREFIX + "*"),
                redisTemplate.keys(COUNT_PREFIX + "*")
            );
            log.debug("Evicted all query caches");
        } catch (Exception e) {
            log.error("Failed to evict query caches", e);
        }
    }

    @CacheEvict(value = "searchResults", allEntries = true)
    public void evictSearchCache() {
        try {
            redisTemplate.delete(redisTemplate.keys(SEARCH_PREFIX + "*"));
            log.debug("Evicted search caches");
        } catch (Exception e) {
            log.error("Failed to evict search caches", e);
        }
    }

    public void warmupCache() {
        log.info("Starting cache warmup...");
        // Implement warmup logic here
    }
}