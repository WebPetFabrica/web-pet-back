package br.edu.utfpr.alunos.webpet.infra.exception;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}