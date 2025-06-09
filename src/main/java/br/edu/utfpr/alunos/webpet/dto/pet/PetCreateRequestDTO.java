package br.edu.utfpr.alunos.webpet.dto.pet;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new pet in the system.
 * 
 * <p>This record contains all the required information to register a new pet
 * for adoption. Only ONGs and PROTETORs can create pets.
 * 
 * @param nome pet's name (2-255 characters)
 * @param especie pet's species (CACHORRO or GATO)
 * @param porte pet's size (PEQUENO, MEDIO, or GRANDE)
 * @param genero pet's gender (MACHO or FEMEA)
 * @param idade pet's age in years (0-30)
 * @param descricao optional description (max 2000 characters)
 * 
 */
@Schema(description = "Request payload for creating a new pet")
public record PetCreateRequestDTO(
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    @Schema(description = "Pet's name", example = "Rex", minLength = 2, maxLength = 255)
    String nome,
    
    @NotNull(message = "Espécie é obrigatória")
    @Schema(description = "Pet's species", example = "CACHORRO")
    Especie especie,
    
    @NotNull(message = "Porte é obrigatório")
    @Schema(description = "Pet's size", example = "MEDIO")
    Porte porte,
    
    @NotNull(message = "Gênero é obrigatório")
    @Schema(description = "Pet's gender", example = "MACHO")
    Genero genero,
    
    @NotNull(message = "Idade é obrigatória")
    @Min(value = 0, message = "Idade deve ser no mínimo 0 anos")
    @Max(value = 30, message = "Idade deve ser no máximo 30 anos")
    @Schema(description = "Pet's age in years", example = "3", minimum = "0", maximum = "30")
    Integer idade,
    
    @Size(max = 2000, message = "Descrição não pode exceder 2000 caracteres")
    @Schema(description = "Optional description of the pet", example = "Cão muito dócil e carinhoso, ideal para famílias", maxLength = 2000)
    String descricao
    
) {}