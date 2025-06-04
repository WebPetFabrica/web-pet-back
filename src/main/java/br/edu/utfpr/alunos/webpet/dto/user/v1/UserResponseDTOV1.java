package br.edu.utfpr.alunos.webpet.dto.user.v1;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;

public record UserResponseDTOV1(
    String id,
    String email,
    String displayName,
    UserType userType,
    String identifier,
    boolean active
) {}