package br.edu.utfpr.alunos.webpet.repositories.projections;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public interface UserProjections {
    
    interface BasicUserProjection {
        String getId();
        String getEmail();
        String getDisplayName();
        UserType getUserType();
        boolean isActive();
    }
    
    interface UserSummaryProjection {
        String getId();
        String getEmail();
        String getDisplayName();
        UserType getUserType();
        boolean isActive();
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime getCreatedAt();
    }
    
    interface UserDetailProjection {
        String getId();
        String getEmail();
        String getDisplayName();
        UserType getUserType();
        String getCelular();
        boolean isActive();
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime getCreatedAt();
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime getUpdatedAt();
    }
    
    interface ONGProjection {
        String getId();
        String getEmail();
        String getNomeOng();
        String getCnpj();
        String getEndereco();
        String getCelular();
        boolean isActive();
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime getCreatedAt();
    }
    
    interface ProtetorProjection {
        String getId();
        String getEmail();
        String getNomeCompleto();
        String getCpf();
        String getEndereco();
        Integer getCapacidadeAcolhimento();
        String getCelular();
        boolean isActive();
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime getCreatedAt();
    }
}