package br.edu.utfpr.alunos.webpet.dto.user;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserDetailedResponseDTO(
    String id,
    String email,
    String displayName,
    UserType userType,
    String identifier,
    String celular,
    boolean active,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {}