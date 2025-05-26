package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.protetor.Protetor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProtetorRepository extends JpaRepository<Protetor, String> {
    Optional<Protetor> findByEmail(String email);
    Optional<Protetor> findByCpf(String cpf);
}
