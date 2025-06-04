package br.edu.utfpr.alunos.webpet.services.user;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.user.*;
import br.edu.utfpr.alunos.webpet.infra.exception.*;
import br.edu.utfpr.alunos.webpet.infra.logging.ExceptionLogger;
import br.edu.utfpr.alunos.webpet.mapper.UserMapper;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final UserMapper userMapper;
    private final ExceptionLogger exceptionLogger;

    @Override
    @Cacheable(value = "users", key = "#email")
    public Optional<BaseUser> findByEmail(String email) {
        String correlationId = MDC.get("correlationId");
        
        if (email == null || email.trim().isEmpty()) {
            log.warn("Email validation failed - null/empty [correlationId: {}]", correlationId);
            exceptionLogger.logValidationError("email", "null/empty", "Required field");
            return Optional.empty();
        }
        
        try {
            log.debug("Looking up user by email: {} [correlationId: {}]", email, correlationId);
            Optional<BaseUser> user = userRepository.findByEmail(email)
                    .map(BaseUser.class::cast)
                    .or(() -> ongRepository.findByEmail(email)
                            .map(BaseUser.class::cast))
                    .or(() -> protetorRepository.findByEmail(email)
                            .map(BaseUser.class::cast));
            
            if (user.isPresent()) {
                log.debug("User found by email: {} [correlationId: {}]", email, correlationId);
            } else {
                log.debug("User not found by email: {} [correlationId: {}]", email, correlationId);
            }
            
            return user;
        } catch (Exception e) {
            log.error("Database error during findByEmail for: {} [correlationId: {}]", 
                email, correlationId, e);
            exceptionLogger.logSystemError("USER_LOOKUP", "Database error during findByEmail", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "userProfiles", key = "#email")
    public UserResponseDTO getCurrentUserProfile(String email) {
        String correlationId = MDC.get("correlationId");
        log.info("Getting current user profile for email: {} [correlationId: {}]", email, correlationId);
        
        if (email == null || email.trim().isEmpty()) {
            log.warn("Email validation failed - null/empty [correlationId: {}]", correlationId);
            exceptionLogger.logValidationError("email", "null/empty", "Required field");
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD);
        }
        
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email: {} [correlationId: {}]", email, correlationId);
                    exceptionLogger.logBusinessError("GET_USER_PROFILE", "User not found", null);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND.getMessage());
                });
        
        if (!user.isActive()) {
            log.warn("Account inactive for user: {} [correlationId: {}]", email, correlationId);
            exceptionLogger.logBusinessError("GET_USER_PROFILE", "Account inactive", user.getId());
            throw new AuthenticationException(ErrorCode.AUTH_ACCOUNT_INACTIVE);
        }
        
        try {
            UserResponseDTO response = userMapper.toResponseDTO(user);
            log.info("User profile retrieved successfully for: {} [correlationId: {}]", email, correlationId);
            return response;
        } catch (Exception e) {
            log.error("Error mapping user to DTO for: {} [correlationId: {}]", email, correlationId, e);
            exceptionLogger.logSystemError("USER_MAPPING", "Error mapping user to DTO", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "userProfiles", key = "#id")
    public UserResponseDTO getUserById(String id) {
        String correlationId = MDC.get("correlationId");
        log.info("Getting user by ID: {} [correlationId: {}]", id, correlationId);
        
        if (id == null || id.trim().isEmpty()) {
            log.warn("ID validation failed - null/empty [correlationId: {}]", correlationId);
            exceptionLogger.logValidationError("id", "null/empty", "Required field");
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD);
        }
        
        BaseUser user = findUserById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for ID: {} [correlationId: {}]", id, correlationId);
                    exceptionLogger.logBusinessError("GET_USER_BY_ID", "User not found", id);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND.getMessage());
                });
        
        try {
            UserResponseDTO response = userMapper.toResponseDTO(user);
            log.info("User retrieved successfully by ID: {} [correlationId: {}]", id, correlationId);
            return response;
        } catch (Exception e) {
            log.error("Error mapping user to DTO for ID: {} [correlationId: {}]", id, correlationId, e);
            exceptionLogger.logSystemError("USER_MAPPING", "Error mapping user to DTO", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    public UserDetailedResponseDTO getUserDetailedProfile(String email) {
        String correlationId = MDC.get("correlationId");
        log.info("Getting detailed user profile for: {} [correlationId: {}]", email, correlationId);
        
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for detailed profile: {} [correlationId: {}]", email, correlationId);
                    exceptionLogger.logBusinessError("GET_DETAILED_PROFILE", "User not found", null);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND.getMessage());
                });
        
        if (!user.isActive()) {
            log.warn("Account inactive for detailed profile: {} [correlationId: {}]", email, correlationId);
            exceptionLogger.logBusinessError("GET_DETAILED_PROFILE", "Account inactive", user.getId());
            throw new AuthenticationException(ErrorCode.AUTH_ACCOUNT_INACTIVE);
        }
        
        try {
            UserDetailedResponseDTO response = userMapper.toDetailedResponseDTO(user);
            log.info("Detailed profile retrieved successfully for: {} [correlationId: {}]", email, correlationId);
            return response;
        } catch (Exception e) {
            log.error("Error mapping detailed user to DTO for: {} [correlationId: {}]", email, correlationId, e);
            exceptionLogger.logSystemError("USER_MAPPING", "Error mapping detailed user to DTO", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    public ONGResponseDTO getONGProfile(String email) {
        String correlationId = MDC.get("correlationId");
        log.info("Getting ONG profile for: {} [correlationId: {}]", email, correlationId);
        
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("ONG not found for: {} [correlationId: {}]", email, correlationId);
                    exceptionLogger.logBusinessError("GET_ONG_PROFILE", "ONG not found", null);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND.getMessage());
                });
        
        if (!(user instanceof ONG ong)) {
            log.warn("User is not ONG type for: {} [correlationId: {}]", email, correlationId);
            exceptionLogger.logBusinessError("GET_ONG_PROFILE", "User is not ONG", user.getId());
            throw new ValidationException(ErrorCode.VALIDATION_INVALID_USER_TYPE, "Usuário não é uma ONG");
        }
        
        try {
            ONGResponseDTO response = userMapper.toONGResponseDTO(ong);
            log.info("ONG profile retrieved successfully for: {} [correlationId: {}]", email, correlationId);
            return response;
        } catch (Exception e) {
            log.error("Error mapping ONG to DTO for: {} [correlationId: {}]", email, correlationId, e);
            exceptionLogger.logSystemError("USER_MAPPING", "Error mapping ONG to DTO", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    public ProtetorResponseDTO getProtetorProfile(String email) {
        String correlationId = MDC.get("correlationId");
        log.info("Getting Protetor profile for: {} [correlationId: {}]", email, correlationId);
        
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Protetor not found for: {} [correlationId: {}]", email, correlationId);
                    exceptionLogger.logBusinessError("GET_PROTETOR_PROFILE", "Protetor not found", null);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND.getMessage());
                });
        
        if (!(user instanceof Protetor protetor)) {
            log.warn("User is not Protetor type for: {} [correlationId: {}]", email, correlationId);
            exceptionLogger.logBusinessError("GET_PROTETOR_PROFILE", "User is not Protetor", user.getId());
            throw new ValidationException(ErrorCode.VALIDATION_INVALID_USER_TYPE, "Usuário não é um protetor");
        }
        
        try {
            ProtetorResponseDTO response = userMapper.toProtetorResponseDTO(protetor);
            log.info("Protetor profile retrieved successfully for: {} [correlationId: {}]", email, correlationId);
            return response;
        } catch (Exception e) {
            log.error("Error mapping Protetor to DTO for: {} [correlationId: {}]", email, correlationId, e);
            exceptionLogger.logSystemError("USER_MAPPING", "Error mapping Protetor to DTO", e);
            throw new SystemException(ErrorCode.SYSTEM_INTERNAL_ERROR, e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userProfiles"}, key = "#email")
    public void deactivateUser(String email) {
        String correlationId = MDC.get("correlationId");
        log.info("Deactivating user: {} [correlationId: {}]", email, correlationId);
        
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for deactivation: {} [correlationId: {}]", email, correlationId);
                    exceptionLogger.logBusinessError("DEACTIVATE_USER", "User not found", null);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND.getMessage());
                });
        
        try {
            user.setActive(false);
            saveUser(user);
            log.info("User deactivated successfully: {} [correlationId: {}]", email, correlationId);
        } catch (Exception e) {
            log.error("Error deactivating user: {} [correlationId: {}]", email, correlationId, e);
            exceptionLogger.logSystemError("USER_DEACTIVATION", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userProfiles"}, key = "#email")
    public void activateUser(String email) {
        String correlationId = MDC.get("correlationId");
        log.info("Activating user: {} [correlationId: {}]", email, correlationId);
        
        BaseUser user = findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for activation: {} [correlationId: {}]", email, correlationId);
                    exceptionLogger.logBusinessError("ACTIVATE_USER", "User not found", null);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND.getMessage());
                });
        
        try {
            user.setActive(true);
            saveUser(user);
            log.info("User activated successfully: {} [correlationId: {}]", email, correlationId);
        } catch (Exception e) {
            log.error("Error activating user: {} [correlationId: {}]", email, correlationId, e);
            exceptionLogger.logSystemError("USER_ACTIVATION", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    @Override
    public boolean isUserActive(String email) {
        String correlationId = MDC.get("correlationId");
        log.debug("Checking user active status for: {} [correlationId: {}]", email, correlationId);
        
        try {
            return findByEmail(email)
                    .map(BaseUser::isActive)
                    .orElse(false);
        } catch (SystemException e) {
            // Re-throw system exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error checking user status for: {} [correlationId: {}]", email, correlationId, e);
            exceptionLogger.logSystemError("USER_STATUS_CHECK", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    private Optional<BaseUser> findUserById(String id) {
        String correlationId = MDC.get("correlationId");
        
        try {
            log.debug("Looking up user by ID: {} [correlationId: {}]", id, correlationId);
            return userRepository.findById(id)
                    .map(BaseUser.class::cast)
                    .or(() -> ongRepository.findById(id)
                            .map(BaseUser.class::cast))
                    .or(() -> protetorRepository.findById(id)
                            .map(BaseUser.class::cast));
        } catch (Exception e) {
            log.error("Database error during findUserById for: {} [correlationId: {}]", id, correlationId, e);
            exceptionLogger.logSystemError("USER_LOOKUP_BY_ID", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    private void saveUser(BaseUser user) {
        String correlationId = MDC.get("correlationId");
        
        try {
            log.debug("Saving user type: {} [correlationId: {}]", user.getUserType(), correlationId);
            switch (user.getUserType()) {
                case USER -> userRepository.save((User) user);
                case ONG -> ongRepository.save((ONG) user);
                case PROTETOR -> protetorRepository.save((Protetor) user);
            }
            log.debug("User saved successfully [correlationId: {}]", correlationId);
        } catch (Exception e) {
            log.error("Database error during saveUser [correlationId: {}]", correlationId, e);
            exceptionLogger.logSystemError("USER_SAVE", "Database error during save", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }
}