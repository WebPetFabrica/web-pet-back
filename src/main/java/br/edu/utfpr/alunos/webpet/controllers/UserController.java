package br.edu.utfpr.alunos.webpet.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.UserListDTO;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.services.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ResponseDTO> getUsers() {
        List<UserListDTO> users = userRepository.findAll().stream()
                .map(user -> new UserListDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getCpf(),
                        user.getCnpj(),
                        user.getUserType()
                ))
                .toList();

        Map<String, Object> data = Map.of("users", users);
        return ResponseEntity.ok(ResponseDTO.success("Lista de usuários obtida com sucesso", data));
    }

    @GetMapping("/ongs")
    public ResponseEntity<ResponseDTO> getAllByUserType() {
        List<Map<String, String>> ongsData = userService.getAllOngs();
        Map<String, Object> data = Map.of("ongs", ongsData);
        return ResponseEntity.ok(ResponseDTO.success("Lista de ONGs obtida com sucesso", data));
    }
}
