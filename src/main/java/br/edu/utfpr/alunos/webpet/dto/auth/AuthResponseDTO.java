package br.edu.utfpr.alunos.webpet.dto.auth;

public record AuthResponseDTO(
    String name,
    String token,
    String tokenType
) {}