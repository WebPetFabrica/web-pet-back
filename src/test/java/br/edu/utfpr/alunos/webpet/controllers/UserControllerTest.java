package br.edu.utfpr.alunos.webpet.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import br.edu.utfpr.alunos.webpet.services.user.UserService;
import br.edu.utfpr.alunos.webpet.dto.user.UserResponseDTO;
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
        UserResponseDTO sampleUser = new UserResponseDTO("1", "testuser", "test@user.com", "Sample bio", "http://avatar.url", "1234567890");
        when(userService.getUserProfile("test@user.com")).thenReturn(sampleUser);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@user.com"))
                .andExpect(jsonPath("$.bio").value("Sample bio"))
                .andExpect(jsonPath("$.avatarUrl").value("http://avatar.url"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"));
    }

    @Test
    void shouldReturnUnauthorizedWhenNoUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="inactive@user.com", roles={"USER"})
    void shouldReturnForbiddenWhenUserIsInactive() throws Exception {
        when(userService.getUserProfile(anyString())).thenThrow(new AuthenticationException("Utilizador inativo ou não autorizado"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }
}