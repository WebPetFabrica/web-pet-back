package br.edu.utfpr.alunos.webpet.domain.pet;

public enum Genero {
    MACHO("Macho"),
    FEMEA("Fêmea");
    
    private final String descricao;
    
    Genero(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}