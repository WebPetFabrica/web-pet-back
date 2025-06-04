package br.edu.utfpr.alunos.webpet.services.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.RegisterRequestDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.AccountLockedException;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginDTO) {
        if (loginAttemptService.isBlocked(loginDTO.email())) {
            throw new AccountLockedException("Conta temporariamente bloqueada devido a muitas tentativas de login");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password())
            );
            
            loginAttemptService.recordSuccessfulLogin(loginDTO.email());
            
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = tokenService.generateToken(userDetails);
            String displayName = findDisplayNameByEmail(loginDTO.email());
            
            return new AuthResponseDTO(displayName, token, "Bearer");
        } catch (Exception e) {
            loginAttemptService.recordFailedAttempt(loginDTO.email());
            int remaining = loginAttemptService.getRemainingAttempts(loginDTO.email());
            
            throw new BusinessException(
                String.format("Credenciais inválidas. %d tentativas restantes.", remaining)
            );
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO registerUser(RegisterRequestDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        User user = User.builder()
            .name(registerDTO.name())
            .build();
        
        user.setEmail(registerDTO.email());
        user.setPassword(passwordEncoder.encode(registerDTO.password()));
        user.setCelular("");
        user.setUserType(UserType.USER);
        
        user = userRepository.save(user);
        
        String token = generateTokenForEmail(user.getEmail());
        return new AuthResponseDTO(user.getDisplayName(), token, "Bearer");
    }

    @Override
    @Transactional
    public AuthResponseDTO registerONG(ONGRegisterDTO ongDTO) {
        if (ongRepository.existsByEmail(ongDTO.email())) {
            throw new BusinessException("Email já cadastrado");
        }
        
        if (ongRepository.existsByCnpj(ongDTO.cnpj())) {
            throw new BusinessException("CNPJ já cadastrado");
        }

        ONG ong = ONG.builder()
            .cnpj(ongDTO.cnpj())
            .nomeOng(ongDTO.nomeOng())
            .build();
        
        ong.setEmail(ongDTO.email());
        ong.setPassword(passwordEncoder.encode(ongDTO.password()));
        ong.setCelular(ongDTO.celular());
        ong.setUserType(UserType.ONG);
        
        ong = ongRepository.save(ong);
        
        String token = generateTokenForEmail(ong.getEmail());
        return new AuthResponseDTO(ong.getDisplayName(), token, "Bearer");
    }

    @Override
    @Transactional
    public AuthResponseDTO registerProtetor(ProtetorRegisterDTO protetorDTO) {
        if (protetorRepository.existsByEmail(protetorDTO.email())) {
            throw new BusinessException("Email já cadastrado");
        }
        
        if (protetorRepository.existsByCpf(protetorDTO.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        Protetor protetor = Protetor.builder()
            .cpf(protetorDTO.cpf())
            .nomeCompleto(protetorDTO.nomeCompleto())
            .build();
        
        protetor.setEmail(protetorDTO.email());
        protetor.setPassword(passwordEncoder.encode(protetorDTO.password()));
        protetor.setCelular(protetorDTO.celular());
        protetor.setUserType(UserType.PROTETOR);
        
        protetor = protetorRepository.save(protetor);
        
        String token = generateTokenForEmail(protetor.getEmail());
        return new AuthResponseDTO(protetor.getDisplayName(), token, "Bearer");
    }

    private String findDisplayNameByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(BaseUser::getDisplayName)
            .orElse(ongRepository.findByEmail(email)
                .map(BaseUser::getDisplayName)
                .orElse(protetorRepository.findByEmail(email)
                    .map(BaseUser::getDisplayName)
                    .orElse("Usuário")));
    }

    private String generateTokenForEmail(String email) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            email, "", java.util.Collections.emptyList()
        );
        return tokenService.generateToken(userDetails);
    }
}