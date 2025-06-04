package br.edu.utfpr.alunos.webpet.services.user;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;

import java.util.Optional;

public interface UserService {
    Optional<BaseUser> findByEmail(String email);
    UserResponseDTO getCurrentUserProfile(String email);
    UserResponseDTO getUserById(String id);
    void deactivateUser(String email);
    void activateUser(String email);
    boolean isUserActive(String email);
}