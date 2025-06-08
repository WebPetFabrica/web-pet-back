package br.edu.utfpr.alunos.webpet.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.domain.pet.StatusAdocao;

@Repository
public interface PetRepository extends JpaRepository<Pet, String> {
    
    @Query("SELECT p FROM Pet p WHERE p.ativo = true AND p.statusAdocao = 'DISPONIVEL'")
    List<Pet> findAllAvailablePets();
    
    @Query("SELECT p FROM Pet p WHERE p.ativo = true AND p.statusAdocao = 'DISPONIVEL'")
    Page<Pet> findAllAvailablePets(Pageable pageable);
    
    @Query("SELECT p FROM Pet p WHERE p.id = :id AND p.ativo = true")
    Optional<Pet> findByIdAndAtivo(@Param("id") String id);
    
    @Query("SELECT p FROM Pet p WHERE p.responsavel.id = :responsavelId AND p.ativo = true")
    List<Pet> findByResponsavelId(@Param("responsavelId") String responsavelId);
    
    @Query("SELECT p FROM Pet p WHERE p.responsavel.id = :responsavelId AND p.ativo = true")
    Page<Pet> findByResponsavelId(@Param("responsavelId") String responsavelId, Pageable pageable);
    
    @Query("""
        SELECT p FROM Pet p 
        WHERE p.ativo = true 
        AND p.statusAdocao = 'DISPONIVEL'
        AND (:especie IS NULL OR p.especie = :especie)
        AND (:genero IS NULL OR p.genero = :genero)
        AND (:porte IS NULL OR p.porte = :porte)
        AND (:idadeMinima IS NULL OR FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', p.dataNascimento) >= :idadeMinima)
        AND (:idadeMaxima IS NULL OR FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', p.dataNascimento) <= :idadeMaxima)
        AND (:raca IS NULL OR LOWER(p.raca) LIKE LOWER(CONCAT('%', :raca, '%')))
        ORDER BY p.criadoEm DESC
        """)
    Page<Pet> findAvailablePetsWithFilters(
            @Param("especie") Especie especie,
            @Param("genero") Genero genero,
            @Param("porte") Porte porte,
            @Param("idadeMinima") Integer idadeMinima,
            @Param("idadeMaxima") Integer idadeMaxima,
            @Param("raca") String raca,
            Pageable pageable);
    
    @Query("""
        SELECT COUNT(p) FROM Pet p 
        WHERE p.ativo = true 
        AND p.statusAdocao = 'DISPONIVEL'
        AND (:especie IS NULL OR p.especie = :especie)
        AND (:genero IS NULL OR p.genero = :genero)
        AND (:porte IS NULL OR p.porte = :porte)
        AND (:idadeMinima IS NULL OR FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', p.dataNascimento) >= :idadeMinima)
        AND (:idadeMaxima IS NULL OR FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', p.dataNascimento) <= :idadeMaxima)
        AND (:raca IS NULL OR LOWER(p.raca) LIKE LOWER(CONCAT('%', :raca, '%')))
        """)
    Long countAvailablePetsWithFilters(
            @Param("especie") Especie especie,
            @Param("genero") Genero genero,
            @Param("porte") Porte porte,
            @Param("idadeMinima") Integer idadeMinima,
            @Param("idadeMaxima") Integer idadeMaxima,
            @Param("raca") String raca);
    
    List<Pet> findByEspecieAndAtivoTrue(Especie especie);
    
    List<Pet> findByPorteAndAtivoTrue(Porte porte);
    
    List<Pet> findByGeneroAndAtivoTrue(Genero genero);
    
    List<Pet> findByStatusAdocaoAndAtivoTrue(StatusAdocao statusAdocao);
    
    @Query("SELECT p FROM Pet p WHERE p.dataNascimento BETWEEN :dataInicio AND :dataFim AND p.ativo = true")
    List<Pet> findByIdadeBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
    
    @Query("SELECT DISTINCT p.raca FROM Pet p WHERE p.ativo = true ORDER BY p.raca")
    List<String> findDistinctRacas();
    
    @Query("""
        SELECT p FROM Pet p 
        WHERE p.ativo = true 
        AND (LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
        OR LOWER(p.raca) LIKE LOWER(CONCAT('%', :termo, '%'))
        OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))
        ORDER BY p.criadoEm DESC
        """)
    Page<Pet> searchPets(@Param("termo") String termo, Pageable pageable);
}