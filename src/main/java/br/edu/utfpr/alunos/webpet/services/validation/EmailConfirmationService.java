package br.edu.utfpr.alunos.webpet.services.validation;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.EmailConfirmation;
import br.edu.utfpr.alunos.webpet.repositories.EmailConfirmationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for email confirmation functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailConfirmationService {
    
    private final EmailConfirmationRepository emailConfirmationRepository;
    private final EmailNotificationService emailNotificationService;
    
    private static final int CONFIRMATION_EXPIRY_HOURS = 24;
    
    /**
     * Generates and sends email confirmation
     */
    @Transactional
    public void sendConfirmationEmail(BaseUser user) {
        String token = UUID.randomUUID().toString();
        
        EmailConfirmation confirmation = EmailConfirmation.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .token(token)
            .expiresAt(LocalDateTime.now().plusHours(CONFIRMATION_EXPIRY_HOURS))
            .build();
        
        emailConfirmationRepository.save(confirmation);
        
        // Send confirmation email
        emailNotificationService.sendConfirmationEmail(user.getEmail(), token);
        
        log.info("Email confirmation sent to: {}", user.getEmail());
    }
    
    /**
     * Confirms email with token
     */
    @Transactional
    public boolean confirmEmail(String token) {
        return emailConfirmationRepository.findByToken(token)
            .filter(confirmation -> !confirmation.isExpired())
            .map(confirmation -> {
                confirmation.setConfirmed(true);
                confirmation.setConfirmedAt(LocalDateTime.now());
                emailConfirmationRepository.save(confirmation);
                return true;
            })
            .orElse(false);
    }
    
    /**
     * Checks if email is confirmed
     */
    public boolean isEmailConfirmed(String userId) {
        return emailConfirmationRepository.findByUserId(userId)
            .map(EmailConfirmation::isConfirmed)
            .orElse(false);
    }
}