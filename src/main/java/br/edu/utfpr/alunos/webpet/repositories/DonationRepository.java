package br.edu.utfpr.alunos.webpet.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.donation.Donation;

/**
 * Repository interface for Donation entity operations.
 * 
 * <p>Provides data access methods for donation transactions in the WebPet system.
 * Supports querying donations by donor, beneficiary, date ranges, and amounts.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Basic CRUD operations via JpaRepository</li>
 *   <li>Custom queries for donation analytics</li>
 *   <li>Support for filtering by users and date ranges</li>
 *   <li>Pagination support for all listing methods</li>
 * </ul>
 * 
 */
@Repository
public interface DonationRepository extends JpaRepository<Donation, String> {
    
    /**
     * Find all donations received by a specific beneficiary.
     * 
     * @param beneficiarioId ID of the beneficiary user
     * @return list of donations received by the user
     */
    List<Donation> findByBeneficiarioId(String beneficiarioId);
    
    /**
     * Find all donations received by a specific beneficiary with pagination.
     * 
     * @param beneficiarioId ID of the beneficiary user
     * @param pageable pagination information
     * @return page of donations received by the user
     */
    Page<Donation> findByBeneficiarioId(String beneficiarioId, Pageable pageable);
    
    /**
     * Find all donations made by a specific donor.
     * 
     * @param doadorId ID of the donor user
     * @return list of donations made by the user
     */
    List<Donation> findByDoadorId(String doadorId);
    
    /**
     * Find all donations made by a specific donor with pagination.
     * 
     * @param doadorId ID of the donor user
     * @param pageable pagination information
     * @return page of donations made by the user
     */
    Page<Donation> findByDoadorId(String doadorId, Pageable pageable);
    
    /**
     * Find all donations involving a specific user (as donor or beneficiary).
     * 
     * @param userId ID of the user
     * @param pageable pagination information
     * @return page of donations involving the user
     */
    @Query("SELECT d FROM Donation d WHERE d.doadorId = :userId OR d.beneficiarioId = :userId ORDER BY d.dataDoacao DESC")
    Page<Donation> findByUsuarioInvolvido(@Param("userId") String userId, Pageable pageable);
    
    /**
     * Calculate total amount received by a beneficiary.
     * 
     * @param beneficiarioId ID of the beneficiary user
     * @return total amount received or null if no donations
     */
    @Query("SELECT SUM(d.valor) FROM Donation d WHERE d.beneficiarioId = :beneficiarioId")
    BigDecimal getTotalReceivedByBeneficiario(@Param("beneficiarioId") String beneficiarioId);
    
    /**
     * Calculate total amount donated by a donor.
     * 
     * @param doadorId ID of the donor user
     * @return total amount donated or null if no donations
     */
    @Query("SELECT SUM(d.valor) FROM Donation d WHERE d.doadorId = :doadorId")
    BigDecimal getTotalDonatedByDoador(@Param("doadorId") String doadorId);
    
    /**
     * Count donations received by a beneficiary.
     * 
     * @param beneficiarioId ID of the beneficiary user
     * @return count of donations received
     */
    Long countByBeneficiarioId(String beneficiarioId);
    
    /**
     * Count donations made by a donor.
     * 
     * @param doadorId ID of the donor user
     * @return count of donations made
     */
    Long countByDoadorId(String doadorId);
    
    /**
     * Find donations within a date range.
     * 
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @param pageable pagination information
     * @return page of donations within the date range
     */
    @Query("SELECT d FROM Donation d WHERE d.dataDoacao BETWEEN :startDate AND :endDate ORDER BY d.dataDoacao DESC")
    Page<Donation> findByDataDoacaoBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
    
    /**
     * Find donations above a minimum amount.
     * 
     * @param valorMinimo minimum donation amount
     * @param pageable pagination information
     * @return page of donations above the minimum amount
     */
    @Query("SELECT d FROM Donation d WHERE d.valor >= :valorMinimo ORDER BY d.valor DESC")
    Page<Donation> findByValorGreaterThanEqual(@Param("valorMinimo") BigDecimal valorMinimo, Pageable pageable);
    
    /**
     * Find recent donations (last N days).
     * 
     * @param days number of days to look back
     * @param pageable pagination information
     * @return page of recent donations
     */
    @Query("SELECT d FROM Donation d WHERE d.dataDoacao >= :sinceDate ORDER BY d.dataDoacao DESC")
    Page<Donation> findRecentDonations(@Param("sinceDate") LocalDateTime sinceDate, Pageable pageable);
    
    /**
     * Get donation statistics for a beneficiary within a date range.
     * 
     * @param beneficiarioId ID of the beneficiary user
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return array with [count, sum, average] or null values if no donations
     */
    @Query("""
        SELECT COUNT(d), SUM(d.valor), AVG(d.valor)
        FROM Donation d 
        WHERE d.beneficiarioId = :beneficiarioId 
        AND d.dataDoacao BETWEEN :startDate AND :endDate
        """)
    Object[] getDonationStatistics(@Param("beneficiarioId") String beneficiarioId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find top beneficiaries by total donations received.
     * 
     * @param limit maximum number of results
     * @return list of [beneficiarioId, totalAmount] pairs
     */
    @Query("""
        SELECT d.beneficiarioId, SUM(d.valor) as total
        FROM Donation d 
        GROUP BY d.beneficiarioId 
        ORDER BY total DESC
        """)
    List<Object[]> findTopBeneficiarios(Pageable pageable);
    
    /**
     * Find top donors by total donations made.
     * 
     * @param limit maximum number of results
     * @return list of [doadorId, totalAmount] pairs
     */
    @Query("""
        SELECT d.doadorId, SUM(d.valor) as total
        FROM Donation d 
        GROUP BY d.doadorId 
        ORDER BY total DESC
        """)
    List<Object[]> findTopDoadores(Pageable pageable);
}