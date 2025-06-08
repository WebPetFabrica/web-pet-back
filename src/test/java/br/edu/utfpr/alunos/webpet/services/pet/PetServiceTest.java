package br.edu.utfpr.alunos.webpet.services.pet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.domain.pet.StatusAdocao;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import br.edu.utfpr.alunos.webpet.dto.pet.PetCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetFilterDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetUpdateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.user.UserBasicResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.mapper.PetMapper;
import br.edu.utfpr.alunos.webpet.repositories.BaseUserRepository;
import br.edu.utfpr.alunos.webpet.repositories.PetRepository;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private BaseUserRepository baseUserRepository;
    @Mock
    private PetMapper petMapper;

    private PetServiceImpl petService;

    private ONG responsavel;
    private Pet pet;
    private PetResponseDTO petResponseDTO;

    @BeforeEach
    void setUp() {
        petService = new PetServiceImpl(petRepository, baseUserRepository, petMapper);
        
        responsavel = ONG.builder()
            .cnpj("12.345.678/0001-90")
            .nomeOng("ONG Test")
            .email("ong@test.com")
            .password("password")
            .celular("11999999999")
            .build();
        responsavel.setActive(true);
        
        pet = Pet.builder()
            .nome("Rex")
            .especie(Especie.CACHORRO)
            .raca("Labrador")
            .genero(Genero.MACHO)
            .porte(Porte.GRANDE)
            .dataNascimento(LocalDate.of(2020, 1, 15))
            .descricao("Cão muito carinhoso")
            .responsavel(responsavel)
            .build();
        
        petResponseDTO = new PetResponseDTO(
            "pet-id",
            "Rex",
            Especie.CACHORRO,
            "Labrador",
            Genero.MACHO,
            Porte.GRANDE,
            LocalDate.of(2020, 1, 15),
            4, // idade em anos
            "Cão muito carinhoso",
            null,
            StatusAdocao.DISPONIVEL,
            new UserBasicResponseDTO("ong-id", "ONG Test", UserType.ONG),
            null,
            null
        );
    }

    @Test
    void shouldCreatePetSuccessfully() {
        // Given
        PetCreateRequestDTO createRequest = new PetCreateRequestDTO(
            "Rex",
            Especie.CACHORRO,
            "Labrador",
            Genero.MACHO,
            Porte.GRANDE,
            LocalDate.of(2020, 1, 15),
            "Cão muito carinhoso",
            null
        );
        
        when(baseUserRepository.findById("ong-id")).thenReturn(Optional.of(responsavel));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);
        when(petMapper.toResponseDTO(pet)).thenReturn(petResponseDTO);

        // When
        PetResponseDTO result = petService.createPet(createRequest, "ong-id");

        // Then
        assertThat(result).isEqualTo(petResponseDTO);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void shouldThrowExceptionWhenResponsavelNotFound() {
        // Given
        PetCreateRequestDTO createRequest = new PetCreateRequestDTO(
            "Rex", Especie.CACHORRO, "Labrador", Genero.MACHO, Porte.GRANDE,
            LocalDate.of(2020, 1, 15), "Cão muito carinhoso", null
        );
        
        when(baseUserRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> petService.createPet(createRequest, "invalid-id"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void shouldThrowExceptionWhenResponsavelIsInactive() {
        // Given
        PetCreateRequestDTO createRequest = new PetCreateRequestDTO(
            "Rex", Especie.CACHORRO, "Labrador", Genero.MACHO, Porte.GRANDE,
            LocalDate.of(2020, 1, 15), "Cão muito carinhoso", null
        );
        
        responsavel.setActive(false);
        when(baseUserRepository.findById("ong-id")).thenReturn(Optional.of(responsavel));

        // When & Then
        assertThatThrownBy(() -> petService.createPet(createRequest, "ong-id"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_INACTIVE);
    }

    @Test
    void shouldUpdatePetSuccessfully() {
        // Given
        PetUpdateRequestDTO updateRequest = new PetUpdateRequestDTO(
            "Rex Updated", null, "Golden Retriever", null, null, null, "Descrição atualizada", null
        );
        
        when(petRepository.findByIdAndAtivo("pet-id")).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);
        when(petMapper.toResponseDTO(pet)).thenReturn(petResponseDTO);

        // When
        PetResponseDTO result = petService.updatePet("pet-id", updateRequest, "ong-id");

        // Then
        assertThat(result).isEqualTo(petResponseDTO);
        verify(petRepository).save(pet);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingPetWithWrongResponsavel() {
        // Given
        PetUpdateRequestDTO updateRequest = new PetUpdateRequestDTO(
            "Rex Updated", null, null, null, null, null, null, null
        );
        
        when(petRepository.findByIdAndAtivo("pet-id")).thenReturn(Optional.of(pet));

        // When & Then
        assertThatThrownBy(() -> petService.updatePet("pet-id", updateRequest, "wrong-id"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    void shouldDeletePetSuccessfully() {
        // Given
        when(petRepository.findByIdAndAtivo("pet-id")).thenReturn(Optional.of(pet));

        // When
        petService.deletePet("pet-id", "ong-id");

        // Then
        assertThat(pet.isAtivo()).isFalse();
        verify(petRepository).save(pet);
    }

    @Test
    void shouldFindPetById() {
        // Given
        when(petRepository.findByIdAndAtivo("pet-id")).thenReturn(Optional.of(pet));
        when(petMapper.toResponseDTO(pet)).thenReturn(petResponseDTO);

        // When
        Optional<PetResponseDTO> result = petService.findById("pet-id");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(petResponseDTO);
    }

    @Test
    void shouldReturnEmptyWhenPetNotFound() {
        // Given
        when(petRepository.findByIdAndAtivo("invalid-id")).thenReturn(Optional.empty());

        // When
        Optional<PetResponseDTO> result = petService.findById("invalid-id");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllAvailablePets() {
        // Given
        Page<Pet> petsPage = new PageImpl<>(List.of(pet));
        when(petRepository.findAllAvailablePets(any(Pageable.class))).thenReturn(petsPage);
        when(petMapper.toResponseDTO(pet)).thenReturn(petResponseDTO);

        // When
        Page<PetResponseDTO> result = petService.findAllAvailablePets(Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(petResponseDTO);
    }

    @Test
    void shouldFindPetsWithFilters() {
        // Given
        PetFilterDTO filters = new PetFilterDTO(
            Especie.CACHORRO, Genero.MACHO, Porte.GRANDE, 2, 6, "Labrador"
        );
        Page<Pet> petsPage = new PageImpl<>(List.of(pet));
        
        when(petRepository.findAvailablePetsWithFilters(
            filters.especie(), filters.genero(), filters.porte(),
            filters.idadeMinima(), filters.idadeMaxima(), filters.raca(),
            any(Pageable.class)
        )).thenReturn(petsPage);
        when(petMapper.toResponseDTO(pet)).thenReturn(petResponseDTO);

        // When
        Page<PetResponseDTO> result = petService.findPetsWithFilters(filters, Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(petResponseDTO);
    }

    @Test
    void shouldMarkPetAsAdopted() {
        // Given
        when(petRepository.findByIdAndAtivo("pet-id")).thenReturn(Optional.of(pet));

        // When
        petService.marcarComoAdotado("pet-id", "ong-id");

        // Then
        assertThat(pet.getStatusAdocao()).isEqualTo(StatusAdocao.ADOTADO);
        verify(petRepository).save(pet);
    }

    @Test
    void shouldMarkPetAsAvailable() {
        // Given
        pet.marcarComoAdotado(); // Set it as adopted first
        when(petRepository.findByIdAndAtivo("pet-id")).thenReturn(Optional.of(pet));

        // When
        petService.marcarComoDisponivel("pet-id", "ong-id");

        // Then
        assertThat(pet.getStatusAdocao()).isEqualTo(StatusAdocao.DISPONIVEL);
        verify(petRepository).save(pet);
    }

    @Test
    void shouldMarkPetAsUnavailable() {
        // Given
        when(petRepository.findByIdAndAtivo("pet-id")).thenReturn(Optional.of(pet));

        // When
        petService.marcarComoIndisponivel("pet-id", "ong-id");

        // Then
        assertThat(pet.getStatusAdocao()).isEqualTo(StatusAdocao.INDISPONIVEL);
        verify(petRepository).save(pet);
    }

    @Test
    void shouldGetAvailableEspecies() {
        // When
        List<Especie> especies = petService.getAvailableEspecies();

        // Then
        assertThat(especies).containsExactly(Especie.values());
    }

    @Test
    void shouldGetAvailablePortes() {
        // When
        List<Porte> portes = petService.getAvailablePortes();

        // Then
        assertThat(portes).containsExactly(Porte.values());
    }

    @Test
    void shouldGetAvailableGeneros() {
        // When
        List<Genero> generos = petService.getAvailableGeneros();

        // Then
        assertThat(generos).containsExactly(Genero.values());
    }

    @Test
    void shouldGetAvailableRacas() {
        // Given
        List<String> racas = List.of("Labrador", "Golden Retriever", "Poodle");
        when(petRepository.findDistinctRacas()).thenReturn(racas);

        // When
        List<String> result = petService.getAvailableRacas();

        // Then
        assertThat(result).isEqualTo(racas);
    }

    @Test
    void shouldSearchPets() {
        // Given
        String searchTerm = "Rex";
        Page<Pet> petsPage = new PageImpl<>(List.of(pet));
        when(petRepository.searchPets(searchTerm, any(Pageable.class))).thenReturn(petsPage);
        when(petMapper.toResponseDTO(pet)).thenReturn(petResponseDTO);

        // When
        Page<PetResponseDTO> result = petService.searchPets(searchTerm, Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(petResponseDTO);
    }

    @Test
    void shouldCountAvailablePetsWithFilters() {
        // Given
        PetFilterDTO filters = new PetFilterDTO(
            Especie.CACHORRO, null, null, null, null, null
        );
        when(petRepository.countAvailablePetsWithFilters(
            filters.especie(), filters.genero(), filters.porte(),
            filters.idadeMinima(), filters.idadeMaxima(), filters.raca()
        )).thenReturn(5L);

        // When
        Long count = petService.countAvailablePetsWithFilters(filters);

        // Then
        assertThat(count).isEqualTo(5L);
    }
}