package br.edu.utfpr.alunos.webpet.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ongs")
@Getter
@Setter
@NoArgsConstructor
public class ONG extends BaseUser {
    @Column(unique = true, nullable = false)
    private String cnpj;
    
    @Column(nullable = false)
    private String nomeOng;
    
    private String endereco;
    private String descricao;
    
    private ONG(String cnpj, String nomeOng, String endereco, String descricao, 
                String email, String password, String celular) {
        super(email, password, celular, UserType.ONG);
        this.cnpj = cnpj;
        this.nomeOng = nomeOng;
        this.endereco = endereco;
        this.descricao = descricao;
    }
    
    public static ONGBuilder builder() {
        return new ONGBuilder();
    }
    
    @Override
    public String getDisplayName() {
        return nomeOng;
    }
    
    @Override
    public String getIdentifier() {
        return cnpj;
    }
    
    public static class ONGBuilder {
        private String cnpj;
        private String nomeOng;
        private String endereco;
        private String descricao;
        private String email;
        private String password;
        private String celular;
        
        public ONGBuilder cnpj(String cnpj) {
            this.cnpj = cnpj;
            return this;
        }
        
        public ONGBuilder nomeOng(String nomeOng) {
            this.nomeOng = nomeOng;
            return this;
        }
        
        public ONGBuilder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }
        
        public ONGBuilder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }
        
        public ONGBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public ONGBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public ONGBuilder celular(String celular) {
            this.celular = celular;
            return this;
        }
        
        public ONG build() {
            return new ONG(cnpj, nomeOng, endereco, descricao, email, password, celular);
        }
    }
}