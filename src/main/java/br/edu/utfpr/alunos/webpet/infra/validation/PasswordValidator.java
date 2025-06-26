package br.edu.utfpr.alunos.webpet.infra.validation;

import br.edu.utfpr.alunos.webpet.services.validation.PasswordPolicyService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Custom password validator that enforces security policies
 */
@Component
@RequiredArgsConstructor
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    private final PasswordPolicyService passwordPolicyService;
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        return passwordPolicyService.isValidPassword(password);
    }
}