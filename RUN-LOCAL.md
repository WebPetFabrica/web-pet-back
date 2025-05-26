# Executando Localmente (Solução Alternativa)

## 1. Apenas o banco no Docker

```bash
docker compose -f docker-compose-simple.yml up -d
```

## 2. Aplicação Java local

```bash
# Terminal 1 - Banco
docker compose -f docker-compose-simple.yml up

# Terminal 2 - Aplicação
./mvnw clean
./mvnw spring-boot:run
```

## 3. Se persistir o erro de encoding

### Opção A - Criar novo application.properties

```bash
# Fazer backup
cp src/main/resources/application.properties src/main/resources/application.properties.bak

# Criar novo sem caracteres especiais
cat > src/main/resources/application.properties << 'EOF'
spring.application.name=WebPet
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5433/webpet_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
api.security.token.secret=macaco-branco-com-tenis-da-nike
EOF
```

### Opção B - Usar IDE

1. Abra o arquivo no IntelliJ/VSCode
2. Salve como UTF-8 sem BOM
3. Remova comentários com acentos

## 4. Build manual

```bash
# Limpar e compilar
./mvnw clean compile

# Testar
./mvnw test

# Executar
./mvnw spring-boot:run
```