package br.edu.utfpr.alunos.webpet.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.services.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDTO) {
        return ResponseEntity.ok(authenticationService.login(loginDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authenticationService.registerUser(registerDTO));
    }

    @PostMapping("/register/ong")
    public ResponseEntity<AuthResponseDTO> registerONG(@Valid @RequestBody ONGRegisterDTO ongDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authenticationService.registerONG(ongDTO));
    }

    @PostMapping("/register/protetor")
    public ResponseEntity<AuthResponseDTO> registerProtetor(@Valid @RequestBody ProtetorRegisterDTO protetorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authenticationService.registerProtetor(protetorDTO));
    }
}
