package br.edu.utfpr.alunos.webpet.services.validation;

import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for managing blacklisted email domains
 * Prevents registration with temporary/disposable email providers
 */
@Service
public class DomainBlacklistService {
    
    // Common temporary email domains
    private static final Set<String> BLACKLISTED_DOMAINS = Set.of(
        "10minutemail.com", "guerrillamail.com", "mailinator.com",
        "tempmail.org", "throwaway.email", "temp-mail.org",
        "yopmail.com", "maildrop.cc", "sharklasers.com",
        "trashmail.com", "getairmail.com", "dispostable.com"
    );
    
    /**
     * Checks if domain is blacklisted
     */
    public boolean isBlacklisted(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return true;
        }
        
        return BLACKLISTED_DOMAINS.contains(domain.toLowerCase().trim());
    }
    
    /**
     * Adds domain to blacklist (for future dynamic management)
     */
    public void addToBlacklist(String domain) {
        // Implementation for dynamic blacklist management
        // Could be backed by database for persistent storage
    }
}