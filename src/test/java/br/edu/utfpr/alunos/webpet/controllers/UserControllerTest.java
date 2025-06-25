package br.edu.utfpr.alunos.webpet.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import br.edu.utfpr.alunos.webpet.services.user.UserService;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
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
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.bio").exists())
                .andExpect(jsonPath("$.avatarUrl").exists())
                .andExpect(jsonPath("$.phoneNumber").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="inactive@user.com", roles={"USER"})
    void shouldReturnForbiddenWhenUserIsInactive() throws Exception {
        when(userService.getUserProfile(anyString())).thenThrow(new RuntimeException("Utilizador inativo"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isInternalServerError());
    }
}