package br.edu.utfpr.alunos.webpet.services.pet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.dto.pet.PetCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetFilterDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetUpdateRequestDTO;

/**
 * Service interface for Pet management operations.
 * 
 * <p>Provides business logic for pet CRUD operations, filtering, searching,
 * and status management. Handles validation and business rules enforcement.
 * 
 */
public interface PetService {
    
    /**
     * Creates a new pet for the specified responsible user.
     * 
     * @param createRequest the pet creation data
     * @param responsavelId the ID of the responsible user (ONG or PROTETOR)
     * @return the created pet response DTO
     */
    PetResponseDTO createPet(PetCreateRequestDTO createRequest, String responsavelId);
    
    /**
     * Updates an existing pet.
     * 
     * @param petId the pet ID
     * @param updateRequest the update data
     * @param responsavelId the ID of the responsible user
     * @return the updated pet response DTO
     */
    PetResponseDTO updatePet(String petId, PetUpdateRequestDTO updateRequest, String responsavelId);
    
    /**
     * Deletes a pet (marks as deleted).
     * 
     * @param petId the pet ID
     * @param responsavelId the ID of the responsible user
     */
    void deletePet(String petId, String responsavelId);
    
    /**
     * Finds a pet by ID.
     * 
     * @param petId the pet ID
     * @return the pet response DTO if found
     */
    Optional<PetResponseDTO> findById(String petId);
    
    /**
     * Finds all available pets with pagination.
     * 
     * @param pageable pagination information
     * @return page of available pets
     */
    Page<PetResponseDTO> findAllAvailablePets(Pageable pageable);
    
    /**
     * Finds pets managed by a specific responsible user.
     * 
     * @param responsavelId the responsible user ID
     * @param pageable pagination information
     * @return page of pets managed by the user
     */
    Page<PetResponseDTO> findPetsByResponsavel(String responsavelId, Pageable pageable);
    
    /**
     * Finds pets with multiple filters.
     * 
     * @param filters the search filters
     * @param pageable pagination information
     * @return page of filtered pets
     */
    Page<PetResponseDTO> findPetsWithFilters(PetFilterDTO filters, Pageable pageable);
    
    /**
     * Counts available pets matching the filters.
     * 
     * @param filters the search filters
     * @return count of matching pets
     */
    Long countAvailablePetsWithFilters(PetFilterDTO filters);
    
    /**
     * Searches pets by name or description.
     * 
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching pets
     */
    Page<PetResponseDTO> searchPets(String searchTerm, Pageable pageable);
    
    /**
     * Marks a pet as adopted.
     * 
     * @param petId the pet ID
     * @param responsavelId the responsible user ID
     */
    void marcarComoAdotado(String petId, String responsavelId);
    
    /**
     * Marks a pet as available for adoption.
     * 
     * @param petId the pet ID
     * @param responsavelId the responsible user ID
     */
    void marcarComoDisponivel(String petId, String responsavelId);
    
    /**
     * Marks a pet as temporarily unavailable.
     * 
     * @param petId the pet ID
     * @param responsavelId the responsible user ID
     */
    void marcarComoIndisponivel(String petId, String responsavelId);
    
    /**
     * Gets all available species.
     * 
     * @return list of species
     */
    List<Especie> getAvailableEspecies();
    
    /**
     * Gets all available sizes.
     * 
     * @return list of sizes
     */
    List<Porte> getAvailablePortes();
    
    /**
     * Gets all available genders.
     * 
     * @return list of genders
     */
    List<Genero> getAvailableGeneros();
}