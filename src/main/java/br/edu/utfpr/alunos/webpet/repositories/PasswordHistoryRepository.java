package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.user.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, String> {
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(String userId);
}