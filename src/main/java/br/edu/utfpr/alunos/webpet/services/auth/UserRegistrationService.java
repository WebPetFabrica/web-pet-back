package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.UserRegisterDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.infra.exception.ValidationException;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.services.validation.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for user registration operations.
 * 
 * <p>This service handles the registration of new users, ONGs, and Protetors
 * in the system. It follows the Single Responsibility Principle by focusing
 * solely on user registration logic, delegating authentication to AuthenticationService.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Validate registration data</li>
 *   <li>Check email uniqueness across all user types</li>
 *   <li>Enforce password policy</li>
 *   <li>Create and persist user entities</li>
 *   <li>Generate authentication tokens for new users</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final PasswordPolicyService passwordPolicyService;

    /**
     * Registers a new regular user in the system.
     * 
     * @param data the user registration data
     * @return authentication response with token
     * @throws BusinessException if email already exists
     * @throws ValidationException if data is invalid
     */
    public AuthResponseDTO registerUser(UserRegisterDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("Registering new user with email: {} [correlationId: {}]", data.email(), correlationId);
        
        try {
            validateRegistrationData(data.email(), data.password());
            validateEmailUniqueness(data.email());
            
            String encryptedPassword = passwordEncoder.encode(data.password());
            User newUser = User.builder()
                    .name(data.name())
                    .email(data.email())
                    .password(encryptedPassword)
                    .celular("")
                    .build();
            
            User savedUser = userRepository.save(newUser);
            String token = tokenService.generateToken(savedUser);
            
            log.info("User registered successfully: {} [correlationId: {}]", data.email(), correlationId);
            return new AuthResponseDTO(savedUser.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("Failed to register user: {} [correlationId: {}]", data.email(), correlationId, e);
            throw e;
        }
    }
    
    /**
     * Registers a new ONG in the system.
     * 
     * @param data the ONG registration data
     * @return authentication response with token
     * @throws BusinessException if email already exists
     * @throws ValidationException if data is invalid
     */
    public AuthResponseDTO registerONG(ONGRegisterDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("Registering new ONG with email: {} [correlationId: {}]", data.email(), correlationId);
        
        try {
            validateRegistrationData(data.email(), data.password());
            validateEmailUniqueness(data.email());
            
            String encryptedPassword = passwordEncoder.encode(data.password());
            ONG newOng = ONG.builder()
                    .cnpj(data.cnpj())
                    .nomeOng(data.nomeOng())
                    .email(data.email())
                    .password(encryptedPassword)
                    .celular(data.celular())
                    .build();
            
            ONG savedOng = ongRepository.save(newOng);
            String token = tokenService.generateToken(savedOng);
            
            log.info("ONG registered successfully: {} [correlationId: {}]", data.email(), correlationId);
            return new AuthResponseDTO(savedOng.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("Failed to register ONG: {} [correlationId: {}]", data.email(), correlationId, e);
            throw e;
        }
    }
    
    /**
     * Registers a new Protetor in the system.
     * 
     * @param data the Protetor registration data
     * @return authentication response with token
     * @throws BusinessException if email already exists
     * @throws ValidationException if data is invalid
     */
    public AuthResponseDTO registerProtetor(ProtetorRegisterDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("Registering new Protetor with email: {} [correlationId: {}]", data.email(), correlationId);
        
        try {
            validateRegistrationData(data.email(), data.password());
            validateEmailUniqueness(data.email());
            
            String encryptedPassword = passwordEncoder.encode(data.password());
            Protetor newProtetor = Protetor.builder()
                    .nomeCompleto(data.nomeCompleto())
                    .cpf(data.cpf())
                    .email(data.email())
                    .password(encryptedPassword)
                    .celular(data.celular())
                    .build();
            
            Protetor savedProtetor = protetorRepository.save(newProtetor);
            String token = tokenService.generateToken(savedProtetor);
            
            log.info("Protetor registered successfully: {} [correlationId: {}]", data.email(), correlationId);
            return new AuthResponseDTO(savedProtetor.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("Failed to register Protetor: {} [correlationId: {}]", data.email(), correlationId, e);
            throw e;
        }
    }
    
    /**
     * Validates basic registration data (email and password).
     * 
     * @param email the email to validate
     * @param password the password to validate
     * @throws ValidationException if validation fails
     */
    private void validateRegistrationData(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD, "Email é obrigatório");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD, "Senha é obrigatória");
        }
        
        if (!passwordPolicyService.isValidPassword(password)) {
            throw new ValidationException(ErrorCode.VALIDATION_ERROR, 
                "Senha não atende aos critérios de segurança: " + passwordPolicyService.getPasswordPolicy());
        }
    }
    
    /**
     * Validates that the email is unique across all user types.
     * 
     * @param email the email to check
     * @throws BusinessException if email already exists
     */
    private void validateEmailUniqueness(String email) {
        boolean emailExists = userRepository.existsByEmail(email) || 
                             ongRepository.existsByEmail(email) || 
                             protetorRepository.existsByEmail(email);
        
        if (emailExists) {
            log.warn("Attempt to register with existing email: {}", email);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, 
                "Já existe um usuário cadastrado com este e-mail");
        }
    }
}