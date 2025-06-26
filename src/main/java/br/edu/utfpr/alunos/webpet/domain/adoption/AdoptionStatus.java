package br.edu.utfpr.alunos.webpet.domain.adoption;

import lombok.Getter;

@Getter
public enum AdoptionStatus {
    PENDING("Pendente"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado");

    private final String label;

    AdoptionStatus(String label) {
        this.label = label;
    }
}