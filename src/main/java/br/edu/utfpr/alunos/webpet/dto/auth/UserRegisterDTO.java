package br.edu.utfpr.alunos.webpet.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(
    @NotBlank(message = "Nome é obrigatório")
    String name,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    String email,
    
    @NotBlank(message = "Password é obrigatória")
    @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    String password
) {}