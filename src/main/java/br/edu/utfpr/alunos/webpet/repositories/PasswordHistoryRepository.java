package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

/**
 * Repository for managing password history data.
 * 
 * <p>Provides data access methods for password history tracking,
 * including CRUD operations and specialized queries for:
 * <ul>
 *   <li>Password reuse validation</li>
 *   <li>History cleanup and maintenance</li>
 *   <li>User-specific password tracking</li>
 *   <li>Bulk operations for system maintenance</li>
 * </ul>
 * 
 */
@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, String> {
    
    /**
     * Finds password history for a user ordered by creation date (newest first).
     * 
     * <p>This method is primarily used for password reuse validation,
     * returning the most recent passwords first for efficient checking.
     * 
     * @param userId the user's unique identifier
     * @return list of password history entries, newest first
     */
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Counts password history entries for a user.
     * 
     * <p>Useful for determining if cleanup is needed or for
     * displaying history statistics.
     * 
     * @param userId the user's unique identifier
     * @return number of password history entries
     */
    int countByUserId(String userId);
    
    /**
     * Deletes all password history for a user.
     * 
     * <p>This method should be used when a user account is deleted
     * or when resetting password history completely.
     * 
     * @param userId the user's unique identifier
     * @return number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM PasswordHistory p WHERE p.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);
    
    /**
     * Finds password history entries older than specified date.
     * 
     * <p>Used for identifying old entries that can be cleaned up
     * to maintain database performance and comply with data retention policies.
     * 
     * @param cutoffDate the cutoff date for old entries
     * @return list of password history entries older than cutoff date
     */
    @Query("SELECT p FROM PasswordHistory p WHERE p.createdAt < :cutoffDate ORDER BY p.createdAt ASC")
    List<PasswordHistory> findOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Deletes password history entries older than specified date.
     * 
     * <p>Bulk delete operation for cleaning up old password history.
     * Should be used by scheduled maintenance jobs to keep the database
     * size manageable and comply with data retention policies.
     * 
     * @param cutoffDate the cutoff date for deletion
     * @return number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM PasswordHistory p WHERE p.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Gets the most recent password history entry for a user.
     * 
     * <p>Returns only the latest password history entry, useful for
     * quick validation or displaying last password change date.
     * 
     * @param userId the user's unique identifier
     * @return the most recent password history entry, if any
     */
    @Query("SELECT p FROM PasswordHistory p WHERE p.userId = :userId ORDER BY p.createdAt DESC LIMIT 1")
    Optional<PasswordHistory> findLatestByUserId(@Param("userId") String userId);
    
    /**
     * Gets the top N most recent password history entries for a user.
     * 
     * <p>More efficient than loading all history when only a specific
     * number of recent entries are needed for validation.
     * 
     * @param userId the user's unique identifier
     * @param limit maximum number of entries to return
     * @return list of recent password history entries, limited by count
     */
    @Query("SELECT p FROM PasswordHistory p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<PasswordHistory> findTopNByUserId(@Param("userId") String userId, Pageable pageable);
    
    /**
     * Checks if a user has any password history.
     * 
     * <p>Efficient existence check without loading actual data.
     * Useful for determining if a user is new or has password history.
     * 
     * @param userId the user's unique identifier
     * @return true if user has any password history, false otherwise
     */
    boolean existsByUserId(String userId);
    
    /**
     * Finds users with password history older than specified date.
     * 
     * <p>Useful for identifying users who haven't changed passwords
     * in a long time, for security compliance reporting.
     * 
     * @param cutoffDate the cutoff date for old passwords
     * @return list of user IDs with old password history
     */
    @Query("SELECT DISTINCT p.userId FROM PasswordHistory p " +
           "WHERE p.userId NOT IN (" +
           "    SELECT p2.userId FROM PasswordHistory p2 " +
           "    WHERE p2.createdAt >= :cutoffDate" +
           ")")
    List<String> findUsersWithOldPasswords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Gets password history count by date range.
     * 
     * <p>Useful for analytics and monitoring password change patterns.
     * 
     * @param startDate start of date range
     * @param endDate end of date range
     * @return number of password changes in the date range
     */
    @Query("SELECT COUNT(p) FROM PasswordHistory p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") LocalDateTime startDate, 
                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Deletes excess password history entries for a user, keeping only the most recent N.
     * 
     * <p>This method ensures that each user's password history doesn't exceed
     * the maximum allowed size while preserving the most recent entries.
     * 
     * @param userId the user's unique identifier
     * @param keepCount number of recent entries to keep
     * @return number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM PasswordHistory p WHERE p.userId = :userId " +
           "AND p.id NOT IN (" +
           "    SELECT p2.id FROM PasswordHistory p2 " +
           "    WHERE p2.userId = :userId " +
           "    ORDER BY p2.createdAt DESC " +
           "    LIMIT :keepCount" +
           ")")
    int deleteExcessHistory(@Param("userId") String userId, @Param("keepCount") int keepCount);
}