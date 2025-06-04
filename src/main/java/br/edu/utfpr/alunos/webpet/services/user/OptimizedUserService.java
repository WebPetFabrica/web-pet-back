package br.edu.utfpr.alunos.webpet.services.user;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.infra.exception.SystemException;
import br.edu.utfpr.alunos.webpet.infra.exception.ValidationException;
import br.edu.utfpr.alunos.webpet.infra.logging.ExceptionLogger;
import br.edu.utfpr.alunos.webpet.repositories.OptimizedONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.OptimizedProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.OptimizedUserRepository;
import br.edu.utfpr.alunos.webpet.repositories.projections.UserProjections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimizedUserService {
    
    private final OptimizedUserRepository userRepository;
    private final OptimizedONGRepository ongRepository;
    private final OptimizedProtetorRepository protetorRepository;
    private final ExceptionLogger exceptionLogger;

    @Cacheable(value = "activeUsers", key = "#email")
    public Optional<BaseUser> findActiveUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            return userRepository.findActiveByEmailWithType(email)
                    .map(BaseUser.class::cast)
                    .or(() -> ongRepository.findActiveByEmailWithType(email)
                            .map(BaseUser.class::cast))
                    .or(() -> protetorRepository.findActiveByEmailWithType(email)
                            .map(BaseUser.class::cast));
        } catch (Exception e) {
            exceptionLogger.logSystemError("OPTIMIZED_USER_LOOKUP", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    public Page<UserProjections.BasicUserProjection> getAllActiveUsers(Pageable pageable) {
        try {
            return userRepository.findAllActiveBasic(pageable);
        } catch (Exception e) {
            exceptionLogger.logSystemError("GET_ALL_USERS", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    public Page<UserProjections.UserSummaryProjection> getUsersSummary(Pageable pageable) {
        try {
            return userRepository.findAllActiveSummary(pageable);
        } catch (Exception e) {
            exceptionLogger.logSystemError("GET_USERS_SUMMARY", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    public Page<UserProjections.ONGProjection> getAllActiveONGs(Pageable pageable) {
        try {
            return ongRepository.findAllActiveONGs(pageable);
        } catch (Exception e) {
            exceptionLogger.logSystemError("GET_ALL_ONGS", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    public Page<UserProjections.ProtetorProjection> getAllActiveProtetores(Pageable pageable) {
        try {
            return protetorRepository.findAllActiveProtetores(pageable);
        } catch (Exception e) {
            exceptionLogger.logSystemError("GET_ALL_PROTETORES", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    public Page<UserProjections.BasicUserProjection> searchUsers(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            exceptionLogger.logValidationError("searchTerm", "null/empty", "Required field");
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD);
        }
        
        try {
            return userRepository.searchUsers(searchTerm.trim(), pageable);
        } catch (Exception e) {
            exceptionLogger.logSystemError("SEARCH_USERS", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    public Page<UserProjections.ONGProjection> searchONGs(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD);
        }
        
        try {
            return ongRepository.searchONGs(searchTerm.trim(), pageable);
        } catch (Exception e) {
            exceptionLogger.logSystemError("SEARCH_ONGS", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    public Page<UserProjections.ProtetorProjection> searchProtetores(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD);
        }
        
        try {
            return protetorRepository.searchProtetores(searchTerm.trim(), pageable);
        } catch (Exception e) {
            exceptionLogger.logSystemError("SEARCH_PROTETORES", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    @Cacheable(value = "userStats")
    public UserStatsDTO getUserStats() {
        try {
            long totalUsers = userRepository.countActiveUsers();
            long totalONGs = ongRepository.countActiveONGs();
            long totalProtetores = protetorRepository.countActiveProtetores();
            Long totalCapacidade = protetorRepository.getTotalCapacidadeAcolhimento();
            
            return new UserStatsDTO(totalUsers, totalONGs, totalProtetores, 
                                  totalCapacidade != null ? totalCapacidade : 0L);
        } catch (Exception e) {
            exceptionLogger.logSystemError("GET_USER_STATS", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    @Cacheable(value = "recentUsers", key = "#hours")
    public List<BaseUser> getRecentActiveUsers(int hours) {
        if (hours <= 0 || hours > 168) { // max 7 days
            throw new ValidationException(ErrorCode.VALIDATION_REQUIRED_FIELD, "Invalid hours range");
        }
        
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            return userRepository.findRecentActiveUsers(since)
                    .stream()
                    .map(BaseUser.class::cast)
                    .toList();
        } catch (Exception e) {
            exceptionLogger.logSystemError("GET_RECENT_USERS", "Database error", e);
            throw new SystemException(ErrorCode.SYSTEM_DATABASE_ERROR, e);
        }
    }

    public record UserStatsDTO(
        long totalUsers,
        long totalONGs,
        long totalProtetores,
        long totalCapacidadeAcolhimento
    ) {}
}