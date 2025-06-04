package br.edu.utfpr.alunos.webpet.dto.auth;

import br.edu.utfpr.alunos.webpet.infra.validation.CPF;
import br.edu.utfpr.alunos.webpet.infra.validation.ValidEmail;
import br.edu.utfpr.alunos.webpet.infra.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProtetorRegisterDTO(
    @NotBlank(message = "Nome completo é obrigatório")
    String nomeCompleto,
    
    @NotBlank(message = "CPF é obrigatório")
    @CPF
    String cpf,
    
    @NotBlank(message = "Email é obrigatório")
    @ValidEmail (message = "Email inválido")
    String email,
    
    @NotBlank(message = "Celular é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Celular inválido")
    String celular,
    
    @NotBlank(message = "Senha é obrigatória")
    @ValidPassword
    String password
) {}