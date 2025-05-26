package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.RegisterRequestDTO;

public interface AuthenticationService {
    AuthResponseDTO login(LoginRequestDTO loginDTO);
    AuthResponseDTO registerUser(RegisterRequestDTO registerDTO);
    AuthResponseDTO registerONG(ONGRegisterDTO ongDTO);
    AuthResponseDTO registerProtetor(ProtetorRegisterDTO protetorDTO);
}