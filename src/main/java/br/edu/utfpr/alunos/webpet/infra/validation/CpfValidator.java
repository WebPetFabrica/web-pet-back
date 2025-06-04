package br.edu.utfpr.alunos.webpet.infra.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<CPF, String> {
    
    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        return cpf != null && isValidCpf(cpf);
    }
    
    private boolean isValidCpf(String cpf) {
        cpf = cpf.replaceAll("\\D", "");
        
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        return calculateFirstDigit(cpf) == Character.getNumericValue(cpf.charAt(9)) &&
               calculateSecondDigit(cpf) == Character.getNumericValue(cpf.charAt(10));
    }
    
    private int calculateFirstDigit(String cpf) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
    
    private int calculateSecondDigit(String cpf) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}