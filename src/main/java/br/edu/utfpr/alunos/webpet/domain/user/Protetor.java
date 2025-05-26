package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "protetores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Protetor extends BaseUser {
    @Column(nullable = false)
    private String nomeCompleto;
    
    @Column(unique = true, nullable = false)
    private String cpf;
    
    private String endereco;
    private Integer capacidadeAcolhimento;
    
    @Override
    public String getDisplayName() {
        return nomeCompleto;
    }
    
    @Override
    public String getIdentifier() {
        return cpf;
    }
}