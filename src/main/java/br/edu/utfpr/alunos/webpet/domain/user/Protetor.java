package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "protetores")
@Getter
@Setter
@NoArgsConstructor
public class Protetor extends BaseUser {
    @Column(nullable = false)
    private String nomeCompleto;
    
    @Column(unique = true, nullable = false)
    private String cpf;
    
    private String endereco;
    private Integer capacidadeAcolhimento;
    
    private Protetor(String nomeCompleto, String cpf, String endereco, Integer capacidadeAcolhimento,
                     String email, String password, String celular) {
        super(email, password, celular, UserType.PROTETOR);
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.endereco = endereco;
        this.capacidadeAcolhimento = capacidadeAcolhimento;
    }
    
    public static ProtetorBuilder builder() {
        return new ProtetorBuilder();
    }
    
    @Override
    public String getDisplayName() {
        return nomeCompleto;
    }
    
    @Override
    public String getIdentifier() {
        return cpf;
    }
    
    public static class ProtetorBuilder {
        private String nomeCompleto;
        private String cpf;
        private String endereco;
        private Integer capacidadeAcolhimento;
        private String email;
        private String password;
        private String celular;
        
        public ProtetorBuilder nomeCompleto(String nomeCompleto) {
            this.nomeCompleto = nomeCompleto;
            return this;
        }
        
        public ProtetorBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }
        
        public ProtetorBuilder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }
        
        public ProtetorBuilder capacidadeAcolhimento(Integer capacidadeAcolhimento) {
            this.capacidadeAcolhimento = capacidadeAcolhimento;
            return this;
        }
        
        public ProtetorBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public ProtetorBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public ProtetorBuilder celular(String celular) {
            this.celular = celular;
            return this;
        }
        
        public Protetor build() {
            return new Protetor(nomeCompleto, cpf, endereco, capacidadeAcolhimento, email, password, celular);
        }
    }
}