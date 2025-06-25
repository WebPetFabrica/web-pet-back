package br.edu.utfpr.alunos.webpet.dto.auth;

public record AuthResponseDTO(
    String displayName,
    String token,
    String tokenType
) {}