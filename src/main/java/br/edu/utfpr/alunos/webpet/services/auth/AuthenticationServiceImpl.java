package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.*;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.UserRegisterDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.AuthenticationException;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.infra.exception.SystemException;
import br.edu.utfpr.alunos.webpet.infra.exception.ValidationException;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService, UserDetailsService {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ONGRepository ongRepository;

    @Autowired
    private ProtetorRepository protetorRepository;

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private LoginAttemptService loginAttemptService;

    private AuthenticationManager authenticationManager;
    
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            BaseUser baseUser = user.get();
            return org.springframework.security.core.userdetails.User.builder()
                .username(baseUser.getEmail())
                .password(baseUser.getPassword())
                .authorities("ROLE_USER")
                .build();
        }
        Optional<ONG> ong = ongRepository.findByEmail(username);
        if (ong.isPresent()) {
            BaseUser baseUser = ong.get();
            return org.springframework.security.core.userdetails.User.builder()
                .username(baseUser.getEmail())
                .password(baseUser.getPassword())
                .authorities("ROLE_ONG")
                .build();
        }
        Optional<Protetor> protetor = protetorRepository.findByEmail(username);
        if (protetor.isPresent()) {
            BaseUser baseUser = protetor.get();
            return org.springframework.security.core.userdetails.User.builder()
                .username(baseUser.getEmail())
                .password(baseUser.getPassword())
                .authorities("ROLE_PROTETOR")
                .build();
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }

    @Override
    public AuthResponseDTO authenticate(LoginRequestDTO data) {
        authenticationManager = context.getBean(AuthenticationManager.class);

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((BaseUser) auth.getPrincipal());
        
        loginAttemptService.recordSuccessfulLogin(data.email());
        
        BaseUser user = (BaseUser) auth.getPrincipal();
        return new AuthResponseDTO(user.getDisplayName(), token, "Bearer");
    }

    @Override
    public AuthResponseDTO loginUser(LoginRequestDTO data) {
        return authenticate(data);
    }

    @Override
    public AuthResponseDTO loginOng(LoginRequestDTO data) {
        return authenticate(data);
    }

    @Override
    public AuthResponseDTO loginProtetor(LoginRequestDTO data) {
        return authenticate(data);
    }

    @Override
    public AuthResponseDTO registerUser(UserRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = User.builder()
                .name(data.name())
                .email(data.email())
                .password(encryptedPassword)
                .celular("")
                .build();
        User savedUser = this.userRepository.save(newUser);
        String token = tokenService.generateToken(savedUser);
        return new AuthResponseDTO(savedUser.getDisplayName(), token, "Bearer");
    }
    
    @Override
    public AuthResponseDTO registerONG(ONGRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        ONG newOng = ONG.builder()
                .cnpj(data.cnpj())
                .nomeOng(data.nomeOng())
                .email(data.email())
                .password(encryptedPassword)
                .celular(data.celular())
                .build();
        ONG savedOng = this.ongRepository.save(newOng);
        String token = tokenService.generateToken(savedOng);
        return new AuthResponseDTO(savedOng.getDisplayName(), token, "Bearer");
    }
    
    @Override
    public AuthResponseDTO registerProtetor(ProtetorRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        Protetor newProtetor = Protetor.builder()
                .nomeCompleto(data.nomeCompleto())
                .cpf(data.cpf())
                .email(data.email())
                .password(encryptedPassword)
                .celular(data.celular())
                .build();
        Protetor savedProtetor = this.protetorRepository.save(newProtetor);
        String token = tokenService.generateToken(savedProtetor);
        return new AuthResponseDTO(savedProtetor.getDisplayName(), token, "Bearer");
    }
    
    private void validateEmail(String email) {
        String correlationId = MDC.get("correlationId");
        if (email == null || email.trim().isEmpty()) {
            log.warn("Tentativa de operação com e-mail nulo ou vazio [correlationID: {}]", correlationId);
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD);
        }
    }

    private void validateEmailUniqueness(String email) {
        validateEmail(email);
        if (userRepository.existsByEmail(email) || ongRepository.existsByEmail(email) || protetorRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}