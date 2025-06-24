package br.edu.utfpr.alunos.webpet.dto;

import java.time.LocalDateTime;

public record AdoptionResponseDTO(
        AnimalDTO animal,
        UserDTO adopter,
        LocalDateTime adoptionDate) {
}
