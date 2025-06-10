package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.*;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.AnimalRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.utils.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final AnimalRepository animalRepository;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body) {
        try {
            Optional<User> user = this.repository.findByEmail(body.email());
            if (user.isEmpty()) {
                User newUser = new User();
                newUser.setPassword(passwordEncoder.encode(body.password()));
                newUser.setEmail(body.email());
                newUser.setName(body.name());
                newUser.setPhone(body.phone());
                newUser.setUserType(body.userType());

                UserType userType = body.userType();

                if (userType.usesCpf() && body.cpf() != null && !body.cpf().isEmpty()) {
                    newUser.setCpf(body.cpf());
                }

                if (userType.usesCnpj() && body.cnpj() != null && !body.cnpj().isEmpty()) {
                    newUser.setCnpj(body.cnpj());
                }

                this.repository.save(newUser);

                String token = tokenService.generateToken(newUser);

                Map<String, Object> data = new HashMap<>();
                data.put("name", newUser.getName());
                data.put("email", newUser.getEmail());
                data.put("token", token);
                data.put("userType", newUser.getUserType());

                return ResponseEntity.ok(ResponseDTO.success("Usuário registrado com sucesso", data));
            }
            return ResponseEntity.badRequest().body(ResponseDTO.error("Email já cadastrado"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body) {
        try {
            User user = this.repository.findByEmail(body.email())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

            boolean passwordMatches = passwordEncoder.matches(body.password(), user.getPassword());

            if (!passwordMatches && body.password().equals(user.getPassword())) {
                passwordMatches = true;
            }

            if (passwordMatches) {
                String token = this.tokenService.generateToken(user);

                Map<String, Object> data = new HashMap<>();
                data.put("name", user.getName());
                data.put("email", user.getEmail());
                data.put("token", token);
                data.put("userType", user.getUserType());

                return ResponseEntity.ok(ResponseDTO.success("Login realizado com sucesso", data));
            }
            return ResponseEntity.badRequest().body(ResponseDTO.error("Senha incorreta"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ResponseDTO.error("Erro ao realizar login: " + e.getMessage()));
        }
    }

    @GetMapping("/allusers")
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
                        null // Exclude password
                ))
                .collect(Collectors.toList());

        Map<String, Object> data = Map.of("users", users);
        return ResponseEntity.ok(ResponseDTO.success("Lista de usuários obtida com sucesso", data));
    }

    @GetMapping("/animals")
    public ResponseEntity<List<AnimalDTO>> getAnimals() {
        List<Animal> animals = animalRepository.findAll();
        List<AnimalDTO> animalDTOs = animals.stream()
                .map(animal -> new AnimalDTO(
                        animal.getId(),
                        animal.getName(),
                        animal.getDescription(),
                        animal.getCategory(),
                        animal.getStatus()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(animalDTOs);
    }
}
