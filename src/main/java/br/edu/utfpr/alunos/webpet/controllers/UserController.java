package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<ResponseDTO> getUsers() {
        List<User> users = userRepository.findAll().stream()
                .map(user -> new User(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getCpf(),
                        user.getCnpj(),
                        user.getUserType(),
                        user.getPassword() // Exclude password
                ))
                .collect(Collectors.toList());

        Map<String, Object> data = Map.of("users", users);
        return ResponseEntity.ok(ResponseDTO.success("Lista de usuários obtida com sucesso", data));
    }
}
