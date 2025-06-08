package br.edu.utfpr.alunos.webpet.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;

@NoRepositoryBean
public interface BaseUserRepository<T extends BaseUser> extends JpaRepository<T, String> {
    Optional<T> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<T> findByIdAndActiveTrue(String id);
    List<T> findByUserTypeAndActiveTrue(UserType userType);
    Page<T> findByUserTypeAndActiveTrue(UserType userType, Pageable pageable);
}