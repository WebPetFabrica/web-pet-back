package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseUser {
    @Column(nullable = false)
    private String name;
    
    private String surname;
    
    private User(String name, String surname, String email, String password, String celular) {
        super(email, password, celular, UserType.USER);
        this.name = name;
        this.surname = surname;
    }
    
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    @Override
    public String getDisplayName() {
        return surname != null ? name + " " + surname : name;
    }
    
    @Override
    public String getIdentifier() {
        return getEmail();
    }
    
    public static class UserBuilder {
        private String name;
        private String surname;
        private String email;
        private String password;
        private String celular;
        
        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public UserBuilder surname(String surname) {
            this.surname = surname;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UserBuilder celular(String celular) {
            this.celular = celular;
            return this;
        }
        
        public User build() {
            return new User(name, surname, email, password, celular);
        }
    }
}