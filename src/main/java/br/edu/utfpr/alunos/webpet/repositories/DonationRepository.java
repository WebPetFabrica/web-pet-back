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
import br.edu.utfpr.alunos.webpet.domain.donation.StatusDoacao;
import br.edu.utfpr.alunos.webpet.domain.donation.TipoDoacao;

@Repository
public interface DonationRepository extends JpaRepository<Donation, String> {
    
    List<Donation> findByBeneficiarioId(String beneficiarioId);
    
    Page<Donation> findByBeneficiarioId(String beneficiarioId, Pageable pageable);
    
    List<Donation> findByStatusDoacao(StatusDoacao statusDoacao);
    
    Page<Donation> findByStatusDoacao(StatusDoacao statusDoacao, Pageable pageable);
    
    List<Donation> findByTipoDoacao(TipoDoacao tipoDoacao);
    
    @Query("SELECT d FROM Donation d WHERE d.beneficiarioId = :beneficiarioId AND d.statusDoacao = :status")
    List<Donation> findByBeneficiarioIdAndStatus(@Param("beneficiarioId") String beneficiarioId, 
                                                 @Param("status") StatusDoacao status);
    
    @Query("SELECT d FROM Donation d WHERE d.beneficiarioId = :beneficiarioId AND d.statusDoacao = :status")
    Page<Donation> findByBeneficiarioIdAndStatus(@Param("beneficiarioId") String beneficiarioId, 
                                                 @Param("status") StatusDoacao status, 
                                                 Pageable pageable);
    
    @Query("SELECT SUM(d.valor) FROM Donation d WHERE d.beneficiarioId = :beneficiarioId AND d.statusDoacao = 'PROCESSADA'")
    BigDecimal getTotalDonationsByBeneficiario(@Param("beneficiarioId") String beneficiarioId);
    
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.beneficiarioId = :beneficiarioId AND d.statusDoacao = 'PROCESSADA'")
    Long getCountDonationsByBeneficiario(@Param("beneficiarioId") String beneficiarioId);
    
    @Query("""
        SELECT d FROM Donation d 
        WHERE d.criadoEm BETWEEN :startDate AND :endDate 
        AND d.statusDoacao = :status
        ORDER BY d.criadoEm DESC
        """)
    List<Donation> findByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           @Param("status") StatusDoacao status);
    
    @Query("""
        SELECT d FROM Donation d 
        WHERE LOWER(d.nomeDoador) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(d.emailDoador) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY d.criadoEm DESC
        """)
    Page<Donation> searchDonations(@Param("searchTerm") String searchTerm, Pageable pageable);
}