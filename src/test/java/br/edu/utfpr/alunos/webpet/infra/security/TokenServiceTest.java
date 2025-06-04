package br.edu.utfpr.alunos.webpet.infra.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class TokenServiceTest {

    private TokenService tokenService;
    private final String testSecret = "test-secret-key-for-testing-purposes-must-be-at-least-64-characters-long";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", testSecret);
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        // Given
        UserDetails userDetails = new User("test@test.com", "password", Collections.emptyList());

        // When
        String token = tokenService.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given
        UserDetails userDetails = new User("test@test.com", "password", Collections.emptyList());
        String token = tokenService.generateToken(userDetails);

        // When
        String validatedEmail = tokenService.validateToken(token);

        // Then
        assertThat(validatedEmail).isEqualTo("test@test.com");
    }

    @Test
    void shouldReturnNullForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        String result = tokenService.validateToken(invalidToken);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForExpiredToken() {
        // Given
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ3ZWJwZXQiLCJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiZXhwIjoxNjcwMDAwMDAwfQ.invalid";

        // When
        String result = tokenService.validateToken(expiredToken);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldGenerateTokenWithCorrectIssuer() {
        // Given
        UserDetails userDetails = new User("test@test.com", "password", Collections.emptyList());

        // When
        String token = tokenService.generateToken(userDetails);

        // Then
        String validatedEmail = tokenService.validateToken(token);
        assertThat(validatedEmail).isEqualTo("test@test.com");
    }

    @Test
    void shouldGenerateTokensWithDifferentTimestamps() throws InterruptedException {
        // Given
        UserDetails userDetails = new User("test@test.com", "password", Collections.emptyList());

        // When
        String token1 = tokenService.generateToken(userDetails);
        Thread.sleep(1000); // Wait 1 second
        String token2 = tokenService.generateToken(userDetails);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(tokenService.validateToken(token1)).isEqualTo("test@test.com");
        assertThat(tokenService.validateToken(token2)).isEqualTo("test@test.com");
    }

    @Test
    void shouldHandleNullToken() {
        // When
        String result = tokenService.validateToken(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleEmptyToken() {
        // When
        String result = tokenService.validateToken("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldValidateTokenWithSpecialCharactersInEmail() {
        // Given
        UserDetails userDetails = new User("test+special@example-domain.com", "password", Collections.emptyList());
        String token = tokenService.generateToken(userDetails);

        // When
        String validatedEmail = tokenService.validateToken(token);

        // Then
        assertThat(validatedEmail).isEqualTo("test+special@example-domain.com");
    }

    @Test
    void shouldThrowExceptionForNullUserDetails() {
        // When & Then
        assertThatThrownBy(() -> tokenService.generateToken(null))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldReturnNullForMalformedToken() {
        // Given
        String malformedToken = "not.a.valid.jwt.token.structure";

        // When
        String result = tokenService.validateToken(malformedToken);

        // Then
        assertThat(result).isNull();
    }
}