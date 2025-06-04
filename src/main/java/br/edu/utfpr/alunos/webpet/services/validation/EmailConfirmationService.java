package br.edu.utfpr.alunos.webpet.services.validation;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.EmailConfirmation;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.infra.exception.ValidationException;
import br.edu.utfpr.alunos.webpet.repositories.EmailConfirmationRepository;
import br.edu.utfpr.alunos.webpet.services.email.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Service for managing email confirmation functionality.
 * 
 * <p>This service handles the complete email confirmation workflow including:
 * <ul>
 *   <li>Generation of cryptographically secure confirmation tokens</li>
 *   <li>Sending confirmation emails via notification service</li>
 *   <li>Token validation and expiration checking</li>
 *   <li>Email confirmation status tracking</li>
 *   <li>Automatic cleanup of expired tokens</li>
 * </ul>
 * 
 * <p>Security Features:
 * <ul>
 *   <li>Tokens are generated using SecureRandom for cryptographic security</li>
 *   <li>Token expiration prevents replay attacks</li>
 *   <li>One-time use tokens (marked as used after confirmation)</li>
 *   <li>Comprehensive audit logging</li>
 * </ul>
 * 
 * @author WebPet Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailConfirmationService {
    
    private final EmailConfirmationRepository emailConfirmationRepository;
    private final EmailNotificationService emailNotificationService;
    
    private static final int CONFIRMATION_EXPIRY_HOURS = 24;
    private static final int TOKEN_LENGTH = 32; // 32 bytes = 64 hex characters
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generates and sends email confirmation token to user.
     * 
     * <p>Creates a cryptographically secure token and stores it in the database
     * with an expiration time. Then sends the confirmation email to the user.
     * 
     * @param user the user to send confirmation email to
     * @throws ValidationException if email sending fails
     */
    @Transactional
    public void sendConfirmationEmail(BaseUser user) {
        String correlationId = MDC.get("correlationId");
        log.info("Sending email confirmation to user: {} [correlationId: {}]", 
            user.getEmail(), correlationId);
        
        try {
            // Invalidate any existing pending confirmations for this user
            invalidateExistingConfirmations(user.getId());
            
            // Generate cryptographically secure token
            String token = generateSecureToken();
            
            EmailConfirmation confirmation = EmailConfirmation.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(CONFIRMATION_EXPIRY_HOURS))
                .confirmed(false)
                .createdAt(LocalDateTime.now())
                .build();
            
            emailConfirmationRepository.save(confirmation);
            log.debug("Email confirmation record created for user: {} [correlationId: {}]", 
                user.getEmail(), correlationId);
            
            // Send confirmation email
            emailNotificationService.sendConfirmationEmail(user.getEmail(), user.getDisplayName(), token);
            
            log.info("Email confirmation sent successfully to: {} [correlationId: {}]", 
                user.getEmail(), correlationId);
            
        } catch (Exception e) {
            log.error("Failed to send email confirmation to: {} [correlationId: {}]", 
                user.getEmail(), correlationId, e);
            throw new ValidationException(ErrorCode.SYSTEM_EXTERNAL_SERVICE_ERROR);
        }
    }
    
    /**
     * Confirms email using the provided token.
     * 
     * <p>Validates the token, checks expiration, and marks the email as confirmed.
     * Tokens can only be used once and become invalid after confirmation.
     * 
     * @param token the confirmation token
     * @return true if confirmation was successful, false otherwise
     * @throws ValidationException if token is expired
     */
    @Transactional
    public boolean confirmEmail(String token) {
        String correlationId = MDC.get("correlationId");
        log.info("Email confirmation attempt with token: {} [correlationId: {}]", 
            token.substring(0, 8) + "...", correlationId); // Log only first 8 chars for security
        
        try {
            Optional<EmailConfirmation> confirmationOpt = emailConfirmationRepository.findByToken(token);
            
            if (confirmationOpt.isEmpty()) {
                log.warn("Email confirmation failed - token not found [correlationId: {}]", correlationId);
                return false;
            }
            
            EmailConfirmation confirmation = confirmationOpt.get();
            
            // Check if already confirmed
            if (confirmation.isConfirmed()) {
                log.warn("Email confirmation failed - token already used for email: {} [correlationId: {}]", 
                    confirmation.getEmail(), correlationId);
                return false;
            }
            
            // Check expiration
            if (confirmation.isExpired()) {
                log.warn("Email confirmation failed - token expired for email: {} [correlationId: {}]", 
                    confirmation.getEmail(), correlationId);
                throw new ValidationException(ErrorCode.VALIDATION_EMAIL_CONFIRMATION_EXPIRED);
            }
            
            // Mark as confirmed
            confirmation.setConfirmed(true);
            confirmation.setConfirmedAt(LocalDateTime.now());
            emailConfirmationRepository.save(confirmation);
            
            log.info("Email confirmed successfully for: {} [correlationId: {}]", 
                confirmation.getEmail(), correlationId);
            
            return true;
            
        } catch (ValidationException e) {
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            log.error("Unexpected error during email confirmation [correlationId: {}]", 
                correlationId, e);
            return false;
        }
    }
    
    /**
     * Checks if a user's email is confirmed.
     * 
     * @param userId the user's unique identifier
     * @return true if email is confirmed, false otherwise
     */
    public boolean isEmailConfirmed(String userId) {
        String correlationId = MDC.get("correlationId");
        log.debug("Checking email confirmation status for user: {} [correlationId: {}]", 
            userId, correlationId);
        
        try {
            boolean isConfirmed = emailConfirmationRepository.findLatestByUserId(userId)
                .map(EmailConfirmation::isConfirmed)
                .orElse(false);
            
            log.debug("Email confirmation status for user {}: {} [correlationId: {}]", 
                userId, isConfirmed, correlationId);
            
            return isConfirmed;
            
        } catch (Exception e) {
            log.error("Error checking email confirmation for user: {} [correlationId: {}]", 
                userId, correlationId, e);
            return false; // Fail safe - assume not confirmed
        }
    }
    
    /**
     * Resends confirmation email to user.
     * 
     * @param user the user to resend confirmation to
     * @throws ValidationException if user email is already confirmed
     */
    @Transactional
    public void resendConfirmationEmail(BaseUser user) {
        String correlationId = MDC.get("correlationId");
        log.info("Resending email confirmation for user: {} [correlationId: {}]", 
            user.getEmail(), correlationId);
        
        if (isEmailConfirmed(user.getId())) {
            log.warn("Cannot resend confirmation - email already confirmed for user: {} [correlationId: {}]", 
                user.getEmail(), correlationId);
            throw new ValidationException(ErrorCode.VALIDATION_EMAIL_NOT_CONFIRMED);
        }
        
        sendConfirmationEmail(user);
    }
    
    /**
     * Gets confirmation details for a user.
     * 
     * @param userId the user's unique identifier
     * @return confirmation details or empty if not found
     */
    public Optional<EmailConfirmation> getConfirmationDetails(String userId) {
        String correlationId = MDC.get("correlationId");
        log.debug("Getting confirmation details for user: {} [correlationId: {}]", 
            userId, correlationId);
        
        return emailConfirmationRepository.findLatestByUserId(userId);
    }
    
    /**
     * Cleans up expired confirmation tokens.
     * This method should be called periodically by a scheduled job.
     * 
     * @return number of expired tokens cleaned up
     */
    @Transactional
    public int cleanupExpiredTokens() {
        String correlationId = MDC.get("correlationId");
        log.info("Starting cleanup of expired email confirmation tokens [correlationId: {}]", 
            correlationId);
        
        try {
            int deletedCount = emailConfirmationRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Cleaned up {} expired email confirmation tokens [correlationId: {}]", 
                deletedCount, correlationId);
            
            return deletedCount;
            
        } catch (Exception e) {
            log.error("Error during cleanup of expired tokens [correlationId: {}]", 
                correlationId, e);
            return 0;
        }
    }
    
    /**
     * Generates a cryptographically secure token for email confirmation.
     * 
     * @return 64-character hex string token
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return HexFormat.of().formatHex(tokenBytes);
    }
    
    /**
     * Invalidates any existing pending confirmations for a user.
     * This prevents token accumulation and ensures only the latest token is valid.
     * 
     * @param userId the user's unique identifier
     */
    private void invalidateExistingConfirmations(String userId) {
        String correlationId = MDC.get("correlationId");
        log.debug("Invalidating existing confirmations for user: {} [correlationId: {}]", 
            userId, correlationId);
        
        try {
            int invalidatedCount = emailConfirmationRepository.invalidatePendingConfirmations(userId);
            if (invalidatedCount > 0) {
                log.debug("Invalidated {} existing confirmation tokens for user: {} [correlationId: {}]", 
                    invalidatedCount, userId, correlationId);
            }
        } catch (Exception e) {
            log.warn("Error invalidating existing confirmations for user: {} [correlationId: {}]", 
                userId, correlationId, e);
            // Don't throw exception as this is not critical for the main flow
        }
    }
}