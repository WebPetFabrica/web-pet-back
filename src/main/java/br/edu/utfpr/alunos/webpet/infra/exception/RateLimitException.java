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