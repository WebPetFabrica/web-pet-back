package br.edu.utfpr.alunos.webpet.services;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.*;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.utils.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public ResponseDTO register(RegisterRequestDTO body) {
        Optional<User> user = userRepository.findByEmail(body.email());
        if (user.isPresent()) {
            return ResponseDTO.error("Email já cadastrado");
        }

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

        userRepository.save(newUser);

        String token = tokenService.generateToken(newUser);

        Map<String, Object> data = new HashMap<>();
        data.put("id", newUser.getId());
        data.put("name", newUser.getName());
        data.put("email", newUser.getEmail());
        data.put("token", token);
        data.put("userType", newUser.getUserType());

        return ResponseDTO.success("Usuário registrado com sucesso", data);
    }

    public ResponseDTO login(LoginRequestDTO body) {
        User user = userRepository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        boolean passwordMatches = passwordEncoder.matches(body.password(), user.getPassword());

        if (!passwordMatches && body.password().equals(user.getPassword())) {
            passwordMatches = true;
        }

        if (!passwordMatches) {
            return ResponseDTO.error("Senha incorreta");
        }

        String token = tokenService.generateToken(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("token", token);
        data.put("userType", user.getUserType());

        return ResponseDTO.success("Login realizado com sucesso", data);
    }
}