package br.edu.utfpr.alunos.webpet.domain.pet;

public enum Especie {
    CACHORRO("Cachorro"),
    GATO("Gato"),
    PASSARO("Pássaro"),
    COELHO("Coelho"),
    HAMSTER("Hamster"),
    PEIXE("Peixe"),
    TARTARUGA("Tartaruga"),
    OUTRO("Outro");
    
    private final String descricao;
    
    Especie(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}