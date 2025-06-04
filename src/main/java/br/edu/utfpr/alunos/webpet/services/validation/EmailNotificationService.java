package br.edu.utfpr.alunos.webpet.services.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications
 * Simple implementation for development - integrate with email provider for production
 */
@Slf4j
@Service
public class EmailNotificationService {
    
    /**
     * Sends email confirmation token
     */
    public void sendConfirmationEmail(String email, String token) {
        // TODO: Integrate with email service provider (SendGrid, AWS SES, etc.)
        // For now, just log the confirmation URL
        String confirmationUrl = "http://localhost:8081/auth/confirm?token=" + token;
        
        log.info("Email confirmation required for: {} - URL: {}", email, confirmationUrl);
        
        // In production, send actual email with template:
        // - Subject: "Confirm your WebPet account"
        // - Body: HTML template with confirmation link
        // - Include token expiration time (24 hours)
    }
    
    /**
     * Sends password change notification
     */
    public void sendPasswordChangeNotification(String email) {
        log.info("Password changed notification for: {}", email);
        
        // In production, send password change confirmation email
    }
}