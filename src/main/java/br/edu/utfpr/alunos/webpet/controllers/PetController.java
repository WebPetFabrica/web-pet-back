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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Endpoints para gerenciamento de pets")
public class PetController {
    
    private final PetService petService;
    
    @PostMapping
    @Operation(summary = "Criar novo pet", description = "Cria um novo pet no sistema")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pet criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<PetResponseDTO> createPet(
            @Valid @RequestBody PetCreateRequestDTO createRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        PetResponseDTO response = petService.createPet(createRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID", description = "Busca um pet específico pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pet encontrado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    public ResponseEntity<PetResponseDTO> getPetById(@PathVariable String id) {
        return petService.findById(id)
                .map(pet -> ResponseEntity.ok(pet))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Listar pets", description = "Lista pets disponíveis com filtros opcionais")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pets retornada com sucesso")
    })
    public ResponseEntity<Page<PetResponseDTO>> listPets(
            @Parameter(description = "Filtrar por espécie") @RequestParam(required = false) Especie especie,
            @Parameter(description = "Filtrar por gênero") @RequestParam(required = false) Genero genero,
            @Parameter(description = "Filtrar por porte") @RequestParam(required = false) Porte porte,
            @Parameter(description = "Idade mínima em anos") @RequestParam(required = false) Integer idadeMinima,
            @Parameter(description = "Idade máxima em anos") @RequestParam(required = false) Integer idadeMaxima,
            @Parameter(description = "Filtrar por raça") @RequestParam(required = false) String raca,
            @Parameter(description = "Termo de busca geral") @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<PetResponseDTO> pets;
        
        if (search != null && !search.trim().isEmpty()) {
            pets = petService.searchPets(search.trim(), pageable);
        } else if (hasFilters(especie, genero, porte, idadeMinima, idadeMaxima, raca)) {
            PetFilterDTO filters = new PetFilterDTO(especie, genero, porte, idadeMinima, idadeMaxima, raca);
            pets = petService.findPetsWithFilters(filters, pageable);
        } else {
            pets = petService.findAllAvailablePets(pageable);
        }
        
        return ResponseEntity.ok(pets);
    }
    
    @GetMapping("/my-pets")
    @Operation(summary = "Listar meus pets", description = "Lista pets do usuário autenticado")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pets do usuário"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Page<PetResponseDTO>> getMyPets(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        String userId = getUserId(userDetails);
        Page<PetResponseDTO> pets = petService.findPetsByResponsavel(userId, pageable);
        return ResponseEntity.ok(pets);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pet", description = "Atualiza informações de um pet")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pet atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    public ResponseEntity<PetResponseDTO> updatePet(
            @PathVariable String id,
            @Valid @RequestBody PetUpdateRequestDTO updateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        PetResponseDTO response = petService.updatePet(id, updateRequest, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pet", description = "Remove um pet do sistema (soft delete)")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pet deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    public ResponseEntity<Void> deletePet(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        petService.deletePet(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/status/adotado")
    @Operation(summary = "Marcar como adotado", description = "Marca um pet como adotado")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    public ResponseEntity<Void> markAsAdopted(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        petService.marcarComoAdotado(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/status/disponivel")
    @Operation(summary = "Marcar como disponível", description = "Marca um pet como disponível para adoção")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    public ResponseEntity<Void> markAsAvailable(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        petService.marcarComoDisponivel(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/status/indisponivel")
    @Operation(summary = "Marcar como indisponível", description = "Marca um pet como indisponível")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    public ResponseEntity<Void> markAsUnavailable(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        petService.marcarComoIndisponivel(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/metadata/especies")
    @Operation(summary = "Listar espécies", description = "Lista todas as espécies disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de espécies")
    })
    public ResponseEntity<List<Especie>> getEspecies() {
        return ResponseEntity.ok(petService.getAvailableEspecies());
    }
    
    @GetMapping("/metadata/portes")
    @Operation(summary = "Listar portes", description = "Lista todos os portes disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de portes")
    })
    public ResponseEntity<List<Porte>> getPortes() {
        return ResponseEntity.ok(petService.getAvailablePortes());
    }
    
    @GetMapping("/metadata/generos")
    @Operation(summary = "Listar gêneros", description = "Lista todos os gêneros disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de gêneros")
    })
    public ResponseEntity<List<Genero>> getGeneros() {
        return ResponseEntity.ok(petService.getAvailableGeneros());
    }
    
    @GetMapping("/metadata/racas")
    @Operation(summary = "Listar raças", description = "Lista todas as raças cadastradas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de raças")
    })
    public ResponseEntity<List<String>> getRacas() {
        return ResponseEntity.ok(petService.getAvailableRacas());
    }
    
    private String getUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        return userDetails.getUsername(); // Assuming username is the user ID
    }
    
    private boolean hasFilters(Especie especie, Genero genero, Porte porte, 
                              Integer idadeMinima, Integer idadeMaxima, String raca) {
        return especie != null || genero != null || porte != null || 
               idadeMinima != null || idadeMaxima != null || 
               (raca != null && !raca.trim().isEmpty());
    }
}