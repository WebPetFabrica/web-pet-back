package br.edu.utfpr.alunos.webpet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.edu.utfpr.alunos.webpet.domain.donation.Donation;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;

/**
 * MapStruct mapper for Donation entity and DTOs conversion.
 * 
 * <p>Handles mapping between Donation domain entities and their corresponding DTOs
 * for API requests and responses. Uses Spring component model for dependency injection.
 * 
 */
@Mapper(componentModel = "spring")
public interface DonationMapper {
    
    /**
     * Converts a Donation entity to a response DTO.
     * 
     * @param donation the donation entity
     * @return the response DTO
     */
    DonationResponseDTO toResponseDTO(Donation donation);
    
    /**
     * Creates a Donation entity from a create request DTO.
     * 
     * @param createDTO the create request DTO
     * @param doadorId the ID of the donor user
     * @return the new donation entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataDoacao", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "doadorId", source = "doadorId")
    Donation toEntity(DonationCreateRequestDTO createDTO, String doadorId);
}