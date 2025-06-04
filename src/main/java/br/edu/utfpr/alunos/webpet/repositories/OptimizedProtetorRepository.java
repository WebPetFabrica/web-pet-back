package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
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
public interface OptimizedProtetorRepository extends ProtetorRepository {
    
    @EntityGraph(attributePaths = {"userType"})
    @Query("SELECT p FROM Protetor p WHERE p.email = :email AND p.active = true")
    Optional<Protetor> findActiveByEmailWithType(@Param("email") String email);
    
    @Query("SELECT p.id as id, p.email as email, p.nomeCompleto as nomeCompleto, " +
           "p.cpf as cpf, p.endereco as endereco, p.capacidadeAcolhimento as capacidadeAcolhimento, " +
           "p.celular as celular, p.active as active, p.createdAt as createdAt " +
           "FROM Protetor p WHERE p.active = true")
    Page<UserProjections.ProtetorProjection> findAllActiveProtetores(Pageable pageable);
    
    @Query("SELECT p FROM Protetor p WHERE p.cpf = :cpf")
    Optional<UserProjections.ProtetorProjection> findProjectionByCpf(@Param("cpf") String cpf);
    
    @Query("SELECT p.id as id, p.email as email, p.nomeCompleto as nomeCompleto, " +
           "p.cpf as cpf, p.endereco as endereco, p.capacidadeAcolhimento as capacidadeAcolhimento, " +
           "p.celular as celular, p.active as active, p.createdAt as createdAt " +
           "FROM Protetor p WHERE p.nomeCompleto LIKE %:searchTerm% OR p.endereco LIKE %:searchTerm%")
    Page<UserProjections.ProtetorProjection> searchProtetores(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM Protetor p WHERE p.endereco LIKE %:cidade% AND p.active = true " +
           "AND p.capacidadeAcolhimento > 0")
    List<Protetor> findActiveProtetoresByCity(@Param("cidade") String cidade);
    
    @Query("SELECT p FROM Protetor p WHERE p.capacidadeAcolhimento >= :minCapacidade AND p.active = true")
    List<Protetor> findByMinCapacidade(@Param("minCapacidade") Integer minCapacidade);
    
    @Query("SELECT COUNT(p) FROM Protetor p WHERE p.active = true")
    long countActiveProtetores();
    
    @Query("SELECT SUM(p.capacidadeAcolhimento) FROM Protetor p WHERE p.active = true")
    Long getTotalCapacidadeAcolhimento();
}