package br.edu.utfpr.alunos.webpet.services.validation;

import br.edu.utfpr.alunos.webpet.domain.user.PasswordHistory;
import br.edu.utfpr.alunos.webpet.repositories.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing password history to prevent reuse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordHistoryService {
    
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final int MAX_HISTORY_SIZE = 5;
    
    /**
     * Checks if password was used recently
     */
    public boolean isPasswordReused(String userId, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
        
        return history.stream()
            .limit(MAX_HISTORY_SIZE)
            .anyMatch(h -> passwordEncoder.matches(newPassword, h.getPasswordHash()));
    }
    
    /**
     * Saves password to history
     */
    @Transactional
    public void savePasswordToHistory(String userId, String passwordHash) {
        // Create new history entry
        PasswordHistory historyEntry = PasswordHistory.builder()
            .userId(userId)
            .passwordHash(passwordHash)
            .createdAt(LocalDateTime.now())
            .build();
        
        passwordHistoryRepository.save(historyEntry);
        
        // Clean old entries
        cleanOldHistory(userId);
    }
    
    /**
     * Removes old password history entries
     */
    @Transactional
    private void cleanOldHistory(String userId) {
        List<PasswordHistory> allHistory = passwordHistoryRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
        
        if (allHistory.size() > MAX_HISTORY_SIZE) {
            List<PasswordHistory> toDelete = allHistory.subList(MAX_HISTORY_SIZE, allHistory.size());
            passwordHistoryRepository.deleteAll(toDelete);
        }
    }
}