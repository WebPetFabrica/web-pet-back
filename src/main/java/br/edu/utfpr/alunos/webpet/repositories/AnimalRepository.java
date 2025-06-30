package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.Animal;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.utils.enums.CategoryType;
import br.edu.utfpr.alunos.webpet.utils.enums.StatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, String> {
    @NonNull()
    List<Animal> findAll();

    @NonNull()
    Optional<Animal> findById(@NonNull() String id);

    List<Animal> findByName(String name);

    List<Animal> findByCategory(CategoryType category);

    List<Animal> findByStatus(StatusType status);

    List<Animal> findByOng(User ong);

    @Query("SELECT a FROM Animal a WHERE (:ong IS NULL OR a.ong = :ong) AND (:category IS NULL OR a.category = :category) AND (:status IS NULL OR a.status = :status)")
    Page<Animal> findByOngAndCategoryAndStatus(
        @Param("ong") User ong,
        @Param("category") CategoryType category,
        @Param("status") StatusType status,
        Pageable pageable
    );
}
