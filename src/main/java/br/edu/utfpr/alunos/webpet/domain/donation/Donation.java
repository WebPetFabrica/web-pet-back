package br.edu.utfpr.alunos.webpet.domain.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

// Remove the import for BaseUser
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
@Table(name = "doacoes")
@Getter
@Setter
@NoArgsConstructor
public class Donation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDoacao tipoDoacao;
    
    @Column(columnDefinition = "TEXT")
    private String mensagem;
    
    @Column(nullable = false)
    private String nomeDoador;
    
    @Column
    private String emailDoador;
    
    @Column
    private String telefoneDoador;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDoacao statusDoacao = StatusDoacao.PENDENTE;
    
    @Column
    private String transactionId;
    
    @Column(name = "beneficiario_id", nullable = false)
    private String beneficiarioId;
    
    @CreationTimestamp
    private LocalDateTime criadoEm;
    
    private LocalDateTime processadoEm;
    
    private Donation(BigDecimal valor, TipoDoacao tipoDoacao, String mensagem, 
                     String nomeDoador, String emailDoador, String telefoneDoador, 
                     String beneficiarioId) {
        this.valor = valor;
        this.tipoDoacao = tipoDoacao;
        this.mensagem = mensagem;
        this.nomeDoador = nomeDoador;
        this.emailDoador = emailDoador;
        this.telefoneDoador = telefoneDoador;
        this.beneficiarioId = beneficiarioId;
        this.statusDoacao = StatusDoacao.PENDENTE;
    }
    
    public static DonationBuilder builder() {
        return new DonationBuilder();
    }
    
    public void marcarComoProcessada(String transactionId) {
        this.statusDoacao = StatusDoacao.PROCESSADA;
        this.transactionId = transactionId;
        this.processadoEm = LocalDateTime.now();
    }
    
    public void marcarComoFalha() {
        this.statusDoacao = StatusDoacao.FALHA;
        this.processadoEm = LocalDateTime.now();
    }
    
    public void cancelar() {
        this.statusDoacao = StatusDoacao.CANCELADA;
        this.processadoEm = LocalDateTime.now();
    }
    
    public static class DonationBuilder {
        private BigDecimal valor;
        private TipoDoacao tipoDoacao;
        private String mensagem;
        private String nomeDoador;
        private String emailDoador;
        private String telefoneDoador;
        private String beneficiarioId;
        
        public DonationBuilder valor(BigDecimal valor) {
            this.valor = valor;
            return this;
        }
        
        public DonationBuilder tipoDoacao(TipoDoacao tipoDoacao) {
            this.tipoDoacao = tipoDoacao;
            return this;
        }
        
        public DonationBuilder mensagem(String mensagem) {
            this.mensagem = mensagem;
            return this;
        }
        
        public DonationBuilder nomeDoador(String nomeDoador) {
            this.nomeDoador = nomeDoador;
            return this;
        }
        
        public DonationBuilder emailDoador(String emailDoador) {
            this.emailDoador = emailDoador;
            return this;
        }
        
        public DonationBuilder telefoneDoador(String telefoneDoador) {
            this.telefoneDoador = telefoneDoador;
            return this;
        }
        
        public DonationBuilder beneficiarioId(String beneficiarioId) {
            this.beneficiarioId = beneficiarioId;
            return this;
        }
        
        public Donation build() {
            return new Donation(valor, tipoDoacao, mensagem, nomeDoador, 
                              emailDoador, telefoneDoador, beneficiarioId);
        }
    }
}