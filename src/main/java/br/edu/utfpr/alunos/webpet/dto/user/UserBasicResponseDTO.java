package br.edu.utfpr.alunos.webpet.dto.user;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;

public record UserBasicResponseDTO(
    String id,
    String displayName,
    UserType userType,
    boolean active
) {}