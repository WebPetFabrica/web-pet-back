package br.edu.utfpr.alunos.webpet.services.cache;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String AUTHENTICATED_USER_PREFIX = "auth_user:";
    private static final String USER_PERMISSIONS_PREFIX = "user_perms:";
    private static final String USER_TOKEN_PREFIX = "user_token:";
    private static final Duration AUTH_USER_TTL = Duration.ofMinutes(15);
    private static final Duration TOKEN_TTL = Duration.ofHours(2);

    @Cacheable(value = "authenticatedUsers", key = "#email")
    public BaseUser getAuthenticatedUser(String email) {
        return null; // Cache miss, will be populated by calling service
    }

    @CachePut(value = "authenticatedUsers", key = "#user.email")
    public BaseUser cacheAuthenticatedUser(BaseUser user) {
        try {
            String key = AUTHENTICATED_USER_PREFIX + user.getEmail();
            redisTemplate.opsForValue().set(key, user, AUTH_USER_TTL);
            log.debug("Cached authenticated user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to cache authenticated user: {}", user.getEmail(), e);
        }
        return user;
    }

    @Cacheable(value = "userProfiles", key = "#userId")
    public UserResponseDTO getUserProfile(String userId) {
        return null; // Cache miss, will be populated by calling service
    }

    @CachePut(value = "userProfiles", key = "#userProfile.id")
    public UserResponseDTO cacheUserProfile(UserResponseDTO userProfile) {
        log.debug("Cached user profile: {}", userProfile.id());
        return userProfile;
    }

    public void cacheUserToken(String email, String token) {
        try {
            String key = USER_TOKEN_PREFIX + email;
            redisTemplate.opsForValue().set(key, token, TOKEN_TTL);
            log.debug("Cached token for user: {}", email);
        } catch (Exception e) {
            log.error("Failed to cache token for user: {}", email, e);
        }
    }

    public String getCachedToken(String email) {
        try {
            String key = USER_TOKEN_PREFIX + email;
            Object token = redisTemplate.opsForValue().get(key);
            return token != null ? token.toString() : null;
        } catch (Exception e) {
            log.error("Failed to get cached token for user: {}", email, e);
            return null;
        }
    }

    public void cacheUserPermissions(String userId, Set<String> permissions) {
        try {
            String key = USER_PERMISSIONS_PREFIX + userId;
            redisTemplate.opsForValue().set(key, permissions, Duration.ofMinutes(10));
            log.debug("Cached permissions for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to cache permissions for user: {}", userId, e);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> getCachedPermissions(String userId) {
        try {
            String key = USER_PERMISSIONS_PREFIX + userId;
            return (Set<String>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to get cached permissions for user: {}", userId, e);
            return null;
        }
    }

    @CacheEvict(value = {"authenticatedUsers", "userProfiles"}, key = "#email")
    public void evictUserCache(String email) {
        try {
            String authKey = AUTHENTICATED_USER_PREFIX + email;
            String tokenKey = USER_TOKEN_PREFIX + email;
            redisTemplate.delete(authKey, tokenKey);
            log.debug("Evicted cache for user: {}", email);
        } catch (Exception e) {
            log.error("Failed to evict cache for user: {}", email, e);
        }
    }

    @CacheEvict(value = {"authenticatedUsers", "userProfiles"}, allEntries = true)
    public void evictAllUserCache() {
        try {
            Set<String> authKeys = redisTemplate.keys(AUTHENTICATED_USER_PREFIX + "*");
            Set<String> tokenKeys = redisTemplate.keys(USER_TOKEN_PREFIX + "*");
            Set<String> permKeys = redisTemplate.keys(USER_PERMISSIONS_PREFIX + "*");
            
            if (authKeys != null && !authKeys.isEmpty()) {
                redisTemplate.delete(authKeys);
            }
            if (tokenKeys != null && !tokenKeys.isEmpty()) {
                redisTemplate.delete(tokenKeys);
            }
            if (permKeys != null && !permKeys.isEmpty()) {
                redisTemplate.delete(permKeys);
            }
            
            log.debug("Evicted all user caches");
        } catch (Exception e) {
            log.error("Failed to evict all user caches", e);
        }
    }

    public boolean isUserCached(String email) {
        try {
            String key = AUTHENTICATED_USER_PREFIX + email;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check if user is cached: {}", email, e);
            return false;
        }
    }

    public void extendUserCache(String email) {
        try {
            String authKey = AUTHENTICATED_USER_PREFIX + email;
            String tokenKey = USER_TOKEN_PREFIX + email;
            
            if (Boolean.TRUE.equals(redisTemplate.hasKey(authKey))) {
                redisTemplate.expire(authKey, AUTH_USER_TTL);
            }
            if (Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey))) {
                redisTemplate.expire(tokenKey, TOKEN_TTL);
            }
            
            log.debug("Extended cache for user: {}", email);
        } catch (Exception e) {
            log.error("Failed to extend cache for user: {}", email, e);
        }
    }
}