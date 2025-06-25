package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ONGRepository extends JpaRepository<ONG, String> {
    Optional<ONG> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCnpj(String cnpj);
}