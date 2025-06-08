package br.edu.utfpr.alunos.webpet.dto.pet;

import java.time.LocalDate;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record PetCreateRequestDTO(
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    String nome,
    
    @NotNull(message = "Espécie é obrigatória")
    Especie especie,
    
    @NotBlank(message = "Raça é obrigatória")
    @Size(min = 2, max = 50, message = "Raça deve ter entre 2 e 50 caracteres")
    String raca,
    
    @NotNull(message = "Gênero é obrigatório")
    Genero genero,
    
    @NotNull(message = "Porte é obrigatório")
    Porte porte,
    
    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    LocalDate dataNascimento,
    
    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    String descricao,
    
    String fotoUrl
) {}