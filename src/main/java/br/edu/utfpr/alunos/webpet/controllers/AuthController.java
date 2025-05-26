package br.edu.utfpr.alunos.webpet.controllers;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.security.CustomUserDetailsService;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.services.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserRegistrationService userRegistrationService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body) {
        try {
            Optional<User> user = this.repository.findByEmail(body.email());
            if (user.isEmpty()) {
                User newUser = new User();
                newUser.setPassword(passwordEncoder.encode(body.password()));
                newUser.setEmail(body.email());
                newUser.setName(body.name());
                this.repository.save(newUser);

                UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getEmail());
                String token = tokenService.generateToken(userDetails);
                return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(body.email(), body.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            
            String token = this.tokenService.generateToken(userDetails);
            
            // Buscar nome do usuário
            String name = "Usuário";
            Optional<User> user = repository.findByEmail(body.email());
            if (user.isPresent()) {
                name = user.get().getName();
            }
            
            return ResponseEntity.ok(new ResponseDTO(name, token));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
    }

    @PostMapping("/register/ong")
    public ResponseEntity<ResponseDTO> registerONG(@Valid @RequestBody ONGRegisterDTO body) {
        try {
            ResponseDTO response = userRegistrationService.registerONG(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/register/protetor")
    public ResponseEntity<ResponseDTO> registerProtetor(@Valid @RequestBody ProtetorRegisterDTO body) {
        try {
            ResponseDTO response = userRegistrationService.registerProtetor(body);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
