package br.edu.utfpr.alunos.webpet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.edu.utfpr.alunos.webpet.domain.donation.Donation;
import br.edu.utfpr.alunos.webpet.dto.donation.DonationResponseDTO;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface DonationMapper {
    
    @Mapping(source = "beneficiario", target = "beneficiario")
    DonationResponseDTO toResponseDTO(Donation donation);
}