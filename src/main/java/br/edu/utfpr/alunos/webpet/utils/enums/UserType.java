package br.edu.utfpr.alunos.webpet.utils.enums;

import lombok.Getter;

@Getter
public enum UserType {
    ADMIN("Administrador"),
    FISICO("Usuário Fisico"),
    JURIDICO("Usuário Juridico");

    private final String label;

    UserType(String label) {
        this.label = label;
    }
    
    public boolean usesCpf() {
        return this == FISICO || this == ADMIN;
    }

    public boolean usesCnpj() {
        return this == JURIDICO || this == ADMIN;
    }
}
