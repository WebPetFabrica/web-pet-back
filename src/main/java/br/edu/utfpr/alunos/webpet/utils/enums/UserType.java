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

    /**
     * Verifica se o tipo de usuário utiliza CPF como identificação
     * @return true se o tipo de usuário utiliza CPF, false caso contrário
     */
    public boolean usesCpf() {
        return this == FISICO || this == ADMIN;
    }

    /**
     * Verifica se o tipo de usuário utiliza CNPJ como identificação
     * @return true se o tipo de usuário utiliza CNPJ, false caso contrário
     */
    public boolean usesCnpj() {
        return this == JURIDICO || this == ADMIN;
    }
}
