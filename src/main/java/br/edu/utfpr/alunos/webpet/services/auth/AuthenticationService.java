package br.edu.utfpr.alunos.webpet.services.auth;

import br.edu.utfpr.alunos.webpet.dto.auth.AuthResponseDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.LoginRequestDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ONGRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.ProtetorRegisterDTO;
import br.edu.utfpr.alunos.webpet.dto.auth.UserRegisterDTO;

public interface AuthenticationService {
    AuthResponseDTO loginUser(LoginRequestDTO data);
    AuthResponseDTO loginOng(LoginRequestDTO data);
    AuthResponseDTO loginProtetor(LoginRequestDTO data);
    AuthResponseDTO registerUser(UserRegisterDTO data);
    AuthResponseDTO registerOng(ONGRegisterDTO data);
    AuthResponseDTO registerProtetor(ProtetorRegisterDTO data);
}