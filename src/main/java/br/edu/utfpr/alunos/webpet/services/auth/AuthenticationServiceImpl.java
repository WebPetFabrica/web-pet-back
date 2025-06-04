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

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("Login attempt for email: {} [correlationId: {}]", loginDTO.email(), correlationId);
        
        if (loginAttemptService.isBlocked(loginDTO.email())) {
            log.warn("Login blocked for email: {} - account locked [correlationId: {}]", 
                loginDTO.email(), correlationId);
            exceptionLogger.logAuthenticationFailure(loginDTO.email(), "Account locked", correlationId);
            throw new AccountLockedException(ErrorCode.AUTH_ACCOUNT_LOCKED.getMessage());
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password())
            );
            
            loginAttemptService.recordSuccessfulLogin(loginDTO.email());
            
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = tokenService.generateToken(userDetails);
            String displayName = findDisplayNameByEmail(loginDTO.email());
            
            log.info("Login successful for email: {} [correlationId: {}]", loginDTO.email(), correlationId);
            return new AuthResponseDTO(displayName, token, "Bearer");
            
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
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

    @Override
    @Transactional
    public AuthResponseDTO registerUser(RegisterRequestDTO registerDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("User registration attempt for email: {} [correlationId: {}]", 
            registerDTO.email(), correlationId);
        
        if (userRepository.existsByEmail(registerDTO.email())) {
            log.warn("User registration failed - email already exists: {} [correlationId: {}]", 
                registerDTO.email(), correlationId);
            exceptionLogger.logBusinessError("USER_REGISTRATION", "Email exists", correlationId);
            throw new ValidationException(ErrorCode.USER_EMAIL_EXISTS);
        }

        try {
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

    @Override
    @Transactional
    public AuthResponseDTO registerONG(ONGRegisterDTO ongDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("ONG registration attempt for email: {} [correlationId: {}]", 
            ongDTO.email(), correlationId);
        
        if (ongRepository.existsByEmail(ongDTO.email())) {
            log.warn("ONG registration failed - email already exists: {} [correlationId: {}]", 
                ongDTO.email(), correlationId);
            exceptionLogger.logBusinessError("ONG_REGISTRATION", "Email exists", correlationId);
            throw new ValidationException(ErrorCode.USER_EMAIL_EXISTS);
        }
        
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

    private String findDisplayNameByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(BaseUser::getDisplayName)
            .orElse(ongRepository.findByEmail(email)
                .map(BaseUser::getDisplayName)
                .orElse(protetorRepository.findByEmail(email)
                    .map(BaseUser::getDisplayName)
                    .orElse("Usuário")));
    }

    private String generateTokenForEmail(String email) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            email, "", java.util.Collections.emptyList()
        );
        return tokenService.generateToken(userDetails);
    }
}