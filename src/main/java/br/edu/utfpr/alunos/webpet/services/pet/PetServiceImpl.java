package br.edu.utfpr.alunos.webpet.services.pet;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.dto.pet.PetCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetFilterDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetUpdateRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.mapper.PetMapper;
import br.edu.utfpr.alunos.webpet.repositories.PetRepository;

/**
 * Implementation of PetService interface.
 * 
 * <p>Provides complete pet management functionality including CRUD operations,
 * filtering, searching, and status management. Implements business rules
 * and validation for pet operations.
 * 
 */
@Service
@Transactional
public class PetServiceImpl implements PetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PetServiceImpl.class);
    
    private final PetRepository petRepository;
    private final PetMapper petMapper;
    
    public PetServiceImpl(PetRepository petRepository, PetMapper petMapper) {
        this.petRepository = petRepository;
        this.petMapper = petMapper;
    }
    
    @Override
    public PetResponseDTO createPet(PetCreateRequestDTO createRequest, String responsavelId) {
        try {
            MDC.put("operation", "createPet");
            MDC.put("responsavelId", responsavelId);
            
            logger.info("Creating pet for responsavel: {}", responsavelId);
            
            // Validate if pet name already exists for this responsible user
            if (petRepository.existsByNomeAndResponsavelId(createRequest.nome(), responsavelId)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, 
                    "Pet com este nome já existe para este responsável");
            }
            
            // Create pet entity using mapper
            Pet pet = petMapper.toEntity(createRequest, responsavelId);
            
            // Save pet
            Pet savedPet = petRepository.save(pet);
            
            logger.info("Pet created successfully with ID: {}", savedPet.getId());
            
            return petMapper.toResponseDTO(savedPet);
            
        } finally {
            MDC.clear();
        }
    }
    
    @Override
    public PetResponseDTO updatePet(String petId, PetUpdateRequestDTO updateRequest, String responsavelId) {
        try {
            MDC.put("operation", "updatePet");
            MDC.put("petId", petId);
            MDC.put("responsavelId", responsavelId);
            
            logger.info("Updating pet: {} for responsavel: {}", petId, responsavelId);
            
            Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Pet não encontrado"));
            
            // Verify ownership
            if (!pet.getResponsavelId().equals(responsavelId)) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, 
                    "Usuário não tem permissão para atualizar este pet");
            }
            
            // Update fields using mapper
            petMapper.updateEntityFromDTO(updateRequest, pet);
            
            Pet savedPet = petRepository.save(pet);
            
            logger.info("Pet updated successfully: {}", petId);
            
            return petMapper.toResponseDTO(savedPet);
            
        } finally {
            MDC.clear();
        }
    }
    
    @Override
    public void deletePet(String petId, String responsavelId) {
        try {
            MDC.put("operation", "deletePet");
            MDC.put("petId", petId);
            MDC.put("responsavelId", responsavelId);
            
            logger.info("Deleting pet: {} for responsavel: {}", petId, responsavelId);
            
            Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Pet não encontrado"));
            
            // Verify ownership
            if (!pet.getResponsavelId().equals(responsavelId)) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, 
                    "Usuário não tem permissão para excluir este pet");
            }
            
            petRepository.delete(pet);
            
            logger.info("Pet deleted successfully: {}", petId);
            
        } finally {
            MDC.clear();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PetResponseDTO> findById(String petId) {
        logger.info("Finding pet by ID: {}", petId);
        
        return petRepository.findById(petId)
            .map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findAllAvailablePets(Pageable pageable) {
        logger.info("Finding all available pets with pagination");
        
        return petRepository.findAllAvailablePets(pageable)
            .map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findPetsByResponsavel(String responsavelId, Pageable pageable) {
        logger.info("Finding pets for responsavel: {}", responsavelId);
        
        return petRepository.findByResponsavelId(responsavelId, pageable)
            .map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findPetsWithFilters(PetFilterDTO filters, Pageable pageable) {
        logger.info("Finding pets with filters: {}", filters);
        
        return petRepository.findAvailablePetsWithFilters(
            filters.especie(),
            filters.porte(),
            filters.genero(),
            filters.idadeMinima(),
            filters.idadeMaxima(),
            pageable
        ).map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countAvailablePetsWithFilters(PetFilterDTO filters) {
        logger.info("Counting pets with filters: {}", filters);
        
        return petRepository.countAvailablePetsWithFilters(
            filters.especie(),
            filters.porte(),
            filters.genero(),
            filters.idadeMinima(),
            filters.idadeMaxima()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> searchPets(String searchTerm, Pageable pageable) {
        logger.info("Searching pets with term: {}", searchTerm);
        
        return petRepository.searchAvailablePets(searchTerm, pageable)
            .map(petMapper::toResponseDTO);
    }
    
    @Override
    public void marcarComoAdotado(String petId, String responsavelId) {
        updatePetStatus(petId, responsavelId, false, "adotado");
    }
    
    @Override
    public void marcarComoDisponivel(String petId, String responsavelId) {
        updatePetStatus(petId, responsavelId, true, "disponível");
    }
    
    @Override
    public void marcarComoIndisponivel(String petId, String responsavelId) {
        updatePetStatus(petId, responsavelId, false, "indisponível");
    }
    
    private void updatePetStatus(String petId, String responsavelId, boolean disponivel, String status) {
        try {
            MDC.put("operation", "updatePetStatus");
            MDC.put("petId", petId);
            MDC.put("responsavelId", responsavelId);
            MDC.put("status", status);
            
            logger.info("Updating pet status to '{}' for pet: {}", status, petId);
            
            Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Pet não encontrado"));
            
            // Verify ownership
            if (!pet.getResponsavelId().equals(responsavelId)) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, 
                    "Usuário não tem permissão para alterar o status deste pet");
            }
            
            pet.setDisponivel(disponivel);
            petRepository.save(pet);
            
            logger.info("Pet status updated successfully to '{}' for pet: {}", status, petId);
            
        } finally {
            MDC.clear();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Especie> getAvailableEspecies() {
        return Arrays.asList(Especie.values());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Porte> getAvailablePortes() {
        return Arrays.asList(Porte.values());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Genero> getAvailableGeneros() {
        return Arrays.asList(Genero.values());
    }
}