package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.AuthenticationException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.services.cache.SessionCacheService;
import br.edu.utfpr.alunos.webpet.services.cache.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CachedAuthenticationService implements AuthenticationService {
    
    private final AuthenticationServiceImpl baseAuthService;
    private final UserCacheService userCacheService;
    private final SessionCacheService sessionCacheService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginDTO) {
        // Check if user is already cached
        BaseUser cachedUser = userCacheService.getAuthenticatedUser(loginDTO.email());
        String cachedToken = userCacheService.getCachedToken(loginDTO.email());
        
        if (cachedUser != null && cachedToken != null && cachedUser.isActive()) {
            log.debug("Using cached authentication for user: {}", loginDTO.email());
            userCacheService.extendUserCache(loginDTO.email());
            return new AuthResponseDTO(cachedUser.getDisplayName(), cachedToken, "Bearer");
        }
        
        // Perform actual authentication
        AuthResponseDTO response = baseAuthService.login(loginDTO);
        
        // Cache the authentication result
        BaseUser user = baseAuthService.findUserByEmail(loginDTO.email());
        if (user != null) {
            userCacheService.cacheAuthenticatedUser(user);
            userCacheService.cacheUserToken(loginDTO.email(), response.token());
            
            // Create session
            String sessionId = UUID.randomUUID().toString();
            SessionData sessionData = new SessionData(user.getId(), user.getEmail(), 
                user.getUserType(), System.currentTimeMillis());
            sessionCacheService.createSession(sessionId, user.getId(), sessionData);
        }
        
        return response;
    }

    @Override
    public AuthResponseDTO registerUser(RegisterRequestDTO registerDTO) {
        AuthResponseDTO response = baseAuthService.registerUser(registerDTO);
        
        // Cache the new user
        BaseUser user = baseAuthService.findUserByEmail(registerDTO.email());
        if (user != null) {
            userCacheService.cacheAuthenticatedUser(user);
            userCacheService.cacheUserToken(registerDTO.email(), response.token());
        }
        
        return response;
    }

    @Override
    public AuthResponseDTO registerONG(ONGRegisterDTO ongDTO) {
        AuthResponseDTO response = baseAuthService.registerONG(ongDTO);
        
        // Cache the new ONG
        BaseUser ong = baseAuthService.findUserByEmail(ongDTO.email());
        if (ong != null) {
            userCacheService.cacheAuthenticatedUser(ong);
            userCacheService.cacheUserToken(ongDTO.email(), response.token());
        }
        
        return response;
    }

    @Override
    public AuthResponseDTO registerProtetor(ProtetorRegisterDTO protetorDTO) {
        AuthResponseDTO response = baseAuthService.registerProtetor(protetorDTO);
        
        // Cache the new Protetor
        BaseUser protetor = baseAuthService.findUserByEmail(protetorDTO.email());
        if (protetor != null) {
            userCacheService.cacheAuthenticatedUser(protetor);
            userCacheService.cacheUserToken(protetorDTO.email(), response.token());
        }
        
        return response;
    }

    public void logout(String email) {
        try {
            // Invalidate all user sessions
            BaseUser user = userCacheService.getAuthenticatedUser(email);
            if (user != null) {
                sessionCacheService.invalidateUserSessions(user.getId());
            }
            
            // Clear user cache
            userCacheService.evictUserCache(email);
            
            log.info("User logged out and cache cleared: {}", email);
        } catch (Exception e) {
            log.error("Failed to logout user: {}", email, e);
        }
    }

    public void invalidateUserCache(String email) {
        userCacheService.evictUserCache(email);
        BaseUser user = userCacheService.getAuthenticatedUser(email);
        if (user != null) {
            sessionCacheService.invalidateUserSessions(user.getId());
        }
    }

    public boolean isUserSessionValid(String sessionId) {
        return sessionCacheService.isSessionValid(sessionId);
    }

    public void extendUserSession(String email, String sessionId) {
        userCacheService.extendUserCache(email);
        sessionCacheService.extendSession(sessionId);
    }

    public record SessionData(
        String userId,
        String email,
        br.edu.utfpr.alunos.webpet.domain.user.UserType userType,
        long lastActivity
    ) {}
}