package br.edu.utfpr.alunos.webpet.services.donation;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.utfpr.alunos.webpet.domain.donation.Donation;
import br.edu.utfpr.alunos.webpet.domain.donation.StatusDoacao;
import br.edu.utfpr.alunos.webpet.domain.donation.TipoDoacao;
import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.mapper.DonationMapper;
import br.edu.utfpr.alunos.webpet.repositories.BaseUserRepository;
import br.edu.utfpr.alunos.webpet.repositories.DonationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DonationServiceImpl implements DonationService {
    
    private final DonationRepository donationRepository;
    private final BaseUserRepository baseUserRepository;
    private final DonationMapper donationMapper;
    
    @Override
    public DonationResponseDTO createDonation(DonationCreateRequestDTO createRequest) {
        log.info("Creating donation for beneficiary: {}", createRequest.beneficiarioId());
        
        if (createRequest.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.DONATION_INVALID_AMOUNT);
        }
        
        BaseUser beneficiario = baseUserRepository.findByIdAndActiveTrue(createRequest.beneficiarioId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Donation donation = Donation.builder()
                .valor(createRequest.valor())
                .tipoDoacao(createRequest.tipoDoacao())
                .mensagem(createRequest.mensagem())
                .nomeDoador(createRequest.nomeDoador())
                .emailDoador(createRequest.emailDoador())
                .telefoneDoador(createRequest.telefoneDoador())
                .beneficiario(beneficiario)
                .build();
        
        Donation savedDonation = donationRepository.save(donation);
        log.info("Donation created successfully with ID: {}", savedDonation.getId());
        
        return donationMapper.toResponseDTO(savedDonation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<DonationResponseDTO> findById(String id) {
        return donationRepository.findById(id)
                .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> findByBeneficiario(String beneficiarioId, Pageable pageable) {
        return donationRepository.findByBeneficiarioId(beneficiarioId, pageable)
                .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> findByStatus(StatusDoacao status, Pageable pageable) {
        return donationRepository.findByStatusDoacao(status, pageable)
                .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> findByBeneficiarioAndStatus(String beneficiarioId, StatusDoacao status, Pageable pageable) {
        return donationRepository.findByBeneficiarioIdAndStatus(beneficiarioId, status, pageable)
                .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> searchDonations(String searchTerm, Pageable pageable) {
        return donationRepository.searchDonations(searchTerm, pageable)
                .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TipoDoacao> getAvailableTipos() {
        return Arrays.asList(TipoDoacao.values());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StatusDoacao> getAvailableStatus() {
        return Arrays.asList(StatusDoacao.values());
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDonationsByBeneficiario(String beneficiarioId) {
        BigDecimal total = donationRepository.getTotalDonationsByBeneficiario(beneficiarioId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getCountDonationsByBeneficiario(String beneficiarioId) {
        return donationRepository.getCountDonationsByBeneficiario(beneficiarioId);
    }
    
    @Override
    public void processarDoacao(String donationId, String transactionId) {
        log.info("Processing donation {} with transaction {}", donationId, transactionId);
        
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DONATION_NOT_FOUND));
        
        if (donation.getStatusDoacao() != StatusDoacao.PENDENTE) {
            throw new BusinessException(ErrorCode.DONATION_ALREADY_PROCESSED);
        }
        
        donation.marcarComoProcessada(transactionId);
        donationRepository.save(donation);
        log.info("Donation {} processed successfully", donationId);
    }
    
    @Override
    public void marcarComoFalha(String donationId) {
        log.info("Marking donation {} as failed", donationId);
        
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DONATION_NOT_FOUND));
        
        donation.marcarComoFalha();
        donationRepository.save(donation);
        log.info("Donation {} marked as failed", donationId);
    }
    
    @Override
    public void cancelarDoacao(String donationId) {
        log.info("Cancelling donation {}", donationId);
        
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DONATION_NOT_FOUND));
        
        if (donation.getStatusDoacao() == StatusDoacao.PROCESSADA) {
            throw new BusinessException(ErrorCode.DONATION_ALREADY_PROCESSED);
        }
        
        donation.cancelar();
        donationRepository.save(donation);
        log.info("Donation {} cancelled successfully", donationId);
    }
}