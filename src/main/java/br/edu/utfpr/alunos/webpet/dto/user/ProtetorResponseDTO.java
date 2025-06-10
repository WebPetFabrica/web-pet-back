package br.edu.utfpr.alunos.webpet.dto.user;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ProtetorResponseDTO(
    String id,
    String email,
    String nomeCompleto,
    String cpf,
    String endereco,
    Integer capacidadeAcolhimento,
    String celular,
    UserType userType,
    boolean active,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {}