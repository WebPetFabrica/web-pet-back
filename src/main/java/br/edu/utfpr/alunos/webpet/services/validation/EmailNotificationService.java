package br.edu.utfpr.alunos.webpet.services.validation;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending email notifications.
 * 
 * <p>This service provides email notification functionality for the WebPet system.
 * Currently implements a development/logging version that outputs to logs.
 * In production, this should be integrated with an email service provider
 * like SendGrid, AWS SES, or similar.
 * 
 * <p>Supported notifications:
 * <ul>
 *   <li>Email confirmation for new accounts</li>
 *   <li>Password change notifications</li>
 *   <li>Account security alerts</li>
 *   <li>Welcome emails for new users</li>
 * </ul>
 * 
 * @author WebPet Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class EmailNotificationService {
    
    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    private static final DateTimeFormatter EXPIRY_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    
    /**
     * Sends email confirmation token with user's display name.
     * 
     * <p>Generates a confirmation URL and logs it for development.
     * In production, this should send an actual HTML email with:
     * <ul>
     *   <li>Personalized greeting with display name</li>
     *   <li>Confirmation button/link</li>
     *   <li>Token expiration information</li>
     *   <li>Security instructions</li>
     * </ul>
     * 
     * @param email the recipient's email address
     * @param displayName the user's display name for personalization
     * @param token the confirmation token
     */
    public void sendConfirmationEmail(String email, String displayName, String token) {
        String correlationId = MDC.get("correlationId");
        log.info("Sending email confirmation to: {} ({}) [correlationId: {}]", 
            displayName, email, correlationId);
        
        try {
            String confirmationUrl = buildConfirmationUrl(token);
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);
            
            // Log the email content for development
            logEmailContent(email, displayName, confirmationUrl, expiryTime, correlationId);
            
            // TODO: In production, integrate with email service provider
            sendActualEmail(email, displayName, confirmationUrl, expiryTime);
            
            log.info("Email confirmation sent successfully to: {} [correlationId: {}]", 
                email, correlationId);
            
        } catch (Exception e) {
            log.error("Failed to send email confirmation to: {} [correlationId: {}]", 
                email, correlationId, e);
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }
    
    /**
     * Sends password change notification to user.
     * 
     * @param email the user's email address
     * @param displayName the user's display name
     */
    public void sendPasswordChangeNotification(String email, String displayName) {
        String correlationId = MDC.get("correlationId");
        log.info("Sending password change notification to: {} ({}) [correlationId: {}]", 
            displayName, email, correlationId);
        
        try {
            String timestamp = LocalDateTime.now().format(EXPIRY_FORMATTER);
            
            log.info("""
                
                ===============================================
                PASSWORD CHANGE NOTIFICATION
                ===============================================
                Para: {} <{}>
                Assunto: Senha alterada - WebPet
                
                Olá {},
                
                Sua senha foi alterada com sucesso em {}.
                
                Se você não fez esta alteração, entre em contato 
                conosco imediatamente através do suporte.
                
                Atenciosamente,
                Equipe WebPet
                ===============================================
                [correlationId: {}]
                """, displayName, email, displayName, timestamp, correlationId);
            
            // TODO: Send actual email in production
            
        } catch (Exception e) {
            log.error("Failed to send password change notification to: {} [correlationId: {}]", 
                email, correlationId, e);
        }
    }
    
    /**
     * Sends welcome email to new users.
     * 
     * @param email the user's email address
     * @param displayName the user's display name
     * @param userType the type of user (USER, ONG, PROTETOR)
     */
    public void sendWelcomeEmail(String email, String displayName, String userType) {
        String correlationId = MDC.get("correlationId");
        log.info("Sending welcome email to: {} ({}) as {} [correlationId: {}]", 
            displayName, email, userType, correlationId);
        
        try {
            String welcomeMessage = getWelcomeMessage(displayName, userType);
            
            log.info("""
                
                ===============================================
                WELCOME EMAIL
                ===============================================
                Para: {} <{}>
                Assunto: Bem-vindo ao WebPet!
                
                {}
                
                Acesse: {}
                
                Atenciosamente,
                Equipe WebPet
                ===============================================
                [correlationId: {}]
                """, displayName, email, welcomeMessage, frontendUrl, correlationId);
            
            // TODO: Send actual email in production
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {} [correlationId: {}]", 
                email, correlationId, e);
        }
    }
    
    /**
     * Sends security alert email.
     * 
     * @param email the user's email address
     * @param displayName the user's display name
     * @param alertType the type of security alert
     * @param details additional details about the alert
     */
    public void sendSecurityAlert(String email, String displayName, String alertType, String details) {
        String correlationId = MDC.get("correlationId");
        log.warn("Sending security alert to: {} ({}) - Type: {} [correlationId: {}]", 
            displayName, email, alertType, correlationId);
        
        try {
            String timestamp = LocalDateTime.now().format(EXPIRY_FORMATTER);
            
            log.warn("""
                
                ===============================================
                SECURITY ALERT
                ===============================================
                Para: {} <{}>
                Assunto: Alerta de Segurança - WebPet
                
                Olá {},
                
                Detectamos uma atividade suspeita em sua conta:
                
                Tipo: {}
                Detalhes: {}
                Data/Hora: {}
                
                Se você reconhece esta atividade, pode ignorar este email.
                Caso contrário, recomendamos alterar sua senha imediatamente.
                
                Atenciosamente,
                Equipe WebPet
                ===============================================
                [correlationId: {}]
                """, displayName, email, displayName, alertType, details, timestamp, correlationId);
            
            // TODO: Send actual email in production
            
        } catch (Exception e) {
            log.error("Failed to send security alert to: {} [correlationId: {}]", 
                email, correlationId, e);
        }
    }
    
    /**
     * Builds the confirmation URL for email verification.
     * 
     * @param token the confirmation token
     * @return complete confirmation URL
     */
    private String buildConfirmationUrl(String token) {
        return String.format("%s/auth/confirm?token=%s", baseUrl, token);
    }
    
    /**
     * Logs email content for development purposes.
     */
    private void logEmailContent(String email, String displayName, String confirmationUrl, 
                                LocalDateTime expiryTime, String correlationId) {
        String expiryFormatted = expiryTime.format(EXPIRY_FORMATTER);
        
        log.info("""
            
            ===============================================
            EMAIL CONFIRMATION
            ===============================================
            Para: {} <{}>
            Assunto: Confirme seu email - WebPet
            
            Olá {},
            
            Obrigado por se cadastrar no WebPet!
            
            Para ativar sua conta, clique no link abaixo:
            {}
            
            Este link expira em: {}
            
            Se você não se cadastrou no WebPet, pode ignorar este email.
            
            Atenciosamente,
            Equipe WebPet
            ===============================================
            [correlationId: {}]
            """, displayName, email, displayName, confirmationUrl, expiryFormatted, correlationId);
    }
    
    /**
     * Placeholder for actual email sending in production.
     * TODO: Integrate with email service provider (SendGrid, AWS SES, etc.)
     */
    private void sendActualEmail(String email, String displayName, String confirmationUrl, 
                                LocalDateTime expiryTime) {
        // In production, implement actual email sending:
        // 1. Create HTML email template
        // 2. Use email service provider SDK
        // 3. Handle delivery failures
        // 4. Track email metrics
        
        log.debug("TODO: Send actual email to {} with confirmation URL: {}", email, confirmationUrl);
    }
    
    /**
     * Gets personalized welcome message based on user type.
     */
    private String getWelcomeMessage(String displayName, String userType) {
        return switch (userType.toUpperCase()) {
            case "ONG" -> String.format("""
                Olá %s!
                
                Bem-vindo ao WebPet! Sua ONG agora faz parte da nossa 
                comunidade dedicada ao bem-estar animal.
                
                Com sua conta, você pode:
                • Cadastrar animais para adoção
                • Gerenciar solicitações de adoção
                • Conectar-se com adotantes
                • Promover campanhas de conscientização
                """, displayName);
                
            case "PROTETOR" -> String.format("""
                Olá %s!
                
                Bem-vindo ao WebPet! Como protetor independente, 
                você agora tem acesso a todas as ferramentas 
                para ajudar animais em situação de vulnerabilidade.
                
                Com sua conta, você pode:
                • Cadastrar animais resgatados
                • Buscar lares temporários
                • Conectar-se com a comunidade
                • Compartilhar histórias de resgate
                """, displayName);
                
            default -> String.format("""
                Olá %s!
                
                Bem-vindo ao WebPet! Você agora faz parte da nossa 
                comunidade que conecta animais a famílias amorosas.
                
                Com sua conta, você pode:
                • Buscar pets para adoção
                • Favoritar animais de interesse
                • Entrar em contato com ONGs e protetores
                • Acompanhar o processo de adoção
                """, displayName);
        };
    }
}