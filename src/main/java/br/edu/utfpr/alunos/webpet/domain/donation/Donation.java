package br.edu.utfpr.alunos.webpet.domain.donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a donation transaction in the WebPet system.
 * 
 * <p>This entity stores monetary donations between users in the system,
 * allowing regular users to donate to ONGs and PROTETORs for animal care.
 * 
 * <p>Key design decisions:
 * <ul>
 *   <li>Simple user-to-user donation model</li>
 *   <li>BigDecimal for precise monetary calculations</li>
 *   <li>Immutable after creation (no status changes)</li>
 *   <li>Audit timestamps for financial tracking</li>
 * </ul>
 * 
 */
@Entity
@Table(name = "doacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    
    /**
     * Unique identifier for the donation.
     * Uses UUID for enhanced security and scalability.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * ID of the user making the donation.
     * References any user type (User, ONG, or PROTETOR).
     */
    @NotBlank(message = "Doador é obrigatório")
    @Column(name = "doador_id", nullable = false)
    private String doadorId;
    
    /**
     * ID of the user receiving the donation.
     * Must be an ONG or PROTETOR (validated in service layer).
     */
    @NotBlank(message = "Beneficiário é obrigatório")
    @Column(name = "beneficiario_id", nullable = false)
    private String beneficiarioId;
    
    /**
     * Donation amount in Brazilian Real (BRL).
     * Must be positive and use precise decimal arithmetic.
     */
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    /**
     * Timestamp when the donation was made.
     * Defaults to current timestamp when created.
     */
    @NotNull(message = "Data da doação é obrigatória")
    @Column(name = "data_doacao", nullable = false)
    private LocalDateTime dataDoacao = LocalDateTime.now();
    
    /**
     * Timestamp when the donation was registered in the system.
     * Automatically set by Hibernate on persist.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the donation was last updated.
     * Automatically updated by Hibernate on merge.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Constructor for creating a new donation.
     * 
     * @param doadorId ID of the user making the donation
     * @param beneficiarioId ID of the user receiving the donation
     * @param valor donation amount
     */
    public Donation(String doadorId, String beneficiarioId, BigDecimal valor) {
        this.doadorId = doadorId;
        this.beneficiarioId = beneficiarioId;
        this.valor = valor;
        this.dataDoacao = LocalDateTime.now();
    }
    
    /**
     * Constructor for creating a donation with a specific timestamp.
     * Used for historical data import or specific timing requirements.
     * 
     * @param doadorId ID of the user making the donation
     * @param beneficiarioId ID of the user receiving the donation
     * @param valor donation amount
     * @param dataDoacao when the donation was made
     */
    public Donation(String doadorId, String beneficiarioId, BigDecimal valor, LocalDateTime dataDoacao) {
        this.doadorId = doadorId;
        this.beneficiarioId = beneficiarioId;
        this.valor = valor;
        this.dataDoacao = dataDoacao;
    }
    
    /**
     * Checks if this donation involves the specified user as either donor or beneficiary.
     * 
     * @param userId the user ID to check
     * @return true if the user is involved in this donation
     */
    public boolean envolveUsuario(String userId) {
        return doadorId.equals(userId) || beneficiarioId.equals(userId);
    }
    
    /**
     * Returns a formatted display string for the donation.
     * 
     * @return formatted string with amount and date
     */
    public String getDisplayInfo() {
        return String.format("R$ %.2f em %s", valor, 
            dataDoacao.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    
    /**
     * Validates that the donor and beneficiary are different users.
     * 
     * @return true if valid, false if same user
     */
    public boolean isValid() {
        return !doadorId.equals(beneficiarioId) && valor.compareTo(BigDecimal.ZERO) > 0;
    }
}