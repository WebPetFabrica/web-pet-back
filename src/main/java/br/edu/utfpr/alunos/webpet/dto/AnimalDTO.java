package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.utils.enums.CategoryType;
import br.edu.utfpr.alunos.webpet.utils.enums.StatusType;

public record AnimalDTO(String id, String name, String description, CategoryType category, StatusType status) {
}
