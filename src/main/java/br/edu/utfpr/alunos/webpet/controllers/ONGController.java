package br.edu.utfpr.alunos.webpet.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.alunos.webpet.dto.user.ONGResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.user.ONGUpdateRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.services.ong.ONGService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ongs")
@RequiredArgsConstructor
@Tag(name = "ONGs", description = "Endpoints para gerenciamento de ONGs")
public class ONGController {
    
    private final ONGService ongService;
    
    @GetMapping
    @Operation(summary = "Listar ONGs", description = "Lista todas as ONGs ativas com paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de ONGs retornada com sucesso")
    })
    public ResponseEntity<Page<ONGResponseDTO>> listONGs(
            @Parameter(description = "Termo de busca") @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<ONGResponseDTO> ongs;
        
        if (search != null && !search.trim().isEmpty()) {
            ongs = ongService.searchONGs(search.trim(), pageable);
        } else {
            ongs = ongService.findAllActiveONGs(pageable);
        }
        
        return ResponseEntity.ok(ongs);
    }
    
    @GetMapping("/all")
    @Operation(summary = "Listar todas as ONGs", description = "Lista todas as ONGs ativas sem paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista completa de ONGs")
    })
    public ResponseEntity<List<ONGResponseDTO>> listAllONGs() {
        List<ONGResponseDTO> ongs = ongService.findAllActiveONGs();
        return ResponseEntity.ok(ongs);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar ONG por ID", description = "Busca uma ONG específica pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ONG encontrada"),
        @ApiResponse(responseCode = "404", description = "ONG não encontrada")
    })
    public ResponseEntity<ONGResponseDTO> getONGById(@PathVariable String id) {
        return ongService.findById(id)
                .map(ong -> ResponseEntity.ok(ong))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cnpj/{cnpj}")
    @Operation(summary = "Buscar ONG por CNPJ", description = "Busca uma ONG pelo seu CNPJ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ONG encontrada"),
        @ApiResponse(responseCode = "404", description = "ONG não encontrada")
    })
    public ResponseEntity<ONGResponseDTO> getONGByCnpj(@PathVariable String cnpj) {
        return ongService.findByCnpj(cnpj)
                .map(ong -> ResponseEntity.ok(ong))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ONG", description = "Atualiza informações de uma ONG")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ONG atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "ONG não encontrada")
    })
    public ResponseEntity<ONGResponseDTO> updateONG(
            @PathVariable String id,
            @Valid @RequestBody ONGUpdateRequestDTO updateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        ONGResponseDTO response = ongService.updateONG(id, updateRequest, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar ONG", description = "Desativa uma ONG (soft delete)")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "ONG desativada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "ONG não encontrada")
    })
    public ResponseEntity<Void> deactivateONG(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        ongService.deactivateONG(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar ONG", description = "Reativa uma ONG desativada")
    @SecurityRequirement(name = "bearer-token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ONG ativada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado"),
        @ApiResponse(responseCode = "404", description = "ONG não encontrada")
    })
    public ResponseEntity<Void> activateONG(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        ongService.activateONG(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/check-cnpj/{cnpj}")
    @Operation(summary = "Verificar CNPJ", description = "Verifica se um CNPJ já está cadastrado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificação realizada"),
        @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado")
    })
    public ResponseEntity<Void> checkCnpjAvailability(@PathVariable String cnpj) {
        if (ongService.existsByCnpj(cnpj)) {
            return ResponseEntity.status(409).build(); // Conflict
        }
        return ResponseEntity.ok().build();
    }
    
    private String getUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        return userDetails.getUsername();
    }
}