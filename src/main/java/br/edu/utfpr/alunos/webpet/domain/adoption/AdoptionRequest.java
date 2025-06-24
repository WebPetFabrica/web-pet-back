package br.edu.utfpr.alunos.webpet.domain.adoption;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "adoption_requests")
@Getter
@Setter
@NoArgsConstructor
public class AdoptionRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String animalId;
    
    @Column(nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionStatus status = AdoptionStatus.PENDING;
    
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime requestDate;
    
    public AdoptionRequest(String animalId, String userId) {
        this.animalId = animalId;
        this.userId = userId;
        this.status = AdoptionStatus.PENDING;
    }
}