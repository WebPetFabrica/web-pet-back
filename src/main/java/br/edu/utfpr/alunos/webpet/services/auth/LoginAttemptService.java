package br.edu.utfpr.alunos.webpet.services.auth;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private final ConcurrentHashMap<String, AtomicInteger> attemptCache = new ConcurrentHashMap<>();

    public void recordFailedLogin(String email) {
        AtomicInteger attempts = attemptCache.computeIfAbsent(email, k -> new AtomicInteger(0));
        int currentAttempts = attempts.incrementAndGet();
        log.warn("Failed login attempt for email: {} (attempt {})", email, currentAttempts);
        
        if (currentAttempts >= MAX_ATTEMPTS) {
            log.error("Maximum login attempts exceeded for email: {}", email);
        }
    }

    public void recordSuccessfulLogin(String email) {
        attemptCache.remove(email);
        log.info("Successful login for email: {}", email);
    }

    public boolean isBlocked(String email) {
        AtomicInteger attempts = attemptCache.get(email);
        return attempts != null && attempts.get() >= MAX_ATTEMPTS;
    }

    public void resetAttempts(String email) {
        attemptCache.remove(email);
    }
}