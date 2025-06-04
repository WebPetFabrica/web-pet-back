package br.edu.utfpr.alunos.webpet.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.RegisterRequestDTO;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "api.security.token.secret=test-secret-key-for-testing-purposes-must-be-at-least-64-characters-long",
    "app.cors.allowed-origins=http://localhost:3000"
})
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequestDTO registerDTO = new RegisterRequestDTO(
            "João Silva", "joao@test.com", "password123"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRegisterONGSuccessfully() throws Exception {
        ONGRegisterDTO ongDTO = new ONGRegisterDTO(
            "12345678000190", "ONG Teste", "ong@test.com", "41999999999", "password123"
        );

        mockMvc.perform(post("/auth/register/ong")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ongDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ONG Teste"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRegisterProtetorSuccessfully() throws Exception {
        ProtetorRegisterDTO protetorDTO = new ProtetorRegisterDTO(
            "Maria Santos", "12345678901", "maria@test.com", "41888888888", "password123"
        );

        mockMvc.perform(post("/auth/register/protetor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(protetorDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maria Santos"))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // First register a user
        RegisterRequestDTO registerDTO = new RegisterRequestDTO(
            "Test User", "login@test.com", "password123"
        );
        
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)));

        // Then login
        LoginRequestDTO loginDTO = new LoginRequestDTO("login@test.com", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        RegisterRequestDTO registerDTO = new RegisterRequestDTO(
            "Test User", "invalid-email", "password123"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email inválido"));
    }

    @Test
    void shouldReturnBadRequestForBlankFields() throws Exception {
        RegisterRequestDTO registerDTO = new RegisterRequestDTO("", "", "");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Nome é obrigatório"))
                .andExpect(jsonPath("$.email").value("Email é obrigatório"))
                .andExpect(jsonPath("$.password").value("Senha é obrigatória"));
    }

    @Test
    void shouldReturnBadRequestForInvalidCNPJ() throws Exception {
        ONGRegisterDTO ongDTO = new ONGRegisterDTO(
            "invalid-cnpj", "ONG Teste", "ong@test.com", "41999999999", "password123"
        );

        mockMvc.perform(post("/auth/register/ong")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ongDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cnpj").value("CNPJ inválido"));
    }

    @Test
    void shouldReturnBadRequestForInvalidCPF() throws Exception {
        ProtetorRegisterDTO protetorDTO = new ProtetorRegisterDTO(
            "Maria Santos", "invalid-cpf", "maria@test.com", "41888888888", "password123"
        );

        mockMvc.perform(post("/auth/register/protetor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(protetorDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cpf").value("CPF inválido"));
    }

    @Test
    void shouldReturnBadRequestForDuplicateEmail() throws Exception {
        RegisterRequestDTO registerDTO = new RegisterRequestDTO(
            "Test User", "duplicate@test.com", "password123"
        );

        // Register first user
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        // Try to register with same email
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já cadastrado"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        LoginRequestDTO loginDTO = new LoginRequestDTO("nonexistent@test.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}