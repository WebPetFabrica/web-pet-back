package br.edu.utfpr.alunos.webpet.infra.security;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Custom implementation of UserDetailsService for Spring Security.
 * 
 * <p>This service is responsible solely for loading user authentication details
 * from the database during the authentication process. It searches across all
 * user types (User, ONG, Protetor) to find the matching email.
 * 
 * <p>Follows the Single Responsibility Principle by handling only user loading
 * operations, delegating authentication logic to AuthenticationService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;

    /**
     * Loads user details by username (email) for Spring Security authentication.
     * 
     * <p>Searches across all user types in the following order:
     * 1. User (regular users)
     * 2. ONG (organizations)
     * 3. Protetor (individual protectors)
     * 
     * @param username the email address to search for
     * @return UserDetails containing user information and authorities
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String correlationId = MDC.get("correlationId");
        log.debug("Loading user details for username: {} [correlationId: {}]", username, correlationId);
        
        // Search in regular users
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            BaseUser baseUser = user.get();
            log.debug("User found in User table: {} [correlationId: {}]", username, correlationId);
            return buildUserDetails(baseUser, "ROLE_USER");
        }
        
        // Search in ONGs
        Optional<ONG> ong = ongRepository.findByEmail(username);
        if (ong.isPresent()) {
            BaseUser baseUser = ong.get();
            log.debug("User found in ONG table: {} [correlationId: {}]", username, correlationId);
            return buildUserDetails(baseUser, "ROLE_ONG");
        }
        
        // Search in Protetors
        Optional<Protetor> protetor = protetorRepository.findByEmail(username);
        if (protetor.isPresent()) {
            BaseUser baseUser = protetor.get();
            log.debug("User found in Protetor table: {} [correlationId: {}]", username, correlationId);
            return buildUserDetails(baseUser, "ROLE_PROTETOR");
        }
        
        log.warn("User not found for username: {} [correlationId: {}]", username, correlationId);
        throw new UsernameNotFoundException("User not found: " + username);
    }
    
    /**
     * Builds Spring Security UserDetails from BaseUser entity.
     * 
     * @param baseUser the user entity
     * @param role the role to assign to the user
     * @return UserDetails object for Spring Security
     */
    private UserDetails buildUserDetails(BaseUser baseUser, String role) {
        return org.springframework.security.core.userdetails.User.builder()
            .username(baseUser.getEmail())
            .password(baseUser.getPassword())
            .authorities(role)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
    }
}