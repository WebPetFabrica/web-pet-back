package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuthenticationService focusing solely on user authentication.
 * 
 * <p>This service has been refactored to follow the Single Responsibility Principle,
 * handling only authentication operations. User registration has been moved to
 * UserRegistrationService, and user loading to CustomUserDetailsService.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Authenticate users using Spring Security</li>
 *   <li>Generate JWT tokens for successful authentication</li>
 *   <li>Record successful login attempts</li>
 *   <li>Handle authentication failures appropriately</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;

    /**
     * Authenticates a user with the provided credentials.
     * 
     * <p>This method uses Spring Security's AuthenticationManager to validate
     * the user credentials. If successful, it generates a JWT token and records
     * the successful login attempt.
     * 
     * @param data the login request containing email and password
     * @return authentication response with user details and JWT token
     * @throws org.springframework.security.core.AuthenticationException if authentication fails
     */
    @Override
    public AuthResponseDTO authenticate(LoginRequestDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("Authenticating user: {} [correlationId: {}]", data.email(), correlationId);
        
        try {
            // Create authentication token with user credentials
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(data.email(), data.password());
            
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // Extract authenticated user
            BaseUser authenticatedUser = (BaseUser) authentication.getPrincipal();
            
            // Validate that authentication returned a valid user
            if (authenticatedUser == null) {
                throw new IllegalStateException("Authentication succeeded but no user principal was returned");
            }
            
            // Record successful login attempt (after validating user)
            loginAttemptService.recordSuccessfulLogin(data.email());
            
            // Generate JWT token
            String jwtToken = tokenService.generateToken(authenticatedUser);
            
            log.info("User authenticated successfully: {} [correlationId: {}]", data.email(), correlationId);
            
            return new AuthResponseDTO(
                authenticatedUser.getDisplayName(), 
                jwtToken, 
                "Bearer"
            );
            
        } catch (Exception e) {
            log.warn("Authentication failed for user: {} [correlationId: {}] - {}", 
                data.email(), correlationId, e.getMessage());
            throw e;
        }
    }
}