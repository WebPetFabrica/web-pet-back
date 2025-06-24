package br.edu.utfpr.alunos.webpet.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;

@Repository
public interface ProtetorRepository extends JpaRepository<Protetor, String> {
    Optional<Protetor> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Protetor> findByIdAndActiveTrue(String id);
    Optional<Protetor> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}