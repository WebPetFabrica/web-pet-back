package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.domain.user.*;
import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.UserRegisterDTO;
import br.edu.utfpr.alunos.webpet.infra.exception.BusinessException;
import br.edu.utfpr.alunos.webpet.infra.exception.ErrorCode;
import br.edu.utfpr.alunos.webpet.infra.security.TokenService;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.ProtetorRepository;
import br.edu.utfpr.alunos.webpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ONGRepository ongRepository;
    private final ProtetorRepository protetorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;

    @Override
    public AuthResponseDTO authenticate(LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var user = (BaseUser) auth.getPrincipal();
        var token = tokenService.generateToken(user);
        loginAttemptService.recordSuccessfulLogin(data.email());
        return new AuthResponseDTO(user.getDisplayName(), token, "Bearer");
    }

    @Override
    public AuthResponseDTO registerUser(UserRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser = User.builder().name(data.name()).email(data.email()).password(encryptedPassword).celular("00000000000").build();
        this.userRepository.save(newUser);
        return new AuthResponseDTO(newUser.getDisplayName(), tokenService.generateToken(newUser), "Bearer");
    }
    
    @Override
    public AuthResponseDTO registerOng(ONGRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = passwordEncoder.encode(data.password());
        ONG newOng = ONG.builder().nomeOng(data.nomeOng()).cnpj(data.cnpj()).email(data.email()).password(encryptedPassword).celular(data.celular()).build();
        this.ongRepository.save(newOng);
        return new AuthResponseDTO(newOng.getDisplayName(), tokenService.generateToken(newOng), "Bearer");
    }
    
    @Override
    public AuthResponseDTO registerProtetor(ProtetorRegisterDTO data) {
        validateEmailUniqueness(data.email());
        String encryptedPassword = passwordEncoder.encode(data.password());
        Protetor newProtetor = Protetor.builder().nomeCompleto(data.nomeCompleto()).cpf(data.cpf()).email(data.email()).password(encryptedPassword).celular(data.celular()).build();
        this.protetorRepository.save(newProtetor);
        return new AuthResponseDTO(newProtetor.getDisplayName(), tokenService.generateToken(newProtetor), "Bearer");
    }
    
    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email) || ongRepository.existsByEmail(email) || protetorRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_EMAIL_EXISTS);
        }
    }
}
