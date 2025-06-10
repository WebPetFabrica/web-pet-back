package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.dto.AnimalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, String> {
    AnimalDTO save(AnimalDTO dto);
    List<Animal> findAll();
    Optional<Animal> findById(String id);
    List<AnimalDTO> findByName(String name);
    List<AnimalDTO> findByDescription(String description);
    List<AnimalDTO> findByCategory(String category);
    List<AnimalDTO> findByStatus(String status);

}
