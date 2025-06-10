package br.edu.utfpr.alunos.webpet.services.ong;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.edu.utfpr.alunos.webpet.dto.user.ONGResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.user.ONGUpdateRequestDTO;

public interface ONGService {
    
    Page<ONGResponseDTO> findAllActiveONGs(Pageable pageable);
    
    List<ONGResponseDTO> findAllActiveONGs();
    
    Optional<ONGResponseDTO> findById(String id);
    
    Optional<ONGResponseDTO> findByCnpj(String cnpj);
    
    ONGResponseDTO updateONG(String id, ONGUpdateRequestDTO updateRequest, String currentUserId);
    
    void deactivateONG(String id, String currentUserId);
    
    void activateONG(String id, String currentUserId);
    
    Page<ONGResponseDTO> searchONGs(String searchTerm, Pageable pageable);
    
    boolean existsByCnpj(String cnpj);
}