// src/main/java/br/edu/utfpr/alunos/webpet/controllers/UserController.java
package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.AnimalDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userService.getUserByEmail(email);

        HashMap<String, Object> data = new HashMap<>();
        data.put("user", user);
        return ResponseEntity.ok(ResponseDTO.success("Usuário autenticado com sucesso", data));
    }

    @PostMapping("/animal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnimalDTO> createAnimal(@RequestBody AnimalDTO dto) {
        AnimalDTO created = userService.createAnimal(dto);
        return ResponseEntity.ok(created);
    }
}