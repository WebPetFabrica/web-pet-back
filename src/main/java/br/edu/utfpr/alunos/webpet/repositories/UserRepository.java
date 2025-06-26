package br.edu.utfpr.alunos.webpet.repositories;

import java.util.Optional;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.edu.utfpr.alunos.webpet.domain.user.User;

import java.util.List;
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findAllByUserType(UserType userType);
    
    boolean existsByEmail(String email);
    Optional<User> findByIdAndActiveTrue(String id);
}