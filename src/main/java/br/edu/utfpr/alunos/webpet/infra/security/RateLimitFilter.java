package br.edu.utfpr.alunos.webpet.infra.security;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.edu.utfpr.alunos.webpet.infra.logging.AuditLogger;
import br.edu.utfpr.alunos.webpet.infra.logging.CorrelationIdInterceptor;
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
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private final RateLimitConfig rateLimitConfig;
    private final ConcurrentHashMap<String, Bucket> rateLimitCache;
    private final AuditLogger auditLogger;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        String correlationId = MDC.get(CorrelationIdInterceptor.CORRELATION_ID_MDC_KEY);
        
        log.debug("Rate limit check for {}:{} from IP: {} [correlationId: {}]", 
            method, endpoint, clientIp, correlationId);
        
        Bucket bucket = getBucket(clientIp, endpoint);
        
        if (bucket.tryConsume(1)) {
            log.debug("Rate limit check passed for {}:{} from IP: {} [correlationId: {}]", 
                method, endpoint, clientIp, correlationId);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for {}:{} from IP: {} [correlationId: {}]", 
                method, endpoint, clientIp, correlationId);
            
            auditLogger.logRateLimitExceeded(clientIp, endpoint);
            auditLogger.logSecurityEvent("RATE_LIMIT_EXCEEDED", "MEDIUM", 
                String.format("Rate limit exceeded for %s:%s from IP: %s", method, endpoint, clientIp));
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.getWriter().write(String.format(
                "{\"message\":\"Rate limit exceeded for endpoint %s. Try again in 1 minute.\",\"code\":429,\"correlationId\":\"%s\"}", 
                endpoint, correlationId
            ));
        }
    }
    
    private Bucket getBucket(String clientIp, String endpoint) {
        String key = clientIp + ":" + endpoint;
        
        return rateLimitCache.computeIfAbsent(key, k -> {
            if (endpoint.contains("/auth/")) {
                log.debug("Creating auth rate limit bucket for: {}", key);
                return rateLimitConfig.createAuthBucket();
            }
            log.debug("Creating general rate limit bucket for: {}", key);
            return rateLimitConfig.createGeneralBucket();
        });
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String ip = xForwardedFor.split(",")[0].trim();
            log.debug("Client IP from X-Forwarded-For: {}", ip);
            return ip;
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            log.debug("Client IP from X-Real-IP: {}", xRealIp);
            return xRealIp;
        }
        
        String remoteAddr = request.getRemoteAddr();
        log.debug("Client IP from RemoteAddr: {}", remoteAddr);
        return remoteAddr;
    }
}