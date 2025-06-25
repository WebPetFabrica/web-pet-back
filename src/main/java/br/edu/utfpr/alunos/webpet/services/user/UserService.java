package br.edu.utfpr.alunos.webpet.services.user;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.AuthenticationException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;

    public UserResponseDTO getCurrentUserProfile(String email) {
        BaseUser user = findUserByEmail(email)
            .orElseThrow(() -> new AuthenticationException(ErrorCode.USER_NOT_FOUND));
        
        if (!user.getActive()) {
            throw new AuthenticationException(ErrorCode.AUTH_ACCOUNT_INACTIVE);
        }
        
        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getUserType(),
            user.getUsername(),
            user.getActive()
        );
    }
    
    private Optional<BaseUser> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(user -> (BaseUser) user)
            .or(() -> ongRepository.findByEmail(email).map(ong -> (BaseUser) ong))
            .or(() -> protetorRepository.findByEmail(email).map(protetor -> (BaseUser) protetor));
    }
}