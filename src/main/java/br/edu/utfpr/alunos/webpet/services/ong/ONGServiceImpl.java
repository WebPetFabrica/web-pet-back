package br.edu.utfpr.alunos.webpet.services.ong;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import br.edu.utfpr.alunos.webpet.dto.user.ONGResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.user.ONGUpdateRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.mapper.UserMapper;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ONGServiceImpl implements ONGService {
    
    private final ONGRepository ongRepository;
    private final UserMapper userMapper;
    
    public ONGServiceImpl(ONGRepository ongRepository, UserMapper userMapper) {
        this.ongRepository = ongRepository;
        this.userMapper = userMapper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ONGResponseDTO> findAllActiveONGs(Pageable pageable) {
        return ongRepository.findAll(pageable)
                .map(ong -> userMapper.toONGResponseDTO(ong));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ONGResponseDTO> findAllActiveONGs() {
        return ongRepository.findAll()
                .stream()
                .filter(ONG::isActive)
                .map(userMapper::toONGResponseDTO)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ONGResponseDTO> findById(String id) {
        return ongRepository.findByIdAndActiveTrue(id)
                .map(userMapper::toONGResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ONGResponseDTO> findByCnpj(String cnpj) {
        return ongRepository.findByCnpj(cnpj)
                .filter(ONG::isActive)
                .map(userMapper::toONGResponseDTO);
    }
    
    @Override
    public ONGResponseDTO updateONG(String id, ONGUpdateRequestDTO updateRequest, String currentUserId) {
        log.info("Updating ONG {} by user {}", id, currentUserId);
        
        ONG ong = ongRepository.findById(id)
                .filter(o -> o instanceof ONG)
                .map(o -> (ONG) o)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        if (!ong.isActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }
        
        // Only allow self-update unless it's an admin (you can add admin check here)
        if (!ong.getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        // Check if email is being changed and if it's unique
        if (updateRequest.email() != null && !updateRequest.email().equals(ong.getEmail())) {
            if (ongRepository.existsByEmail(updateRequest.email())) {
                throw new BusinessException(ErrorCode.USER_EMAIL_EXISTS);
            }
            ong.setEmail(updateRequest.email());
        }
        
        // Check if CNPJ is being changed and if it's unique
        if (updateRequest.cnpj() != null && !updateRequest.cnpj().equals(ong.getCnpj())) {
            if (ongRepository.existsByCnpj(updateRequest.cnpj())) {
                throw new BusinessException(ErrorCode.USER_CNPJ_EXISTS);
            }
            ong.setCnpj(updateRequest.cnpj());
        }
        
        // Update other fields
        if (updateRequest.nomeOng() != null) {
            ong.setNomeOng(updateRequest.nomeOng());
        }
        if (updateRequest.celular() != null) {
            ong.setCelular(updateRequest.celular());
        }
        if (updateRequest.endereco() != null) {
            ong.setEndereco(updateRequest.endereco());
        }
        if (updateRequest.descricao() != null) {
            ong.setDescricao(updateRequest.descricao());
        }
        
        ONG updatedONG = ongRepository.save(ong);
        log.info("ONG {} updated successfully", id);
        
        return userMapper.toONGResponseDTO(updatedONG);
    }
    
    @Override
    public void deactivateONG(String id, String currentUserId) {
        log.info("Deactivating ONG {} by user {}", id, currentUserId);
        
        ONG ong = ongRepository.findById(id)
                .filter(o -> o instanceof ONG)
                .map(o -> (ONG) o)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // Only allow self-deactivation unless it's an admin
        if (!ong.getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        ong.setActive(false);
        ongRepository.save(ong);
        log.info("ONG {} deactivated successfully", id);
    }
    
    @Override
    public void activateONG(String id, String currentUserId) {
        log.info("Activating ONG {} by user {}", id, currentUserId);
        
        ONG ong = ongRepository.findById(id)
                .filter(o -> o instanceof ONG)
                .map(o -> (ONG) o)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // Only allow self-activation unless it's an admin
        if (!ong.getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        ong.setActive(true);
        ongRepository.save(ong);
        log.info("ONG {} activated successfully", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ONGResponseDTO> searchONGs(String searchTerm, Pageable pageable) {
        return ongRepository.searchByEmailOrNameAndActive(searchTerm, pageable)
                .map(ong -> userMapper.toONGResponseDTO((ONG) ong));
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByCnpj(String cnpj) {
        return ongRepository.existsByCnpj(cnpj);
    }
}