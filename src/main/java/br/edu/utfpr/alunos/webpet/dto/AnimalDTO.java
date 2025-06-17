package br.edu.utfpr.alunos.webpet.dto;

public record AnimalDTO(String id, String name, String description, br.edu.utfpr.alunos.webpet.utils.enums.CategoryType category, br.edu.utfpr.alunos.webpet.utils.enums.StatusType status) {
}
