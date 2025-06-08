package br.edu.utfpr.alunos.webpet.dto.donation;

import java.math.BigDecimal;

import br.edu.utfpr.alunos.webpet.domain.donation.TipoDoacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DonationCreateRequestDTO(
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    BigDecimal valor,
    
    @NotNull(message = "Tipo de doação é obrigatório")
    TipoDoacao tipoDoacao,
    
    @Size(max = 500, message = "Mensagem não pode exceder 500 caracteres")
    String mensagem,
    
    @NotBlank(message = "Nome do doador é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    String nomeDoador,
    
    @Email(message = "Email deve ter formato válido")
    String emailDoador,
    
    @Size(max = 20, message = "Telefone não pode exceder 20 caracteres")
    String telefoneDoador,
    
    @NotBlank(message = "ID do beneficiário é obrigatório")
    String beneficiarioId
) {}