package br.edu.utfpr.alunos.webpet.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;

@NoRepositoryBean
public interface BaseUserRepository<T extends BaseUser> extends JpaRepository<T, String> {
    Optional<T> findByEmail(String email);
    boolean existsByEmail(String email);
}