package br.edu.utfpr.alunos.webpet.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    String email,
    
    @NotBlank(message = "Password é obrigatória")
    String password
) {}