package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.dto.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.AnimalRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final AnimalRepository animalRepository;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body) {
        try {
            return ResponseEntity.ok(authService.register(body));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body) {
        try {
            return ResponseEntity.ok(authService.login(body));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Erro ao realizar login: " + e.getMessage()));
        }
    }
}