package br.edu.utfpr.alunos.webpet.infra.validation;

import br.edu.utfpr.alunos.webpet.services.validation.EmailValidationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Custom email validator that performs RFC compliant validation
 * and domain verification
 */
@Component
@RequiredArgsConstructor
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    private final EmailValidationService emailValidationService;
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return emailValidationService.isValidEmail(email);
    }
}