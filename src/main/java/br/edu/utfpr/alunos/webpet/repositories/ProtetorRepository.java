package br.edu.utfpr.alunos.webpet.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.user.Protetor;

@Repository
public interface ProtetorRepository extends BaseUserRepository<Protetor> {
    Optional<Protetor> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}