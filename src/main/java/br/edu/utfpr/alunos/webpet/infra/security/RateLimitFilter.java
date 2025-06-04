package br.edu.utfpr.alunos.webpet.infra.security;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RateLimitConfig rateLimitConfig;
    private final ConcurrentHashMap<String, Bucket> rateLimitCache;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();
        
        Bucket bucket = getBucket(clientIp, endpoint);
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"message\":\"Muitas tentativas. Tente novamente em alguns minutos.\",\"code\":429}"
            );
        }
    }
    
    private Bucket getBucket(String clientIp, String endpoint) {
        String key = clientIp + ":" + endpoint;
        
        return rateLimitCache.computeIfAbsent(key, k -> {
            if (endpoint.contains("/auth/")) {
                return rateLimitConfig.createAuthBucket();
            }
            return rateLimitConfig.createGeneralBucket();
        });
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}