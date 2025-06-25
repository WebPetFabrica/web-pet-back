package br.edu.utfpr.alunos.webpet.dto.user;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;

public record UserResponseDTO(
    String id,
    String email,
    String displayName,
    UserType userType,
    String username,
    Boolean active
) {}