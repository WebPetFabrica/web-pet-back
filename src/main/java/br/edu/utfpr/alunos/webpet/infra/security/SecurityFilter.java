package br.edu.utfpr.alunos.webpet.infra.security;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// src/main/java/br/edu/utfpr/alunos/webpet/infra/security/SecurityFilter.java
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        
        if (token != null) {
            var login = tokenService.validateToken(token);
            if (login != null) {
                UserDetails userDetails = findUserByEmail(login);
                if (userDetails != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private UserDetails findUserByEmail(String email) {
        // Buscar como usuário comum
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return new org.springframework.security.core.userdetails.User(
                user.get().getEmail(), 
                user.get().getPassword(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        // Buscar como ONG
        Optional<ONG> ong = ongRepository.findByEmail(email);
        if (ong.isPresent()) {
            return new org.springframework.security.core.userdetails.User(
                ong.get().getEmail(), 
                ong.get().getPassword(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ONG"))
            );
        }

        // Buscar como Protetor
        Optional<Protetor> protetor = protetorRepository.findByEmail(email);
        if (protetor.isPresent()) {
            return new org.springframework.security.core.userdetails.User(
                protetor.get().getEmail(), 
                protetor.get().getPassword(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROTETOR"))
            );
        }

        return null;
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}