package br.edu.utfpr.alunos.webpet.infra.security;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.domain.ong.ONG;
import br.edu.utfpr.alunos.webpet.domain.protetor.Protetor;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Primeiro tenta encontrar como usuário comum
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            return new org.springframework.security.core.userdetails.User(
                    user.get().getEmail(),
                    user.get().getPassword(),
                    new ArrayList<>());
        }

        // Depois tenta encontrar como ONG
        Optional<ONG> ong = ongRepository.findByEmail(username);
        if (ong.isPresent()) {
            return new org.springframework.security.core.userdetails.User(
                    ong.get().getEmail(),
                    ong.get().getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ONG")));
        }

        // Por último tenta encontrar como Protetor
        Optional<Protetor> protetor = protetorRepository.findByEmail(username);
        if (protetor.isPresent()) {
            return new org.springframework.security.core.userdetails.User(
                    protetor.get().getEmail(),
                    protetor.get().getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROTETOR")));
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}