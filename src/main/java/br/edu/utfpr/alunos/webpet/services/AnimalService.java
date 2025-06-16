package br.edu.utfpr.alunos.webpet.services;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.AnimalDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.repositories.AnimalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class AnimalService {
    AnimalRepository animalRepository; // Assuming you have a way to get the repository instance

    public ResponseEntity<ResponseDTO> getAll() {
        List<Animal> animals = animalRepository.findAll();
        List<AnimalDTO> animalDTOs = animals.stream()
                .map(animal -> new AnimalDTO(
                        animal.getId(),
                        animal.getName(),
                        animal.getDescription(),
                        animal.getCategory(),
                        animal.getStatus()
                ))
                .toList();

        HashMap<String, Object> data = new HashMap<>();
        data.put("animals", animalDTOs);
        return ResponseEntity.ok(ResponseDTO.success("Lista de animais obtida com sucesso", data));
    }

    public AnimalDTO getById(String id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal não encontrado"));
        return new AnimalDTO(
                animal.getId(),
                animal.getName(),
                animal.getDescription(),
                animal.getCategory(),
                animal.getStatus()
        );
    }

    public List<AnimalDTO> getByName(String name) {
        List<Animal> animals = animalRepository.findByName(name);
        return animals.stream()
                .map(animal -> new AnimalDTO(
                        animal.getId(),
                        animal.getName(),
                        animal.getDescription(),
                        animal.getCategory(),
                        animal.getStatus()
                ))
                .toList();
    }
}
