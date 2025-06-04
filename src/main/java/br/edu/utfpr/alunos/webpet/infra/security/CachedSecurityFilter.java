package br.edu.utfpr.alunos.webpet.infra.security;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.infra.logging.AuditLogger;
import br.edu.utfpr.alunos.webpet.infra.logging.CorrelationIdInterceptor;
import br.edu.utfpr.alunos.webpet.services.cache.CacheMetricsService;
import br.edu.utfpr.alunos.webpet.services.cache.UserCacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class CachedSecurityFilter extends OncePerRequestFilter {
    
    private final TokenService tokenService;
    private final UserCacheService userCacheService;
    private final CacheMetricsService cacheMetricsService;
    private final AuditLogger auditLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = recoverToken(request);
        String requestUri = request.getRequestURI();
        String correlationId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);
        
        if (token != null) {
            log.debug("Token found for request: {} [correlationId: {}]", requestUri, correlationId);
            
            String email = tokenService.validateToken(token);
            if (email != null) {
                // Try to get user from cache first
                BaseUser user = userCacheService.getAuthenticatedUser(email);
                
                if (user != null) {
                    cacheMetricsService.recordCacheHit("authenticatedUsers");
                    log.debug("User found in cache: {} [correlationId: {}]", email, correlationId);
                    
                    // Extend cache TTL on activity
                    userCacheService.extendUserCache(email);
                } else {
                    cacheMetricsService.recordCacheMiss("authenticatedUsers");
                    log.debug("User not in cache, loading from database: {} [correlationId: {}]", email, correlationId);
                    
                    // Load from database and cache
                    user = loadAndCacheUser(email);
                }
                
                if (user != null) {
                    if (!user.isActive()) {
                        log.warn("Authentication denied - account inactive: {} [correlationId: {}]", 
                            email, correlationId);
                        auditLogger.logTokenValidation(email, false, "Account inactive");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Conta desativada\"}");
                        return;
                    }
                    
                    var authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(user.getUserType().getRole())
                    );
                    var authentication = new UsernamePasswordAuthenticationToken(
                        user, null, authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Add user context to MDC
                    MDC.put(CorrelationIdInterceptor.USER_ID_MDC_KEY, user.getId());
                    
                    log.debug("Authentication successful for user: {} [correlationId: {}]", 
                        email, correlationId);
                    auditLogger.logTokenValidation(email, true, "Token validated successfully");
                } else {
                    log.warn("Token validation failed - user not found: {} [correlationId: {}]", 
                        email, correlationId);
                    auditLogger.logTokenValidation(email, false, "User not found");
                }
            } else {
                log.warn("Invalid token for request: {} [correlationId: {}]", requestUri, correlationId);
                auditLogger.logTokenValidation("unknown", false, "Invalid token");
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private BaseUser loadAndCacheUser(String email) {
        // This would typically call the repository directly
        // For now, return null - should be injected with proper service
        return null;
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}