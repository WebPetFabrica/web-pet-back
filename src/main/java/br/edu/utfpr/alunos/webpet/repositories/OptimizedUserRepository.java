package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.repositories.projections.UserProjections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OptimizedUserRepository extends BaseUserRepository<User> {
    
    @EntityGraph(attributePaths = {"userType"})
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
    Optional<User> findActiveByEmailWithType(@Param("email") String email);
    
    @Query("SELECT u.id as id, u.email as email, u.name as displayName, " +
           "u.userType as userType, u.active as active " +
           "FROM User u WHERE u.active = true")
    Page<UserProjections.BasicUserProjection> findAllActiveBasic(Pageable pageable);
    
    @Query("SELECT u.id as id, u.email as email, u.name as displayName, " +
           "u.userType as userType, u.active as active, u.createdAt as createdAt " +
           "FROM User u WHERE u.active = true ORDER BY u.createdAt DESC")
    Page<UserProjections.UserSummaryProjection> findAllActiveSummary(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<UserProjections.UserDetailProjection> findDetailByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.active = true AND u.createdAt >= :since")
    List<User> findRecentActiveUsers(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();
    
    @Query("SELECT u.id as id, u.email as email, u.name as displayName, " +
           "u.userType as userType, u.active as active " +
           "FROM User u WHERE u.email LIKE %:searchTerm% OR u.name LIKE %:searchTerm%")
    Page<UserProjections.BasicUserProjection> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}