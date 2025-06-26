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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserRegistrationService.
 * 
 * <p>These tests validate the user registration logic, ensuring proper
 * validation, error handling, and integration with dependencies.
 * Key scenarios tested:
 * <ul>
 *   <li>Successful registration for all user types</li>
 *   <li>Email uniqueness validation</li>
 *   <li>Password policy enforcement</li>
 *   <li>Data validation</li>
 *   <li>Error handling and logging</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegistrationService Tests")
class UserRegistrationServiceTest {

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
    private PasswordPolicyService passwordPolicyService;
    
    @InjectMocks
    private UserRegistrationService userRegistrationService;
    
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "ValidPassword123!";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPassword";
    private static final String JWT_TOKEN = "jwt.token.here";
    
    @BeforeEach
    void setUp() {
        // Default mock behaviors (lenient to avoid unnecessary stubbing warnings)
        lenient().when(passwordPolicyService.isValidPassword(VALID_PASSWORD)).thenReturn(true);
        lenient().when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        lenient().when(tokenService.generateToken(any())).thenReturn(JWT_TOKEN);
        
        // Default email uniqueness - no existing users
        lenient().when(userRepository.existsByEmail(anyString())).thenReturn(false);
        lenient().when(ongRepository.existsByEmail(anyString())).thenReturn(false);
        lenient().when(protetorRepository.existsByEmail(anyString())).thenReturn(false);
    }
    
    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register a new user")
        void shouldSuccessfullyRegisterNewUser() {
            // Given
            UserRegisterDTO registerDTO = new UserRegisterDTO(
                "John Doe", VALID_EMAIL, VALID_PASSWORD
            );
            
            User savedUser = User.builder()
                .name("John Doe")
                .email(VALID_EMAIL)
                .password(ENCODED_PASSWORD)
                .celular("")
                .build();
            
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            
            // When
            AuthResponseDTO result = userRegistrationService.registerUser(registerDTO);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(JWT_TOKEN);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            
            verify(passwordPolicyService).isValidPassword(VALID_PASSWORD);
            verify(passwordEncoder).encode(VALID_PASSWORD);
            verify(userRepository).save(any(User.class));
            verify(tokenService).generateToken(savedUser);
        }
        
        @Test
        @DisplayName("Should throw ValidationException for null email")
        void shouldThrowValidationExceptionForNullEmail() {
            // Given
            UserRegisterDTO registerDTO = new UserRegisterDTO(
                "John Doe", null, VALID_PASSWORD
            );
            
            // When & Then
            assertThatThrownBy(() -> userRegistrationService.registerUser(registerDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email é obrigatório");
            
            verify(userRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw ValidationException for empty email")
        void shouldThrowValidationExceptionForEmptyEmail() {
            // Given
            UserRegisterDTO registerDTO = new UserRegisterDTO(
                "John Doe", "  ", VALID_PASSWORD
            );
            
            // When & Then
            assertThatThrownBy(() -> userRegistrationService.registerUser(registerDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email é obrigatório");
        }
        
        @Test
        @DisplayName("Should throw ValidationException for weak password")
        void shouldThrowValidationExceptionForWeakPassword() {
            // Given
            String weakPassword = "123";
            UserRegisterDTO registerDTO = new UserRegisterDTO(
                "John Doe", VALID_EMAIL, weakPassword
            );
            
            when(passwordPolicyService.isValidPassword(weakPassword)).thenReturn(false);
            when(passwordPolicyService.getPasswordPolicy()).thenReturn("Password must be strong");
            
            // When & Then
            assertThatThrownBy(() -> userRegistrationService.registerUser(registerDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Senha não atende aos critérios de segurança");
        }
        
        @Test
        @DisplayName("Should throw BusinessException for existing email")
        void shouldThrowBusinessExceptionForExistingEmail() {
            // Given
            UserRegisterDTO registerDTO = new UserRegisterDTO(
                "John Doe", VALID_EMAIL, VALID_PASSWORD
            );
            
            when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);
            
            // When & Then
            assertThatThrownBy(() -> userRegistrationService.registerUser(registerDTO))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
    
    @Nested
    @DisplayName("ONG Registration Tests")
    class ONGRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register a new ONG")
        void shouldSuccessfullyRegisterNewONG() {
            // Given
            ONGRegisterDTO registerDTO = new ONGRegisterDTO(
                "12345678901234", "Animal Care ONG", VALID_EMAIL, 
                VALID_PASSWORD, "11999999999"
            );
            
            ONG savedONG = ONG.builder()
                .cnpj("12345678901234")
                .nomeOng("Animal Care ONG")
                .email(VALID_EMAIL)
                .password(ENCODED_PASSWORD)
                .celular("11999999999")
                .build();
            
            when(ongRepository.save(any(ONG.class))).thenReturn(savedONG);
            
            // When
            AuthResponseDTO result = userRegistrationService.registerONG(registerDTO);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(JWT_TOKEN);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            
            verify(ongRepository).save(any(ONG.class));
            verify(tokenService).generateToken(savedONG);
        }
        
        @Test
        @DisplayName("Should throw BusinessException when ONG email already exists")
        void shouldThrowBusinessExceptionWhenONGEmailExists() {
            // Given
            ONGRegisterDTO registerDTO = new ONGRegisterDTO(
                "12345678901234", "Animal Care ONG", VALID_EMAIL, 
                VALID_PASSWORD, "11999999999"
            );
            
            when(ongRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);
            
            // When & Then
            assertThatThrownBy(() -> userRegistrationService.registerONG(registerDTO))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
    
    @Nested
    @DisplayName("Protetor Registration Tests")
    class ProtetorRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register a new Protetor")
        void shouldSuccessfullyRegisterNewProtetor() {
            // Given
            ProtetorRegisterDTO registerDTO = new ProtetorRegisterDTO(
                "João Silva", "12345678901", VALID_EMAIL, 
                VALID_PASSWORD, "11999999999"
            );
            
            Protetor savedProtetor = Protetor.builder()
                .nomeCompleto("João Silva")
                .cpf("12345678901")
                .email(VALID_EMAIL)
                .password(ENCODED_PASSWORD)
                .celular("11999999999")
                .build();
            
            when(protetorRepository.save(any(Protetor.class))).thenReturn(savedProtetor);
            
            // When
            AuthResponseDTO result = userRegistrationService.registerProtetor(registerDTO);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(JWT_TOKEN);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            
            verify(protetorRepository).save(any(Protetor.class));
            verify(tokenService).generateToken(savedProtetor);
        }
        
        @Test
        @DisplayName("Should throw BusinessException when Protetor email already exists")
        void shouldThrowBusinessExceptionWhenProtetorEmailExists() {
            // Given
            ProtetorRegisterDTO registerDTO = new ProtetorRegisterDTO(
                "João Silva", "12345678901", VALID_EMAIL, 
                VALID_PASSWORD, "11999999999"
            );
            
            when(protetorRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);
            
            // When & Then
            assertThatThrownBy(() -> userRegistrationService.registerProtetor(registerDTO))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
    
    @Nested
    @DisplayName("Email Uniqueness Validation Tests")
    class EmailUniquenessTests {
        
        @Test
        @DisplayName("Should validate email uniqueness across all user types")
        void shouldValidateEmailUniquenessAcrossAllUserTypes() {
            // Test User collision
            when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);
            
            UserRegisterDTO userDTO = new UserRegisterDTO("Test", VALID_EMAIL, VALID_PASSWORD);
            assertThatThrownBy(() -> userRegistrationService.registerUser(userDTO))
                .isInstanceOf(BusinessException.class);
            
            // Reset and test ONG collision
            when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);
            when(ongRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);
            
            ONGRegisterDTO ongDTO = new ONGRegisterDTO("123", "ONG", VALID_EMAIL, VALID_PASSWORD, "111");
            assertThatThrownBy(() -> userRegistrationService.registerONG(ongDTO))
                .isInstanceOf(BusinessException.class);
            
            // Reset and test Protetor collision
            when(ongRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);
            when(protetorRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);
            
            ProtetorRegisterDTO protetorDTO = new ProtetorRegisterDTO("Nome", "123", VALID_EMAIL, VALID_PASSWORD, "111");
            assertThatThrownBy(() -> userRegistrationService.registerProtetor(protetorDTO))
                .isInstanceOf(BusinessException.class);
        }
    }
}