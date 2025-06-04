package br.edu.utfpr.alunos.webpet.services.validation;

import br.edu.utfpr.alunos.webpet.domain.user.PasswordHistory;
import br.edu.utfpr.alunos.webpet.repositories.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing password history to prevent reuse of recent passwords.
 * 
 * <p>This service implements password history tracking as a security measure
 * to prevent users from reusing their recent passwords. It maintains a configurable
 * number of previous passwords per user and validates new passwords against this history.
 * 
 * <p>Security Features:
 * <ul>
 *   <li>Prevents password reuse for last 5 passwords</li>
 *   <li>Uses secure password comparison with BCrypt</li>
 *   <li>Automatic cleanup of old password history</li>
 *   <li>Comprehensive audit logging</li>
 * </ul>
 * 
 * @author WebPet Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordHistoryService {
    
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final int MAX_HISTORY_SIZE = 5;
    
    /**
     * Checks if a password was used recently by the user.
     * 
     * <p>This method retrieves the user's password history and compares
     * the new password against the stored hashes using secure comparison.
     * 
     * @param userId the user's unique identifier
     * @param newPassword the new password to validate (plain text)
     * @return true if password was used recently, false otherwise
     */
    public boolean isPasswordReused(String userId, String newPassword) {
        String correlationId = MDC.get("correlationId");
        log.debug("Checking password reuse for user: {} [correlationId: {}]", userId, correlationId);
        
        try {
            List<PasswordHistory> history = passwordHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
            
            boolean isReused = history.stream()
                .limit(MAX_HISTORY_SIZE)
                .anyMatch(h -> passwordEncoder.matches(newPassword, h.getPasswordHash()));
            
            if (isReused) {
                log.warn("Password reuse detected for user: {} [correlationId: {}]", userId, correlationId);
            } else {
                log.debug("Password validation passed for user: {} [correlationId: {}]", userId, correlationId);
            }
            
            return isReused;
            
        } catch (Exception e) {
            log.error("Error checking password reuse for user: {} [correlationId: {}]", 
                userId, correlationId, e);
            // In case of error, allow password change (fail open for usability)
            return false;
        }
    }
    
    /**
     * Saves a password hash to the user's history.
     * 
     * <p>This method creates a new password history entry and automatically
     * cleans up old entries to maintain the configured history size.
     * 
     * @param userId the user's unique identifier
     * @param passwordHash the BCrypt hashed password
     */
    @Transactional
    public void savePasswordToHistory(String userId, String passwordHash) {
        String correlationId = MDC.get("correlationId");
        log.info("Saving password to history for user: {} [correlationId: {}]", userId, correlationId);
        
        try {
            // Create new history entry
            PasswordHistory historyEntry = PasswordHistory.builder()
                .userId(userId)
                .passwordHash(passwordHash)
                .createdAt(LocalDateTime.now())
                .build();
            
            passwordHistoryRepository.save(historyEntry);
            log.debug("Password history entry created for user: {} [correlationId: {}]", 
                userId, correlationId);
            
            // Clean old entries to maintain history size
            cleanOldHistory(userId);
            
            log.info("Password history updated successfully for user: {} [correlationId: {}]", 
                userId, correlationId);
            
        } catch (Exception e) {
            log.error("Failed to save password history for user: {} [correlationId: {}]", 
                userId, correlationId, e);
            throw new RuntimeException("Failed to save password history", e);
        }
    }
    
    /**
     * Removes old password history entries to maintain the configured history size.
     * 
     * <p>This method ensures that only the most recent passwords are kept
     * in the history, automatically removing older entries.
     * 
     * @param userId the user's unique identifier
     */
    @Transactional
    private void cleanOldHistory(String userId) {
        String correlationId = MDC.get("correlationId");
        log.debug("Cleaning old password history for user: {} [correlationId: {}]", 
            userId, correlationId);
        
        try {
            List<PasswordHistory> allHistory = passwordHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
            
            if (allHistory.size() > MAX_HISTORY_SIZE) {
                List<PasswordHistory> toDelete = allHistory.subList(MAX_HISTORY_SIZE, allHistory.size());
                passwordHistoryRepository.deleteAll(toDelete);
                
                log.info("Cleaned {} old password history entries for user: {} [correlationId: {}]", 
                    toDelete.size(), userId, correlationId);
            } else {
                log.debug("No cleanup needed - history size: {} for user: {} [correlationId: {}]", 
                    allHistory.size(), userId, correlationId);
            }
            
        } catch (Exception e) {
            log.error("Failed to clean old password history for user: {} [correlationId: {}]", 
                userId, correlationId, e);
            // Don't throw exception here as cleanup failure shouldn't block password change
        }
    }
    
    /**
     * Gets the password history count for a user.
     * 
     * @param userId the user's unique identifier
     * @return number of passwords in history
     */
    public int getPasswordHistoryCount(String userId) {
        String correlationId = MDC.get("correlationId");
        log.debug("Getting password history count for user: {} [correlationId: {}]", 
            userId, correlationId);
        
        try {
            int count = passwordHistoryRepository.countByUserId(userId);
            log.debug("Password history count: {} for user: {} [correlationId: {}]", 
                count, userId, correlationId);
            return count;
            
        } catch (Exception e) {
            log.error("Failed to get password history count for user: {} [correlationId: {}]", 
                userId, correlationId, e);
            return 0;
        }
    }
    
    /**
     * Clears all password history for a user.
     * This should only be used in special circumstances (e.g., user deletion).
     * 
     * @param userId the user's unique identifier
     */
    @Transactional
    public void clearPasswordHistory(String userId) {
        String correlationId = MDC.get("correlationId");
        log.warn("Clearing all password history for user: {} [correlationId: {}]", 
            userId, correlationId);
        
        try {
            int deletedCount = passwordHistoryRepository.deleteByUserId(userId);
            log.info("Cleared {} password history entries for user: {} [correlationId: {}]", 
                deletedCount, userId, correlationId);
            
        } catch (Exception e) {
            log.error("Failed to clear password history for user: {} [correlationId: {}]", 
                userId, correlationId, e);
            throw new RuntimeException("Failed to clear password history", e);
        }
    }
}