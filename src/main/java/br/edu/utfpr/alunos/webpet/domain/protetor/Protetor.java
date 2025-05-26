package br.edu.utfpr.alunos.webpet.domain.protetor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "protetores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Protetor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String nomeCompleto;
    
    @Column(unique = true, nullable = false)
    private String cpf;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String celular;
    
    @Column(nullable = false)
    private String password;
}