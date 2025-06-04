package br.edu.utfpr.alunos.webpet.dto.user;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ONGResponseDTO(
    String id,
    String email,
    String nomeOng,
    String cnpj,
    String endereco,
    String descricao,
    String celular,
    UserType userType,
    boolean active,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {}