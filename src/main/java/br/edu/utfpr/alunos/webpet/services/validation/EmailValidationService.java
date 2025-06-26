package br.edu.utfpr.alunos.webpet.services.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Service responsible for comprehensive email validation
 * including RFC compliance and domain verification
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailValidationService {
    
    private final DomainBlacklistService domainBlacklistService;
    private final Map<String, Boolean> domainCache = new ConcurrentHashMap<>();
    
    // RFC 5322 compliant regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // Common invalid patterns
    private static final Set<String> INVALID_PATTERNS = Set.of(
        "test@test.com", "admin@admin.com", "user@user.com"
    );
    
    /**
     * Validates email with RFC compliance, domain verification and blacklist check
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim().toLowerCase();
        
        // Basic RFC validation
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            log.debug("Email failed RFC validation: {}", email);
            return false;
        }
        
        // Check invalid patterns
        if (INVALID_PATTERNS.contains(email)) {
            log.debug("Email matches invalid pattern: {}", email);
            return false;
        }
        
        String domain = extractDomain(email);
        
        // Check blacklisted domains
        if (domainBlacklistService.isBlacklisted(domain)) {
            log.debug("Email domain is blacklisted: {}", domain);
            return false;
        }
        
        // Verify domain exists
        if (!verifyDomainExists(domain)) {
            log.debug("Email domain does not exist: {}", domain);
            return false;
        }
        
        return true;
    }
    
    /**
     * Extracts domain from email address
     */
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        return atIndex > 0 ? email.substring(atIndex + 1) : "";
    }
    
    /**
     * Verifies if domain exists using DNS lookup
     */
    private boolean verifyDomainExists(String domain) {
        Boolean cachedResult = domainCache.get(domain);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        try {
            InetAddress.getByName(domain);
            domainCache.put(domain, true);
            return true;
        } catch (Exception e) {
            log.debug("Domain verification failed for: {}", domain);
            domainCache.put(domain, false);
            return false;
        }
    }
}