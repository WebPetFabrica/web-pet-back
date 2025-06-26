package br.edu.utfpr.alunos.webpet.domain.pet;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.edu.utfpr.alunos.webpet.domain.user.BaseUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a pet available for adoption in the WebPet system.
 * 
 * <p>This entity stores all the necessary information about pets that can be adopted,
 * including their basic characteristics, availability status, and responsible user.
 * 
 * <p>Key design decisions:
 * <ul>
 *   <li>Uses UUID for primary key to ensure uniqueness across distributed systems</li>
 *   <li>Age is stored as integer (years) for simplicity</li>
 *   <li>Boolean flag for availability instead of complex status enum</li>
 *   <li>Responsible user ID references ONGs or PROTETORs only</li>
 * </ul>
 * 
 */
@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pet {
    
    /**
     * Unique identifier for the pet.
     * Uses UUID for enhanced security and scalability.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * Pet's name (required, max 255 characters).
     * Used for identification and display purposes.
     */
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    @Column(nullable = false)
    private String nome;
    
    /**
     * Pet's species - currently supports dogs and cats only.
     * Used for filtering and categorization.
     */
    @NotNull(message = "Espécie é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especie especie;
    
    /**
     * Pet's size category for matching with suitable adopters.
     * Important for housing compatibility.
     */
    @NotNull(message = "Porte é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Porte porte;
    
    /**
     * Pet's gender for breeding control and adopter preferences.
     */
    @NotNull(message = "Gênero é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;
    
    /**
     * Pet's age in years (0-30).
     * Used for filtering and care requirements assessment.
     */
    @NotNull(message = "Idade é obrigatória")
    @Min(value = 0, message = "Idade deve ser no mínimo 0 anos")
    @Max(value = 30, message = "Idade deve ser no máximo 30 anos")
    @Column(nullable = false)
    private Integer idade;
    
    /**
     * The responsible user (ONG or PROTETOR) ID for the pet's care.
     * References a BaseUser ID through any of the user tables.
     */
    @NotNull(message = "Responsável é obrigatório")
    @Column(name = "responsavel_id", nullable = false)
    private String responsavelId;
    
    /**
     * Whether the pet is available for adoption.
     * False means adopted or temporarily unavailable.
     */
    @Column(nullable = false)
    private Boolean disponivel = true;
    
    /**
     * Optional description providing more details about the pet.
     * Max 2000 characters for detailed information.
     */
    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    /**
     * Timestamp when the pet was registered in the system.
     * Automatically set by Hibernate on persist.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the pet information was last updated.
     * Automatically updated by Hibernate on merge.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Constructor for creating a new pet with required fields.
     * 
     * @param nome pet's name
     * @param especie pet's species
     * @param porte pet's size
     * @param genero pet's gender
     * @param idade pet's age in years
     * @param responsavelId the responsible user ID
     */
    public Pet(String nome, Especie especie, Porte porte, Genero genero, 
               Integer idade, String responsavelId) {
        this.nome = nome;
        this.especie = especie;
        this.porte = porte;
        this.genero = genero;
        this.idade = idade;
        this.responsavelId = responsavelId;
        this.disponivel = true;
    }
    
    /**
     * Constructor for creating a new pet with optional description.
     * 
     * @param nome pet's name
     * @param especie pet's species
     * @param porte pet's size
     * @param genero pet's gender
     * @param idade pet's age in years
     * @param responsavelId the responsible user ID
     * @param descricao optional description
     */
    public Pet(String nome, Especie especie, Porte porte, Genero genero, 
               Integer idade, String responsavelId, String descricao) {
        this(nome, especie, porte, genero, idade, responsavelId);
        this.descricao = descricao;
    }
    
    /**
     * Marks the pet as adopted (no longer available).
     * This method should be called when an adoption is confirmed.
     */
    public void marcarComoAdotado() {
        this.disponivel = false;
    }
    
    /**
     * Marks the pet as available for adoption.
     * This method can be called to make a pet available again.
     */
    public void marcarComoDisponivel() {
        this.disponivel = true;
    }
    
    /**
     * Temporarily marks the pet as unavailable.
     * Used for medical treatment, behavioral issues, etc.
     */
    public void marcarComoIndisponivel() {
        this.disponivel = false;
    }
    
    /**
     * Updates the pet's basic information.
     * 
     * @param nome new name
     * @param descricao new description
     */
    public void atualizarInformacoes(String nome, String descricao) {
        if (nome != null && !nome.trim().isEmpty()) {
            this.nome = nome;
        }
        this.descricao = descricao;
    }
    
    /**
     * Checks if the pet is available for adoption.
     * 
     * @return true if available, false otherwise
     */
    public boolean isDisponivel() {
        return Boolean.TRUE.equals(this.disponivel);
    }
    
    /**
     * Returns a formatted display name for the pet.
     * 
     * @return formatted string with name, species, and age
     */
    public String getDisplayName() {
        return String.format("%s (%s, %d anos)", nome, especie.getDescricao(), idade);
    }
}