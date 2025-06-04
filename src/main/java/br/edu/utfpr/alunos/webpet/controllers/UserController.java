package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;
import br.edu.utfpr.alunos.webpet.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        UserResponseDTO user = userService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PatchMapping("/deactivate")
    public ResponseEntity<Void> deactivateCurrentUser(Authentication authentication) {
        userService.deactivateUser(authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/activate")
    public ResponseEntity<Void> activateCurrentUser(Authentication authentication) {
        userService.activateUser(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}