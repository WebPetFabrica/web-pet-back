package br.edu.utfpr.alunos.webpet.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ONGRegisterDTO(
    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos")
    String cnpj,
    
    @NotBlank(message = "Nome da ONG é obrigatório")
    String nomeOng,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "Celular é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Celular inválido")
    String celular,
    
    @NotBlank(message = "Senha é obrigatória")
    String password
) {}