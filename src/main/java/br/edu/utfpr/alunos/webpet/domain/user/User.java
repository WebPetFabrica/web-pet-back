package br.edu.utfpr.alunos.webpet.domain.user;


import br.edu.utfpr.alunos.webpet.utils.enums.UserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String email;
    private String phone;
    private String cpf;
    private String cnpj;

    @Enumerated(EnumType.STRING)
    private UserType userType;
    private String password;
    private String description;

}
