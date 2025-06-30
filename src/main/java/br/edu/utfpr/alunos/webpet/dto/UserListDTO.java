package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.utils.enums.UserType;

public record UserListDTO(
        String id,
        String name,
        String email,
        String phone,
        String cpf,
        String cnpj,
        UserType userType
) {}
