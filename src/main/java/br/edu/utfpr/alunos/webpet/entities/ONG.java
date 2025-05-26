package br.edu.utfpr.alunos.webpet.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ongs")
@Data
public class ONG {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String cnpj;
    
    @Column(nullable = false)
    private String nomeOng;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String celular;
    
    @Column(nullable = false)
    private String password;
}
