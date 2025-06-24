package br.edu.utfpr.alunos.webpet.infra.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BaseUser user = findUserByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getUserType().getRole()))
        );
    }

    private BaseUser findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(BaseUser.class::cast)
                .orElse(ongRepository.findByEmail(email)
                        .map(BaseUser.class::cast)
                        .orElse(protetorRepository.findByEmail(email)
                                .orElse(null)));
    }
}