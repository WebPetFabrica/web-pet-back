package br.edu.utfpr.alunos.webpet.services.auth;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class LoginAttemptService {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    private final ConcurrentHashMap<String, AttemptInfo> attemptCache = new ConcurrentHashMap<>();
    
    public void recordFailedAttempt(String email) {
        AttemptInfo info = attemptCache.computeIfAbsent(email, k -> new AttemptInfo());
        info.incrementAttempts();
        info.setLastAttempt(LocalDateTime.now());
    }
    
    public void recordSuccessfulLogin(String email) {
        attemptCache.remove(email);
    }
    
    public boolean isBlocked(String email) {
        AttemptInfo info = attemptCache.get(email);
        if (info == null) return false;
        
        if (info.getAttempts() >= MAX_ATTEMPTS) {
            if (info.getLastAttempt().plusMinutes(LOCKOUT_DURATION_MINUTES).isAfter(LocalDateTime.now())) {
                return true;
            } else {
                attemptCache.remove(email); // Reset após expirar
                return false;
            }
        }
        return false;
    }
    
    public int getRemainingAttempts(String email) {
        AttemptInfo info = attemptCache.get(email);
        return info == null ? MAX_ATTEMPTS : Math.max(0, MAX_ATTEMPTS - info.getAttempts());
    }
    
    public boolean requiresCaptcha(String email) {
        AttemptInfo info = attemptCache.get(email);
        return info != null && info.getAttempts() >= 3;
    }
    
    @Data
    @AllArgsConstructor
    private static class AttemptInfo {
        private int attempts = 0;
        private LocalDateTime lastAttempt = LocalDateTime.now();
        
        public AttemptInfo() {}
        
        public void incrementAttempts() {
            this.attempts++;
        }
    }
}