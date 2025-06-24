package br.edu.utfpr.alunos.webpet.domain.pet;

/**
 * Enum representing the species of pets supported by the WebPet system.
 * Currently supports dogs and cats as the primary species for adoption.
 * 
 */
public enum Especie {
    
    /**
     * Dog species - most common pet for adoption
     */
    CACHORRO("Cachorro"),
    
    /**
     * Cat species - second most common pet for adoption
     */
    GATO("Gato");
    
    private final String descricao;
    
    Especie(String descricao) {
        this.descricao = descricao;
    }
    
    /**
     * Returns the human-readable description of the species.
     * 
     * @return the description in Portuguese
     */
    public String getDescricao() {
        return descricao;
    }
}