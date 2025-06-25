package br.edu.utfpr.alunos.webpet.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ONGRegisterDTO(
    @NotBlank(message = "Nome da ONG é obrigatório")
    String nomeOng,
    
    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 dígitos")
    String cnpj,
    
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