package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseUser {
    @Column(nullable = false)
    private String name;
    
    private String surname;
    
    @Override
    public String getDisplayName() {
        return surname != null ? name + " " + surname : name;
    }
    
    @Override
    public String getIdentifier() {
        return getEmail();
    }
}