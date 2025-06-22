package br.edu.utfpr.alunos.webpet.utils.enums;

import lombok.Getter;

@Getter
public enum StatusType {
    AVAILABLE("Disponível"),
    ADOPTED("Adotado");


    private final String label;

    StatusType(String label) {
        this.label = label;
    }
}
