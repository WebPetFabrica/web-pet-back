public record ProtetorRegisterDTO(
    String nomeCompleto,
    String cpf,
    String email,
    String celular,
    String password
) implements RegisterDTO {
    @Override
    public UserType getUserType() {
        return UserType.PROTETOR;
    }
}