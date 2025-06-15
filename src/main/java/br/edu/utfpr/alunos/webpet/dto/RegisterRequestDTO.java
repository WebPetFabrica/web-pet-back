package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.utils.enums.UserType;

public record RegisterRequestDTO(String name,
                                 String email,
                                 String phone,
                                 String cpf,
                                 String cnpj,
                                 UserType userType,
                                 String password) {
}
