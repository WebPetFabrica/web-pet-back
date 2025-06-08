package br.edu.utfpr.alunos.webpet.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.user.ONG;

@Repository
public interface ONGRepository extends BaseUserRepository<ONG> {
    Optional<ONG> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
    
    @Query("""
        SELECT o FROM ONG o 
        WHERE o.active = true 
        AND (LOWER(o.nomeOng) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(o.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(o.descricao) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY o.nomeOng
        """)
    Page<ONG> searchByEmailOrNameAndActive(@Param("searchTerm") String searchTerm, Pageable pageable);
}