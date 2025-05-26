package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record ProtetorRegisterDTO(
    @NotBlank(message = "Nome completo é obrigatório")
    String nomeCompleto,
    
    @NotBlank(message = "CPF é obrigatório")
    @CPF(message = "CPF inválido")
    String cpf,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "Celular é obrigatório")
    String celular,
    
    @NotBlank(message = "Password é obrigatório")
    String password
) implements RegisterDTO {
    @Override
    public UserType getUserType() {
        return UserType.PROTETOR;
    }
    
    @Override
    public String getEmail() {
        return email();
    }
    
    @Override
    public String getPassword() {
        return password();
    }
}
