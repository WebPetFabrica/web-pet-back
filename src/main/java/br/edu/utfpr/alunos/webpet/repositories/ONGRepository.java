package br.edu.utfpr.alunos.webpet.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.user.ONG;

@Repository
public interface ONGRepository extends BaseUserRepository<ONG> {
    Optional<ONG> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
}