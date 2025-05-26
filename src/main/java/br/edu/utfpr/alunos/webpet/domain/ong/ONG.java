package br.edu.utfpr.alunos.webpet.domain.ong;

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
@Table(name = "ongs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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