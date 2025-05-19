public interface ProtetorRepository extends JpaRepository<Protetor, String> {
    Optional<Protetor> findByEmail(String email);
    Optional<Protetor> findByCpf(String cpf);
}