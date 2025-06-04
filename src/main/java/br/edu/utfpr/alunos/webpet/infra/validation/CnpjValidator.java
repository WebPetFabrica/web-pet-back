package br.edu.utfpr.alunos.webpet.infra.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<CNPJ, String> {
    
    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext context) {
        return cnpj != null && isValidCnpj(cnpj);
    }
    
    private boolean isValidCnpj(String cnpj) {
        cnpj = cnpj.replaceAll("\\D", "");
        
        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }
        
        return calculateFirstDigit(cnpj) == Character.getNumericValue(cnpj.charAt(12)) &&
               calculateSecondDigit(cnpj) == Character.getNumericValue(cnpj.charAt(13));
    }
    
    private int calculateFirstDigit(String cnpj) {
        int[] weights = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
    
    private int calculateSecondDigit(String cnpj) {
        int[] weights = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}