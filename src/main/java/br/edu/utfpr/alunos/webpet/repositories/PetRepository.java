package br.edu.utfpr.alunos.webpet.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.dto.pet.PetFilterDTO;

/**
 * Repository interface for Pet entity operations.
 * 
 * <p>Extends JpaRepository for basic CRUD operations and JpaSpecificationExecutor
 * for dynamic query building with criteria API. This allows for flexible
 * filtering and searching of pets.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Basic CRUD operations via JpaRepository</li>
 *   <li>Dynamic filtering via JpaSpecificationExecutor</li>
 *   <li>Custom queries for common use cases</li>
 *   <li>Pagination support for all query methods</li>
 * </ul>
 * 
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, String>, JpaSpecificationExecutor<Pet> {
    
    /**
     * Find all pets available for adoption.
     * 
     * @return list of available pets ordered by creation date
     */
    @Query("SELECT p FROM Pet p WHERE p.disponivel = true ORDER BY p.createdAt DESC")
    List<Pet> findAllAvailablePets();
    
    /**
     * Find all pets available for adoption with pagination.
     * 
     * @param pageable pagination information
     * @return page of available pets ordered by creation date
     */
    @Query("SELECT p FROM Pet p WHERE p.disponivel = true ORDER BY p.createdAt DESC")
    Page<Pet> findAllAvailablePets(Pageable pageable);
    
    /**
     * Find all pets managed by a specific responsible user.
     * 
     * @param responsavelId ID of the responsible user (ONG or PROTETOR)
     * @return list of pets managed by the user
     */
    @Query("SELECT p FROM Pet p WHERE p.responsavelId = :responsavelId ORDER BY p.createdAt DESC")
    List<Pet> findByResponsavelId(@Param("responsavelId") String responsavelId);
    
    /**
     * Find all pets managed by a specific responsible user with pagination.
     * 
     * @param responsavelId ID of the responsible user
     * @param pageable pagination information
     * @return page of pets managed by the user
     */
    @Query(value = "SELECT p FROM Pet p WHERE p.responsavelId = :responsavelId ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Pet p WHERE p.responsavelId = :responsavelId")
    Page<Pet> findByResponsavelId(@Param("responsavelId") String responsavelId, Pageable pageable);
    
    /**
     * Find available pets by species.
     * 
     * @param especie the species to filter by
     * @return list of available pets of the specified species
     */
    List<Pet> findByEspecieAndDisponivelTrue(Especie especie);
    
    /**
     * Find available pets by size.
     * 
     * @param porte the size to filter by
     * @return list of available pets of the specified size
     */
    List<Pet> findByPorteAndDisponivelTrue(Porte porte);
    
    /**
     * Find available pets by gender.
     * 
     * @param genero the gender to filter by
     * @return list of available pets of the specified gender
     */
    List<Pet> findByGeneroAndDisponivelTrue(Genero genero);
    
    /**
     * Find available pets within an age range.
     * 
     * @param idadeMinima minimum age (inclusive)
     * @param idadeMaxima maximum age (inclusive)
     * @return list of available pets within the age range
     */
    @Query("SELECT p FROM Pet p WHERE p.disponivel = true AND p.idade BETWEEN :idadeMinima AND :idadeMaxima ORDER BY p.createdAt DESC")
    List<Pet> findByIdadeBetweenAndDisponivelTrue(@Param("idadeMinima") Integer idadeMinima, @Param("idadeMaxima") Integer idadeMaxima);
    
    /**
     * Search pets by name or description.
     * 
     * @param termo search term
     * @param pageable pagination information
     * @return page of pets matching the search term
     */
    @Query("""
        SELECT p FROM Pet p 
        WHERE p.disponivel = true 
        AND (LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
        OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))
        ORDER BY p.createdAt DESC
        """)
    Page<Pet> searchAvailablePets(@Param("termo") String termo, Pageable pageable);
    
    /**
     * Find pets with multiple filters using optimized query with JOIN FETCH.
     * 
     * @param filters filter criteria for pet search
     * @param pageable pagination information
     * @return page of pets matching the filters
     */
    @Query(value = """
        SELECT p FROM Pet p
        WHERE p.disponivel = true
        AND (:#{#filters.especie} IS NULL OR p.especie = :#{#filters.especie})
        AND (:#{#filters.porte} IS NULL OR p.porte = :#{#filters.porte})
        AND (:#{#filters.genero} IS NULL OR p.genero = :#{#filters.genero})
        AND (:#{#filters.idadeMinima} IS NULL OR p.idade >= :#{#filters.idadeMinima})
        AND (:#{#filters.idadeMaxima} IS NULL OR p.idade <= :#{#filters.idadeMaxima})
        ORDER BY p.createdAt DESC
        """,
        countQuery = """
        SELECT COUNT(p) FROM Pet p
        WHERE p.disponivel = true
        AND (:#{#filters.especie} IS NULL OR p.especie = :#{#filters.especie})
        AND (:#{#filters.porte} IS NULL OR p.porte = :#{#filters.porte})
        AND (:#{#filters.genero} IS NULL OR p.genero = :#{#filters.genero})
        AND (:#{#filters.idadeMinima} IS NULL OR p.idade >= :#{#filters.idadeMinima})
        AND (:#{#filters.idadeMaxima} IS NULL OR p.idade <= :#{#filters.idadeMaxima})
        """)
    Page<Pet> findAvailablePetsWithFilters(@Param("filters") PetFilterDTO filters, Pageable pageable);
    
    /**
     * Count available pets with filters.
     * 
     * @param filters filter criteria for pet search
     * @return count of pets matching the filters
     */
    @Query("""
        SELECT COUNT(p) FROM Pet p 
        WHERE p.disponivel = true
        AND (:#{#filters.especie} IS NULL OR p.especie = :#{#filters.especie})
        AND (:#{#filters.porte} IS NULL OR p.porte = :#{#filters.porte})
        AND (:#{#filters.genero} IS NULL OR p.genero = :#{#filters.genero})
        AND (:#{#filters.idadeMinima} IS NULL OR p.idade >= :#{#filters.idadeMinima})
        AND (:#{#filters.idadeMaxima} IS NULL OR p.idade <= :#{#filters.idadeMaxima})
        """)
    Long countAvailablePetsWithFilters(@Param("filters") PetFilterDTO filters);
    
    /**
     * Check if a pet with the given name already exists for a responsible user.
     * Used to prevent duplicate pet names for the same responsible user.
     * 
     * @param nome pet name
     * @param responsavelId responsible user ID
     * @return true if a pet with the name exists for the user
     */
    @Query("SELECT COUNT(p) > 0 FROM Pet p WHERE p.nome = :nome AND p.responsavelId = :responsavelId")
    boolean existsByNomeAndResponsavelId(@Param("nome") String nome, @Param("responsavelId") String responsavelId);
    
    /**
     * Count total pets managed by a responsible user.
     * 
     * @param responsavelId responsible user ID
     * @return total count of pets
     */
    @Query("SELECT COUNT(p) FROM Pet p WHERE p.responsavelId = :responsavelId")
    Long countByResponsavelId(@Param("responsavelId") String responsavelId);
    
    /**
     * Count available pets managed by a responsible user.
     * 
     * @param responsavelId responsible user ID
     * @return count of available pets
     */
    @Query("SELECT COUNT(p) FROM Pet p WHERE p.responsavelId = :responsavelId AND p.disponivel = true")
    Long countByResponsavelIdAndDisponivelTrue(@Param("responsavelId") String responsavelId);
}