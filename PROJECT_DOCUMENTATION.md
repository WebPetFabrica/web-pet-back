# 📚 WebPet - Documentação Completa do Projeto

## 📖 Índice
1. [Visão Geral](#visão-geral)
2. [Arquitetura do Sistema](#arquitetura-do-sistema)
3. [Tecnologias Utilizadas](#tecnologias-utilizadas)
4. [Estrutura do Projeto](#estrutura-do-projeto)
5. [Modelos de Dados](#modelos-de-dados)
6. [APIs e Endpoints](#apis-e-endpoints)
7. [Autenticação e Segurança](#autenticação-e-segurança)
8. [Sistema de Cache](#sistema-de-cache)
9. [Configuração de Ambiente](#configuração-de-ambiente)
10. [Deploy e Produção](#deploy-e-produção)
11. [Testes](#testes)
12. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral

O **WebPet** é uma plataforma digital que conecta pessoas interessadas em adoção de animais com ONGs e protetores independentes. O sistema facilita o processo de adoção e permite doações para apoiar o cuidado dos animais.

### 🎪 Funcionalidades Principais:
- **Cadastro de Pets** para adoção
- **Sistema de Usuários** multi-perfil (Usuário, Protetor, ONG)
- **Processo de Adoção** simplificado
- **Sistema de Doações** (monetárias e materiais)
- **Geolocalização** para facilitar encontros
- **Sistema de Notificações**

---

## 🏗️ Arquitetura do Sistema

### 📐 Padrão Arquitetural
O projeto segue uma **arquitetura em camadas** (Layered Architecture) com os seguintes níveis:

```
┌─────────────────────────────────────┐
│           Controllers               │ ← API REST Layer
├─────────────────────────────────────┤
│            Services                 │ ← Business Logic Layer  
├─────────────────────────────────────┤
│          Repositories               │ ← Data Access Layer
├─────────────────────────────────────┤
│            Domain                   │ ← Entity Layer
└─────────────────────────────────────┘
```

### 🔧 Componentes Principais:

#### **Controllers** (`src/main/java/.../controllers/`)
- **AuthController**: Autenticação e autorização
- **UserController**: Gestão de usuários
- **PetController**: CRUD de pets
- **DonationController**: Sistema de doações

#### **Services** (`src/main/java/.../services/`)
- **Camada de lógica de negócio**
- **Validações complexas**
- **Orquestração de operações**
- **Cache management**

#### **Repositories** (`src/main/java/.../repositories/`)
- **Acesso a dados com Spring Data JPA**
- **Queries customizadas**
- **Projeções otimizadas**

#### **Domain** (`src/main/java/.../domain/`)
- **Entidades JPA**
- **Value Objects**
- **Enums de domínio**

---

## 💻 Tecnologias Utilizadas

### 🖥️ Backend Core
- **Java 21** - Linguagem de programação
- **Spring Boot 3.4.5** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM
- **MapStruct** - Mapeamento DTO ↔ Entity

### 🗃️ Banco de Dados
- **H2 Database** - Desenvolvimento
- **PostgreSQL** - Produção
- **Flyway** - Migrações de banco

### ⚡ Cache e Performance
- **Redis** - Cache distribuído (produção)
- **Caffeine** - Cache local
- **JCache API** - Abstração de cache

### 📊 Documentação e Testes
- **OpenAPI 3** / **Swagger** - Documentação de APIs
- **JUnit 5** - Testes unitários
- **TestContainers** - Testes de integração

### 🔧 Infraestrutura
- **Docker** - Containerização
- **Maven** - Gerenciamento de dependências
- **Logback** - Sistema de logs

---

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/edu/utfpr/alunos/webpet/
│   │   ├── controllers/          # REST Controllers
│   │   ├── domain/              # Entidades de Domínio
│   │   │   ├── user/           # User, ONG, Protetor
│   │   │   ├── pet/            # Pet, Adoção
│   │   │   └── donation/       # Doações
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── repositories/       # Repositories JPA
│   │   ├── services/           # Lógica de Negócio
│   │   ├── infra/              # Infraestrutura
│   │   │   ├── config/         # Configurações
│   │   │   ├── security/       # Segurança
│   │   │   ├── cache/          # Cache
│   │   │   ├── validation/     # Validadores
│   │   │   └── exception/      # Tratamento de erros
│   │   └── mapper/             # Mapeadores DTO
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       ├── application-prod.properties
│       └── db/migration/       # Scripts Flyway
└── test/                       # Testes automatizados
```

---

## 🗂️ Modelos de Dados

### 👤 **Usuários**

#### **BaseUser** (Superclasse)
```java
@MappedSuperclass
public abstract class BaseUser {
    private String id;
    private String email;
    private String password;
    private String celular;
    private UserType userType;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### **User** (Usuário Comum)
```java
@Entity
public class User extends BaseUser {
    private String name;
    private String surname;
}
```

#### **Protetor** (Protetor Independente)
```java
@Entity  
public class Protetor extends BaseUser {
    private String nomeCompleto;
    private String cpf;
    private String endereco;
    private Integer capacidadeAcolhimento;
}
```

#### **ONG** (Organização)
```java
@Entity
public class ONG extends BaseUser {
    private String nomeOng;
    private String cnpj;
    private String endereco;
    private String descricao;
}
```

### 🐕 **Pet**
```java
@Entity
public class Pet {
    private String id;
    private String nome;
    private Especie especie;
    private String raca;
    private Genero genero;
    private Porte porte;
    private LocalDate dataNascimento;
    private String descricao;
    private String fotoUrl;
    private StatusAdocao statusAdocao;
    private String responsavelId;  // ID do Protetor/ONG
    private boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
```

### 💰 **Donation**
```java
@Entity
public class Donation {
    private String id;
    private String nomeDoador;
    private String emailDoador;
    private String telefoneDoador;
    private BigDecimal valor;
    private TipoDoacao tipoDoacao;
    private String mensagem;
    private StatusDoacao statusDoacao;
    private String beneficiarioId;  // ID do Protetor/ONG
    private String transactionId;
    private LocalDateTime criadoEm;
    private LocalDateTime processadoEm;
}
```

### 📧 **EmailConfirmation**
```java
@Entity
public class EmailConfirmation {
    private String id;
    private String userId;
    private String email;
    private String token;
    private boolean confirmed;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
}
```

---

## 🔌 APIs e Endpoints

### 🔐 **Autenticação** (`/api/auth`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/register` | Cadastro de usuário comum |
| `POST` | `/register/protetor` | Cadastro de protetor |
| `POST` | `/register/ong` | Cadastro de ONG |
| `POST` | `/login` | Login no sistema |
| `POST` | `/logout` | Logout |
| `POST` | `/refresh` | Renovar token JWT |

#### **Exemplo de Registro:**
```json
POST /api/auth/register
{
  "name": "João Silva",
  "surname": "Santos",
  "email": "joao@email.com",
  "password": "MinhaSenh@123",
  "celular": "(11) 99999-9999"
}
```

### 👥 **Usuários** (`/api/users`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/profile` | Perfil do usuário logado |
| `PUT` | `/profile` | Atualizar perfil |
| `GET` | `/protetores` | Listar protetores |
| `GET` | `/ongs` | Listar ONGs |

### 🐾 **Pets** (`/api/pets`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/` | Listar pets disponíveis |
| `GET` | `/{id}` | Detalhes de um pet |
| `POST` | `/` | Cadastrar novo pet |
| `PUT` | `/{id}` | Atualizar pet |
| `DELETE` | `/{id}` | Remover pet |
| `GET` | `/search` | Buscar pets com filtros |

#### **Exemplo de Cadastro de Pet:**
```json
POST /api/pets
{
  "nome": "Rex",
  "especie": "CACHORRO",
  "raca": "Labrador",
  "genero": "MACHO",
  "porte": "GRANDE",
  "dataNascimento": "2020-06-15",
  "descricao": "Cão muito carinhoso e brincalhão",
  "fotoUrl": "https://exemplo.com/foto-rex.jpg"
}
```

### 💝 **Doações** (`/api/donations`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/` | Fazer doação |
| `GET` | `/my-donations` | Minhas doações |
| `GET` | `/received` | Doações recebidas |
| `GET` | `/{id}` | Detalhes da doação |

#### **Exemplo de Doação:**
```json
POST /api/donations
{
  "nomeDoador": "Maria Silva",
  "emailDoador": "maria@email.com",
  "telefoneDoador": "(11) 88888-8888",
  "valor": 100.00,
  "tipoDoacao": "MONETARIA",
  "mensagem": "Para ajudar com ração",
  "beneficiarioId": "ong-123"
}
```

---

## 🔒 Autenticação e Segurança

### 🎫 **JWT (JSON Web Tokens)**
- **Access Token**: Válido por 2 horas
- **Refresh Token**: Válido por 7 dias
- **Algorithm**: HMAC SHA256
- **Claims**: user_id, email, roles

### 🛡️ **Configurações de Segurança**

#### **Rate Limiting**
- **Login**: 5 tentativas por minuto
- **APIs gerais**: 100 requests por minuto
- **Upload**: 10 uploads por minuto

#### **Validações**
- **Senhas**: Mínimo 8 caracteres, maiúscula, minúscula, número, símbolo
- **CPF/CNPJ**: Validação com dígitos verificadores
- **Email**: Formato válido + confirmação obrigatória

#### **Headers de Segurança**
```yaml
security.headers:
  - X-Content-Type-Options: nosniff
  - X-Frame-Options: DENY
  - X-XSS-Protection: 1; mode=block
  - Strict-Transport-Security: max-age=31536000
```

### 🔑 **Exemplo de Autenticação**
```bash
# 1. Login
POST /api/auth/login
{
  "email": "usuario@email.com", 
  "password": "senha123"
}

# Resposta:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "user-123",
    "email": "usuario@email.com",
    "userType": "USER"
  }
}

# 2. Usar token nas requisições
GET /api/pets
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

---

## ⚡ Sistema de Cache

### 📚 **Estratégia Multi-layer**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   L1 Cache      │───▶│   L2 Cache      │───▶│    Database     │
│   (Caffeine)    │    │   (Redis)       │    │  (PostgreSQL)   │
│   In-Memory     │    │   Distributed   │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 🎯 **Configurações de Cache**

#### **Desenvolvimento** (application-dev.properties)
```properties
spring.cache.type=simple
spring.cache.cache-names=users,pets,ongs,protetores
```

#### **Produção** (application-prod.properties)
```properties
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=${REDIS_PASSWORD}
spring.cache.redis.time-to-live=3600000
```

### 📋 **Dados Cacheados**
- **Usuários**: Perfis e autenticação (TTL: 1h)
- **Pets**: Lista de pets disponíveis (TTL: 30min)  
- **ONGs/Protetores**: Informações públicas (TTL: 2h)
- **Queries frequentes**: Relatórios e estatísticas (TTL: 15min)

---

## ⚙️ Configuração de Ambiente

### 🏃‍♂️ **Execução Local**

#### **Pré-requisitos**
- Java 21+
- Maven 3.8+
- Docker (opcional)

#### **Passos**
```bash
# 1. Clone o projeto
git clone <repository-url>
cd web-pet-back

# 2. Configure as variáveis de ambiente
export JWT_SECRET=$(openssl rand -base64 64)
export DB_PASSWORD=mypassword

# 3. Execute a aplicação
mvn spring-boot:run -Dspring.profiles.active=dev

# 4. Acesse
# API: http://localhost:8081
# Swagger: http://localhost:8081/swagger-ui/index.html
# H2 Console: http://localhost:8081/h2-console
```

### 🐳 **Execução com Docker**

#### **Docker Compose** (Desenvolvimento)
```yaml
version: '3.8'
services:
  webpet-backend:
    build: .
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JWT_SECRET=${JWT_SECRET}
    volumes:
      - ./uploads:/app/uploads

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

```bash
# Executar
docker-compose up -d
```

### 🌍 **Variáveis de Ambiente**

#### **Obrigatórias**
```bash
JWT_SECRET=<sua-chave-secreta-base64>
DB_PASSWORD=<senha-do-banco>
```

#### **Opcionais**
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=webpet
DB_USERNAME=webpet_user

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=
SMTP_PASSWORD=

# Upload
UPLOAD_DIR=/app/uploads
MAX_FILE_SIZE=10MB
```

---

## 🚀 Deploy e Produção

### 🏗️ **Docker Compose Produção**

```yaml
version: '3.8'
services:
  webpet-app:
    image: webpet/backend:latest
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JWT_SECRET=${JWT_SECRET}
      - DB_HOST=postgres
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
    restart: unless-stopped

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=webpet
      - POSTGRES_USER=webpet_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - webpet-app
    restart: unless-stopped

volumes:
  postgres_data:
```

### 📋 **Checklist de Deploy**

#### **Antes do Deploy**
- [ ] Variáveis de ambiente configuradas
- [ ] Banco de dados criado
- [ ] Migrações Flyway executadas
- [ ] Certificados SSL configurados
- [ ] Backup configurado

#### **Após Deploy**  
- [ ] Health check: `GET /actuator/health`
- [ ] Logs verificados
- [ ] Métricas funcionando
- [ ] Endpoints testados

### 📊 **Monitoramento**

#### **Health Checks**
```yaml
# application-prod.properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

#### **Logs Estruturados**
```json
{
  "timestamp": "2025-01-08T10:30:00Z",
  "level": "INFO", 
  "logger": "WebPetApplication",
  "message": "User authenticated successfully",
  "correlationId": "abc-123-def",
  "userId": "user-456",
  "clientIp": "192.168.1.100"
}
```

---

## 🧪 Testes

### 📝 **Estrutura de Testes**

```
src/test/java/
├── controllers/           # Testes de Controllers (Integration)
├── services/             # Testes de Services (Unit)  
├── repositories/         # Testes de Repositories (Data)
└── infra/               # Testes de Infraestrutura
```

### 🔬 **Tipos de Teste**

#### **Testes Unitários**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks 
    private UserServiceImpl userService;
    
    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        var userRequest = new RegisterRequestDTO(/*...*/);
        
        // When
        var result = userService.createUser(userRequest);
        
        // Then
        assertThat(result.email()).isEqualTo("test@email.com");
        verify(userRepository).save(any());
    }
}
```

#### **Testes de Integração**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("webpet_test")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("test@email.com"));
    }
}
```

### ▶️ **Executar Testes**

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=UserServiceTest

# Testes com coverage
mvn clean test jacoco:report

# Testes de integração apenas
mvn test -Dgroups=integration
```

---

## 🔧 Troubleshooting

### ❗ **Problemas Comuns**

#### **1. Aplicação não inicia**
```bash
# Verificar Java version
java -version  # Deve ser 21+

# Verificar variáveis
echo $JWT_SECRET

# Logs detalhados
java -jar app.jar --debug --logging.level.root=DEBUG
```

#### **2. Erro de conexão com banco**
```bash
# Verificar se postgres está rodando
docker ps | grep postgres

# Testar conexão
psql -h localhost -U webpet_user -d webpet

# Verificar logs
docker logs postgres-container
```

#### **3. Cache não funciona**
```bash
# Redis em produção
redis-cli ping  # Deve retornar PONG

# Verificar configuração
curl http://localhost:8081/actuator/caches
```

#### **4. JWT inválido**
```bash
# Regenerar secret
export JWT_SECRET=$(openssl rand -base64 64)

# Verificar formato
echo $JWT_SECRET | base64 -d | wc -c  # Deve ser >= 32
```

### 📊 **Logs Importantes**

#### **Startup Bem-sucedido**
```
Started WebPetApplication in 24.168 seconds
Tomcat started on port 8081
H2 console available at '/h2-console'
```

#### **Configuração de Cache**
```
Cache type: redis
Cache names: [users, pets, ongs, protetores]
```

#### **Segurança Configurada**
```
Security filter chain: [SecurityFilter, RateLimitFilter, ...]
CORS configured for origins: [http://localhost:3000]
```

### 🆘 **Suporte**

#### **Logs de Debug**
```properties
# application-dev.properties
logging.level.br.edu.utfpr.alunos.webpet=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

#### **Contatos**
- **Documentação**: Swagger UI (`/swagger-ui/index.html`)
- **Issues**: GitHub Issues
- **Discord**: Canal #webpet-backend

---

## 📈 **Métricas e Performance**

### 📊 **Endpoints de Monitoramento**
- `GET /actuator/health` - Status da aplicação
- `GET /actuator/metrics` - Métricas da aplicação
- `GET /actuator/info` - Informações da build

### ⚡ **Otimizações Implementadas**
- **Cache em múltiplas camadas**
- **Lazy loading** de relacionamentos JPA
- **Connection pooling** otimizado
- **Queries otimizadas** com projeções
- **Rate limiting** para proteção

---

*📝 Documentação atualizada em Janeiro 2025*
*🏫 Projeto desenvolvido para UTFPR - Universidade Tecnológica Federal do Paraná*