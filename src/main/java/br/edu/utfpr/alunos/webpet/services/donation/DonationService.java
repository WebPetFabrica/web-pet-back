package br.edu.utfpr.alunos.webpet.services.donation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.edu.utfpr.alunos.webpet.domain.donation.StatusDoacao;
import br.edu.utfpr.alunos.webpet.domain.donation.TipoDoacao;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;

public interface DonationService {
    
    DonationResponseDTO createDonation(DonationCreateRequestDTO createRequest);
    
    Optional<DonationResponseDTO> findById(String id);
    
    Page<DonationResponseDTO> findByBeneficiario(String beneficiarioId, Pageable pageable);
    
    Page<DonationResponseDTO> findByStatus(StatusDoacao status, Pageable pageable);
    
    Page<DonationResponseDTO> findByBeneficiarioAndStatus(String beneficiarioId, StatusDoacao status, Pageable pageable);
    
    Page<DonationResponseDTO> searchDonations(String searchTerm, Pageable pageable);
    
    List<TipoDoacao> getAvailableTipos();
    
    List<StatusDoacao> getAvailableStatus();
    
    BigDecimal getTotalDonationsByBeneficiario(String beneficiarioId);
    
    Long getCountDonationsByBeneficiario(String beneficiarioId);
    
    void processarDoacao(String donationId, String transactionId);
    
    void marcarComoFalha(String donationId);
    
    void cancelarDoacao(String donationId);
}