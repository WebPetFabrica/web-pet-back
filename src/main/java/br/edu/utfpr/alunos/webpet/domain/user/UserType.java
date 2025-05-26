package br.edu.utfpr.alunos.webpet.domain.user;

public enum UserType {
    USER,
    ONG,
    PROTETOR;
    
    public String getRole() {
        return "ROLE_" + this.name();
    }
}