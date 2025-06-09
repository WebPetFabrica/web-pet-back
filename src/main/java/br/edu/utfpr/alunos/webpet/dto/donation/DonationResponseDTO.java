package br.edu.utfpr.alunos.webpet.dto.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for returning donation information in API responses.
 * 
 * <p>This record contains all the donation information that should be exposed
 * to API consumers, including metadata and timestamps.
 * 
 * @param id unique donation identifier
 * @param doadorId ID of the user who made the donation
 * @param beneficiarioId ID of the user receiving the donation
 * @param valor donation amount
 * @param dataDoacao when the donation was made
 * @param createdAt when the donation was registered in the system
 * @param updatedAt when the donation was last updated
 * 
 */
@Schema(description = "Donation information response")
public record DonationResponseDTO(
    
    @Schema(description = "Unique donation identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    String id,
    
    @Schema(description = "ID of the user who made the donation", example = "123e4567-e89b-12d3-a456-426614174002")
    String doadorId,
    
    @Schema(description = "ID of the user receiving the donation", example = "123e4567-e89b-12d3-a456-426614174001")
    String beneficiarioId,
    
    @Schema(description = "Donation amount in Brazilian Real", example = "50.00")
    BigDecimal valor,
    
    @Schema(description = "When the donation was made")
    LocalDateTime dataDoacao,
    
    @Schema(description = "When the donation was registered in the system")
    LocalDateTime createdAt,
    
    @Schema(description = "When the donation was last updated")
    LocalDateTime updatedAt
    
) {}