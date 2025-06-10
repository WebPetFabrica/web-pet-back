package br.edu.utfpr.alunos.webpet.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.dto.pet.PetCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetFilterDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.pet.PetUpdateRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.services.pet.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for Pet management operations.
 * 
 * <p>Provides comprehensive pet CRUD operations, filtering, searching,
 * and status management endpoints. Supports pagination and filtering
 * for efficient data retrieval.
 * 
 */
@RestController
@RequestMapping("/pets")
@Tag(name = "Pets", description = "Endpoints para gerenciamento de pets")
public class PetController {
    
    private final PetService petService;
    
    public PetController(PetService petService) {
        this.petService = petService;
    }
    
    @PostMapping
    @Operation(summary = "Cadastrar novo pet", description = "Cadastra um novo pet para adoção")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pet cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<PetResponseDTO> createPet(
            @Valid @RequestBody PetCreateRequestDTO createRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        PetResponseDTO response = petService.createPet(createRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Listar pets disponíveis", description = "Lista todos os pets disponíveis para adoção com filtros opcionais")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pets retornada com sucesso")
    })
    public ResponseEntity<Page<PetResponseDTO>> listAvailablePets(
            @Parameter(description = "Filtro por espécie") @RequestParam(required = false) Especie especie,
            @Parameter(description = "Filtro por porte") @RequestParam(required = false) Porte porte,
            @Parameter(description = "Filtro por gênero") @RequestParam(required = false) Genero genero,
            @Parameter(description = "Idade mínima") @RequestParam(required = false) Integer idadeMinima,
            @Parameter(description = "Idade máxima") @RequestParam(required = false) Integer idadeMaxima,
            @Parameter(description = "Filtro por responsável") @RequestParam(required = false) String responsavelId,
            @Parameter(description = "Termo de busca") @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<PetResponseDTO> pets;
        
        if (search != null && !search.trim().isEmpty()) {
            // Search by name/description
            pets = petService.searchPets(search.trim(), pageable);
        } else if (hasFilters(especie, porte, genero, idadeMinima, idadeMaxima, responsavelId)) {
            // Apply filters
            PetFilterDTO filters = new PetFilterDTO(especie, genero, porte, idadeMinima, idadeMaxima, responsavelId);
            pets = petService.findPetsWithFilters(filters, pageable);
        } else {
            // Get all available pets
            pets = petService.findAllAvailablePets(pageable);
        }
        
        return ResponseEntity.ok(pets);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID", description = "Retorna as informações de um pet específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pet encontrado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    public ResponseEntity<PetResponseDTO> getPetById(
            @Parameter(description = "ID do pet") @PathVariable String id) {
        
        return petService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pet", description = "Atualiza as informações de um pet")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pet atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<PetResponseDTO> updatePet(
            @Parameter(description = "ID do pet") @PathVariable String id,
            @Valid @RequestBody PetUpdateRequestDTO updateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        PetResponseDTO response = petService.updatePet(id, updateRequest, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir pet", description = "Remove um pet do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pet excluído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<Void> deletePet(
            @Parameter(description = "ID do pet") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        petService.deletePet(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/meus-pets")
    @Operation(summary = "Listar meus pets", description = "Lista todos os pets do usuário logado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pets retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<Page<PetResponseDTO>> getMyPets(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<PetResponseDTO> pets = petService.findPetsByResponsavel(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(pets);
    }
    
    @PatchMapping("/{id}/adotar")
    @Operation(summary = "Marcar pet como adotado", description = "Marca um pet como adotado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<Void> markAsAdopted(
            @Parameter(description = "ID do pet") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        petService.marcarComoAdotado(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/disponibilizar")
    @Operation(summary = "Marcar pet como disponível", description = "Marca um pet como disponível para adoção")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<Void> markAsAvailable(
            @Parameter(description = "ID do pet") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        petService.marcarComoDisponivel(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/indisponibilizar")
    @Operation(summary = "Marcar pet como indisponível", description = "Marca um pet como temporariamente indisponível")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<Void> markAsUnavailable(
            @Parameter(description = "ID do pet") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        petService.marcarComoIndisponivel(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/especies")
    @Operation(summary = "Listar espécies", description = "Lista todas as espécies disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de espécies retornada com sucesso")
    })
    public ResponseEntity<List<Especie>> getAvailableSpecies() {
        List<Especie> especies = petService.getAvailableEspecies();
        return ResponseEntity.ok(especies);
    }
    
    @GetMapping("/portes")
    @Operation(summary = "Listar portes", description = "Lista todos os portes disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de portes retornada com sucesso")
    })
    public ResponseEntity<List<Porte>> getAvailableSizes() {
        List<Porte> portes = petService.getAvailablePortes();
        return ResponseEntity.ok(portes);
    }
    
    @GetMapping("/generos")
    @Operation(summary = "Listar gêneros", description = "Lista todos os gêneros disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de gêneros retornada com sucesso")
    })
    public ResponseEntity<List<Genero>> getAvailableGenders() {
        List<Genero> generos = petService.getAvailableGeneros();
        return ResponseEntity.ok(generos);
    }
    
    private boolean hasFilters(Especie especie, Porte porte, Genero genero, 
                              Integer idadeMinima, Integer idadeMaxima, String responsavelId) {
        return especie != null || porte != null || genero != null || 
               idadeMinima != null || idadeMaxima != null || responsavelId != null;
    }
}