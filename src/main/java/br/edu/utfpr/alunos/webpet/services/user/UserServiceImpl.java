package br.edu.utfpr.alunos.webpet.services.user;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.user.*;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.mapper.UserMapper;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final UserMapper userMapper;

    @Override
    @Cacheable(value = "users", key = "#email")
    public Optional<BaseUser> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return userRepository.findByEmail(email)
                .map(BaseUser.class::cast)
                .or(() -> ongRepository.findByEmail(email)
                        .map(BaseUser.class::cast))
                .or(() -> protetorRepository.findByEmail(email)
                        .map(BaseUser.class::cast));
    }

    @Override
    @Cacheable(value = "userProfiles", key = "#email")
    public UserResponseDTO getCurrentUserProfile(String email) {
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        
        if (!user.isActive()) {
            throw new BusinessException("Conta desativada");
        }
        
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Cacheable(value = "userProfiles", key = "#id")
    public UserResponseDTO getUserById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException("ID do usuário é obrigatório");
        }
        
        BaseUser user = findUserById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        
        return userMapper.toResponseDTO(user);
    }

    public UserDetailedResponseDTO getUserDetailedProfile(String email) {
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        
        if (!user.isActive()) {
            throw new BusinessException("Conta desativada");
        }
        
        return userMapper.toDetailedResponseDTO(user);
    }

    public ONGResponseDTO getONGProfile(String email) {
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> new BusinessException("ONG não encontrada"));
        
        if (!(user instanceof ONG ong)) {
            throw new BusinessException("Usuário não é uma ONG");
        }
        
        return userMapper.toONGResponseDTO(ong);
    }

    public ProtetorResponseDTO getProtetorProfile(String email) {
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> new BusinessException("Protetor não encontrado"));
        
        if (!(user instanceof Protetor protetor)) {
            throw new BusinessException("Usuário não é um protetor");
        }
        
        return userMapper.toProtetorResponseDTO(protetor);
    }

    @Override
    @Transactional
    public void deactivateUser(String email) {
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        
        user.setActive(false);
        saveUser(user);
    }

    @Override
    @Transactional
    public void activateUser(String email) {
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        
        user.setActive(true);
        saveUser(user);
    }

    @Override
    public boolean isUserActive(String email) {
        return findByEmail(email)
                .map(BaseUser::isActive)
                .orElse(false);
    }

    private Optional<BaseUser> findUserById(String id) {
        return userRepository.findById(id)
                .map(BaseUser.class::cast)
                .or(() -> ongRepository.findById(id)
                        .map(BaseUser.class::cast))
                .or(() -> protetorRepository.findById(id)
                        .map(BaseUser.class::cast));
    }

    private void saveUser(BaseUser user) {
        switch (user.getUserType()) {
            case USER -> userRepository.save((User) user);
            case ONG -> ongRepository.save((ONG) user);
            case PROTETOR -> protetorRepository.save((Protetor) user);
        }
    }
}