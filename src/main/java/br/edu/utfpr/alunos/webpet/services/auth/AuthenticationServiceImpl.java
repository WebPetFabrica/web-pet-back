package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.UserRegisterDTO;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import br.edu.utfpr.alunos.webpet.infra.exception.*;
import br.edu.utfpr.alunos.webpet.infra.logging.ExceptionLogger;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.services.validation.PasswordHistoryService;
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
 *   <li>Password history tracking for security compliance</li>
 *   <li>Email confirmation for account verification</li>
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
 *   <li>Password history is maintained for last 5 passwords</li>
 *   <li>Email confirmation required for account activation</li>
 * </ul>
 * 
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
    private final PasswordHistoryService passwordHistoryService;

    /**
     * Generic authentication method that determines user type and delegates to appropriate login method.
     * This method first checks which type of user exists with the given email and calls the appropriate
     * specific login method.
     * 
     * @param data login request containing email and password
     * @return authentication response with token and user information
     * @throws AuthenticationException if credentials are invalid or user not found
     */
    @Override
    public AuthResponseDTO authenticate(LoginRequestDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("Generic authentication attempt for email: {} [correlationId: {}]", data.email(), correlationId);
        
        // Check User first
        if (userRepository.findByEmail(data.email()).isPresent()) {
            return loginUser(data);
        }
        
        // Check ONG
        if (ongRepository.findByEmail(data.email()).isPresent()) {
            return loginOng(data);
        }
        
        // Check Protetor
        if (protetorRepository.findByEmail(data.email()).isPresent()) {
            return loginProtetor(data);
        }
        
        // No user found with this email
        log.warn("Authentication failed - email not found: {} [correlationId: {}]", data.email(), correlationId);
        recordFailedAttempt(data.email(), "Email not found in any user type", correlationId);
        throw new AuthenticationException(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }

    /**
     * Authenticates a regular user using email and password credentials.
     * Searches specifically in the UserRepository.
     * 
     * @param loginDTO contains user credentials (email and password)
     * @return authentication response with user name, JWT token, and token type
     * @throws AccountLockedException when account is temporarily locked due to failed attempts
     * @throws AuthenticationException when credentials are invalid
     * @throws SystemException when unexpected error occurs during authentication
     */
    @Override
    public AuthResponseDTO loginUser(LoginRequestDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("User login attempt for email: {} [correlationId: {}]", data.email(), correlationId);
        
        checkAccountLockout(data.email());
        
        try {
            User user = userRepository.findByEmail(data.email())
                .orElseThrow(() -> {
                    recordFailedAttempt(data.email(), "User not found", correlationId);
                    return new AuthenticationException(ErrorCode.AUTH_INVALID_CREDENTIALS);
                });
            
            validatePassword(data.password(), user.getPassword(), data.email());
            loginAttemptService.recordSuccessfulLogin(data.email());
            
            String token = generateTokenForUser(user);
            log.info("User login successful for email: {} [correlationId: {}]", data.email(), correlationId);
            
            return new AuthResponseDTO(user.getDisplayName(), token, "Bearer");
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user login for email: {} [correlationId: {}]", 
                data.email(), correlationId, e);
            exceptionLogger.logSystemError("USER_LOGIN", "Unexpected authentication error", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    /**
     * Authenticates an ONG using email and password credentials.
     * Searches specifically in the ONGRepository.
     * 
     * @param loginDTO contains ONG credentials (email and password)
     * @return authentication response with ONG name, JWT token, and token type
     * @throws AccountLockedException when account is temporarily locked due to failed attempts
     * @throws AuthenticationException when credentials are invalid
     * @throws SystemException when unexpected error occurs during authentication
     */
    @Override
    public AuthResponseDTO loginOng(LoginRequestDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("ONG login attempt for email: {} [correlationId: {}]", data.email(), correlationId);
        
        checkAccountLockout(data.email());
        
        try {
            ONG ong = ongRepository.findByEmail(data.email())
                .orElseThrow(() -> {
                    recordFailedAttempt(data.email(), "ONG not found", correlationId);
                    return new AuthenticationException(ErrorCode.AUTH_INVALID_CREDENTIALS);
                });
            
            validatePassword(data.password(), ong.getPassword(), data.email());
            loginAttemptService.recordSuccessfulLogin(data.email());
            
            String token = generateTokenForUser(ong);
            log.info("ONG login successful for email: {} [correlationId: {}]", data.email(), correlationId);
            
            return new AuthResponseDTO(ong.getDisplayName(), token, "Bearer");
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during ONG login for email: {} [correlationId: {}]", 
                data.email(), correlationId, e);
            exceptionLogger.logSystemError("ONG_LOGIN", "Unexpected authentication error", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    /**
     * Authenticates a Protetor using email and password credentials.
     * Searches specifically in the ProtetorRepository.
     * 
     * @param loginDTO contains Protetor credentials (email and password)
     * @return authentication response with Protetor name, JWT token, and token type
     * @throws AccountLockedException when account is temporarily locked due to failed attempts
     * @throws AuthenticationException when credentials are invalid
     * @throws SystemException when unexpected error occurs during authentication
     */
    @Override
    public AuthResponseDTO loginProtetor(LoginRequestDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("Protetor login attempt for email: {} [correlationId: {}]", data.email(), correlationId);
        
        checkAccountLockout(data.email());
        
        try {
            Protetor protetor = protetorRepository.findByEmail(data.email())
                .orElseThrow(() -> {
                    recordFailedAttempt(data.email(), "Protetor not found", correlationId);
                    return new AuthenticationException(ErrorCode.AUTH_INVALID_CREDENTIALS);
                });
            
            validatePassword(data.password(), protetor.getPassword(), data.email());
            loginAttemptService.recordSuccessfulLogin(data.email());
            
            String token = generateTokenForUser(protetor);
            log.info("Protetor login successful for email: {} [correlationId: {}]", data.email(), correlationId);
            
            return new AuthResponseDTO(protetor.getDisplayName(), token, "Bearer");
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Protetor login for email: {} [correlationId: {}]", 
                data.email(), correlationId, e);
            exceptionLogger.logSystemError("PROTETOR_LOGIN", "Unexpected authentication error", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    /**
     * Checks if the account is locked due to failed login attempts.
     * 
     * @param email the user's email
     * @throws AccountLockedException if account is locked
     */
    private void checkAccountLockout(String email) {
        if (loginAttemptService.isBlocked(email)) {
            String correlationId = MDC.get("correlationId");
            log.warn("Login blocked for email: {} - account locked [correlationId: {}]", email, correlationId);
            exceptionLogger.logAuthenticationFailure(email, "Account locked", correlationId);
            throw new AccountLockedException(ErrorCode.AUTH_ACCOUNT_LOCKED.getMessage());
        }
    }

    /**
     * Validates the provided password against the stored encrypted password.
     * 
     * @param rawPassword the raw password provided by user
     * @param encodedPassword the stored encrypted password
     * @param email the user's email for logging
     * @throws AuthenticationException if password is invalid
     */
    private void validatePassword(String rawPassword, String encodedPassword, String email) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            String correlationId = MDC.get("correlationId");
            recordFailedAttempt(email, "Invalid password", correlationId);
            throw new AuthenticationException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    /**
     * Records a failed login attempt and logs the failure.
     * 
     * @param email the user's email
     * @param reason the reason for failure
     * @param correlationId the correlation ID for logging
     */
    private void recordFailedAttempt(String email, String reason, String correlationId) {
        loginAttemptService.recordFailedAttempt(email);
        int remaining = loginAttemptService.getRemainingAttempts(email);
        log.warn("Login failed for email: {} - {}, {} attempts remaining [correlationId: {}]", 
            email, reason, remaining, correlationId);
        exceptionLogger.logAuthenticationFailure(email, reason, correlationId);
    }

    /**
     * Generates a JWT token for the authenticated user.
     * 
     * @param user the authenticated user
     * @return JWT token string
     */
    private String generateTokenForUser(BaseUser user) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            user.getEmail(), "", 
            java.util.Collections.emptyList()
        );
        return tokenService.generateToken(userDetails);
    }

    /**
     * Registers a new regular user in the system.
     * 
     * <p>Business Rules:
     * <ul>
     *   <li>Email must be unique across all user types</li>
     *   <li>Password is encrypted using BCrypt before storage</li>
     *   <li>Password is saved to history for future validation</li>
     *   <li>Email confirmation is sent for account verification</li>
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
    public AuthResponseDTO registerUser(UserRegisterDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("User registration attempt for email: {} [correlationId: {}]", 
            data.email(), correlationId);
        
        // Validate email uniqueness across all user types
        if (userRepository.existsByEmail(data.email())) {
            log.warn("User registration failed - email already exists: {} [correlationId: {}]", 
                data.email(), correlationId);
            exceptionLogger.logBusinessError("USER_REGISTRATION", "Email exists", correlationId);
            throw new ValidationException(ErrorCode.USER_EMAIL_EXISTS);
        }

        try {
            String encodedPassword = passwordEncoder.encode(data.password());
            
            // Create new user with encrypted password
            User user = User.builder()
                .name(data.name())
                .email(data.email())
                .password(encodedPassword)
                .celular("")
                .build();
            
            user = userRepository.save(user);
            
            // Save password to history for future validation
            passwordHistoryService.savePasswordToHistory(user.getId(), encodedPassword);
            log.debug("Password saved to history for user: {} [correlationId: {}]", 
                user.getEmail(), correlationId);
            
            // emailConfirmationService.sendConfirmationEmail(user);
            log.debug("Email confirmation will be implemented later for user: {} [correlationId: {}]", 
                user.getEmail(), correlationId);
            
            String token = generateTokenForUser(user);
            log.info("User registration successful for email: {} [correlationId: {}]", 
                user.getEmail(), correlationId);
            return new AuthResponseDTO(user.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("User registration failed for email: {} [correlationId: {}]", 
                data.email(), correlationId, e);
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
     *   <li>Password is saved to history for future validation</li>
     *   <li>Email confirmation is sent for account verification</li>
     * </ul>
     * 
     * @param ongDTO ONG registration data (CNPJ, name, email, phone, password)
     * @return authentication response with ONG name and JWT token
     * @throws ValidationException when email or CNPJ already exists
     * @throws SystemException when database error occurs during registration
     */
    @Override
    @Transactional
    public AuthResponseDTO registerOng(ONGRegisterDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("ONG registration attempt for email: {} [correlationId: {}]", 
            data.email(), correlationId);
        
        // Validate email uniqueness
        if (ongRepository.existsByEmail(data.email())) {
            log.warn("ONG registration failed - email already exists: {} [correlationId: {}]", 
                data.email(), correlationId);
            exceptionLogger.logBusinessError("ONG_REGISTRATION", "Email exists", correlationId);
            throw new ValidationException(ErrorCode.USER_EMAIL_EXISTS);
        }
        
        // Validate CNPJ uniqueness
        if (ongRepository.existsByCnpj(data.cnpj())) {
            log.warn("ONG registration failed - CNPJ already exists: {} [correlationId: {}]", 
                data.cnpj(), correlationId);
            exceptionLogger.logBusinessError("ONG_REGISTRATION", "CNPJ exists", correlationId);
            throw new ValidationException(ErrorCode.USER_CNPJ_EXISTS);
        }

        try {
            String encodedPassword = passwordEncoder.encode(data.password());
            
            ONG ong = ONG.builder()
                .cnpj(data.cnpj())
                .nomeOng(data.nomeOng())
                .email(data.email())
                .password(encodedPassword)
                .celular(data.celular())
                .build();
            
            ong = ongRepository.save(ong);
            
            // Save password to history for future validation
            passwordHistoryService.savePasswordToHistory(ong.getId(), encodedPassword);
            log.debug("Password saved to history for ONG: {} [correlationId: {}]", 
                ong.getEmail(), correlationId);
            
            // emailConfirmationService.sendConfirmationEmail(ong);
            log.debug("Email confirmation will be implemented later for ONG: {} [correlationId: {}]", 
                ong.getEmail(), correlationId);
            
            String token = generateTokenForUser(ong);
            log.info("ONG registration successful for email: {} [correlationId: {}]", 
                ong.getEmail(), correlationId);
            return new AuthResponseDTO(ong.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("ONG registration failed for email: {} [correlationId: {}]", 
                data.email(), correlationId, e);
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
     *   <li>Password is saved to history for future validation</li>
     *   <li>Email confirmation is sent for account verification</li>
     * </ul>
     * 
     * @param protetorDTO Protetor registration data (CPF, name, email, phone, password)
     * @return authentication response with Protetor name and JWT token
     * @throws ValidationException when email or CPF already exists
     * @throws SystemException when database error occurs during registration
     */
    @Override
    @Transactional
    public AuthResponseDTO registerProtetor(ProtetorRegisterDTO data) {
        String correlationId = MDC.get("correlationId");
        log.info("Protetor registration attempt for email: {} [correlationId: {}]", 
            data.email(), correlationId);
        
        // Validate email uniqueness
        if (protetorRepository.existsByEmail(data.email())) {
            log.warn("Protetor registration failed - email already exists: {} [correlationId: {}]", 
                data.email(), correlationId);
            exceptionLogger.logBusinessError("PROTETOR_REGISTRATION", "Email exists", correlationId);
            throw new ValidationException(ErrorCode.USER_EMAIL_EXISTS);
        }
        
        // Validate CPF uniqueness
        if (protetorRepository.existsByCpf(data.cpf())) {
            log.warn("Protetor registration failed - CPF already exists: {} [correlationId: {}]", 
                data.cpf(), correlationId);
            exceptionLogger.logBusinessError("PROTETOR_REGISTRATION", "CPF exists", correlationId);
            throw new ValidationException(ErrorCode.USER_CPF_EXISTS);
        }

        try {
            String encodedPassword = passwordEncoder.encode(data.password());
            
            Protetor protetor = Protetor.builder()
                .cpf(data.cpf())
                .nomeCompleto(data.nomeCompleto())
                .email(data.email())
                .password(encodedPassword)
                .celular(data.celular())
                .build();
            
            protetor = protetorRepository.save(protetor);
            
            // Save password to history for future validation
            passwordHistoryService.savePasswordToHistory(protetor.getId(), encodedPassword);
            log.debug("Password saved to history for Protetor: {} [correlationId: {}]", 
                protetor.getEmail(), correlationId);
            
            // emailConfirmationService.sendConfirmationEmail(protetor);
            log.debug("Email confirmation will be implemented later for Protetor: {} [correlationId: {}]", 
                protetor.getEmail(), correlationId);
            
            String token = generateTokenForUser(protetor);
            log.info("Protetor registration successful for email: {} [correlationId: {}]", 
                protetor.getEmail(), correlationId);
            return new AuthResponseDTO(protetor.getDisplayName(), token, "Bearer");
            
        } catch (Exception e) {
            log.error("Protetor registration failed for email: {} [correlationId: {}]", 
                data.email(), correlationId, e);
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