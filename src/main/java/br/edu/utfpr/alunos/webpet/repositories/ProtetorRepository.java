package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProtetorRepository extends JpaRepository<Protetor, String> {
    Optional<Protetor> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}