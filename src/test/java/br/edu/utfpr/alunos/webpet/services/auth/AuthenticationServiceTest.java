package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService.
 * 
 * <p>These tests validate the authentication logic, ensuring proper
 * integration with Spring Security, token generation, and error handling.
 * Key scenarios tested:
 * <ul>
 *   <li>Successful authentication</li>
 *   <li>Invalid credentials handling</li>
 *   <li>Token generation integration</li>
 *   <li>Login attempt recording</li>
 *   <li>Error handling and logging</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private TokenService tokenService;
    
    @Mock
    private LoginAttemptService loginAttemptService;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthenticationServiceImpl authenticationService;
    
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "ValidPassword123!";
    private static final String INVALID_PASSWORD = "wrongpassword";
    private static final String JWT_TOKEN = "jwt.token.here";
    
    private User mockUser;
    
    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .name("Test User")
            .email(VALID_EMAIL)
            .password("encodedPassword")
            .celular("11999999999")
            .build();
    }
    
    @Nested
    @DisplayName("Successful Authentication Tests")
    class SuccessfulAuthenticationTests {
        
        @Test
        @DisplayName("Should successfully authenticate user with valid credentials")
        void shouldSuccessfullyAuthenticateUserWithValidCredentials() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, VALID_PASSWORD);
            
            when(authentication.getPrincipal()).thenReturn(mockUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(tokenService.generateToken(mockUser)).thenReturn(JWT_TOKEN);
            
            // When
            AuthResponseDTO result = authenticationService.authenticate(loginRequest);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(JWT_TOKEN);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.name()).isEqualTo(mockUser.getDisplayName());
            
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService).generateToken(mockUser);
            verify(loginAttemptService).recordSuccessfulLogin(VALID_EMAIL);
        }
        
        @Test
        @DisplayName("Should create correct authentication token")
        void shouldCreateCorrectAuthenticationToken() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, VALID_PASSWORD);
            
            when(authentication.getPrincipal()).thenReturn(mockUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(tokenService.generateToken(any(BaseUser.class))).thenReturn(JWT_TOKEN);
            
            // When
            authenticationService.authenticate(loginRequest);
            
            // Then
            verify(authenticationManager).authenticate(argThat(token -> {
                UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) token;
                return VALID_EMAIL.equals(authToken.getPrincipal()) &&
                       VALID_PASSWORD.equals(authToken.getCredentials());
            }));
        }
        
        @Test
        @DisplayName("Should record successful login attempt")
        void shouldRecordSuccessfulLoginAttempt() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, VALID_PASSWORD);
            
            when(authentication.getPrincipal()).thenReturn(mockUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(tokenService.generateToken(any(BaseUser.class))).thenReturn(JWT_TOKEN);
            
            // When
            authenticationService.authenticate(loginRequest);
            
            // Then
            verify(loginAttemptService).recordSuccessfulLogin(VALID_EMAIL);
        }
    }
    
    @Nested
    @DisplayName("Authentication Failure Tests")
    class AuthenticationFailureTests {
        
        @Test
        @DisplayName("Should throw BadCredentialsException for invalid password")
        void shouldThrowBadCredentialsExceptionForInvalidPassword() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, INVALID_PASSWORD);
            
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
            
            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
            
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService, never()).generateToken(any());
            verify(loginAttemptService, never()).recordSuccessfulLogin(anyString());
        }
        
        @Test
        @DisplayName("Should throw AuthenticationException for non-existent user")
        void shouldThrowAuthenticationExceptionForNonExistentUser() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";
            LoginRequestDTO loginRequest = new LoginRequestDTO(nonExistentEmail, VALID_PASSWORD);
            
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("User not found"));
            
            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationException.class);
            
            verify(tokenService, never()).generateToken(any());
            verify(loginAttemptService, never()).recordSuccessfulLogin(anyString());
        }
        
        @Test
        @DisplayName("Should not record login attempt on authentication failure")
        void shouldNotRecordLoginAttemptOnAuthenticationFailure() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, INVALID_PASSWORD);
            
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
            
            // When
            try {
                authenticationService.authenticate(loginRequest);
            } catch (AuthenticationException e) {
                // Expected exception
            }
            
            // Then
            verify(loginAttemptService, never()).recordSuccessfulLogin(anyString());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null principal gracefully")
        void shouldHandleNullPrincipalGracefully() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, VALID_PASSWORD);
            
            when(authentication.getPrincipal()).thenReturn(null);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            
            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authentication succeeded but no user principal was returned");
            
            verify(tokenService, never()).generateToken(any());
        }
        
        @Test
        @DisplayName("Should handle TokenService failure")
        void shouldHandleTokenServiceFailure() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, VALID_PASSWORD);
            
            when(authentication.getPrincipal()).thenReturn(mockUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(tokenService.generateToken(mockUser))
                .thenThrow(new RuntimeException("Token generation failed"));
            
            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token generation failed");
            
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService).generateToken(mockUser);
            // Note: LoginAttemptService should still record successful authentication
            // since the failure is in token generation, not authentication
            verify(loginAttemptService).recordSuccessfulLogin(VALID_EMAIL);
        }
        
        @Test
        @DisplayName("Should handle LoginAttemptService failure gracefully")
        void shouldHandleLoginAttemptServiceFailureGracefully() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, VALID_PASSWORD);
            
            when(authentication.getPrincipal()).thenReturn(mockUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            doThrow(new RuntimeException("Login recording failed"))
                .when(loginAttemptService).recordSuccessfulLogin(VALID_EMAIL);
            
            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Login recording failed");
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should perform complete authentication flow")
        void shouldPerformCompleteAuthenticationFlow() {
            // Given
            LoginRequestDTO loginRequest = new LoginRequestDTO(VALID_EMAIL, VALID_PASSWORD);
            
            when(authentication.getPrincipal()).thenReturn(mockUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(tokenService.generateToken(mockUser)).thenReturn(JWT_TOKEN);
            
            // When
            AuthResponseDTO result = authenticationService.authenticate(loginRequest);
            
            // Then - Verify complete flow
            assertThat(result.name()).isEqualTo(mockUser.getDisplayName());
            assertThat(result.token()).isEqualTo(JWT_TOKEN);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            
            // Verify all dependencies were called in correct order
            var inOrder = inOrder(authenticationManager, tokenService, loginAttemptService);
            inOrder.verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            inOrder.verify(tokenService).generateToken(mockUser);
            inOrder.verify(loginAttemptService).recordSuccessfulLogin(VALID_EMAIL);
        }
    }
}