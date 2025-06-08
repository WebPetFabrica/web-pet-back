package br.edu.utfpr.alunos.webpet.services.pet;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.dto.pet.PetCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetFilterDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetUpdateRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.mapper.PetMapper;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PetServiceImpl implements PetService {
    
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final PetMapper petMapper;
    
    @Override
    public PetResponseDTO createPet(PetCreateRequestDTO createRequest, String responsavelId) {
        log.info("Creating pet for responsavel: {}", responsavelId);
        
        BaseUser responsavel = findUserById(responsavelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        if (!responsavel.isActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }
        
        Pet pet = Pet.builder()
                .nome(createRequest.nome())
                .especie(createRequest.especie())
                .raca(createRequest.raca())
                .genero(createRequest.genero())
                .porte(createRequest.porte())
                .dataNascimento(createRequest.dataNascimento())
                .descricao(createRequest.descricao())
                .fotoUrl(createRequest.fotoUrl())
                .responsavelId(responsavel.getId())
                .build();
        
        Pet savedPet = petRepository.save(pet);
        log.info("Pet created successfully with ID: {}", savedPet.getId());
        
        return petMapper.toResponseDTO(savedPet);
    }
    
    @Override
    public PetResponseDTO updatePet(String petId, PetUpdateRequestDTO updateRequest, String responsavelId) {
        log.info("Updating pet {} for responsavel: {}", petId, responsavelId);
        
        Pet pet = petRepository.findByIdAndAtivo(petId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
        
        if (!pet.getResponsavelId().equals(responsavelId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        if (updateRequest.nome() != null) {
            pet.setNome(updateRequest.nome());
        }
        if (updateRequest.especie() != null) {
            pet.setEspecie(updateRequest.especie());
        }
        if (updateRequest.raca() != null) {
            pet.setRaca(updateRequest.raca());
        }
        if (updateRequest.genero() != null) {
            pet.setGenero(updateRequest.genero());
        }
        if (updateRequest.porte() != null) {
            pet.setPorte(updateRequest.porte());
        }
        if (updateRequest.dataNascimento() != null) {
            pet.setDataNascimento(updateRequest.dataNascimento());
        }
        if (updateRequest.descricao() != null) {
            pet.setDescricao(updateRequest.descricao());
        }
        if (updateRequest.fotoUrl() != null) {
            pet.setFotoUrl(updateRequest.fotoUrl());
        }
        
        Pet updatedPet = petRepository.save(pet);
        log.info("Pet {} updated successfully", petId);
        
        return petMapper.toResponseDTO(updatedPet);
    }
    
    @Override
    public void deletePet(String petId, String responsavelId) {
        log.info("Deleting pet {} for responsavel: {}", petId, responsavelId);
        
        Pet pet = petRepository.findByIdAndAtivo(petId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
        
        if (!pet.getResponsavelId().equals(responsavelId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        pet.desativar();
        petRepository.save(pet);
        log.info("Pet {} deactivated successfully", petId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PetResponseDTO> findById(String petId) {
        return petRepository.findByIdAndAtivo(petId)
                .map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findAllAvailablePets(Pageable pageable) {
        return petRepository.findAllAvailablePets(pageable)
                .map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findPetsByResponsavel(String responsavelId, Pageable pageable) {
        return petRepository.findByResponsavelId(responsavelId, pageable)
                .map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findPetsWithFilters(PetFilterDTO filters, Pageable pageable) {
        return petRepository.findAvailablePetsWithFilters(
                filters.especie(),
                filters.genero(),
                filters.porte(),
                filters.idadeMinima(),
                filters.idadeMaxima(),
                filters.raca(),
                pageable
        ).map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countAvailablePetsWithFilters(PetFilterDTO filters) {
        return petRepository.countAvailablePetsWithFilters(
                filters.especie(),
                filters.genero(),
                filters.porte(),
                filters.idadeMinima(),
                filters.idadeMaxima(),
                filters.raca()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> searchPets(String searchTerm, Pageable pageable) {
        return petRepository.searchPets(searchTerm, pageable)
                .map(petMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableRacas() {
        return petRepository.findDistinctRacas();
    }
    
    @Override
    public void marcarComoAdotado(String petId, String responsavelId) {
        Pet pet = getPetWithValidation(petId, responsavelId);
        pet.marcarComoAdotado();
        petRepository.save(pet);
        log.info("Pet {} marked as adopted", petId);
    }
    
    @Override
    public void marcarComoDisponivel(String petId, String responsavelId) {
        Pet pet = getPetWithValidation(petId, responsavelId);
        pet.marcarComoDisponivel();
        petRepository.save(pet);
        log.info("Pet {} marked as available", petId);
    }
    
    @Override
    public void marcarComoIndisponivel(String petId, String responsavelId) {
        Pet pet = getPetWithValidation(petId, responsavelId);
        pet.marcarComoIndisponivel();
        petRepository.save(pet);
        log.info("Pet {} marked as unavailable", petId);
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
    
    private Pet getPetWithValidation(String petId, String responsavelId) {
        Pet pet = petRepository.findByIdAndAtivo(petId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
        
        if (!pet.getResponsavelId().equals(responsavelId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        return pet;
    }
    
    private Optional<BaseUser> findUserById(String id) {
        return userRepository.findById(id)
                .map(BaseUser.class::cast)
                .or(() -> ongRepository.findById(id)
                        .map(BaseUser.class::cast))
                .or(() -> protetorRepository.findById(id)
                        .map(BaseUser.class::cast));
    }
}