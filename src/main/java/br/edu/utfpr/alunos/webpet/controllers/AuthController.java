package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body) {
        try {
            Optional<User> user = this.repository.findByEmail(body.email());
            if (user.isEmpty()) {
                User newUser = new User();
                newUser.setPassword(passwordEncoder.encode(body.password()));
                newUser.setEmail(body.email());
                newUser.setName(body.name());
                // Comentário: setSurname está comentado
                this.repository.save(newUser);

                String token = tokenService.generateToken(newUser);
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
            User user = this.repository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));
            if (passwordEncoder.matches(body.password(), user.getPassword())) {
                String token = this.tokenService.generateToken(user);
                return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }

}
