package br.edu.utfpr.alunos.webpet.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.utfpr.alunos.webpet.domain.donation.TipoDoacao;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class DonationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldCreateDonationSuccessfully() throws Exception {
        // Given
        DonationCreateRequestDTO createRequest = new DonationCreateRequestDTO(
            new BigDecimal("100.00"),
            TipoDoacao.MONETARIA,
            "Doação para ajudar os animais",
            "João Silva",
            "joao@email.com",
            "11999999999",
            "some-beneficiario-id"
        );

        // When & Then
        mockMvc.perform(post("/doacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valor").value(100.00))
                .andExpect(jsonPath("$.tipoDoacao").value("MONETARIA"))
                .andExpect(jsonPath("$.nomeDoador").value("João Silva"));
    }

    @Test
    void shouldReturnBadRequestForInvalidDonationData() throws Exception {
        // Given
        DonationCreateRequestDTO createRequest = new DonationCreateRequestDTO(
            BigDecimal.ZERO, // Valor inválido
            null, // Tipo inválido
            "Doação para ajudar os animais",
            "", // Nome vazio
            "email-inválido", // Email inválido
            "11999999999",
            "some-beneficiario-id"
        );

        // When & Then
        mockMvc.perform(post("/doacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetDonationById() throws Exception {
        // When & Then
        mockMvc.perform(get("/doacoes/{id}", "some-donation-id"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnNotFoundForInvalidDonationId() throws Exception {
        // When & Then
        mockMvc.perform(get("/doacoes/{id}", "invalid-donation-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListDonationsWithPagination() throws Exception {
        // When & Then
        mockMvc.perform(get("/doacoes")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void shouldListDonationsWithFilters() throws Exception {
        // When & Then
        mockMvc.perform(get("/doacoes")
                .param("beneficiarioId", "some-beneficiario-id")
                .param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldSearchDonations() throws Exception {
        // When & Then
        mockMvc.perform(get("/doacoes")
                .param("search", "João"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldGetDonationsByBeneficiario() throws Exception {
        // When & Then
        mockMvc.perform(get("/doacoes/beneficiario/{beneficiarioId}", "some-beneficiario-id"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldGetDonationStats() throws Exception {
        // When & Then
        mockMvc.perform(get("/doacoes/beneficiario/{beneficiarioId}/stats", "some-beneficiario-id"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalValue").exists())
                .andExpect(jsonPath("$.totalCount").exists());
    }

    @Test
    void shouldProcessDonation() throws Exception {
        // When & Then
        mockMvc.perform(patch("/doacoes/{id}/processar", "some-donation-id")
                .param("transactionId", "TXN-123456"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMarkDonationAsFailed() throws Exception {
        // When & Then
        mockMvc.perform(patch("/doacoes/{id}/falha", "some-donation-id"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCancelDonation() throws Exception {
        // When & Then
        mockMvc.perform(patch("/doacoes/{id}/cancelar", "some-donation-id"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDonationMetadata() throws Exception {
        // Test tipos endpoint
        mockMvc.perform(get("/doacoes/metadata/tipos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        // Test status endpoint
        mockMvc.perform(get("/doacoes/metadata/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnBadRequestForMissingTransactionId() throws Exception {
        // When & Then
        mockMvc.perform(patch("/doacoes/{id}/processar", "some-donation-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateEmailFormat() throws Exception {
        // Given
        DonationCreateRequestDTO createRequest = new DonationCreateRequestDTO(
            new BigDecimal("100.00"),
            TipoDoacao.MONETARIA,
            "Doação para ajudar os animais",
            "João Silva",
            "email.sem.arroba", // Email inválido
            "11999999999",
            "some-beneficiario-id"
        );

        // When & Then
        mockMvc.perform(post("/doacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateMinimumDonationAmount() throws Exception {
        // Given
        DonationCreateRequestDTO createRequest = new DonationCreateRequestDTO(
            new BigDecimal("-10.00"), // Valor negativo
            TipoDoacao.MONETARIA,
            "Doação inválida",
            "João Silva",
            "joao@email.com",
            "11999999999",
            "some-beneficiario-id"
        );

        // When & Then
        mockMvc.perform(post("/doacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }
}