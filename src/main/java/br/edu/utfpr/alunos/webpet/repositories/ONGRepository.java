package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.ong.ONG;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ONGRepository extends JpaRepository<ONG, String> {
    Optional<ONG> findByEmail(String email);
    Optional<ONG> findByCnpj(String cnpj);
}
