package br.edu.utfpr.alunos.webpet.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;
import br.edu.utfpr.alunos.webpet.services.donation.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for Donation management operations.
 * 
 * <p>Provides donation creation, querying, and analytics endpoints.
 * Supports pagination and filtering for efficient data retrieval.
 * 
 */
@RestController
@RequestMapping("/api/v1/doacoes")
@Tag(name = "Donations", description = "Endpoints para gerenciamento de doações")
public class DonationController {
    
    private final DonationService donationService;
    
    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }
    
    @PostMapping
    @Operation(summary = "Criar nova doação", description = "Cria uma nova doação no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Doação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<DonationResponseDTO> createDonation(
            @Valid @RequestBody DonationCreateRequestDTO createRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        DonationResponseDTO response = donationService.createDonation(createRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar doação por ID", description = "Busca uma doação específica pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doação encontrada"),
        @ApiResponse(responseCode = "404", description = "Doação não encontrada")
    })
    public ResponseEntity<DonationResponseDTO> getDonationById(@PathVariable String id) {
        return donationService.findById(id)
                .map(donation -> ResponseEntity.ok(donation))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Listar doações", description = "Lista doações com filtros opcionais")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de doações retornada com sucesso")
    })
    public ResponseEntity<Page<DonationResponseDTO>> listDonations(
            @Parameter(description = "Filtrar por beneficiário") @RequestParam(required = false) String beneficiarioId,
            @Parameter(description = "Filtrar por doador") @RequestParam(required = false) String doadorId,
            @Parameter(description = "Data de início (formato: yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Data de fim (formato: yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<DonationResponseDTO> donations;
        
        if (beneficiarioId != null) {
            donations = donationService.findByBeneficiario(beneficiarioId, pageable);
        } else if (doadorId != null) {
            donations = donationService.findByDoador(doadorId, pageable);
        } else if (startDate != null && endDate != null) {
            donations = donationService.findByDateRange(startDate, endDate, pageable);
        } else {
            // Return recent donations (last 30 days) for performance
            donations = donationService.getRecentDonations(30, pageable);
        }
        
        return ResponseEntity.ok(donations);
    }
    
    @GetMapping("/beneficiario/{beneficiarioId}")
    @Operation(summary = "Listar doações por beneficiário", description = "Lista todas as doações de um beneficiário específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de doações do beneficiário")
    })
    public ResponseEntity<Page<DonationResponseDTO>> getDonationsByBeneficiario(
            @PathVariable String beneficiarioId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<DonationResponseDTO> donations = donationService.findByBeneficiario(beneficiarioId, pageable);
        return ResponseEntity.ok(donations);
    }
    
    @GetMapping("/beneficiario/{beneficiarioId}/stats")
    @Operation(summary = "Estatísticas de doações", description = "Retorna estatísticas de doações do beneficiário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<DonationStatsResponse> getDonationStats(@PathVariable String beneficiarioId) {
        BigDecimal totalValue = donationService.getTotalReceivedByBeneficiario(beneficiarioId);
        Long totalCount = donationService.getCountReceivedByBeneficiario(beneficiarioId);
        
        DonationStatsResponse stats = new DonationStatsResponse(totalValue, totalCount);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/doador/{doadorId}")
    @Operation(summary = "Listar doações por doador", description = "Lista todas as doações feitas por um doador específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de doações do doador")
    })
    public ResponseEntity<Page<DonationResponseDTO>> getDonationsByDoador(
            @PathVariable String doadorId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<DonationResponseDTO> donations = donationService.findByDoador(doadorId, pageable);
        return ResponseEntity.ok(donations);
    }
    
    @GetMapping("/doador/{doadorId}/stats")
    @Operation(summary = "Estatísticas de doações do doador", description = "Retorna estatísticas de doações feitas por um doador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<DonationStatsResponse> getDonorStats(@PathVariable String doadorId) {
        BigDecimal totalValue = donationService.getTotalDonatedByDoador(doadorId);
        Long totalCount = donationService.getCountMadeByDoador(doadorId);
        
        DonationStatsResponse stats = new DonationStatsResponse(totalValue, totalCount);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/usuario/{userId}")
    @Operation(summary = "Listar todas as doações de um usuário", description = "Lista doações onde o usuário está envolvido como doador ou beneficiário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de doações do usuário")
    })
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<Page<DonationResponseDTO>> getUserDonations(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<DonationResponseDTO> donations = donationService.findByUsuarioInvolvido(userId, pageable);
        return ResponseEntity.ok(donations);
    }
    
    public record DonationStatsResponse(BigDecimal totalValue, Long totalCount) {}
}