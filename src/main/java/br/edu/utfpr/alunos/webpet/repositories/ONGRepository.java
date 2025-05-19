public interface ONGRepository extends JpaRepository<ONG, String> {
    Optional<ONG> findByEmail(String email);
    Optional<ONG> findByCnpj(String cnpj);
}