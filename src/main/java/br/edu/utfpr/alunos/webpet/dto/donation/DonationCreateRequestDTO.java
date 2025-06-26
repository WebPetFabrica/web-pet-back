package br.edu.utfpr.alunos.webpet.dto.donation;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new donation in the system.
 * 
 * <p>This record contains the essential information needed to process
 * a donation from one user to another (ONG or PROTETOR).
 * 
 * @param beneficiarioId ID of the user receiving the donation
 * @param valor donation amount in Brazilian Real
 * 
 */
@Schema(description = "Request payload for creating a new donation")
public record DonationCreateRequestDTO(
    
    @NotBlank(message = "ID do beneficiário é obrigatório")
    @Schema(description = "ID of the user receiving the donation", example = "123e4567-e89b-12d3-a456-426614174001")
    String beneficiarioId,
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Schema(description = "Donation amount in Brazilian Real", example = "50.00", minimum = "0.01")
    BigDecimal valor
    
) {}