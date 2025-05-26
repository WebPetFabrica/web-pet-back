package br.edu.utfpr.alunos.webpet.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "protetores")
@Data
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
