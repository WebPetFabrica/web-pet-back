#!/bin/bash

echo "🔧 Corrigindo problemas de compilação..."

# 1. Corrigir UserType.java
echo "📝 Corrigindo UserType.java..."
cat > src/main/java/br/edu/utfpr/alunos/webpet/domain/user/UserType.java << 'EOF'
package br.edu.utfpr.alunos.webpet.domain.user;

public enum UserType {
    ONG,
    PROTETOR
}
EOF

# 2. Reescrever DTOs completamente
echo "📝 Reescrevendo ONGRegisterDTO.java..."
cat > src/main/java/br/edu/utfpr/alunos/webpet/dto/ONGRegisterDTO.java << 'EOF'
package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CNPJ;

public record ONGRegisterDTO(
    @NotBlank(message = "CNPJ é obrigatório")
    @CNPJ(message = "CNPJ inválido")
    String cnpj,
    
    @NotBlank(message = "Nome da ONG é obrigatório")
    String nomeOng,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "Celular é obrigatório")
    String celular,
    
    @NotBlank(message = "Password é obrigatório")
    String password
) implements RegisterDTO {
    @Override
    public UserType getUserType() {
        return UserType.ONG;
    }
    
    @Override
    public String getEmail() {
        return email();
    }
    
    @Override
    public String getPassword() {
        return password();
    }
}
EOF

echo "📝 Reescrevendo ProtetorRegisterDTO.java..."
cat > src/main/java/br/edu/utfpr/alunos/webpet/dto/ProtetorRegisterDTO.java << 'EOF'
package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record ProtetorRegisterDTO(
    @NotBlank(message = "Nome completo é obrigatório")
    String nomeCompleto,
    
    @NotBlank(message = "CPF é obrigatório")
    @CPF(message = "CPF inválido")
    String cpf,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "Celular é obrigatório")
    String celular,
    
    @NotBlank(message = "Password é obrigatório")
    String password
) implements RegisterDTO {
    @Override
    public UserType getUserType() {
        return UserType.PROTETOR;
    }
    
    @Override
    public String getEmail() {
        return email();
    }
    
    @Override
    public String getPassword() {
        return password();
    }
}
EOF

echo "📝 Reescrevendo RegisterDTO.java..."
cat > src/main/java/br/edu/utfpr/alunos/webpet/dto/RegisterDTO.java << 'EOF'
package br.edu.utfpr.alunos.webpet.dto;

import br.edu.utfpr.alunos.webpet.domain.user.UserType;

public interface RegisterDTO {
    UserType getUserType();
    String getEmail();
    String getPassword();
}
EOF

# 3. Criar application.properties limpo
echo "📝 Recriando application.properties..."
cat > src/main/resources/application.properties << 'EOF'
spring.application.name=WebPet
server.port=8081
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/webpet_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
api.security.token.secret=macaco-branco-com-tenis-da-nike
EOF

# 4. Limpar containers e volumes
echo "🐳 Limpando ambiente Docker..."
docker compose down -v
docker system prune -f

# 5. Limpar arquivos compilados
echo "🧹 Limpando target..."
rm -rf target/
rm -rf .m2/

# 6. Compilar e executar
echo "🚀 Iniciando containers..."
docker compose up --build -d

# 7. Aguardar inicialização
echo "⏳ Aguardando serviços (30s)..."
sleep 30

# 8. Verificar status
echo "✅ Verificando status..."
docker compose ps
echo ""
echo "📋 Últimos logs da aplicação:"
docker compose logs --tail=50 app

echo ""
echo "🎯 Comandos úteis:"
echo "   docker compose logs -f app     # Ver logs em tempo real"
echo "   docker compose restart app      # Reiniciar aplicação"
echo "   ./test-endpoints.sh             # Testar endpoints"
echo ""
echo "🌐 Aplicação disponível em: http://localhost:8081"