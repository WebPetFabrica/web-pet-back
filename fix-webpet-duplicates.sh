#!/bin/bash

echo "🔧 Corrigindo duplicidade de entidades no WebPet..."

# 1. Remover classes duplicadas em entities
echo "❌ Removendo classes duplicadas..."
rm -f src/main/java/br/edu/utfpr/alunos/webpet/entities/ONG.java
rm -f src/main/java/br/edu/utfpr/alunos/webpet/entities/Protetor.java

# 2. Corrigir imports em CustomUserDetailsService
echo "📝 Atualizando CustomUserDetailsService..."
sed -i 's/br.edu.utfpr.alunos.webpet.entities.ONG/br.edu.utfpr.alunos.webpet.domain.ong.ONG/g' \
    src/main/java/br/edu/utfpr/alunos/webpet/infra/security/CustomUserDetailsService.java

sed -i 's/br.edu.utfpr.alunos.webpet.entities.Protetor/br.edu.utfpr.alunos.webpet.domain.protetor.Protetor/g' \
    src/main/java/br/edu/utfpr/alunos/webpet/infra/security/CustomUserDetailsService.java

# 3. Corrigir imports em UserRegistrationService
echo "📝 Atualizando UserRegistrationService..."
sed -i 's/br.edu.utfpr.alunos.webpet.entities.ONG/br.edu.utfpr.alunos.webpet.domain.ong.ONG/g' \
    src/main/java/br/edu/utfpr/alunos/webpet/services/UserRegistrationService.java

sed -i 's/br.edu.utfpr.alunos.webpet.entities.Protetor/br.edu.utfpr.alunos.webpet.domain.protetor.Protetor/g' \
    src/main/java/br/edu/utfpr/alunos/webpet/services/UserRegistrationService.java

# 4. Corrigir imports em SecurityFilter
echo "📝 Atualizando SecurityFilter..."
sed -i 's/br.edu.utfpr.alunos.webpet.entities.ONG/br.edu.utfpr.alunos.webpet.domain.ong.ONG/g' \
    src/main/java/br/edu/utfpr/alunos/webpet/infra/security/SecurityFilter.java

sed -i 's/br.edu.utfpr.alunos.webpet.entities.Protetor/br.edu.utfpr.alunos.webpet.domain.protetor.Protetor/g' \
    src/main/java/br/edu/utfpr/alunos/webpet/infra/security/SecurityFilter.java

# 5. Completar ONGRepository
echo "📝 Completando ONGRepository..."
cat > src/main/java/br/edu/utfpr/alunos/webpet/repositories/ONGRepository.java << 'EOF'
package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.ong.ONG;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ONGRepository extends JpaRepository<ONG, String> {
    Optional<ONG> findByEmail(String email);
    Optional<ONG> findByCnpj(String cnpj);
}
EOF

# 6. Completar ProtetorRepository
echo "📝 Completando ProtetorRepository..."
cat > src/main/java/br/edu/utfpr/alunos/webpet/repositories/ProtetorRepository.java << 'EOF'
package br.edu.utfpr.alunos.webpet.repositories;

import br.edu.utfpr.alunos.webpet.domain.protetor.Protetor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProtetorRepository extends JpaRepository<Protetor, String> {
    Optional<Protetor> findByEmail(String email);
    Optional<Protetor> findByCpf(String cpf);
}
EOF

# 7. Remover BaseRegisterDTO não utilizado
echo "🧹 Removendo arquivos não utilizados..."
rm -f src/main/java/br/edu/utfpr/alunos/webpet/dto/BaseRegisterDTO.java

# 8. Limpar target
echo "🧹 Limpando arquivos compilados..."
rm -rf target/

# 9. Parar containers existentes
echo "🐳 Parando containers existentes..."
docker compose down -v

# 10. Subir containers novamente
echo "🚀 Iniciando containers Docker..."
docker compose up -d

# 11. Aguardar banco inicializar
echo "⏳ Aguardando banco de dados inicializar..."
sleep 10

# 12. Verificar logs
echo "📋 Verificando status dos containers..."
docker compose ps

echo "✅ Correções aplicadas!"
echo ""
echo "🔍 Para verificar os logs:"
echo "   docker compose logs -f app"
echo ""
echo "🧪 Para testar os endpoints:"
echo "   ./test-endpoints.sh"