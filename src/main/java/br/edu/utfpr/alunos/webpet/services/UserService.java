// src/main/java/br/edu/utfpr/alunos/webpet/service/UserService.java
package br.edu.utfpr.alunos.webpet.services;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.utils.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<Map<String, String>> getAllOngs() {
        List<User> ongs = userRepository.findAllByUserType(UserType.JURIDICO);
        return ongs.stream()
                .map(ong -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", ong.getId());
                    map.put("name", ong.getName());
                    map.put("email", ong.getEmail());
                    map.put("phone", ong.getPhone());
                    map.put("cnpj", ong.getCnpj());
                    map.put("description", ong.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }
}