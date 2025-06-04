package br.edu.utfpr.alunos.webpet.infra.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Authentication & Authorization
    AUTH_INVALID_CREDENTIALS("AUTH001", "Credenciais inválidas"),
    AUTH_ACCOUNT_LOCKED("AUTH002", "Conta temporariamente bloqueada"),
    AUTH_ACCOUNT_INACTIVE("AUTH003", "Conta desativada"),
    AUTH_TOKEN_INVALID("AUTH004", "Token inválido"),
    AUTH_TOKEN_EXPIRED("AUTH005", "Token expirado"),
    
    // User Management
    USER_NOT_FOUND("USER001", "Usuário não encontrado"),
    USER_EMAIL_EXISTS("USER002", "Email já cadastrado"),
    USER_CPF_EXISTS("USER003", "CPF já cadastrado"),
    USER_CNPJ_EXISTS("USER004", "CNPJ já cadastrado"),
    
    // Validation
    VALIDATION_INVALID_EMAIL("VAL001", "Email inválido"),
    VALIDATION_INVALID_CPF("VAL002", "CPF inválido"),
    VALIDATION_INVALID_CNPJ("VAL003", "CNPJ inválido"),
    VALIDATION_REQUIRED_FIELD("VAL004", "Campo obrigatório não informado"),
    
    // System
    SYSTEM_INTERNAL_ERROR("SYS001", "Erro interno do sistema"),
    SYSTEM_DATABASE_ERROR("SYS002", "Erro de conexão com banco de dados"),
    SYSTEM_EXTERNAL_SERVICE_ERROR("SYS003", "Serviço externo indisponível"),
    
    // Rate Limiting
    RATE_LIMIT_EXCEEDED("RATE001", "Muitas tentativas. Tente novamente mais tarde");

    private final String code;
    private final String message;
}