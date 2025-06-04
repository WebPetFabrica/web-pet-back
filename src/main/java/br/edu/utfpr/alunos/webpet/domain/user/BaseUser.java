package br.edu.utfpr.alunos.webpet.domain.user;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Abstract base entity for all user types in the WebPet system.
 * 
 * <p>This class implements the Table Per Class inheritance strategy,
 * providing common attributes and behavior for User, ONG, and Protetor entities.
 * 
 * <p>Key design decisions:
 * <ul>
 *   <li>UUID primary keys for better scalability and security</li>
 *   <li>Soft delete pattern using 'active' flag</li>
 *   <li>Audit timestamps for data tracking</li>
 *   <li>Abstract methods enforce polymorphic behavior</li>
 * </ul>
 * 
 * @author WebPet Team
 * @since 1.0.0
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseUser {
    
    /**
     * Unique identifier for the user.
     * Uses UUID for enhanced security and scalability.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * User's email address. Must be unique across all user types.
     * Used as primary authentication credential.
     */
    @Column(unique = true, nullable = false)
    private String email;
    
    /**
     * Encrypted password using BCrypt algorithm.
     * Raw password is never stored in the database.
     */
    @Column(nullable = false)
    private String password;
    
    /**
     * User's mobile phone number for contact purposes.
     * Format validation is handled at the service layer.
     */
    @Column(nullable = false)
    private String celular;
    
    /**
     * Determines the user's role and permissions in the system.
     * Used for role-based access control (RBAC).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;
    
    /**
     * Soft delete flag. Allows account deactivation without data loss.
     * Inactive users cannot authenticate but retain data for audit purposes.
     */
    @Column(nullable = false)
    private boolean active = true;
    
    /**
     * Timestamp when the user was first created.
     * Automatically set by Hibernate on persist.
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the user was last modified.
     * Automatically updated by Hibernate on merge.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    /**
     * Constructor for creating new users with basic information.
     * 
     * @param email user's email address (must be unique)
     * @param password encrypted password
     * @param celular mobile phone number
     * @param userType determines user's role in the system
     */
    protected BaseUser(String email, String password, String celular, UserType userType) {
        this.email = email;
        this.password = password;
        this.celular = celular;
        this.userType = userType;
        this.active = true;
    }
    
    /**
     * Returns the display name for the user.
     * Implementation varies by user type:
     * - User: concatenated first and last name
     * - ONG: organization name
     * - Protetor: full name
     * 
     * @return user-friendly display name
     */
    public abstract String getDisplayName();
    
    /**
     * Returns the unique business identifier for the user.
     * Implementation varies by user type:
     * - User: email address
     * - ONG: CNPJ number
     * - Protetor: CPF number
     * 
     * @return business identifier for the user
     */
    public abstract String getIdentifier();
}