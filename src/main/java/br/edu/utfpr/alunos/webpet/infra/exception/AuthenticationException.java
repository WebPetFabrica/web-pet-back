// AuthenticationException.java
package br.edu.utfpr.alunos.webpet.infra.exception;

import lombok.Getter;

/**
 * Exception thrown when authentication-related errors occur.
 * 
 * <p>This exception is used for authentication failures such as:
 * <ul>
 *   <li>Invalid credentials (wrong email/password)</li>
 *   <li>Account locked due to failed attempts</li>
 *   <li>Account disabled or inactive</li>
 *   <li>Token-related authentication issues</li>
 *   <li>Email not verified</li>
 * </ul>
 * 
 * <p>The exception carries an {@link ErrorCode} that provides
 * structured error information for proper error handling and
 * internationalization support.
 * 
 * @author WebPet Team
 * @since 1.0.0
 */
@Getter
public class AuthenticationException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    /**
     * Creates an authentication exception with the specified error code.
     * 
     * @param errorCode the error code describing the authentication failure
     */
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    /**
     * Creates an authentication exception with the specified error code and cause.
     * 
     * @param errorCode the error code describing the authentication failure
     * @param cause the underlying cause of the exception
     */
    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Creates an authentication exception with a custom message.
     * 
     * @param errorCode the error code describing the authentication failure
     * @param customMessage custom message overriding the default error code message
     */
    public AuthenticationException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    
    /**
     * Returns the formatted error message with error code.
     * 
     * @return formatted error message
     */
    public String getFormattedMessage() {
        return errorCode.getFormattedMessage();
    }
    
    /**
     * Checks if this is a security-sensitive authentication error.
     * 
     * @return true if the error is security-sensitive
     */
    public boolean isSecuritySensitive() {
        return errorCode.isSecuritySensitive();
    }
}

// ValidationException.java
package br.edu.utfpr.alunos.webpet.infra.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public ValidationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public ValidationException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}

// SystemException.java
package br.edu.utfpr.alunos.webpet.infra.exception;

import lombok.Getter;

@Getter
public class SystemException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public SystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}

// RateLimitException.java
package br.edu.utfpr.alunos.webpet.infra.exception;

import lombok.Getter;

@Getter
public class RateLimitException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public RateLimitException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}