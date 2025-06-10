package br.edu.utfpr.alunos.webpet.controllers;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.dto.AnimalDTO;
import br.edu.utfpr.alunos.webpet.services.AnimalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/animals")
@RequiredArgsConstructor
public class AnimalController {
    private final AnimalService animalService;

    @GetMapping
    public ResponseEntity<List<AnimalDTO>> getAll() {
        return ResponseEntity.ok((List<AnimalDTO>) animalService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable String id) {
        AnimalDTO animal = animalService.getById(id);
        if (animal != null) {
            return ResponseEntity.ok("Request successful! Animal details: " + animal.toString());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}