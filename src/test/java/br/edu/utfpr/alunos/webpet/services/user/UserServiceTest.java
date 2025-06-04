package br.edu.utfpr.alunos.webpet.services.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.mapper.UserMapper;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ONGRepository ongRepository;
    @Mock
    private ProtetorRepository protetorRepository;
    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, ongRepository, protetorRepository, userMapper);
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        String email = "test@test.com";
        User user = User.builder()
            .name("Test User")
            .email(email)
            .password("password")
            .celular("123456789")
            .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<BaseUser> result = userService.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // Given
        String email = "notfound@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(ongRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(protetorRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<BaseUser> result = userService.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetCurrentUserProfile() {
        // Given
        String email = "test@test.com";
        User user = User.builder()
            .name("Test User")
            .email(email)
            .password("password")
            .celular("123456789")
            .build();
        user.setActive(true);
        
        UserResponseDTO expectedResponse = new UserResponseDTO(
            "1", email, "Test User", UserType.USER, email, true
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(expectedResponse);

        // When
        UserResponseDTO result = userService.getCurrentUserProfile(email);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.displayName()).isEqualTo("Test User");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForProfile() {
        // Given
        String email = "notfound@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(ongRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(protetorRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getCurrentUserProfile(email))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Usuário não encontrado");
    }

    @Test
    void shouldThrowExceptionWhenAccountIsDeactivated() {
        // Given
        String email = "test@test.com";
        User user = User.builder()
            .name("Test User")
            .email(email)
            .password("password")
            .celular("123456789")
            .build();
        user.setActive(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userService.getCurrentUserProfile(email))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Conta desativada");
    }

    @Test
    void shouldDeactivateUser() {
        // Given
        String email = "test@test.com";
        User user = User.builder()
            .name("Test User")
            .email(email)
            .password("password")
            .celular("123456789")
            .build();
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        userService.deactivateUser(email);

        // Then
        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void shouldActivateUser() {
        // Given
        String email = "test@test.com";
        User user = User.builder()
            .name("Test User")
            .email(email)
            .password("password")
            .celular("123456789")
            .build();
        user.setActive(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        userService.activateUser(email);

        // Then
        assertThat(user.isActive()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void shouldCheckIfUserIsActive() {
        // Given
        String email = "test@test.com";
        User user = User.builder()
            .name("Test User")
            .email(email)
            .password("password")
            .celular("123456789")
            .build();
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        boolean isActive = userService.isUserActive(email);

        // Then
        assertThat(isActive).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserNotFoundForActiveCheck() {
        // Given
        String email = "notfound@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(ongRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(protetorRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean isActive = userService.isUserActive(email);

        // Then
        assertThat(isActive).isFalse();
    }

    @Test
    void shouldThrowExceptionForNullEmail() {
        // When & Then
        assertThatThrownBy(() -> userService.getCurrentUserProfile(null))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldGetUserById() {
        // Given
        String id = "user-id-123";
        User user = User.builder()
            .name("Test User")
            .email("test@test.com")
            .password("password")
            .celular("123456789")
            .build();
        
        UserResponseDTO expectedResponse = new UserResponseDTO(
            id, "test@test.com", "Test User", UserType.USER, "test@test.com", true
        );

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(expectedResponse);

        // When
        UserResponseDTO result = userService.getUserById(id);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        String id = "nonexistent-id";
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        when(ongRepository.findById(id)).thenReturn(Optional.empty());
        when(protetorRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(id))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Usuário não encontrado");
    }
}