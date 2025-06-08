package br.edu.utfpr.alunos.webpet.infra.exception;

public enum ErrorCode {
    // Authentication & Authorization
    AUTH_INVALID_CREDENTIALS("AUTH001", "Credenciais inválidas"),
    AUTH_ACCOUNT_LOCKED("AUTH002", "Conta temporariamente bloqueada"),
    AUTH_ACCOUNT_INACTIVE("AUTH003", "Conta desativada"),
    AUTH_TOKEN_INVALID("AUTH004", "Token inválido"),
    AUTH_TOKEN_EXPIRED("AUTH005", "Token expirado"),
    AUTH_EMAIL_NOT_VERIFIED("AUTH006", "Email não verificado"),
    AUTH_PASSWORD_EXPIRED("AUTH007", "Senha expirada"),
    
    // User Management
    USER_NOT_FOUND("USER001", "Usuário não encontrado"),
    USER_EMAIL_EXISTS("USER002", "Email já cadastrado"),
    USER_CPF_EXISTS("USER003", "CPF já cadastrado"),
    USER_CNPJ_EXISTS("USER004", "CNPJ já cadastrado"),
    
    // Validation - Basic
    VALIDATION_INVALID_EMAIL("VAL001", "Email inválido"),
    VALIDATION_INVALID_CPF("VAL002", "CPF inválido"),
    VALIDATION_INVALID_CNPJ("VAL003", "CNPJ inválido"),
    VALIDATION_REQUIRED_FIELD("VAL004", "Campo obrigatório não informado"),
    
    // Validation - User Type
    VALIDATION_INVALID_USER_TYPE("VAL005", "Tipo de usuário inválido"),
    
    // Validation - Password Security
    VALIDATION_PASSWORD_TOO_WEAK("VAL006", "Senha não atende aos requisitos de segurança"),
    VALIDATION_PASSWORD_REUSED("VAL007", "Senha já foi utilizada recentemente"),
    VALIDATION_PASSWORD_COMMON("VAL008", "Senha muito comum, escolha uma mais segura"),
    
    // Validation - Email Security
    VALIDATION_EMAIL_DOMAIN_BLACKLISTED("VAL009", "Domínio de email não permitido"),
    VALIDATION_EMAIL_NOT_CONFIRMED("VAL010", "Email não confirmado"),
    VALIDATION_EMAIL_CONFIRMATION_EXPIRED("VAL011", "Token de confirmação expirado"),
    
    // System
    SYSTEM_INTERNAL_ERROR("SYS001", "Erro interno do sistema"),
    SYSTEM_DATABASE_ERROR("SYS002", "Erro de conexão com banco de dados"),
    SYSTEM_EXTERNAL_SERVICE_ERROR("SYS003", "Serviço externo indisponível"),
    
    // Rate Limiting
    RATE_LIMIT_EXCEEDED("RATE001", "Muitas tentativas. Tente novamente mais tarde"),
    
    // Pet Management
    PET_NOT_FOUND("PET001", "Pet não encontrado"),
    PET_ALREADY_ADOPTED("PET002", "Pet já foi adotado"),
    PET_NOT_AVAILABLE("PET003", "Pet não está disponível para adoção"),
    
    // Donation Management
    DONATION_NOT_FOUND("DON001", "Doação não encontrada"),
    DONATION_INVALID_AMOUNT("DON002", "Valor de doação inválido"),
    DONATION_ALREADY_PROCESSED("DON003", "Doação já foi processada"),
    
    // Authorization
    ACCESS_DENIED("AUTH008", "Acesso negado"),
    AUTHENTICATION_REQUIRED("AUTH009", "Autenticação obrigatória"),
    
    // User Status
    USER_INACTIVE("USER005", "Usuário inativo");

    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * Returns a formatted error message with the error code.
     * 
     * @return formatted error message
     */
    public String getFormattedMessage() {
        return String.format("[%s] %s", code, message);
    }
    
    /**
     * Checks if this error code is related to authentication.
     * 
     * @return true if authentication-related error
     */
    public boolean isAuthenticationError() {
        return this.code.startsWith("AUTH");
    }
    
    /**
     * Checks if this error code is related to validation.
     * 
     * @return true if validation-related error
     */
    public boolean isValidationError() {
        return this.code.startsWith("VAL");
    }
    
    /**
     * Checks if this error code is related to system errors.
     * 
     * @return true if system-related error
     */
    public boolean isSystemError() {
        return this.code.startsWith("SYS");
    }
    
    /**
     * Checks if this error code is security-sensitive.
     * Security-sensitive errors should have limited details in responses.
     * 
     * @return true if security-sensitive error
     */
    public boolean isSecuritySensitive() {
        return this == AUTH_INVALID_CREDENTIALS ||
               this == AUTH_ACCOUNT_LOCKED ||
               this == AUTH_EMAIL_NOT_VERIFIED ||
               this == VALIDATION_PASSWORD_TOO_WEAK ||
               this == VALIDATION_PASSWORD_REUSED;
    }
}