package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "adoptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Adoption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User adopter;

    private LocalDateTime adoptionDate;
}