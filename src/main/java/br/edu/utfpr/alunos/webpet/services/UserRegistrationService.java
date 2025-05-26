package br.edu.utfpr.alunos.webpet.services;

import br.edu.utfpr.alunos.webpet.dto.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.ResponseDTO;
import br.edu.utfpr.alunos.webpet.entities.ONG;
import br.edu.utfpr.alunos.webpet.entities.Protetor;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    
    public ResponseDTO registerONG(ONGRegisterDTO dto) {
        if (ongRepository.findByEmail(dto.email()).isPresent() ||
            ongRepository.findByCnpj(dto.cnpj()).isPresent()) {
            throw new RuntimeException("ONG já cadastrada");
        }
        
        ONG ong = new ONG();
        ong.setCnpj(dto.cnpj());
        ong.setNomeOng(dto.nomeOng());
        ong.setEmail(dto.email());
        ong.setCelular(dto.celular());
        ong.setPassword(passwordEncoder.encode(dto.password()));
        
        ong = ongRepository.save(ong);
        String token = tokenService.generateToken(createUserDetails(ong));
        
        return new ResponseDTO(ong.getNomeOng(), token);
    }
    
    public ResponseDTO registerProtetor(ProtetorRegisterDTO dto) {
        if (protetorRepository.findByEmail(dto.email()).isPresent() ||
            protetorRepository.findByCpf(dto.cpf()).isPresent()) {
            throw new RuntimeException("Protetor já cadastrado");
        }
        
        Protetor protetor = new Protetor();
        protetor.setNomeCompleto(dto.nomeCompleto());
        protetor.setCpf(dto.cpf());
        protetor.setEmail(dto.email());
        protetor.setCelular(dto.celular());
        protetor.setPassword(passwordEncoder.encode(dto.password()));
        
        protetor = protetorRepository.save(protetor);
        String token = tokenService.generateToken(createUserDetails(protetor));
        
        return new ResponseDTO(protetor.getNomeCompleto(), token);
    }
    
    private UserDetails createUserDetails(Object user) {
        String email = user instanceof ONG ? ((ONG) user).getEmail() : ((Protetor) user).getEmail();
        String password = user instanceof ONG ? ((ONG) user).getPassword() : ((Protetor) user).getPassword();
        return new org.springframework.security.core.userdetails.User(
            email, password, Collections.emptyList()
        );
    }
}