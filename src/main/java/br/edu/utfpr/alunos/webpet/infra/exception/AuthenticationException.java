package br.edu.utfpr.alunos.webpet.infra.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}