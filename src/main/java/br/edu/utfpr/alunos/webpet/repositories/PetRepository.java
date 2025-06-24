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
    List<Pet> findByResponsavelId(String responsavelId);
    
    /**
     * Find all pets managed by a specific responsible user with pagination.
     * 
     * @param responsavelId ID of the responsible user
     * @param pageable pagination information
     * @return page of pets managed by the user
     */
    Page<Pet> findByResponsavelId(String responsavelId, Pageable pageable);
    
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
     * Find pets with multiple filters.
     * 
     * @param especie species filter (optional)
     * @param porte size filter (optional)
     * @param genero gender filter (optional)
     * @param idadeMinima minimum age filter (optional)
     * @param idadeMaxima maximum age filter (optional)
     * @param pageable pagination information
     * @return page of pets matching the filters
     */
    @Query("""
        SELECT p FROM Pet p 
        WHERE p.disponivel = true
        AND (:especie IS NULL OR p.especie = :especie)
        AND (:porte IS NULL OR p.porte = :porte)
        AND (:genero IS NULL OR p.genero = :genero)
        AND (:idadeMinima IS NULL OR p.idade >= :idadeMinima)
        AND (:idadeMaxima IS NULL OR p.idade <= :idadeMaxima)
        ORDER BY p.createdAt DESC
        """)
    Page<Pet> findAvailablePetsWithFilters(
            @Param("especie") Especie especie,
            @Param("porte") Porte porte,
            @Param("genero") Genero genero,
            @Param("idadeMinima") Integer idadeMinima,
            @Param("idadeMaxima") Integer idadeMaxima,
            Pageable pageable);
    
    /**
     * Count available pets with filters.
     * 
     * @param especie species filter (optional)
     * @param porte size filter (optional)
     * @param genero gender filter (optional)
     * @param idadeMinima minimum age filter (optional)
     * @param idadeMaxima maximum age filter (optional)
     * @return count of pets matching the filters
     */
    @Query("""
        SELECT COUNT(p) FROM Pet p 
        WHERE p.disponivel = true
        AND (:especie IS NULL OR p.especie = :especie)
        AND (:porte IS NULL OR p.porte = :porte)
        AND (:genero IS NULL OR p.genero = :genero)
        AND (:idadeMinima IS NULL OR p.idade >= :idadeMinima)
        AND (:idadeMaxima IS NULL OR p.idade <= :idadeMaxima)
        """)
    Long countAvailablePetsWithFilters(
            @Param("especie") Especie especie,
            @Param("porte") Porte porte,
            @Param("genero") Genero genero,
            @Param("idadeMinima") Integer idadeMinima,
            @Param("idadeMaxima") Integer idadeMaxima);
    
    /**
     * Check if a pet with the given name already exists for a responsible user.
     * Used to prevent duplicate pet names for the same responsible user.
     * 
     * @param nome pet name
     * @param responsavelId responsible user ID
     * @return true if a pet with the name exists for the user
     */
    boolean existsByNomeAndResponsavelId(String nome, String responsavelId);
    
    /**
     * Count total pets managed by a responsible user.
     * 
     * @param responsavelId responsible user ID
     * @return total count of pets
     */
    Long countByResponsavelId(String responsavelId);
    
    /**
     * Count available pets managed by a responsible user.
     * 
     * @param responsavelId responsible user ID
     * @return count of available pets
     */
    Long countByResponsavelIdAndDisponivelTrue(String responsavelId);
}