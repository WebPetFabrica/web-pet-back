package br.edu.utfpr.alunos.webpet.infra.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExceptionLogger {
    
    public void logAuthenticationFailure(String email, String reason, String correlationId) {
        MDC.put("event", "AUTH_FAILURE");
        MDC.put("user_email", email);
        MDC.put("failure_reason", reason);
        log.warn("Authentication failed for user: {} - reason: {} [correlationId: {}]", 
            email, reason, correlationId);
        MDC.remove("event");
        MDC.remove("user_email");
        MDC.remove("failure_reason");
    }
    
    public void logBusinessError(String operation, String error, String userId) {
        MDC.put("event", "BUSINESS_ERROR");
        MDC.put("operation", operation);
        MDC.put("user_id", userId);
        log.warn("Business error in operation: {} - error: {} [userId: {}]", 
            operation, error, userId);
        MDC.remove("event");
        MDC.remove("operation");
        MDC.remove("user_id");
    }
    
    public void logSystemError(String component, String error, Throwable cause) {
        MDC.put("event", "SYSTEM_ERROR");
        MDC.put("component", component);
        log.error("System error in component: {} - error: {}", component, error, cause);
        MDC.remove("event");
        MDC.remove("component");
    }
    
    public void logValidationError(String field, String value, String reason) {
        MDC.put("event", "VALIDATION_ERROR");
        MDC.put("field", field);
        log.warn("Validation error - field: {} value: {} reason: {}", field, value, reason);
        MDC.remove("event");
        MDC.remove("field");
    }
}