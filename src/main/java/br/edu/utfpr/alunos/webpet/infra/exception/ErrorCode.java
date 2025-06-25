package br.edu.utfpr.alunos.webpet.infra.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_EMAIL_EXISTS("E001", "Email já está em uso"),
    AUTH_INVALID_CREDENTIALS("E002", "Credenciais inválidas"),
    AUTH_ACCOUNT_INACTIVE("E003", "Conta inativa"),
    USER_NOT_FOUND("E004", "Usuário não encontrado"),
    ACCESS_DENIED("E005", "Acesso negado");

    private final String code;
    private final String message;
}