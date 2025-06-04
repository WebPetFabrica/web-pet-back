package br.edu.utfpr.alunos.webpet.services.validation;

import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for detecting commonly used passwords
 * Prevents users from choosing easily guessable passwords
 */
@Service
public class CommonPasswordService {
    
    // Top 100 most common passwords
    private static final Set<String> COMMON_PASSWORDS = Set.of(
        "123456", "password", "123456789", "12345678", "12345",
        "1234567", "1234567890", "qwerty", "abc123", "111111",
        "123123", "admin", "letmein", "welcome", "monkey",
        "dragon", "pass", "master", "hello", "freedom",
        "whatever", "qazwsx", "trustno1", "jordan23", "harley",
        "robert", "matthew", "jordan", "asshole", "daniel",
        "andrew", "martin", "jordan1", "baseball", "samsung",
        "liverpool", "chelsea", "arsenal", "football", "soccer",
        "iloveyou", "password1", "123qwe", "000000", "password123"
    );
    
    /**
     * Checks if password is commonly used
     */
    public boolean isCommonPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return true;
        }
        
        return COMMON_PASSWORDS.contains(password.toLowerCase());
    }
    
    /**
     * Checks if password contains common patterns
     */
    public boolean hasCommonPattern(String password) {
        if (password == null) return true;
        
        String lower = password.toLowerCase();
        
        // Sequential numbers
        if (lower.matches(".*(?:012|123|234|345|456|567|678|789|890).*")) {
            return true;
        }
        
        // Keyboard patterns
        if (lower.matches(".*(?:qwerty|asdf|zxcv|qwe|asd|zxc).*")) {
            return true;
        }
        
        // Repeated characters
        if (lower.matches(".*(.)\\1{2,}.*")) {
            return true;
        }
        
        return false;
    }
}