package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.*;
import br.edu.utfpr.alunos.webpet.infra.logging.ExceptionLogger;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of authentication services for the WebPet system.
 * 
 * <p>Handles user authentication and registration for all user types (User, ONG, Protetor).
 * Implements security features including:
 * <ul>
 *   <li>Brute force protection via login attempt tracking</li>
 *   <li>JWT token generation and validation</li>
 *   <li>Password encryption using BCrypt</li>
 *   <li>Comprehensive audit logging</li>
 * </ul>
 * 
 * <p>Business Rules:
 * <ul>
 *   <li>Email must be unique across all user types</li>
 *   <li>CNPJ must be unique for ONGs</li>
 *   <li>CPF must be unique for Protetors</li>
 *   <li>Account lockout after 5 failed login attempts</li>
 *   <li>JWT tokens expire after 2 hours</li>
 * </ul>
 * 
 * @author WebPet Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final ExceptionLogger exceptionLogger;

    /**
     * Authenticates a user using email and password credentials.
     * 
     * <p>Security features:
     * <ul>
     *   <li>Checks for account lockout before attempting authentication</li>
     *   <li>Records failed attempts for brute force protection</li>
     *   <li>Generates JWT token on successful authentication</li>
     *   <li>Logs all authentication attempts for audit</li>
     * </ul>
     * 
     * @param loginDTO contains user credentials (email and password)
     * @return authentication response with user name, JWT token, and token type
     * @throws AccountLockedException when account is temporarily locked due to failed attempts
     * @throws AuthenticationException when credentials are invalid
     * @throws SystemException when unexpected error occurs during authentication
     */
    @Override
    public AuthResponseDTO login(LoginRequestDTO loginDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("Login attempt for email: {} [correlationId: {}]", loginDTO.email(), correlationId);
        
        // Check for account lockout to prevent brute force attacks
        if (loginAttemptService.isBlocked(loginDTO.email())) {
            log.warn("Login blocked for email: {} - account locked [correlationId: {}]", 
                loginDTO.email(), correlationId);
            exceptionLogger.logAuthenticationFailure(loginDTO.email(), "Account locked", correlationId);
            throw new AccountLockedException(ErrorCode.AUTH_ACCOUNT_LOCKED.getMessage());
        }

        try {
            // Delegate authentication to Spring Security
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password())
            );
            
            // Reset failed attempt counter on successful authentication
            loginAttemptService.recordSuccessfulLogin(loginDTO.email());
            
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = tokenService.generateToken(userDetails);
            String displayName = findDisplayNameByEmail(loginDTO.email());
            
            log.info("Login successful for email: {} [correlationId: {}]", loginDTO.email(), correlationId);
            return new AuthResponseDTO(displayName, token, "Bearer");
            
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Record failed attempt for brute force protection
            loginAttemptService.recordFailedAttempt(loginDTO.email());
            int remaining = loginAttemptService.getRemainingAttempts(loginDTO.email());
            
            log.warn("Login failed for email: {} - invalid credentials, {} attempts remaining [correlationId: {}]", 
                loginDTO.email(), remaining, correlationId);
            exceptionLogger.logAuthenticationFailure(loginDTO.email(), "Invalid credentials", correlationId);
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_CREDENTIALS);
            
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {} [correlationId: {}]", 
                loginDTO.email(), correlationId, e);
            exceptionLogger.logSystemError("LOGIN", "Unexpected authentication error", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    /**
     * Registers a new regular user in the system.
     * 
     * <p>Business Rules:
     * <ul>
     *   <li>Email must be unique across all user types</li>
     *   <li>Password is encrypted using BCrypt before storage</li>
     *   <li>User is automatically activated upon registration</li>
     *   <li>JWT token is generated for immediate authentication</li>
     * </ul>
     * 
     * @param registerDTO user registration data (name, email, password)
     * @return authentication response with user name and JWT token
     * @throws ValidationException when email already exists
     * @throws SystemException when database error occurs during registration
     */
    @Override
    @Transactional
    public AuthResponseDTO registerUser(RegisterRequestDTO registerDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("User registration attempt for email: {} [correlationId: {}]", 
            registerDTO.email(), correlationId);
        
        // Validate email uniqueness across all user types
        if (userRepository.existsByEmail(registerDTO.email())) {
            log.warn("User registration failed - email already exists: {} [correlationId: {}]", 
                registerDTO.email(), correlationId);
            exceptionLogger.logBusinessError("USER_REGISTRATION", "Email exists", correlationId);
            throw new ValidationException(ErrorCode.USER_EMAIL_EXISTS);
        }

        try {
            // Create new user with encrypted password
            User user = User.builder()
                .name(registerDTO.name())
                .email(registerDTO.email())
                .password(passwordEncoder.encode(registerDTO.password()))
                .celular("")
                .build();
            
            user = userRepository.save(user);
            
            String token = generateTokenForEmail(user.getEmail());
            log.info("User registration successful for email: {} [correlationId: {}]", 
                user.getEmail(), correlationId);
            return new AuthResponseDTO(user.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("User registration failed for email: {} [correlationId: {}]", 
                registerDTO.email(), correlationId, e);
            exceptionLogger.logSystemError("USER_REGISTRATION", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    /**
     * Registers a new ONG (Non-Governmental Organization) in the system.
     * 
     * <p>Business Rules:
     * <ul>
     *   <li>Email must be unique across all user types</li>
     *   <li>CNPJ must be unique among ONGs</li>
     *   <li>CNPJ validation is performed at DTO level</li>
     *   <li>Password is encrypted using BCrypt before storage</li>
     * </ul>
     * 
     * @param ongDTO ONG registration data (CNPJ, name, email, phone, password)
     * @return authentication response with ONG name and JWT token
     * @throws ValidationException when email or CNPJ already exists
     * @throws SystemException when database error occurs during registration
     */
    @Override
    @Transactional
    public AuthResponseDTO registerONG(ONGRegisterDTO ongDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("ONG registration attempt for email: {} [correlationId: {}]", 
            ongDTO.email(), correlationId);
        
        // Validate email uniqueness
        if (ongRepository.existsByEmail(ongDTO.email())) {
            log.warn("ONG registration failed - email already exists: {} [correlationId: {}]", 
                ongDTO.email(), correlationId);
            exceptionLogger.logBusinessError("ONG_REGISTRATION", "Email exists", correlationId);
            throw new ValidationException(ErrorCode.USER_EMAIL_EXISTS);
        }
        
        // Validate CNPJ uniqueness
        if (ongRepository.existsByCnpj(ongDTO.cnpj())) {
            log.warn("ONG registration failed - CNPJ already exists: {} [correlationId: {}]", 
                ongDTO.cnpj(), correlationId);
            exceptionLogger.logBusinessError("ONG_REGISTRATION", "CNPJ exists", correlationId);
            throw new ValidationException(ErrorCode.USER_CNPJ_EXISTS);
        }

        try {
            ONG ong = ONG.builder()
                .cnpj(ongDTO.cnpj())
                .nomeOng(ongDTO.nomeOng())
                .email(ongDTO.email())
                .password(passwordEncoder.encode(ongDTO.password()))
                .celular(ongDTO.celular())
                .build();
            
            ong = ongRepository.save(ong);
            
            String token = generateTokenForEmail(ong.getEmail());
            log.info("ONG registration successful for email: {} [correlationId: {}]", 
                ong.getEmail(), correlationId);
            return new AuthResponseDTO(ong.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("ONG registration failed for email: {} [correlationId: {}]", 
                ongDTO.email(), correlationId, e);
            exceptionLogger.logSystemError("ONG_REGISTRATION", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    /**
     * Registers a new Protetor (Independent Animal Protector) in the system.
     * 
     * <p>Business Rules:
     * <ul>
     *   <li>Email must be unique across all user types</li>
     *   <li>CPF must be unique among Protetors</li>
     *   <li>CPF validation is performed at DTO level</li>
     *   <li>Password is encrypted using BCrypt before storage</li>
     * </ul>
     * 
     * @param protetorDTO Protetor registration data (CPF, name, email, phone, password)
     * @return authentication response with Protetor name and JWT token
     * @throws ValidationException when email or CPF already exists
     * @throws SystemException when database error occurs during registration
     */
    @Override
    @Transactional
    public AuthResponseDTO registerProtetor(ProtetorRegisterDTO protetorDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("Protetor registration attempt for email: {} [correlationId: {}]", 
            protetorDTO.email(), correlationId);
        
        if (protetorRepository.existsByEmail(protetorDTO.email())) {
            log.warn("Protetor registration failed - email already exists: {} [correlationId: {}]", 
                protetorDTO.email(), correlationId);
            exceptionLogger.logBusinessError("PROTETOR_REGISTRATION", "Email exists", correlationId);
            throw new ValidationException(ErrorCode.USER_EMAIL_EXISTS);
        }
        
        if (protetorRepository.existsByCpf(protetorDTO.cpf())) {
            log.warn("Protetor registration failed - CPF already exists: {} [correlationId: {}]", 
                protetorDTO.cpf(), correlationId);
            exceptionLogger.logBusinessError("PROTETOR_REGISTRATION", "CPF exists", correlationId);
            throw new ValidationException(ErrorCode.USER_CPF_EXISTS);
        }

        try {
            Protetor protetor = Protetor.builder()
                .cpf(protetorDTO.cpf())
                .nomeCompleto(protetorDTO.nomeCompleto())
                .email(protetorDTO.email())
                .password(passwordEncoder.encode(protetorDTO.password()))
                .celular(protetorDTO.celular())
                .build();
            
            protetor = protetorRepository.save(protetor);
            
            String token = generateTokenForEmail(protetor.getEmail());
            log.info("Protetor registration successful for email: {} [correlationId: {}]", 
                protetor.getEmail(), correlationId);
            return new AuthResponseDTO(protetor.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("Protetor registration failed for email: {} [correlationId: {}]", 
                protetorDTO.email(), correlationId, e);
            exceptionLogger.logSystemError("PROTETOR_REGISTRATION", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    /**
     * Finds the display name for a user by email address.
     * Searches across all user types (User, ONG, Protetor).
     * 
     * @param email the user's email address
     * @return the user's display name, or "Usuário" if not found
     */
    private String findDisplayNameByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(BaseUser::getDisplayName)
            .orElse(ongRepository.findByEmail(email)
                .map(BaseUser::getDisplayName)
                .orElse(protetorRepository.findByEmail(email)
                    .map(BaseUser::getDisplayName)
                    .orElse("Usuário")));
    }

    /**
     * Generates a JWT token for the given email address.
     * Creates a minimal UserDetails object for token generation.
     * 
     * @param email the user's email address
     * @return generated JWT token
     */
    private String generateTokenForEmail(String email) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            email, "", java.util.Collections.emptyList()
        );
        return tokenService.generateToken(userDetails);
    }
}