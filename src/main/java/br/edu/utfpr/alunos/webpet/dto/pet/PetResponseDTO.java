package br.edu.utfpr.alunos.webpet.dto.pet;

import java.time.LocalDateTime;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for returning pet information in API responses.
 * 
 * <p>This record contains all the pet information that should be exposed
 * to API consumers, including computed fields and metadata.
 * 
 * @param id unique pet identifier
 * @param nome pet's name
 * @param especie pet's species
 * @param porte pet's size
 * @param genero pet's gender
 * @param idade pet's age in years
 * @param responsavelId ID of the responsible user
 * @param disponivel whether the pet is available for adoption
 * @param descricao optional description
 * @param createdAt when the pet was registered
 * @param updatedAt when the pet was last updated
 * 
 */
@Schema(description = "Pet information response")
public record PetResponseDTO(
    
    @Schema(description = "Unique pet identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    String id,
    
    @Schema(description = "Pet's name", example = "Rex")
    String nome,
    
    @Schema(description = "Pet's species", example = "CACHORRO")
    Especie especie,
    
    @Schema(description = "Pet's size", example = "MEDIO")
    Porte porte,
    
    @Schema(description = "Pet's gender", example = "MACHO")
    Genero genero,
    
    @Schema(description = "Pet's age in years", example = "3")
    Integer idade,
    
    @Schema(description = "ID of the responsible user", example = "123e4567-e89b-12d3-a456-426614174001")
    String responsavelId,
    
    @Schema(description = "Whether the pet is available for adoption", example = "true")
    Boolean disponivel,
    
    @Schema(description = "Optional description of the pet", example = "Cão muito dócil e carinhoso")
    String descricao,
    
    @Schema(description = "When the pet was registered")
    LocalDateTime createdAt,
    
    @Schema(description = "When the pet was last updated")
    LocalDateTime updatedAt
    
) {}