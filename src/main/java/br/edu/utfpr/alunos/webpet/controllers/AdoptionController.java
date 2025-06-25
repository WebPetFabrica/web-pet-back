package br.edu.utfpr.alunos.webpet.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.utfpr.alunos.webpet.domain.adoption.AdoptionRequest;
import br.edu.utfpr.alunos.webpet.dto.adoption.AdoptionRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.openapi.ErrorExamples;
import br.edu.utfpr.alunos.webpet.repositories.AdoptionRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@RestController
@RequestMapping("/adocoes")
@RequiredArgsConstructor
@Tag(name = "Adoção", description = "Endpoints para gerenciamento de solicitações de adoção")
@SecurityRequirement(name = "bearerAuth")
public class AdoptionController {
    
    private final AdoptionRequestRepository adoptionRequestRepository;
    
    @PostMapping
    @Operation(
        summary = "Criar solicitação de adoção",
        description = "Cria uma nova solicitação de adoção para um animal específico"
    )
    @ApiResponse(
        responseCode = "201",
        description = "Solicitação de adoção criada com sucesso",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = AdoptionRequest.class),
            examples = @ExampleObject(
                name = "Solicitação criada",
                value = """
                {
                  "id": "uuid-123",
                  "animalId": "uuid-456",
                  "userId": "uuid-789",
                  "status": "PENDING",
                  "requestDate": "2025-06-04T10:30:00Z"
                }
                """
            )
        )
    )
    @ErrorExamples.BadRequest
    @ErrorExamples.Unauthorized
    @ErrorExamples.InternalServerError
    public ResponseEntity<AdoptionRequest> createAdoptionRequest(@Valid @RequestBody AdoptionRequestDTO requestDTO) {
        String correlationId = MDC.get("correlationId");
        
        log.info("Creating adoption request for animal: {} by user: {} [correlationId: {}]", 
            requestDTO.animalId(), requestDTO.userId(), correlationId);
        
        AdoptionRequest adoptionRequest = new AdoptionRequest(requestDTO.animalId(), requestDTO.userId());
        AdoptionRequest savedRequest = adoptionRequestRepository.save(adoptionRequest);
        
        log.info("Adoption request created successfully with ID: {} [correlationId: {}]", 
            savedRequest.getId(), correlationId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
    }
}