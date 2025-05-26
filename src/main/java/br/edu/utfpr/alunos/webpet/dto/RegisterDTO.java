package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;

public interface RegisterDTO {
    UserType getUserType();
    String getEmail();
    String getPassword();
}
