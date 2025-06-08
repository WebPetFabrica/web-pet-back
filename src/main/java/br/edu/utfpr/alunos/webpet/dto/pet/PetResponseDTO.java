package br.edu.utfpr.alunos.webpet.dto.pet;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.domain.pet.StatusAdocao;
import br.edu.utfpr.alunos.webpet.dto.user.UserBasicResponseDTO;

public record PetResponseDTO(
    String id,
    String nome,
    Especie especie,
    String raca,
    Genero genero,
    Porte porte,
    LocalDate dataNascimento,
    Integer idadeEmAnos,
    String descricao,
    String fotoUrl,
    StatusAdocao statusAdocao,
    UserBasicResponseDTO responsavel,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm
) {}