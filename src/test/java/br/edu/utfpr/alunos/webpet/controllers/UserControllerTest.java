package br.edu.utfpr.alunos.webpet.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import br.edu.utfpr.alunos.webpet.services.user.UserService;
import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import br.edu.utfpr.alunos.webpet.infra.exception.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username="test@user.com", roles={"USER"})
    void shouldReturnUserProfileForAuthenticatedUser() throws Exception {
        // Prepara um DTO de exemplo para o mock retornar
        UserResponseDTO sampleUser = new UserResponseDTO("1", "test@user.com", "testuser", UserType.USER, "test@user.com", true);
        when(userService.getCurrentUserProfile("test@user.com")).thenReturn(sampleUser);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.displayName").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@user.com"))
                .andExpect(jsonPath("$.userType").value("USER"))
                .andExpect(jsonPath("$.identifier").value("test@user.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnUnauthorizedWhenNoUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="inactive@user.com", roles={"USER"})
    void shouldReturnForbiddenWhenUserIsInactive() throws Exception {
        when(userService.getCurrentUserProfile(anyString())).thenThrow(new AuthenticationException(ErrorCode.AUTH_ACCOUNT_INACTIVE));

        mockMvc.perform(get("/user"))
                .andExpect(status().isForbidden());
    }
}