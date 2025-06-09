package br.edu.utfpr.alunos.webpet.infra.exception;

import lombok.Getter;

/**
 * Exception thrown when validation errors occur.
 * 
 * <p>This exception is used for validation failures such as:
 * <ul>
 *   <li>Invalid input data format (email, CPF, CNPJ)</li>
 *   <li>Business rule violations</li>
 *   <li>Required field validation failures</li>
 *   <li>Data constraint violations</li>
 *   <li>Password policy violations</li>
 * </ul>
 * 
 * <p>The exception carries an {@link ErrorCode} that provides
 * structured error information for proper error handling and
 * client-side validation feedback.
 * 
 */
@Getter
public class ValidationException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String fieldName;
    private final Object invalidValue;
    
    /**
     * Creates a validation exception with the specified error code.
     * 
     * @param errorCode the error code describing the validation failure
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldName = null;
        this.invalidValue = null;
    }
    
    /**
     * Creates a validation exception with a custom message.
     * 
     * @param errorCode the error code describing the validation failure
     * @param customMessage custom message overriding the default error code message
     */
    public ValidationException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.fieldName = null;
        this.invalidValue = null;
    }
    
    /**
     * Creates a validation exception with field-specific information.
     * 
     * @param errorCode the error code describing the validation failure
     * @param fieldName the name of the field that failed validation
     * @param invalidValue the invalid value that caused the failure
     */
    public ValidationException(ErrorCode errorCode, String fieldName, Object invalidValue) {
        super(String.format("%s - Field: %s", errorCode.getMessage(), fieldName));
        this.errorCode = errorCode;
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
    
    /**
     * Creates a validation exception with field and custom message.
     * 
     * @param errorCode the error code describing the validation failure
     * @param fieldName the name of the field that failed validation
     * @param customMessage custom message describing the validation failure
     */
    public ValidationException(ErrorCode errorCode, String fieldName, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.fieldName = fieldName;
        this.invalidValue = null;
    }
    
    /**
     * Creates a validation exception with complete details.
     * 
     * @param errorCode the error code describing the validation failure
     * @param fieldName the name of the field that failed validation
     * @param invalidValue the invalid value that caused the failure
     * @param customMessage custom message describing the validation failure
     */
    public ValidationException(ErrorCode errorCode, String fieldName, Object invalidValue, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
    
    /**
     * Returns the formatted error message with error code.
     * 
     * @return formatted error message including error code
     */
    public String getFormattedMessage() {
        return errorCode.getFormattedMessage();
    }
    
    /**
     * Checks if this validation error is related to a specific field.
     * 
     * @return true if field name is specified
     */
    public boolean hasFieldName() {
        return fieldName != null && !fieldName.trim().isEmpty();
    }
    
    /**
     * Checks if the invalid value information is available.
     * 
     * @return true if invalid value is specified
     */
    public boolean hasInvalidValue() {
        return invalidValue != null;
    }
    
    /**
     * Gets a sanitized version of the invalid value for logging.
     * Masks sensitive information like passwords or tokens.
     * 
     * @return sanitized invalid value safe for logging
     */
    public String getSanitizedInvalidValue() {
        if (invalidValue == null) {
            return "null";
        }
        
        String value = invalidValue.toString();
        
        // Mask sensitive fields
        if (fieldName != null) {
            String lowerFieldName = fieldName.toLowerCase();
            if (lowerFieldName.contains("password") || 
                lowerFieldName.contains("token") || 
                lowerFieldName.contains("secret")) {
                return "***MASKED***";
            }
            
            // Partially mask email addresses
            if (lowerFieldName.contains("email") && value.contains("@")) {
                String[] parts = value.split("@");
                if (parts.length == 2 && parts[0].length() > 2) {
                    return parts[0].substring(0, 2) + "***@" + parts[1];
                }
            }
            
            // Partially mask CPF/CNPJ
            if ((lowerFieldName.contains("cpf") || lowerFieldName.contains("cnpj")) && value.length() > 4) {
                return value.substring(0, 3) + "***" + value.substring(value.length() - 2);
            }
        }
        
        // For long values, truncate
        if (value.length() > 50) {
            return value.substring(0, 47) + "...";
        }
        
        return value;
    }
    
    /**
     * Creates a user-friendly error message for API responses.
     * 
     * @return user-friendly error message
     */
    public String getUserFriendlyMessage() {
        if (hasFieldName()) {
            return String.format("Erro de validação no campo '%s': %s", fieldName, errorCode.getMessage());
        }
        return errorCode.getMessage();
    }
    
    /**
     * Checks if this is a validation error that should be logged as a warning.
     * Some validation errors are expected and don't require error-level logging.
     * 
     * @return true if should be logged as warning instead of error
     */
    public boolean isExpectedValidationError() {
        return errorCode == ErrorCode.VALIDATION_INVALID_EMAIL ||
               errorCode == ErrorCode.VALIDATION_INVALID_CPF ||
               errorCode == ErrorCode.VALIDATION_INVALID_CNPJ ||
               errorCode == ErrorCode.VALIDATION_REQUIRED_FIELD;
    }
    
    /**
     * Creates a validation exception for required field errors.
     * 
     * @param fieldName the name of the required field
     * @return ValidationException for required field
     */
    public static ValidationException requiredField(String fieldName) {
        return new ValidationException(
            ErrorCode.VALIDATION_REQUIRED_FIELD, 
            fieldName, 
            String.format("Campo '%s' é obrigatório", fieldName)
        );
    }
    
    /**
     * Creates a validation exception for invalid email format.
     * 
     * @param email the invalid email value
     * @return ValidationException for invalid email
     */
    public static ValidationException invalidEmail(String email) {
        return new ValidationException(
            ErrorCode.VALIDATION_INVALID_EMAIL, 
            "email", 
            email,
            "Formato de email inválido"
        );
    }
    
    /**
     * Creates a validation exception for invalid CPF format.
     * 
     * @param cpf the invalid CPF value
     * @return ValidationException for invalid CPF
     */
    public static ValidationException invalidCPF(String cpf) {
        return new ValidationException(
            ErrorCode.VALIDATION_INVALID_CPF, 
            "cpf", 
            cpf,
            "CPF inválido"
        );
    }
    
    /**
     * Creates a validation exception for invalid CNPJ format.
     * 
     * @param cnpj the invalid CNPJ value
     * @return ValidationException for invalid CNPJ
     */
    public static ValidationException invalidCNPJ(String cnpj) {
        return new ValidationException(
            ErrorCode.VALIDATION_INVALID_CNPJ, 
            "cnpj", 
            cnpj,
            "CNPJ inválido"
        );
    }
}