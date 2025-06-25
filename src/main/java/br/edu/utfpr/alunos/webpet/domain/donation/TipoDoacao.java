package br.edu.utfpr.alunos.webpet.domain.donation;

public enum TipoDoacao {
    MONETARIA("Monetária"),
    RACAO("Ração"),
    MEDICAMENTOS("Medicamentos"),
    BRINQUEDOS("Brinquedos"),
    ACESSORIOS("Acessórios"),
    OUTRO("Outro");
    
    private final String descricao;
    
    TipoDoacao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}