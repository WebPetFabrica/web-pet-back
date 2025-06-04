package br.edu.utfpr.alunos.webpet.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ONGRepository ongRepository;
    @Mock
    private ProtetorRepository protetorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private Authentication authentication;

    private AuthenticationServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthenticationServiceImpl(
            userRepository, ongRepository, protetorRepository,
            passwordEncoder, tokenService, authenticationManager, loginAttemptService
        );
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        String email = "test@test.com";
        String password = "password123";
        String token = "jwt-token";
        LoginRequestDTO loginDTO = new LoginRequestDTO(email, password);
        
        User userDetails = new User(email, password, Collections.emptyList());
        
        when(loginAttemptService.isBlocked(email)).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(tokenService.generateToken(userDetails)).thenReturn(token);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(
            br.edu.utfpr.alunos.webpet.domain.user.User.builder()
                .name("Test User")
                .email(email)
                .password("encoded")
                .celular("123456789")
                .build()
        ));

        // When
        AuthResponseDTO response = authService.login(loginDTO);

        // Then
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(loginAttemptService).recordSuccessfulLogin(email);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterRequestDTO registerDTO = new RegisterRequestDTO("Test User", "test@test.com", "password123");
        String encodedPassword = "encoded-password";
        String token = "jwt-token";
        
        br.edu.utfpr.alunos.webpet.domain.user.User savedUser = br.edu.utfpr.alunos.webpet.domain.user.User.builder()
            .name("Test User")
            .email("test@test.com")
            .password(encodedPassword)
            .celular("")
            .build();

        when(userRepository.existsByEmail(registerDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.password())).thenReturn(encodedPassword);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(tokenService.generateToken(any())).thenReturn(token);

        // When
        AuthResponseDTO response = authService.registerUser(registerDTO);

        // Then
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void shouldRegisterONGSuccessfully() {
        // Given
        ONGRegisterDTO ongDTO = new ONGRegisterDTO("12345678000190", "ONG Test", "ong@test.com", "41999999999", "password123");
        String encodedPassword = "encoded-password";
        String token = "jwt-token";
        
        ONG savedONG = ONG.builder()
            .cnpj("12345678000190")
            .nomeOng("ONG Test")
            .email("ong@test.com")
            .password(encodedPassword)
            .celular("41999999999")
            .build();

        when(ongRepository.existsByEmail(ongDTO.email())).thenReturn(false);
        when(ongRepository.existsByCnpj(ongDTO.cnpj())).thenReturn(false);
        when(passwordEncoder.encode(ongDTO.password())).thenReturn(encodedPassword);
        when(ongRepository.save(any())).thenReturn(savedONG);
        when(tokenService.generateToken(any())).thenReturn(token);

        // When
        AuthResponseDTO response = authService.registerONG(ongDTO);

        // Then
        assertThat(response.name()).isEqualTo("ONG Test");
        assertThat(response.token()).isEqualTo(token);
    }

    @Test
    void shouldRegisterProtetorSuccessfully() {
        // Given
        ProtetorRegisterDTO protetorDTO = new ProtetorRegisterDTO("João Silva", "12345678901", "joao@test.com", "41888888888", "password123");
        String encodedPassword = "encoded-password";
        String token = "jwt-token";
        
        Protetor savedProtetor = Protetor.builder()
            .nomeCompleto("João Silva")
            .cpf("12345678901")
            .email("joao@test.com")
            .password(encodedPassword)
            .celular("41888888888")
            .build();

        when(protetorRepository.existsByEmail(protetorDTO.email())).thenReturn(false);
        when(protetorRepository.existsByCpf(protetorDTO.cpf())).thenReturn(false);
        when(passwordEncoder.encode(protetorDTO.password())).thenReturn(encodedPassword);
        when(protetorRepository.save(any())).thenReturn(savedProtetor);
        when(tokenService.generateToken(any())).thenReturn(token);

        // When
        AuthResponseDTO response = authService.registerProtetor(protetorDTO);

        // Then
        assertThat(response.name()).isEqualTo("João Silva");
        assertThat(response.token()).isEqualTo(token);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequestDTO registerDTO = new RegisterRequestDTO("Test User", "test@test.com", "password123");
        when(userRepository.existsByEmail(registerDTO.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(registerDTO))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Email já cadastrado");
    }

    @Test
    void shouldThrowExceptionWhenCNPJAlreadyExists() {
        // Given
        ONGRegisterDTO ongDTO = new ONGRegisterDTO("12345678000190", "ONG Test", "ong@test.com", "41999999999", "password123");
        when(ongRepository.existsByEmail(ongDTO.email())).thenReturn(false);
        when(ongRepository.existsByCnpj(ongDTO.cnpj())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerONG(ongDTO))
            .isInstanceOf(BusinessException.class)
            .hasMessage("CNPJ já cadastrado");
    }

    @Test
    void shouldThrowExceptionWhenCPFAlreadyExists() {
        // Given
        ProtetorRegisterDTO protetorDTO = new ProtetorRegisterDTO("João Silva", "12345678901", "joao@test.com", "41888888888", "password123");
        when(protetorRepository.existsByEmail(protetorDTO.email())).thenReturn(false);
        when(protetorRepository.existsByCpf(protetorDTO.cpf())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerProtetor(protetorDTO))
            .isInstanceOf(BusinessException.class)
            .hasMessage("CPF já cadastrado");
    }
}