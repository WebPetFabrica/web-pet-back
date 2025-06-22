package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.utils.enums.CategoryType;
import br.edu.utfpr.alunos.webpet.utils.enums.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, String> {
    List<Animal> findAll();
    Optional<Animal> findById(String id);
    List<Animal> findByName(String name);
    List<Animal> findByCategory(CategoryType category);
    List<Animal> findByStatus(StatusType status);

    List<Animal> status(StatusType status);
}
