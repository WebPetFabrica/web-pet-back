package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.repositories.projections.UserProjections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptimizedONGRepository extends ONGRepository {
    
    @EntityGraph(attributePaths = {"userType"})
    @Query("SELECT o FROM ONG o WHERE o.email = :email AND o.active = true")
    Optional<ONG> findActiveByEmailWithType(@Param("email") String email);
    
    @Query("SELECT o.id as id, o.email as email, o.nomeOng as nomeOng, " +
           "o.cnpj as cnpj, o.endereco as endereco, o.celular as celular, " +
           "o.active as active, o.createdAt as createdAt " +
           "FROM ONG o WHERE o.active = true")
    Page<UserProjections.ONGProjection> findAllActiveONGs(Pageable pageable);
    
    @Query("SELECT o FROM ONG o WHERE o.cnpj = :cnpj")
    Optional<UserProjections.ONGProjection> findProjectionByCnpj(@Param("cnpj") String cnpj);
    
    @Query("SELECT o.id as id, o.email as email, o.nomeOng as nomeOng, " +
           "o.cnpj as cnpj, o.endereco as endereco, o.celular as celular, " +
           "o.active as active, o.createdAt as createdAt " +
           "FROM ONG o WHERE o.nomeOng LIKE %:searchTerm% OR o.endereco LIKE %:searchTerm%")
    Page<UserProjections.ONGProjection> searchONGs(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT o FROM ONG o WHERE o.endereco LIKE %:cidade% AND o.active = true")
    List<ONG> findActiveONGsByCity(@Param("cidade") String cidade);
    
    @Query("SELECT COUNT(o) FROM ONG o WHERE o.active = true")
    long countActiveONGs();
}