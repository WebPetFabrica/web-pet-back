package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CNPJ;

public record ONGRegisterDTO(
    @NotBlank(message = "CNPJ é obrigatório")
    @CNPJ(message = "CNPJ inválido")
    String cnpj,
    
    @NotBlank(message = "Nome da ONG é obrigatório")
    String nomeOng,
    
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
        return UserType.ONG;
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
