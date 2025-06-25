package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;
import br.edu.utfpr.alunos.webpet.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponseDTO userProfile = userService.getCurrentUserProfile(email);
        return ResponseEntity.ok(userProfile);
    }
}
