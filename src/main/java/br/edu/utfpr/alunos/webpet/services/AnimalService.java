package br.edu.utfpr.alunos.webpet.services;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.dto.AnimalDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.repositories.AnimalRepository;
import br.edu.utfpr.alunos.webpet.utils.enums.CategoryType;
import br.edu.utfpr.alunos.webpet.utils.enums.StatusType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;

@Service
public class AnimalService {
    AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

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
        if (animals.isEmpty()) {
            throw new RuntimeException("Nenhum animal encontrado com o nome: " + name);
        }
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

    public List<AnimalDTO> getByCategory(CategoryType category) {
        List<Animal> animals = animalRepository.findByCategory(category);
        if (animals.isEmpty()) {
            throw new RuntimeException("Nenhum animal encontrado na categoria: " + category);
        }
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

    public List<AnimalDTO> getByStatus(StatusType status) {
        List<Animal> animals = animalRepository.findByStatus(status);
        if (animals.isEmpty()) {
            throw new RuntimeException("Nenhum animal encontrado com o status: " + status);
        }
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

    public AnimalDTO createAnimal(AnimalDTO animalDTO) {
        Animal animal = new Animal();
        animal.setName(animalDTO.name());
        animal.setDescription(animalDTO.description());
        animal.setCategory(animalDTO.category());
        animal.setStatus(animalDTO.status());

        Animal savedAnimal = animalRepository.save(animal);
        return new AnimalDTO(
                savedAnimal.getId(),
                savedAnimal.getName(),
                savedAnimal.getDescription(),
                savedAnimal.getCategory(),
                savedAnimal.getStatus()
        );
    }

    public AnimalDTO updateAnimal(String id, AnimalDTO animalDTO) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal não encontrado"));

        animal.setName(animalDTO.name());
        animal.setDescription(animalDTO.description());
        animal.setCategory(animalDTO.category());
        animal.setStatus(animalDTO.status());

        Animal updatedAnimal = animalRepository.save(animal);
        return new AnimalDTO(
                updatedAnimal.getId(),
                updatedAnimal.getName(),
                updatedAnimal.getDescription(),
                updatedAnimal.getCategory(),
                updatedAnimal.getStatus()
        );
    }

    public ResponseEntity<ResponseDTO> deleteAnimal(String id) {
        Animal animal = animalRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal não encontrado"));
        animalRepository.delete(animal);
        return ResponseEntity.ok(ResponseDTO.success("Animal deletado com sucesso", null));
    }
}
