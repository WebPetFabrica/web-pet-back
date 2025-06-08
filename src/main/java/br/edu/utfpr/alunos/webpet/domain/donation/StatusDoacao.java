package br.edu.utfpr.alunos.webpet.domain.donation;

public enum StatusDoacao {
    PENDENTE("Pendente"),
    PROCESSADA("Processada"),
    FALHA("Falha"),
    CANCELADA("Cancelada");
    
    private final String descricao;
    
    StatusDoacao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}