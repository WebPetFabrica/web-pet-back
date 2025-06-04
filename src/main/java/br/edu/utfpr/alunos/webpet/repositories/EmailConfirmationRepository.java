package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.EmailConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, String> {
    Optional<EmailConfirmation> findByToken(String token);
    Optional<EmailConfirmation> findByUserId(String userId);
    
    // ADICIONAR estes métodos:
    @Query("SELECT e FROM EmailConfirmation e WHERE e.userId = :userId ORDER BY e.createdAt DESC")
    Optional<EmailConfirmation> findLatestByUserId(@Param("userId") String userId);
    
    @Modifying
    @Query("DELETE FROM EmailConfirmation e WHERE e.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE EmailConfirmation e SET e.confirmed = true WHERE e.userId = :userId AND e.confirmed = false")
    int invalidatePendingConfirmations(@Param("userId") String userId);
}