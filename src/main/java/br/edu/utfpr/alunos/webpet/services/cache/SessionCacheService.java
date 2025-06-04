package br.edu.utfpr.alunos.webpet.services.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    public void createSession(String sessionId, String userId, Object sessionData) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            
            // Store session data
            redisTemplate.opsForValue().set(sessionKey, sessionData, SESSION_TTL);
            
            // Track user sessions
            redisTemplate.opsForSet().add(userSessionsKey, sessionId);
            redisTemplate.expire(userSessionsKey, SESSION_TTL);
            
            log.debug("Session created: {} for user: {}", sessionId, userId);
        } catch (Exception e) {
            log.error("Failed to create session: {} for user: {}", sessionId, userId, e);
        }
    }

    public Object getSession(String sessionId) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            return redisTemplate.opsForValue().get(sessionKey);
        } catch (Exception e) {
            log.error("Failed to get session: {}", sessionId, e);
            return null;
        }
    }

    public void updateSession(String sessionId, Object sessionData) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                redisTemplate.opsForValue().set(sessionKey, sessionData, SESSION_TTL);
                log.debug("Session updated: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Failed to update session: {}", sessionId, e);
        }
    }

    public void extendSession(String sessionId) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                redisTemplate.expire(sessionKey, SESSION_TTL);
                log.debug("Session extended: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Failed to extend session: {}", sessionId, e);
        }
    }

    public void invalidateSession(String sessionId) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            redisTemplate.delete(sessionKey);
            log.debug("Session invalidated: {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to invalidate session: {}", sessionId, e);
        }
    }

    public void invalidateUserSessions(String userId) {
        try {
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
            
            if (sessionIds != null && !sessionIds.isEmpty()) {
                String[] sessionKeys = sessionIds.stream()
                    .map(id -> SESSION_PREFIX + id)
                    .toArray(String[]::new);
                
                redisTemplate.delete(sessionKeys);
                redisTemplate.delete(userSessionsKey);
                
                log.debug("Invalidated {} sessions for user: {}", sessionIds.size(), userId);
            }
        } catch (Exception e) {
            log.error("Failed to invalidate sessions for user: {}", userId, e);
        }
    }

    public boolean isSessionValid(String sessionId) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
        } catch (Exception e) {
            log.error("Failed to check session validity: {}", sessionId, e);
            return false;
        }
    }

    public long getActiveSessionCount(String userId) {
        try {
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            Long count = redisTemplate.opsForSet().size(userSessionsKey);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Failed to get session count for user: {}", userId, e);
            return 0;
        }
    }
}