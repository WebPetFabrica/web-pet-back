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

@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;
    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        
        // Verifica token se existir
        if (token != null) {
            var login = tokenService.validateToken(token);
            if (login != null) {
                User user = userRepository.findByEmail(login).orElse(null);
                if (user != null) {
                    var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        // Continua o filtro, mas as rotas ainda serão protegidas pelo SecurityConfig
        // A autenticação só ocorrerá se o token estiver presente e for válido
        filterChain.doFilter(request, response);
        
        // TODO: Em ambiente de produção, implementar validação mais rigorosa e retornar 
        // 401 Unauthorized se o token for inválido em rotas protegidas
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}