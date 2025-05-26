package br.edu.utfpr.alunos.webpet.repositories;

import org.springframework.stereotype.Repository;

import br.edu.utfpr.alunos.webpet.domain.user.User;

@Repository
public interface UserRepository extends BaseUserRepository<User> {
}