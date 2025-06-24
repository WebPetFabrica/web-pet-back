package br.edu.utfpr.alunos.webpet.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.adoption.AdoptionRequest;
import br.edu.utfpr.alunos.webpet.domain.adoption.AdoptionStatus;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, String> {
    List<AdoptionRequest> findByUserId(String userId);
    List<AdoptionRequest> findByAnimalId(String animalId);
    List<AdoptionRequest> findByStatus(AdoptionStatus status);
    Optional<AdoptionRequest> findByAnimalIdAndUserId(String animalId, String userId);
    List<AdoptionRequest> findByUserIdAndStatus(String userId, AdoptionStatus status);
    List<AdoptionRequest> findByAnimalIdAndStatus(String animalId, AdoptionStatus status);
}