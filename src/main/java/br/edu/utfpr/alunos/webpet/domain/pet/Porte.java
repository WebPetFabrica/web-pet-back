package br.edu.utfpr.alunos.webpet.domain.pet;

/**
 * Enum representing the size categories for pets in the WebPet system.
 * Used for filtering and matching pets with suitable adopters.
 * 
 */
public enum Porte {
    
    /**
     * Small size pets - typically under 10kg for dogs, most cats
     */
    PEQUENO("Pequeno"),
    
    /**
     * Medium size pets - typically 10-25kg for dogs
     */
    MEDIO("Médio"),
    
    /**
     * Large size pets - typically over 25kg for dogs
     */
    GRANDE("Grande");
    
    private final String descricao;
    
    Porte(String descricao) {
        this.descricao = descricao;
    }
    
    /**
     * Returns the human-readable description of the size.
     * 
     * @return the description in Portuguese
     */
    public String getDescricao() {
        return descricao;
    }
}