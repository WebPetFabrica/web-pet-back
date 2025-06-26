package br.edu.utfpr.alunos.webpet.domain.pet;

public enum StatusAdocao {
    DISPONIVEL("Disponível"),
    EM_PROCESSO("Em Processo"),
    ADOTADO("Adotado"),
    INDISPONIVEL("Indisponível");
    
    private final String descricao;
    
    StatusAdocao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}