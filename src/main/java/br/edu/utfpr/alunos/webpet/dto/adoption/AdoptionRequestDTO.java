package br.edu.utfpr.alunos.webpet.dto.adoption;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new adoption request in the system.
 * 
 * <p>This record contains the essential information needed to process
 * an adoption request from a user for a specific animal.
 * 
 * @param animalId ID of the animal being requested for adoption
 * @param userId ID of the user making the adoption request
 * 
 */
@Schema(description = "Request payload for creating a new adoption request")
public record AdoptionRequestDTO(
    
    @NotBlank(message = "ID do animal é obrigatório")
    @Schema(description = "ID of the animal being requested for adoption", example = "123e4567-e89b-12d3-a456-426614174001")
    String animalId,
    
    @NotBlank(message = "ID do usuário é obrigatório")
    @Schema(description = "ID of the user making the adoption request", example = "456e7890-e89b-12d3-a456-426614174002")
    String userId
    
) {}