package br.edu.utfpr.alunos.webpet.services.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.utfpr.alunos.webpet.domain.donation.Donation;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationCreateRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.mapper.DonationMapper;
import br.edu.utfpr.alunos.webpet.repositories.DonationRepository;

/**
 * Implementation of DonationService interface.
 * 
 * <p>Provides complete donation management functionality including creation,
 * querying, and analytics. Implements business rules and validation for
 * donation operations.
 * 
 */
@Service
@Transactional
public class DonationServiceImpl implements DonationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DonationServiceImpl.class);
    
    private final DonationRepository donationRepository;
    private final DonationMapper donationMapper;
    
    public DonationServiceImpl(DonationRepository donationRepository, DonationMapper donationMapper) {
        this.donationRepository = donationRepository;
        this.donationMapper = donationMapper;
    }
    
    @Override
    public DonationResponseDTO createDonation(DonationCreateRequestDTO createRequest, String doadorId) {
        try {
            MDC.put("operation", "createDonation");
            MDC.put("doadorId", doadorId);
            MDC.put("beneficiarioId", createRequest.beneficiarioId());
            
            logger.info("Creating donation from donor: {} to beneficiary: {}", doadorId, createRequest.beneficiarioId());
            
            // Validate that donor and beneficiary are different
            if (doadorId.equals(createRequest.beneficiarioId())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, 
                    "Usuário não pode fazer doação para si mesmo");
            }
            
            // Create donation entity using mapper
            Donation donation = donationMapper.toEntity(createRequest, doadorId);
            
            // Validate donation
            if (!donation.isValid()) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, 
                    "Dados da doação são inválidos");
            }
            
            // Save donation
            Donation savedDonation = donationRepository.save(donation);
            
            logger.info("Donation created successfully with ID: {} amount: {}", 
                savedDonation.getId(), savedDonation.getValor());
            
            return donationMapper.toResponseDTO(savedDonation);
            
        } finally {
            MDC.clear();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<DonationResponseDTO> findById(String id) {
        logger.info("Finding donation by ID: {}", id);
        
        return donationRepository.findById(id)
            .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> findByBeneficiario(String beneficiarioId, Pageable pageable) {
        logger.info("Finding donations for beneficiary: {}", beneficiarioId);
        
        return donationRepository.findByBeneficiarioId(beneficiarioId, pageable)
            .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> findByDoador(String doadorId, Pageable pageable) {
        logger.info("Finding donations for donor: {}", doadorId);
        
        return donationRepository.findByDoadorId(doadorId, pageable)
            .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> findByUsuarioInvolvido(String userId, Pageable pageable) {
        logger.info("Finding donations for user: {}", userId);
        
        return donationRepository.findByUsuarioInvolvido(userId, pageable)
            .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        logger.info("Finding donations between {} and {}", startDate, endDate);
        
        return donationRepository.findByDataDoacaoBetween(startDate, endDate, pageable)
            .map(donationMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalReceivedByBeneficiario(String beneficiarioId) {
        logger.info("Calculating total received by beneficiary: {}", beneficiarioId);
        
        BigDecimal total = donationRepository.getTotalReceivedByBeneficiario(beneficiarioId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDonatedByDoador(String doadorId) {
        logger.info("Calculating total donated by donor: {}", doadorId);
        
        BigDecimal total = donationRepository.getTotalDonatedByDoador(doadorId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getCountReceivedByBeneficiario(String beneficiarioId) {
        logger.info("Counting donations received by beneficiary: {}", beneficiarioId);
        
        return donationRepository.countByBeneficiarioId(beneficiarioId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getCountMadeByDoador(String doadorId) {
        logger.info("Counting donations made by donor: {}", doadorId);
        
        return donationRepository.countByDoadorId(doadorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> getRecentDonations(int days, Pageable pageable) {
        logger.info("Finding donations from last {} days", days);
        
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return donationRepository.findRecentDonations(sinceDate, pageable)
            .map(donationMapper::toResponseDTO);
    }
}