package br.edu.utfpr.alunos.webpet.dto.pet;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;

public record PetFilterDTO(
    Especie especie,
    Genero genero,
    Porte porte,
    Integer idadeMinima,
    Integer idadeMaxima,
    String raca
) {}