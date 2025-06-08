package br.edu.utfpr.alunos.webpet.infra.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("Validation error: {} - {} [correlationId: {}]", 
            e.getErrorCode().getCode(), e.getMessage(), correlationId);
        
        ErrorResponse error = ErrorResponse.of(e.getErrorCode(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(br.edu.utfpr.alunos.webpet.infra.exception.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            br.edu.utfpr.alunos.webpet.infra.exception.AuthenticationException e, 
            HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("Authentication error: {} - {} [correlationId: {}]", 
            e.getErrorCode().getCode(), e.getMessage(), correlationId);
        
        ErrorResponse error = ErrorResponse.of(e.getErrorCode(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ErrorResponse> handleSystemException(SystemException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("System error: {} - {} [correlationId: {}]", 
            e.getErrorCode().getCode(), e.getMessage(), correlationId, e);
        
        ErrorResponse error = ErrorResponse.of(e.getErrorCode(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String clientIp = getClientIpAddress(request);
        log.warn("Rate limit exceeded for IP: {} - {} [correlationId: {}]", 
            clientIp, e.getMessage(), correlationId);
        
        ErrorResponse error = ErrorResponse.of(e.getErrorCode(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("Constraint validation error: {} violations [correlationId: {}]", 
            e.getConstraintViolations().size(), correlationId);
        
        Map<String, List<String>> fieldErrors = e.getConstraintViolations().stream()
            .collect(Collectors.groupingBy(
                violation -> violation.getPropertyPath().toString(),
                Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
            ));
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.VALIDATION_REQUIRED_FIELD, request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("Business rule violation: {} [correlationId: {}]", e.getMessage(), correlationId);
        
        ErrorResponse error = ErrorResponse.of(
            ErrorCode.VALIDATION_REQUIRED_FIELD, 
            request.getRequestURI(),
            Map.of("business", List.of(e.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String clientIp = getClientIpAddress(request);
        log.warn("Account locked attempt from IP: {} - {} [correlationId: {}]", 
            clientIp, e.getMessage(), correlationId);
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.AUTH_ACCOUNT_LOCKED, request.getRequestURI())
            .withAdditionalInfo("lockoutDuration", "30 minutos")
            .withAdditionalInfo("retryAfter", Instant.now().plusSeconds(1800).toString());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("Method argument validation error: {} field errors [correlationId: {}]", 
            e.getBindingResult().getFieldErrorCount(), correlationId);
        
        Map<String, List<String>> fieldErrors = e.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.groupingBy(
                FieldError::getField,
                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
            ));
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.VALIDATION_REQUIRED_FIELD, request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String clientIp = getClientIpAddress(request);
        log.warn("Bad credentials attempt from IP: {} [correlationId: {}]", clientIp, correlationId);
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.AUTH_INVALID_CREDENTIALS, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");
        log.warn("Access denied for user: {} [correlationId: {}]", userId, correlationId);
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.AUTH_ACCOUNT_INACTIVE, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSpringAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("Spring authentication error: {} [correlationId: {}]", e.getMessage(), correlationId);
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.AUTH_INVALID_CREDENTIALS, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("Illegal argument: {} [correlationId: {}]", e.getMessage(), correlationId);
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.VALIDATION_REQUIRED_FIELD, request.getRequestURI())
            .withAdditionalInfo("argument", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("Unexpected runtime error [correlationId: {}]", correlationId, e);
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.SYSTEM_INTERNAL_ERROR, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("Unexpected error [correlationId: {}]", correlationId, e);
        
        ErrorResponse error = ErrorResponse.of(ErrorCode.SYSTEM_INTERNAL_ERROR, request.getRequestURI())
            .withAdditionalInfo("type", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Extracts the real client IP address from the request.
     * Handles common proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}