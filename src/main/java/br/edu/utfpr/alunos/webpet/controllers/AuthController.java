package br.edu.utfpr.alunos.webpet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.alunos.webpet.dto.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.services.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@RequestBody RegisterRequestDTO body) {
        try {
            return ResponseEntity.ok(authService.register(body));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody LoginRequestDTO body) {
        try {
            return ResponseEntity.ok(authService.login(body));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Erro ao realizar login: " + e.getMessage()));
        }
    }
}