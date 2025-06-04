package br.edu.utfpr.alunos.webpet.infra.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.infra.logging.AuditLogger;
import br.edu.utfpr.alunos.webpet.infra.logging.CorrelationIdInterceptor;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);
    
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final AuditLogger auditLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = recoverToken(request);
        String requestUri = request.getRequestURI();
        String clientIp = getClientIp(request);
        String correlationId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);
        
        // Add client IP to MDC
        MDC.put("clientIp", clientIp);
        
        if (token != null) {
            log.debug("Token found for request: {} [correlationId: {}]", requestUri, correlationId);
            
            String email = tokenService.validateToken(token);
            if (email != null) {
                BaseUser user = findUserByEmail(email);
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
        } else {
            log.debug("No token provided for request: {} [correlationId: {}]", requestUri, correlationId);
        }
        
        filterChain.doFilter(request, response);
    }

    private BaseUser findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(BaseUser.class::cast)
                .orElse(ongRepository.findByEmail(email)
                        .map(BaseUser.class::cast)
                        .orElse(protetorRepository.findByEmail(email)
                                .orElse(null)));
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}