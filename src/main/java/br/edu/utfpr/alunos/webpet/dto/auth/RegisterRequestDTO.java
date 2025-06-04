package br.edu.utfpr.alunos.webpet.dto.auth;

import jakarta.validation.constraints.NotBlank;
import br.edu.utfpr.alunos.webpet.infra.validation.ValidEmail;
import br.edu.utfpr.alunos.webpet.infra.validation.ValidPassword;

public record RegisterRequestDTO(
    @NotBlank(message = "Nome é obrigatório")
    String name,
    
    @NotBlank(message = "Email é obrigatório")
    @ValidEmail(message = "Email inválido")
    String email,
    
    @NotBlank(message = "Senha é obrigatória")
    @ValidPassword
    String password
) {}