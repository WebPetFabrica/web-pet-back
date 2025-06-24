package br.edu.utfpr.alunos.webpet.infra.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

/**
 * Service responsible for JWT token generation and validation.
 * 
 * <p>Security features:
 * <ul>
 *   <li>HMAC256 algorithm for token signing</li>
 *   <li>2-hour token expiration</li>
 *   <li>Issuer validation ("webpet")</li>
 *   <li>Subject contains user email</li>
 * </ul>
 * 
 */
@Service
public class TokenService {
    
    @Value("${api.security.token.secret}")
    private String secret;
\n    @Value("${app.jwt.timezone}")
    private String timezone;

    /**
     * Generates a JWT token for authenticated user.
     * 
     * @param userDetails authenticated user information
     * @return signed JWT token valid for 2 hours
     * @throws RuntimeException if token generation fails
     */
    public String generateToken(UserDetails userDetails) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("webpet")
                    .withSubject(userDetails.getUsername())
                    .withExpiresAt(generationExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error generating token", exception);
        }
    }

    /**
     * Validates JWT token and extracts user email.
     * 
     * @param token JWT token to validate
     * @return user email if token is valid, null otherwise
     */
    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("webpet")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    /**
     * Calculates token expiration time (2 hours from now).
     * Uses UTC-3 timezone for Brazilian time.
     * 
     * @return expiration instant
     */
    private Instant generationExpirationDate() {
        return ZonedDateTime.now(ZoneId.of(this.timezone)).plusHours(2).toInstant();
    }
}