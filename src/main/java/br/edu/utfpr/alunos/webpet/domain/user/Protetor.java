package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "protetores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Protetor extends BaseUser {
    @Column(nullable = false)
    private String nomeCompleto;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    @PrePersist
    protected void onCreate() {
        if (getUserType() == null) {
            setUserType(UserType.PROTETOR);
        }
    }

    @Override
    public String getDisplayName() {
        return nomeCompleto;
    }
}