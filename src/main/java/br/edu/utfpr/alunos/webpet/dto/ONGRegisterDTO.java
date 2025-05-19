public record ONGRegisterDTO(
    String cnpj,
    String nomeOng,
    String email,
    String celular,
    String password
) implements RegisterDTO {
    @Override
    public UserType getUserType() {
        return UserType.ONG;
    }
}