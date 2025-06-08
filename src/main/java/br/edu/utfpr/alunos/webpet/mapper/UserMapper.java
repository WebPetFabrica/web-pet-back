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
    
    UserResponseDTO toResponseDTO(BaseUser user);
    
    UserDetailedResponseDTO toDetailedResponseDTO(BaseUser user);
    
    ONGResponseDTO toONGResponseDTO(ONG ong);
    
    ProtetorResponseDTO toProtetorResponseDTO(Protetor protetor);
    
    UserBasicResponseDTO toBasicResponseDTO(BaseUser user);
}