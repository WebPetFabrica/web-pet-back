package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;

/**
 * Service interface for user authentication operations.
 * 
 * <p>This interface defines the contract for authentication-related operations,
 * focusing solely on login functionality. Registration operations have been
 * moved to UserRegistrationService to follow the Single Responsibility Principle.
 */
public interface AuthenticationService {
    
    /**
     * Authenticates a user with the provided credentials.
     * 
     * @param data the login request containing email and password
     * @return authentication response with user details and token
     */
    AuthResponseDTO authenticate(LoginRequestDTO data);
}