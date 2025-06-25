package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.*;
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
        return userRepository.findByEmail(username);
    }

    @Override
    public String login(LoginRequestDTO data) {
        authenticationManager = context.getBean(AuthenticationManager.class);

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((BaseUser) auth.getPrincipal());
        
        loginAttemptService.loginSucceeded(data.email());
        
        return token;
    }

    @Override
    public UserDetails registerUser(UserRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.username(), data.email(), encryptedPassword, UserType.USER);
        return this.userRepository.save(newUser);
    }
    
    @Override
    public UserDetails registerOng(ONGRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        ONG newOng = new ONG(data.username(), data.email(), encryptedPassword, data.cnpj());
        return this.ongRepository.save(newOng);
    }
    
    @Override
    public UserDetails registerProtetor(ProtetorRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        Protetor newProtetor = new Protetor(data.username(), data.email(), encryptedPassword, data.cpf());
        return this.protetorRepository.save(newProtetor);
    }
    
    private void validateEmail(String email) {
        String correlationId = MDC.get("correlationId");
        if (email == null || email.trim().isEmpty()) {
            log.warn("Tentativa de operação com e-mail nulo ou vazio [correlationID: {}]", correlationId);
            throw new ValidationException("O e-mail não pode ser nulo ou vazio.");
        }
    }

    private void validateEmailUniqueness(String email) {
        validateEmail(email);
        if (userRepository.existsByEmail(email) || ongRepository.existsByEmail(email) || protetorRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}