package br.edu.utfpr.alunos.webpet.domain.user;

public enum UserType {
    USER,
    ONG,
    PROTETOR,
    ADMIN;
    
    public String getRole() {
        return "ROLE_" + this.name();
    }
}