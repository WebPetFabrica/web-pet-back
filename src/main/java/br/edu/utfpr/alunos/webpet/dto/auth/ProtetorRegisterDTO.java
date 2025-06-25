package br.edu.utfpr.alunos.webpet.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProtetorRegisterDTO(
    @NotBlank(message = "Nome completo é obrigatório")
    String nomeCompleto,
    
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos")
    String cpf,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    String email,
    
    @NotBlank(message = "Password é obrigatória")
    @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    String password,
    
    @NotBlank(message = "Celular é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Celular deve ter 10 ou 11 dígitos")
    String celular
) {}