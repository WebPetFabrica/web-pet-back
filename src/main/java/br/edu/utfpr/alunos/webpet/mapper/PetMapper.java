package br.edu.utfpr.alunos.webpet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.dto.pet.PetResponseDTO;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PetMapper {
    
    @Mapping(source = "responsavelId", target = "responsavelId")
    PetResponseDTO toResponseDTO(Pet pet);
}