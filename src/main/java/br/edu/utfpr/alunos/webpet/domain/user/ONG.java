package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "ongs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ONG extends BaseUser {
    @Column(unique = true, nullable = false)
    private String cnpj;
    
    @Column(nullable = false)
    private String nomeOng;
    
    private String endereco;
    private String descricao;
    
    @Override
    public String getDisplayName() {
        return nomeOng;
    }
    
    @Override
    public String getIdentifier() {
        return cnpj;
    }
}