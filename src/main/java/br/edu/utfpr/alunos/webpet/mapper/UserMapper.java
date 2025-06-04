package br.edu.utfpr.alunos.webpet.mapper;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.domain.user.Protetor;
import br.edu.utfpr.alunos.webpet.domain.user.User;
import br.edu.utfpr.alunos.webpet.dto.user.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    @Mapping(target = "password", ignore = true)
    UserResponseDTO toResponseDTO(BaseUser user);
    
    @Mapping(target = "password", ignore = true)
    UserDetailedResponseDTO toDetailedResponseDTO(BaseUser user);
    
    @Mapping(target = "password", ignore = true)
    ONGResponseDTO toONGResponseDTO(ONG ong);
    
    @Mapping(target = "password", ignore = true)
    ProtetorResponseDTO toProtetorResponseDTO(Protetor protetor);
    
    UserBasicResponseDTO toBasicResponseDTO(BaseUser user);
}