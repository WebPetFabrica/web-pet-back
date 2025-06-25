package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ongs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ONG extends BaseUser {
    @Column(nullable = false)
    private String nomeOng;

    @Column(unique = true, nullable = false, length = 14)
    private String cnpj;

    @PrePersist
    protected void onCreate() {
        if (getUserType() == null) {
            setUserType(UserType.ONG);
        }
    }

    @Override
    public String getDisplayName() {
        return nomeOng;
    }
}