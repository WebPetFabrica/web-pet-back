package br.edu.utfpr.alunos.webpet.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.dto.pet.PetCreateRequestDTO;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class PetControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Given
        PetCreateRequestDTO createRequest = new PetCreateRequestDTO(
            "Rex",
            Especie.CACHORRO,
            "Labrador",
            Genero.MACHO,
            Porte.GRANDE,
            LocalDate.of(2020, 1, 15),
            "Cão muito carinhoso",
            null
        );

        // When & Then
        mockMvc.perform(post("/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldCreatePetWhenAuthenticated() throws Exception {
        // Given
        PetCreateRequestDTO createRequest = new PetCreateRequestDTO(
            "Rex",
            Especie.CACHORRO,
            "Labrador",
            Genero.MACHO,
            Porte.GRANDE,
            LocalDate.of(2020, 1, 15),
            "Cão muito carinhoso",
            null
        );

        // When & Then
        mockMvc.perform(post("/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Rex"))
                .andExpect(jsonPath("$.especie").value("CACHORRO"))
                .andExpect(jsonPath("$.raca").value("Labrador"));
    }

    @Test
    void shouldReturnBadRequestForInvalidPetData() throws Exception {
        // Given
        PetCreateRequestDTO createRequest = new PetCreateRequestDTO(
            "", // Nome inválido (vazio)
            null, // Espécie inválida (null)
            "Labrador",
            Genero.MACHO,
            Porte.GRANDE,
            LocalDate.now().plusDays(1), // Data futura (inválida)
            "Cão muito carinhoso",
            null
        );

        // When & Then
        mockMvc.perform(post("/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetPetById() throws Exception {
        // When & Then
        mockMvc.perform(get("/pets/{id}", "some-pet-id"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnNotFoundForInvalidPetId() throws Exception {
        // When & Then
        mockMvc.perform(get("/pets/{id}", "invalid-pet-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListPetsWithPagination() throws Exception {
        // When & Then
        mockMvc.perform(get("/pets")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void shouldListPetsWithFilters() throws Exception {
        // When & Then
        mockMvc.perform(get("/pets")
                .param("especie", "CACHORRO")
                .param("genero", "MACHO")
                .param("porte", "GRANDE")
                .param("idadeMinima", "2")
                .param("idadeMaxima", "8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldSearchPets() throws Exception {
        // When & Then
        mockMvc.perform(get("/pets")
                .param("search", "Rex"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldGetMyPets() throws Exception {
        // When & Then
        mockMvc.perform(get("/pets/my-pets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldGetPetMetadata() throws Exception {
        // Test especies endpoint
        mockMvc.perform(get("/pets/metadata/especies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        // Test portes endpoint
        mockMvc.perform(get("/pets/metadata/portes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        // Test generos endpoint
        mockMvc.perform(get("/pets/metadata/generos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        // Test racas endpoint
        mockMvc.perform(get("/pets/metadata/racas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldUpdatePetStatus() throws Exception {
        String petId = "some-pet-id";

        // Test mark as adopted
        mockMvc.perform(patch("/pets/{id}/status/adotado", petId))
                .andExpect(status().isOk());

        // Test mark as available
        mockMvc.perform(patch("/pets/{id}/status/disponivel", petId))
                .andExpect(status().isOk());

        // Test mark as unavailable
        mockMvc.perform(patch("/pets/{id}/status/indisponivel", petId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedForStatusUpdateWithoutAuth() throws Exception {
        String petId = "some-pet-id";

        mockMvc.perform(patch("/pets/{id}/status/adotado", petId))
                .andExpect(status().isUnauthorized());
    }
}