package br.edu.utfpr.alunos.webpet.dto.user;

import br.edu.utfpr.alunos.webpet.infra.validation.CNPJ;
import br.edu.utfpr.alunos.webpet.infra.validation.ValidEmail;
import jakarta.validation.constraints.Size;

public record ONGUpdateRequestDTO(
    @Size(min = 2, max = 255, message = "Nome da ONG deve ter entre 2 e 255 caracteres")
    String nomeOng,
    
    @ValidEmail
    String email,
    
    @CNPJ
    String cnpj,
    
    @Size(max = 20, message = "Celular não pode exceder 20 caracteres")
    String celular,
    
    @Size(max = 500, message = "Endereço não pode exceder 500 caracteres")
    String endereco,
    
    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    String descricao
) {}