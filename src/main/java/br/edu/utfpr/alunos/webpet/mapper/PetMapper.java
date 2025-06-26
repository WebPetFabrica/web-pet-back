package br.edu.utfpr.alunos.webpet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.dto.pet.PetCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetUpdateRequestDTO;

/**
 * MapStruct mapper for Pet entity and DTOs conversion.
 * 
 * <p>Handles mapping between Pet domain entities and their corresponding DTOs
 * for API requests and responses. Uses Spring component model for dependency injection.
 * 
 */
@Mapper(componentModel = "spring")
public interface PetMapper {
    
    /**
     * Converts a Pet entity to a response DTO.
     * Note: responsavelNome will be populated separately by the service layer.
     * 
     * @param pet the pet entity
     * @return the response DTO
     */
    @Mapping(target = "responsavelNome", ignore = true)
    PetResponseDTO toResponseDTO(Pet pet);
    
    /**
     * Creates a Pet entity from a create request DTO.
     * 
     * @param createDTO the create request DTO
     * @param responsavelId the responsible user ID
     * @return the new pet entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "responsavelId", source = "responsavelId")
    @Mapping(target = "disponivel", constant = "true")
    Pet toEntity(PetCreateRequestDTO createDTO, String responsavelId);
    
    /**
     * Updates an existing Pet entity with data from an update request DTO.
     * Only non-null fields from the DTO will be mapped to the entity.
     * 
     * @param updateDTO the update request DTO
     * @param pet the existing pet entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "especie", ignore = true)
    @Mapping(target = "porte", ignore = true)
    @Mapping(target = "genero", ignore = true)
    @Mapping(target = "responsavelId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(PetUpdateRequestDTO updateDTO, @MappingTarget Pet pet);
}