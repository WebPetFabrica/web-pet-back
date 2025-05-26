package br.edu.utfpr.alunos.webpet.infra.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
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
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        var token = this.recoverToken(request);
        
        if (token != null) {
            var email = tokenService.validateToken(token);
            if (email != null) {
                BaseUser user = findUserByEmail(email);
                if (user != null) {
                    var authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(user.getUserType().getRole())
                    );
                    var authentication = new UsernamePasswordAuthenticationToken(
                        user, null, authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
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
}