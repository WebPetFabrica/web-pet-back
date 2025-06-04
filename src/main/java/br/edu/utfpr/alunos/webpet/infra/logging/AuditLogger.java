package br.edu.utfpr.alunos.webpet.infra.logging;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuditLogger {
    
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    private static final Logger SECURITY_LOG = LoggerFactory.getLogger("SECURITY");
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void logAuthentication(String email, String action, boolean success, String reason) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .action(action)
            .userEmail(email)
            .success(success)
            .reason(reason)
            .correlationId(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY))
            .ipAddress(getClientIp())
            .build();
            
        if (success) {
            SECURITY_LOG.info("Authentication successful: {}", toJson(event));
        } else {
            SECURITY_LOG.warn("Authentication failed: {}", toJson(event));
        }
    }
    
    public void logUserRegistration(String email, String userType, boolean success, String reason) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .action("USER_REGISTRATION")
            .userEmail(email)
            .success(success)
            .reason(reason)
            .correlationId(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY))
            .ipAddress(getClientIp())
            .details(Map.of("userType", userType))
            .build();
            
        AUDIT_LOG.info("User registration: {}", toJson(event));
    }
    
    public void logAccountAction(String email, String action, boolean success, String reason) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .action(action)
            .userEmail(email)
            .success(success)
            .reason(reason)
            .correlationId(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY))
            .ipAddress(getClientIp())
            .build();
            
        AUDIT_LOG.info("Account action: {}", toJson(event));
    }
    
    public void logSecurityEvent(String event, String severity, String description) {
        SecurityEvent secEvent = SecurityEvent.builder()
            .timestamp(LocalDateTime.now())
            .event(event)
            .severity(severity)
            .description(description)
            .correlationId(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY))
            .ipAddress(getClientIp())
            .build();
            
        SECURITY_LOG.warn("Security event: {}", toJson(secEvent));
    }
    
    public void logRateLimitExceeded(String clientIp, String endpoint) {
        SecurityEvent event = SecurityEvent.builder()
            .timestamp(LocalDateTime.now())
            .event("RATE_LIMIT_EXCEEDED")
            .severity("MEDIUM")
            .description("Rate limit exceeded for endpoint: " + endpoint)
            .correlationId(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY))
            .ipAddress(clientIp)
            .build();
            
        SECURITY_LOG.warn("Rate limit exceeded: {}", toJson(event));
    }
    
    public void logTokenValidation(String email, boolean success, String reason) {
        AuditEvent event = AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .action("TOKEN_VALIDATION")
            .userEmail(email)
            .success(success)
            .reason(reason)
            .correlationId(MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY))
            .ipAddress(getClientIp())
            .build();
            
        if (success) {
            SECURITY_LOG.debug("Token validation successful: {}", toJson(event));
        } else {
            SECURITY_LOG.warn("Token validation failed: {}", toJson(event));
        }
    }
    
    private String getClientIp() {
        return MDC.get("clientIp");
    }
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
    
    public static class AuditEvent {
        public LocalDateTime timestamp;
        public String action;
        public String userEmail;
        public boolean success;
        public String reason;
        public String correlationId;
        public String ipAddress;
        public Map<String, Object> details;
        
        public static AuditEventBuilder builder() {
            return new AuditEventBuilder();
        }
        
        public static class AuditEventBuilder {
            private final AuditEvent event = new AuditEvent();
            
            public AuditEventBuilder timestamp(LocalDateTime timestamp) {
                event.timestamp = timestamp;
                return this;
            }
            
            public AuditEventBuilder action(String action) {
                event.action = action;
                return this;
            }
            
            public AuditEventBuilder userEmail(String userEmail) {
                event.userEmail = userEmail;
                return this;
            }
            
            public AuditEventBuilder success(boolean success) {
                event.success = success;
                return this;
            }
            
            public AuditEventBuilder reason(String reason) {
                event.reason = reason;
                return this;
            }
            
            public AuditEventBuilder correlationId(String correlationId) {
                event.correlationId = correlationId;
                return this;
            }
            
            public AuditEventBuilder ipAddress(String ipAddress) {
                event.ipAddress = ipAddress;
                return this;
            }
            
            public AuditEventBuilder details(Map<String, Object> details) {
                event.details = details;
                return this;
            }
            
            public AuditEvent build() {
                return event;
            }
        }
    }
    
    public static class SecurityEvent {
        public LocalDateTime timestamp;
        public String event;
        public String severity;
        public String description;
        public String correlationId;
        public String ipAddress;
        
        public static SecurityEventBuilder builder() {
            return new SecurityEventBuilder();
        }
        
        public static class SecurityEventBuilder {
            private final SecurityEvent event = new SecurityEvent();
            
            public SecurityEventBuilder timestamp(LocalDateTime timestamp) {
                event.timestamp = timestamp;
                return this;
            }
            
            public SecurityEventBuilder event(String eventType) {
                event.event = eventType;
                return this;
            }
            
            public SecurityEventBuilder severity(String severity) {
                event.severity = severity;
                return this;
            }
            
            public SecurityEventBuilder description(String description) {
                event.description = description;
                return this;
            }
            
            public SecurityEventBuilder correlationId(String correlationId) {
                event.correlationId = correlationId;
                return this;
            }
            
            public SecurityEventBuilder ipAddress(String ipAddress) {
                event.ipAddress = ipAddress;
                return this;
            }
            
            public SecurityEvent build() {
                return event;
            }
        }
    }
}