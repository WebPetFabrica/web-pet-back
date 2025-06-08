package br.edu.utfpr.alunos.webpet.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.alunos.webpet.domain.donation.StatusDoacao;
import br.edu.utfpr.alunos.webpet.domain.donation.TipoDoacao;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;
import br.edu.utfpr.alunos.webpet.services.donation.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/doacoes")
@RequiredArgsConstructor
@Tag(name = "Doações", description = "Endpoints para gerenciamento de doações")
public class DonationController {
    
    private final DonationService donationService;
    
    @PostMapping
    @Operation(summary = "Criar nova doação", description = "Cria uma nova doação no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Doação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    })
    public ResponseEntity<DonationResponseDTO> createDonation(
            @Valid @RequestBody DonationCreateRequestDTO createRequest) {
        
        DonationResponseDTO response = donationService.createDonation(createRequest);
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
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) StatusDoacao status,
            @Parameter(description = "Termo de busca") @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<DonationResponseDTO> donations;
        
        if (search != null && !search.trim().isEmpty()) {
            donations = donationService.searchDonations(search.trim(), pageable);
        } else if (beneficiarioId != null && status != null) {
            donations = donationService.findByBeneficiarioAndStatus(beneficiarioId, status, pageable);
        } else if (beneficiarioId != null) {
            donations = donationService.findByBeneficiario(beneficiarioId, pageable);
        } else if (status != null) {
            donations = donationService.findByStatus(status, pageable);
        } else {
            // Return all donations - you might want to restrict this in production
            donations = donationService.findByStatus(StatusDoacao.PROCESSADA, pageable);
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
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) StatusDoacao status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<DonationResponseDTO> donations;
        
        if (status != null) {
            donations = donationService.findByBeneficiarioAndStatus(beneficiarioId, status, pageable);
        } else {
            donations = donationService.findByBeneficiario(beneficiarioId, pageable);
        }
        
        return ResponseEntity.ok(donations);
    }
    
    @GetMapping("/beneficiario/{beneficiarioId}/stats")
    @Operation(summary = "Estatísticas de doações", description = "Retorna estatísticas de doações do beneficiário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<DonationStatsResponse> getDonationStats(@PathVariable String beneficiarioId) {
        BigDecimal totalValue = donationService.getTotalDonationsByBeneficiario(beneficiarioId);
        Long totalCount = donationService.getCountDonationsByBeneficiario(beneficiarioId);
        
        DonationStatsResponse stats = new DonationStatsResponse(totalValue, totalCount);
        return ResponseEntity.ok(stats);
    }
    
    @PatchMapping("/{id}/processar")
    @Operation(summary = "Processar doação", description = "Marca uma doação como processada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doação processada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Doação não encontrada"),
        @ApiResponse(responseCode = "409", description = "Doação já foi processada")
    })
    public ResponseEntity<Void> processarDoacao(
            @PathVariable String id,
            @RequestParam String transactionId) {
        
        donationService.processarDoacao(id, transactionId);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/falha")
    @Operation(summary = "Marcar como falha", description = "Marca uma doação como falha no processamento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doação marcada como falha"),
        @ApiResponse(responseCode = "404", description = "Doação não encontrada")
    })
    public ResponseEntity<Void> marcarComoFalha(@PathVariable String id) {
        donationService.marcarComoFalha(id);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar doação", description = "Cancela uma doação pendente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doação cancelada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Doação não encontrada"),
        @ApiResponse(responseCode = "409", description = "Doação já foi processada")
    })
    public ResponseEntity<Void> cancelarDoacao(@PathVariable String id) {
        donationService.cancelarDoacao(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/metadata/tipos")
    @Operation(summary = "Listar tipos de doação", description = "Lista todos os tipos de doação disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tipos de doação")
    })
    public ResponseEntity<List<TipoDoacao>> getTiposDoacao() {
        return ResponseEntity.ok(donationService.getAvailableTipos());
    }
    
    @GetMapping("/metadata/status")
    @Operation(summary = "Listar status de doação", description = "Lista todos os status de doação disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de status de doação")
    })
    public ResponseEntity<List<StatusDoacao>> getStatusDoacao() {
        return ResponseEntity.ok(donationService.getAvailableStatus());
    }
    
    public record DonationStatsResponse(BigDecimal totalValue, Long totalCount) {}
}