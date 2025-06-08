package br.edu.utfpr.alunos.webpet.services.donation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.edu.utfpr.alunos.webpet.domain.donation.Donation;
import br.edu.utfpr.alunos.webpet.domain.donation.StatusDoacao;
import br.edu.utfpr.alunos.webpet.domain.donation.TipoDoacao;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.user.UserBasicResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.mapper.DonationMapper;
import br.edu.utfpr.alunos.webpet.repositories.BaseUserRepository;
import br.edu.utfpr.alunos.webpet.repositories.DonationRepository;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;
    @Mock
    private BaseUserRepository baseUserRepository;
    @Mock
    private DonationMapper donationMapper;

    private DonationServiceImpl donationService;

    private ONG beneficiario;
    private Donation donation;
    private DonationResponseDTO donationResponseDTO;

    @BeforeEach
    void setUp() {
        donationService = new DonationServiceImpl(donationRepository, baseUserRepository, donationMapper);
        
        beneficiario = ONG.builder()
            .cnpj("12.345.678/0001-90")
            .nomeOng("ONG Test")
            .email("ong@test.com")
            .password("password")
            .celular("11999999999")
            .build();
        beneficiario.setActive(true);
        
        donation = Donation.builder()
            .valor(new BigDecimal("100.00"))
            .tipoDoacao(TipoDoacao.MONETARIA)
            .mensagem("Doação para ajudar os animais")
            .nomeDoador("João Silva")
            .emailDoador("joao@email.com")
            .telefoneDoador("11999999999")
            .beneficiario(beneficiario)
            .build();
        
        donationResponseDTO = new DonationResponseDTO(
            "donation-id",
            new BigDecimal("100.00"),
            TipoDoacao.MONETARIA,
            "Doação para ajudar os animais",
            "João Silva",
            "joao@email.com",
            "11999999999",
            StatusDoacao.PENDENTE,
            null,
            new UserBasicResponseDTO("ong-id", "ONG Test", UserType.ONG),
            LocalDateTime.now(),
            null
        );
    }

    @Test
    void shouldCreateDonationSuccessfully() {
        // Given
        DonationCreateRequestDTO createRequest = new DonationCreateRequestDTO(
            new BigDecimal("100.00"),
            TipoDoacao.MONETARIA,
            "Doação para ajudar os animais",
            "João Silva",
            "joao@email.com",
            "11999999999",
            "ong-id"
        );
        
        when(baseUserRepository.findByIdAndActiveTrue("ong-id")).thenReturn(Optional.of(beneficiario));
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);
        when(donationMapper.toResponseDTO(donation)).thenReturn(donationResponseDTO);

        // When
        DonationResponseDTO result = donationService.createDonation(createRequest);

        // Then
        assertThat(result).isEqualTo(donationResponseDTO);
        verify(donationRepository).save(any(Donation.class));
    }

    @Test
    void shouldThrowExceptionWhenDonationAmountIsZero() {
        // Given
        DonationCreateRequestDTO createRequest = new DonationCreateRequestDTO(
            BigDecimal.ZERO,
            TipoDoacao.MONETARIA,
            "Doação inválida",
            "João Silva",
            "joao@email.com",
            "11999999999",
            "ong-id"
        );

        // When & Then
        assertThatThrownBy(() -> donationService.createDonation(createRequest))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DONATION_INVALID_AMOUNT);
    }

    @Test
    void shouldThrowExceptionWhenDonationAmountIsNegative() {
        // Given
        DonationCreateRequestDTO createRequest = new DonationCreateRequestDTO(
            new BigDecimal("-50.00"),
            TipoDoacao.MONETARIA,
            "Doação inválida",
            "João Silva",
            "joao@email.com",
            "11999999999",
            "ong-id"
        );

        // When & Then
        assertThatThrownBy(() -> donationService.createDonation(createRequest))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DONATION_INVALID_AMOUNT);
    }

    @Test
    void shouldThrowExceptionWhenBeneficiarioNotFound() {
        // Given
        DonationCreateRequestDTO createRequest = new DonationCreateRequestDTO(
            new BigDecimal("100.00"),
            TipoDoacao.MONETARIA,
            "Doação para ajudar os animais",
            "João Silva",
            "joao@email.com",
            "11999999999",
            "invalid-id"
        );
        
        when(baseUserRepository.findByIdAndActiveTrue("invalid-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> donationService.createDonation(createRequest))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void shouldFindDonationById() {
        // Given
        when(donationRepository.findById("donation-id")).thenReturn(Optional.of(donation));
        when(donationMapper.toResponseDTO(donation)).thenReturn(donationResponseDTO);

        // When
        Optional<DonationResponseDTO> result = donationService.findById("donation-id");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(donationResponseDTO);
    }

    @Test
    void shouldReturnEmptyWhenDonationNotFound() {
        // Given
        when(donationRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // When
        Optional<DonationResponseDTO> result = donationService.findById("invalid-id");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindDonationsByBeneficiario() {
        // Given
        Page<Donation> donationsPage = new PageImpl<>(List.of(donation));
        when(donationRepository.findByBeneficiarioId("ong-id", Pageable.unpaged())).thenReturn(donationsPage);
        when(donationMapper.toResponseDTO(donation)).thenReturn(donationResponseDTO);

        // When
        Page<DonationResponseDTO> result = donationService.findByBeneficiario("ong-id", Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(donationResponseDTO);
    }

    @Test
    void shouldFindDonationsByStatus() {
        // Given
        Page<Donation> donationsPage = new PageImpl<>(List.of(donation));
        when(donationRepository.findByStatusDoacao(StatusDoacao.PENDENTE, Pageable.unpaged())).thenReturn(donationsPage);
        when(donationMapper.toResponseDTO(donation)).thenReturn(donationResponseDTO);

        // When
        Page<DonationResponseDTO> result = donationService.findByStatus(StatusDoacao.PENDENTE, Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(donationResponseDTO);
    }

    @Test
    void shouldProcessarDoacaoSuccessfully() {
        // Given
        String transactionId = "TXN-123456";
        when(donationRepository.findById("donation-id")).thenReturn(Optional.of(donation));

        // When
        donationService.processarDoacao("donation-id", transactionId);

        // Then
        assertThat(donation.getStatusDoacao()).isEqualTo(StatusDoacao.PROCESSADA);
        assertThat(donation.getTransactionId()).isEqualTo(transactionId);
        assertThat(donation.getProcessadoEm()).isNotNull();
        verify(donationRepository).save(donation);
    }

    @Test
    void shouldThrowExceptionWhenProcessingAlreadyProcessedDonation() {
        // Given
        donation.marcarComoProcessada("OLD-TXN");
        when(donationRepository.findById("donation-id")).thenReturn(Optional.of(donation));

        // When & Then
        assertThatThrownBy(() -> donationService.processarDoacao("donation-id", "NEW-TXN"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DONATION_ALREADY_PROCESSED);
    }

    @Test
    void shouldMarcarComoFalhaSuccessfully() {
        // Given
        when(donationRepository.findById("donation-id")).thenReturn(Optional.of(donation));

        // When
        donationService.marcarComoFalha("donation-id");

        // Then
        assertThat(donation.getStatusDoacao()).isEqualTo(StatusDoacao.FALHA);
        assertThat(donation.getProcessadoEm()).isNotNull();
        verify(donationRepository).save(donation);
    }

    @Test
    void shouldCancelarDoacaoSuccessfully() {
        // Given
        when(donationRepository.findById("donation-id")).thenReturn(Optional.of(donation));

        // When
        donationService.cancelarDoacao("donation-id");

        // Then
        assertThat(donation.getStatusDoacao()).isEqualTo(StatusDoacao.CANCELADA);
        assertThat(donation.getProcessadoEm()).isNotNull();
        verify(donationRepository).save(donation);
    }

    @Test
    void shouldThrowExceptionWhenCancellingProcessedDonation() {
        // Given
        donation.marcarComoProcessada("TXN-123");
        when(donationRepository.findById("donation-id")).thenReturn(Optional.of(donation));

        // When & Then
        assertThatThrownBy(() -> donationService.cancelarDoacao("donation-id"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DONATION_ALREADY_PROCESSED);
    }

    @Test
    void shouldGetTotalDonationsByBeneficiario() {
        // Given
        BigDecimal totalValue = new BigDecimal("500.00");
        when(donationRepository.getTotalDonationsByBeneficiario("ong-id")).thenReturn(totalValue);

        // When
        BigDecimal result = donationService.getTotalDonationsByBeneficiario("ong-id");

        // Then
        assertThat(result).isEqualTo(totalValue);
    }

    @Test
    void shouldReturnZeroWhenNoTotalDonations() {
        // Given
        when(donationRepository.getTotalDonationsByBeneficiario("ong-id")).thenReturn(null);

        // When
        BigDecimal result = donationService.getTotalDonationsByBeneficiario("ong-id");

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void shouldGetCountDonationsByBeneficiario() {
        // Given
        when(donationRepository.getCountDonationsByBeneficiario("ong-id")).thenReturn(10L);

        // When
        Long result = donationService.getCountDonationsByBeneficiario("ong-id");

        // Then
        assertThat(result).isEqualTo(10L);
    }

    @Test
    void shouldGetAvailableTipos() {
        // When
        List<TipoDoacao> tipos = donationService.getAvailableTipos();

        // Then
        assertThat(tipos).containsExactly(TipoDoacao.values());
    }

    @Test
    void shouldGetAvailableStatus() {
        // When
        List<StatusDoacao> status = donationService.getAvailableStatus();

        // Then
        assertThat(status).containsExactly(StatusDoacao.values());
    }

    @Test
    void shouldSearchDonations() {
        // Given
        String searchTerm = "João";
        Page<Donation> donationsPage = new PageImpl<>(List.of(donation));
        when(donationRepository.searchDonations(searchTerm, Pageable.unpaged())).thenReturn(donationsPage);
        when(donationMapper.toResponseDTO(donation)).thenReturn(donationResponseDTO);

        // When
        Page<DonationResponseDTO> result = donationService.searchDonations(searchTerm, Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(donationResponseDTO);
    }

    @Test
    void shouldFindByBeneficiarioAndStatus() {
        // Given
        Page<Donation> donationsPage = new PageImpl<>(List.of(donation));
        when(donationRepository.findByBeneficiarioIdAndStatus("ong-id", StatusDoacao.PENDENTE, Pageable.unpaged()))
            .thenReturn(donationsPage);
        when(donationMapper.toResponseDTO(donation)).thenReturn(donationResponseDTO);

        // When
        Page<DonationResponseDTO> result = donationService.findByBeneficiarioAndStatus(
            "ong-id", StatusDoacao.PENDENTE, Pageable.unpaged()
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(donationResponseDTO);
    }
}