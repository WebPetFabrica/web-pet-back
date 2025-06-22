package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.dto.AnimalDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.repositories.AnimalRepository;
import br.edu.utfpr.alunos.webpet.services.AnimalService;
import br.edu.utfpr.alunos.webpet.utils.enums.CategoryType;
import br.edu.utfpr.alunos.webpet.utils.enums.StatusType;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/animal")
@RequiredArgsConstructor
public class AnimalController {
    private final AnimalService animalService;

    @GetMapping("/animals")
    public ResponseEntity<ResponseDTO> getAllAnimals() {
        return animalService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalDTO> getAnimalById(@PathVariable String id) {
        AnimalDTO animal = animalService.getById(id);
        return ResponseEntity.ok(animal);
    }

    @GetMapping("/animalName/{name}")
    public ResponseEntity<List<AnimalDTO>> getAnimalsByName(@PathVariable String name) {
        List<AnimalDTO> animals = animalService.getByName(name);
        return ResponseEntity.ok(animals);
    }

    @GetMapping("/animalStatus/{status}")
    public ResponseEntity<List<AnimalDTO>> getAnimalsByStatus(@PathVariable StatusType status) {
        List<AnimalDTO> animals = animalService.getByStatus(status);
        return ResponseEntity.ok(animals);
    }

    @GetMapping("/animalCategory/{category}")
    public ResponseEntity<List<AnimalDTO>> getAnimalsByCategory(@PathVariable CategoryType category) {
        List<AnimalDTO> animals = animalService.getByCategory(category);
        return ResponseEntity.ok(animals);
    }

    @PostMapping("/createAnimal")
    public ResponseEntity<AnimalDTO> createAnimal(@RequestBody AnimalDTO animalDTO) {
        AnimalDTO createdAnimal = animalService.createAnimal(animalDTO);
        return ResponseEntity.ok(createdAnimal);
    }

    @PutMapping("/updateAnimal/{id}")
    public ResponseEntity<AnimalDTO> updateAnimal(@PathVariable String id, @RequestBody AnimalDTO animalDTO) {
        AnimalDTO updatedAnimal = animalService.updateAnimal(id, animalDTO);
        return ResponseEntity.ok(updatedAnimal);
    }

    @DeleteMapping("/deleteAnimal/{id}")
    public ResponseEntity<Void> deleteAnimal(@PathVariable String id) {
        animalService.deleteAnimal(id);
        return ResponseEntity.noContent().build();
    }
}