package br.edu.utfpr.alunos.webpet.utils.enums;

import lombok.Getter;

@Getter
public enum CategoryType {
    DOG("Cachorro"),
    CAT("Gato"),
    BIRD("Passaro"),
    FISH("Peixe"),
    REPTILE("Reptil"),
    RODENT("Roedor"),
    OTHER("Outro");

    private final String label;

    CategoryType(String label) {
        this.label = label;
    }
}
