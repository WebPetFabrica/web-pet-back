package br.edu.utfpr.alunos.webpet.services.pet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.dto.pet.PetCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetFilterDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetUpdateRequestDTO;

public interface PetService {
    
    PetResponseDTO createPet(PetCreateRequestDTO createRequest, String responsavelId);
    
    PetResponseDTO updatePet(String petId, PetUpdateRequestDTO updateRequest, String responsavelId);
    
    void deletePet(String petId, String responsavelId);
    
    Optional<PetResponseDTO> findById(String petId);
    
    Page<PetResponseDTO> findAllAvailablePets(Pageable pageable);
    
    Page<PetResponseDTO> findPetsByResponsavel(String responsavelId, Pageable pageable);
    
    Page<PetResponseDTO> findPetsWithFilters(PetFilterDTO filters, Pageable pageable);
    
    Long countAvailablePetsWithFilters(PetFilterDTO filters);
    
    Page<PetResponseDTO> searchPets(String searchTerm, Pageable pageable);
    
    List<String> getAvailableRacas();
    
    void marcarComoAdotado(String petId, String responsavelId);
    
    void marcarComoDisponivel(String petId, String responsavelId);
    
    void marcarComoIndisponivel(String petId, String responsavelId);
    
    List<Especie> getAvailableEspecies();
    
    List<Porte> getAvailablePortes();
    
    List<Genero> getAvailableGeneros();
}