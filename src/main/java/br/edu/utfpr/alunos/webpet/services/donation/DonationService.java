package br.edu.utfpr.alunos.webpet.services.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;

/**
 * Service interface for Donation management operations.
 * 
 * <p>Provides business logic for donation operations, including creation,
 * querying, and analytics. Handles validation and business rules enforcement.
 * 
 */
public interface DonationService {
    
    /**
     * Creates a new donation from a donor to a beneficiary.
     * 
     * @param createRequest the donation creation data
     * @param doadorId the ID of the user making the donation
     * @return the created donation response DTO
     */
    DonationResponseDTO createDonation(DonationCreateRequestDTO createRequest, String doadorId);
    
    /**
     * Finds a donation by ID.
     * 
     * @param id the donation ID
     * @return the donation response DTO if found
     */
    Optional<DonationResponseDTO> findById(String id);
    
    /**
     * Finds donations received by a specific beneficiary.
     * 
     * @param beneficiarioId the beneficiary user ID
     * @param pageable pagination information
     * @return page of donations received by the beneficiary
     */
    Page<DonationResponseDTO> findByBeneficiario(String beneficiarioId, Pageable pageable);
    
    /**
     * Finds donations made by a specific donor.
     * 
     * @param doadorId the donor user ID
     * @param pageable pagination information
     * @return page of donations made by the donor
     */
    Page<DonationResponseDTO> findByDoador(String doadorId, Pageable pageable);
    
    /**
     * Finds all donations involving a specific user (as donor or beneficiary).
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of donations involving the user
     */
    Page<DonationResponseDTO> findByUsuarioInvolvido(String userId, Pageable pageable);
    
    /**
     * Finds donations within a date range.
     * 
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @param pageable pagination information
     * @return page of donations within the date range
     */
    Page<DonationResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Calculates total amount received by a beneficiary.
     * 
     * @param beneficiarioId the beneficiary user ID
     * @return total amount received
     */
    BigDecimal getTotalReceivedByBeneficiario(String beneficiarioId);
    
    /**
     * Calculates total amount donated by a donor.
     * 
     * @param doadorId the donor user ID
     * @return total amount donated
     */
    BigDecimal getTotalDonatedByDoador(String doadorId);
    
    /**
     * Counts donations received by a beneficiary.
     * 
     * @param beneficiarioId the beneficiary user ID
     * @return count of donations received
     */
    Long getCountReceivedByBeneficiario(String beneficiarioId);
    
    /**
     * Counts donations made by a donor.
     * 
     * @param doadorId the donor user ID
     * @return count of donations made
     */
    Long getCountMadeByDoador(String doadorId);
    
    /**
     * Gets recent donations (last N days).
     * 
     * @param days number of days to look back
     * @param pageable pagination information
     * @return page of recent donations
     */
    Page<DonationResponseDTO> getRecentDonations(int days, Pageable pageable);
}