package br.edu.utfpr.alunos.webpet.dto.pet;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO for filtering pets in search and listing operations.
 * 
 * <p>All fields are optional - null values mean no filtering on that attribute.
 * This allows for flexible pet searches with multiple criteria.
 * 
 * @param especie filter by species (optional)
 * @param genero filter by gender (optional)
 * @param porte filter by size (optional)
 * @param idadeMinima minimum age filter (optional)
 * @param idadeMaxima maximum age filter (optional)
 * @param responsavelId filter by responsible user (optional)
 * 
 */
@Schema(description = "Filters for pet search operations")
public record PetFilterDTO(
    
    @Schema(description = "Filter by pet species", example = "CACHORRO")
    Especie especie,
    
    @Schema(description = "Filter by pet gender", example = "MACHO")
    Genero genero,
    
    @Schema(description = "Filter by pet size", example = "MEDIO")
    Porte porte,
    
    @Min(value = 0, message = "Idade mínima deve ser no mínimo 0")
    @Max(value = 30, message = "Idade mínima deve ser no máximo 30")
    @Schema(description = "Minimum age filter", example = "1", minimum = "0", maximum = "30")
    Integer idadeMinima,
    
    @Min(value = 0, message = "Idade máxima deve ser no mínimo 0")
    @Max(value = 30, message = "Idade máxima deve ser no máximo 30")
    @Schema(description = "Maximum age filter", example = "10", minimum = "0", maximum = "30")
    Integer idadeMaxima,
    
    @Schema(description = "Filter by responsible user ID", example = "123e4567-e89b-12d3-a456-426614174001")
    String responsavelId
    
) {}