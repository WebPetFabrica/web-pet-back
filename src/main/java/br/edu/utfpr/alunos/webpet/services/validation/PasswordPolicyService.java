package br.edu.utfpr.alunos.webpet.services.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service responsible for password policy enforcement
 * Implements security requirements for password complexity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {
    
    private final CommonPasswordService commonPasswordService;
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    // Password complexity patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*(),.?\":{}|<>].*");
    
    /**
     * Validates password against security policy
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Length requirements
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            log.debug("Password length validation failed");
            return false;
        }
        
        // Complexity requirements
        if (!hasRequiredComplexity(password)) {
            log.debug("Password complexity validation failed");
            return false;
        }
        
        // Common password check
        if (commonPasswordService.isCommonPassword(password)) {
            log.debug("Password is too common");
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if password meets complexity requirements
     */
    private boolean hasRequiredComplexity(String password) {
        int complexityScore = 0;
        
        if (UPPERCASE_PATTERN.matcher(password).find()) complexityScore++;
        if (LOWERCASE_PATTERN.matcher(password).matches()) complexityScore++;
        if (DIGIT_PATTERN.matcher(password).matches()) complexityScore++;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) complexityScore++;
        
        // Require at least 3 of 4 complexity types
        return complexityScore >= 3;
    }
    
    /**
     * Gets password policy information for user guidance
     */
    public String getPasswordPolicy() {
        return "Password must be 8-128 characters with at least 3 of: uppercase, lowercase, numbers, special characters";
    }
}