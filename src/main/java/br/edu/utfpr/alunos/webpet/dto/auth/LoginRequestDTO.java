package br.edu.utfpr.alunos.webpet.dto.auth;

import br.edu.utfpr.alunos.webpet.infra.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "Email é obrigatório")
    @ValidEmail
    String email,
    
    @NotBlank(message = "Senha é obrigatória")
    String password
) {}