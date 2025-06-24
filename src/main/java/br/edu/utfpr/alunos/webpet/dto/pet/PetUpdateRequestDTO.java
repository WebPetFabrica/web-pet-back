package br.edu.utfpr.alunos.webpet.dto.pet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating pet information.
 * 
 * <p>All fields are optional for partial updates. Only provided fields
 * will be updated, null values are ignored.
 * 
 * @param nome new pet name (optional)
 * @param idade new pet age (optional)
 * @param disponivel new availability status (optional)
 * @param descricao new description (optional)
 * 
 */
@Schema(description = "Request payload for updating pet information")
public record PetUpdateRequestDTO(
    
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    @Schema(description = "Pet's new name", example = "Rex Junior", minLength = 2, maxLength = 255)
    String nome,
    
    @Min(value = 0, message = "Idade deve ser no mínimo 0 anos")
    @Max(value = 30, message = "Idade deve ser no máximo 30 anos")
    @Schema(description = "Pet's new age in years", example = "4", minimum = "0", maximum = "30")
    Integer idade,
    
    @Schema(description = "Whether the pet is available for adoption", example = "false")
    Boolean disponivel,
    
    @Size(max = 2000, message = "Descrição não pode exceder 2000 caracteres")
    @Schema(description = "Pet's new description", example = "Cão muito dócil, ótimo para crianças", maxLength = 2000)
    String descricao
    
) {}