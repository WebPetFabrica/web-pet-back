// src/main/java/br/edu/utfpr/alunos/webpet/service/UserService.java
package br.edu.utfpr.alunos.webpet.services;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.AnimalDTO;
import br.edu.utfpr.alunos.webpet.repositories.AnimalRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return new User(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getCpf(),
                user.getCnpj(),
                user.getUserType(),
                user.getPassword()
        );
    }

    public AnimalDTO createAnimal(AnimalDTO dto) {
        var animal = new Animal();
        // Ajuste conforme os nomes reais dos métodos ou campos de AnimalDTO
        animal.setId(dto.id());
        animal.setName(dto.name());
        animal.setDescription(dto.description());
        animal.setCategory(dto.category());
        animal.setStatus(dto.status());

        var saved = animalRepository.save(animal);
        return new AnimalDTO(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getCategory(),
                saved.getStatus()
        );
    }
}