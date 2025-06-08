package br.edu.utfpr.alunos.webpet.domain.pet;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Removed BaseUser import
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
// Removed FetchType import
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// Removed unused JPA imports
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
public class Pet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String nome;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especie especie;
    
    @Column(nullable = false)
    private String raca;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Porte porte;
    
    @Column(nullable = false)
    private LocalDate dataNascimento;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Column
    private String fotoUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAdocao statusAdocao = StatusAdocao.DISPONIVEL;
    
    @Column(nullable = false)
    private boolean ativo = true;
    
    @Column(name = "responsavel_id", nullable = false)
    private String responsavelId;
    
    @CreationTimestamp
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    private LocalDateTime atualizadoEm;
    
    private Pet(String nome, Especie especie, String raca, Genero genero, Porte porte,
                LocalDate dataNascimento, String descricao, String fotoUrl, String responsavelId) {
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.genero = genero;
        this.porte = porte;
        this.dataNascimento = dataNascimento;
        this.descricao = descricao;
        this.fotoUrl = fotoUrl;
        this.responsavelId = responsavelId;
        this.statusAdocao = StatusAdocao.DISPONIVEL;
        this.ativo = true;
    }
    
    public static PetBuilder builder() {
        return new PetBuilder();
    }
    
    public int getIdadeEmAnos() {
        return LocalDate.now().getYear() - dataNascimento.getYear();
    }
    
    public void marcarComoAdotado() {
        this.statusAdocao = StatusAdocao.ADOTADO;
    }
    
    public void marcarComoIndisponivel() {
        this.statusAdocao = StatusAdocao.INDISPONIVEL;
    }
    
    public void marcarComoDisponivel() {
        this.statusAdocao = StatusAdocao.DISPONIVEL;
    }
    
    public void desativar() {
        this.ativo = false;
    }
    
    public void ativar() {
        this.ativo = true;
    }
    
    public static class PetBuilder {
        private String nome;
        private Especie especie;
        private String raca;
        private Genero genero;
        private Porte porte;
        private LocalDate dataNascimento;
        private String descricao;
        private String fotoUrl;
        private String responsavelId;
        
        public PetBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }
        
        public PetBuilder especie(Especie especie) {
            this.especie = especie;
            return this;
        }
        
        public PetBuilder raca(String raca) {
            this.raca = raca;
            return this;
        }
        
        public PetBuilder genero(Genero genero) {
            this.genero = genero;
            return this;
        }
        
        public PetBuilder porte(Porte porte) {
            this.porte = porte;
            return this;
        }
        
        public PetBuilder dataNascimento(LocalDate dataNascimento) {
            this.dataNascimento = dataNascimento;
            return this;
        }
        
        public PetBuilder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }
        
        public PetBuilder fotoUrl(String fotoUrl) {
            this.fotoUrl = fotoUrl;
            return this;
        }
        
        public PetBuilder responsavelId(String responsavelId) {
            this.responsavelId = responsavelId;
            return this;
        }
        
        public Pet build() {
            return new Pet(nome, especie, raca, genero, porte, dataNascimento, descricao, fotoUrl, responsavelId);
        }
    }
}