# **WebPet Backend 🐾**

Backend robusto para a plataforma de adoção de animais WebPet. Desenvolvido com Java 21 e Spring Boot, o projeto oferece uma API RESTful completa, segura e performática para gerenciar usuários, pets para adoção e doações.

## ✨ **Funcionalidades Principais**

  * **Sistema Multi-Usuário:** Suporte para três tipos de perfis com papéis distintos:
      * `USER`: Adotantes em busca de um pet
      * `ONG`: Organizações que gerenciam múltiplos animais
      * `PROTETOR`: Protetores independentes
  * **Gerenciamento Completo de Pets:** CRUD completo para animais, incluindo detalhes como espécie, raça, porte, gênero, idade e status de adoção
  * **Busca e Filtragem Avançada:** API de listagem de pets com filtros por espécie, porte, gênero, idade e busca por texto livre
  * **Sistema de Doações:** Endpoint para receber doações destinadas a ONGs e Protetores
  * **Autenticação e Autorização:** Sistema seguro baseado em JWT com controle de acesso por papel
  * **Documentação de API:** Documentação interativa e automatizada com SpringDoc (OpenAPI 3)

## 🏗️ **Arquitetura do Sistema**

O projeto adota uma **Arquitetura em Camadas (Layered Architecture)**, promovendo separação de responsabilidades, alta coesão e baixo acoplamento entre os componentes.

```
┌───────────────────┐
│  Controllers (API)│  ← Camada de Apresentação (REST)
├───────────────────┤
│  Services         │  ← Camada de Negócio e Lógica
├───────────────────┤
│  Repositories     │  ← Camada de Acesso a Dados (JPA)
├───────────────────┤
│  Domain (Entities)│  ← Camada de Domínio
└───────────────────┘
```

Esta estrutura é suportada por um módulo de `infra`, que contém componentes transversais como segurança, configuração e tratamento de exceções.

## 🛠️ **Tech Stack**

| Categoria | Tecnologia | Versão | Propósito |
| :--- | :--- | :--- | :--- |
| **Linguagem & Framework** | Java | 21 | Linguagem principal do projeto |
| | Spring Boot | 3.4.5 | Framework que simplifica a criação de aplicações Spring |
| | Spring Modulith | 1.3.5 | Organização modular de aplicações monolíticas |
| **Segurança** | Spring Security | - | Gerencia autenticação e autorização |
| | JWT (java-jwt) | 4.4.0 | Tokens para autenticação stateless |
| **Acesso a Dados** | Spring Data JPA | - | Abstração para acesso ao banco de dados |
| | Hibernate | - | ORM para mapeamento objeto-relacional |
| **Banco de Dados** | PostgreSQL | 15 | Banco de dados principal (produção) |
| | H2 | - | Banco de dados em memória (testes) |
| **Migrações de BD** | Flyway | 10.8.1 | Versionamento e controle do schema |
| **Documentação da API** | SpringDoc (OpenAPI 3) | 2.3.0 | Geração de documentação interativa |
| **Mapeamento de DTOs** | MapStruct | 1.5.5 | Conversão eficiente entre entidades e DTOs |
| **Validação** | Hibernate Validator | - | Validação de dados de entrada |
| **Cache** | Caffeine | - | Cache em memória de alta performance |
| **Containerização** | Docker & Docker Compose | - | Empacotamento e orquestração |
| **Build** | Maven | 3.9+ | Gerenciamento de dependências e build |
| **Desenvolvimento** | Spring Boot DevTools | - | Hot reload e melhorias para desenvolvimento |
| **Utilitários** | Lombok | 1.18.32 | Redução de boilerplate code |

## 🔒 **Destaques de Segurança**

A segurança é um pilar fundamental deste projeto:

  * **Autenticação Robusta:** Implementação de JWT com expiração e validação
  * **Hashing de Senhas:** As senhas são protegidas usando o algoritmo BCrypt
  * **Políticas de Senha:** Validação de complexidade e checagem contra senhas comuns
  * **Histórico de Senhas:** Prevenção de reuso das últimas 5 senhas
  * **Proteção contra Brute-Force:** Limite de tentativas de login com bloqueio temporário
  * **Rate Limiting:** Controle de requisições por IP para evitar abuso da API
  * **Logging de Auditoria:** Logs detalhados para eventos de segurança e autenticação

## 📖 **Documentação da API (Swagger)**

A documentação completa e interativa da API está disponível via Swagger UI. Após iniciar a aplicação, acesse:

**http://localhost:8081/swagger-ui.html**

## ⚙️ **Configuração e Execução**

### **Pré-requisitos**

  * Java 21
  * Maven 3.9+
  * Docker e Docker Compose (Recomendado)

### **Variáveis de Ambiente**

Crie um arquivo `.env` na raiz do projeto com o seguinte conteúdo. Use o script `generate-jwt-secret.sh` para criar uma chave segura.

```bash
# Configurações do Banco de Dados
POSTGRES_DB=webpet_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres # Altere para uma senha segura em produção

# Configurações da Aplicação
APP_PORT=8081
DB_PORT=5433

# Chave Secreta para JWT (Use o script ./generate-jwt-secret.sh)
JWT_SECRET=sua-chave-secreta-super-segura-de-pelo-menos-64-caracteres-hexadecimais

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### **Como Executar**

#### **1. Com Docker (Recomendado)**

Este é o método mais simples e não requer instalação local do Java ou PostgreSQL.

```bash
docker compose up --build -d
```

A aplicação estará disponível em `http://localhost:8081`.

#### **2. Localmente com Maven**

Se você tiver o Java 21 e o Maven configurados localmente:

```bash
# Inicie um banco de dados PostgreSQL (pode ser via Docker)
docker run --name postgres-webpet -e POSTGRES_PASSWORD=postgres -p 5433:5432 -d postgres:15

# Execute a aplicação
./mvnw spring-boot:run
```

## 🧪 **Testes**

Para garantir a qualidade e a estabilidade do código, execute a suíte de testes automatizados:

```bash
./mvnw clean test
```


## 📁 **Estrutura do Projeto**

```
.
├── src
│   ├── main
│   │   ├── java/br/edu/utfpr/alunos/webpet
│   │   │   ├── WebPetApplication.java     # Classe principal da aplicação
│   │   │   ├── config/                    # Configurações gerais
│   │   │   │   └── WebConfig.java         # Configurações web
│   │   │   ├── controllers/               # Camada de apresentação - Endpoints REST
│   │   │   │   ├── AdminController.java   # Endpoints administrativos
│   │   │   │   ├── AdoptionController.java # Gestão de adoções
│   │   │   │   ├── AnimalController.java  # CRUD de animais
│   │   │   │   ├── AuthController.java    # Autenticação e registro
│   │   │   │   ├── DonationController.java # Sistema de doações
│   │   │   │   ├── ONGController.java     # Gestão de ONGs
│   │   │   │   ├── PetController.java     # CRUD de pets
│   │   │   │   └── UserController.java    # Gestão de usuários
│   │   │   ├── domain/                    # Camada de domínio - Entidades JPA
│   │   │   │   ├── adoption/              # Domínio de adoções
│   │   │   │   │   ├── AdoptionRequest.java
│   │   │   │   │   └── AdoptionStatus.java
│   │   │   │   ├── donation/              # Domínio de doações
│   │   │   │   │   ├── Donation.java
│   │   │   │   │   ├── StatusDoacao.java
│   │   │   │   │   └── TipoDoacao.java
│   │   │   │   ├── pet/                   # Domínio de pets
│   │   │   │   │   ├── Especie.java
│   │   │   │   │   ├── Genero.java
│   │   │   │   │   ├── Pet.java
│   │   │   │   │   ├── Porte.java
│   │   │   │   │   └── StatusAdocao.java
│   │   │   │   └── user/                  # Domínio de usuários
│   │   │   │       ├── Animal.java
│   │   │   │       ├── BaseUser.java      # Classe base para usuários
│   │   │   │       ├── EmailConfirmation.java
│   │   │   │       ├── ONG.java
│   │   │   │       ├── PasswordHistory.java
│   │   │   │       ├── Protetor.java
│   │   │   │       ├── User.java
│   │   │   │       └── UserType.java
│   │   │   ├── dto/                       # Objetos de Transferência de Dados
│   │   │   │   ├── adoption/              # DTOs de adoção
│   │   │   │   ├── auth/                  # DTOs de autenticação
│   │   │   │   ├── donation/              # DTOs de doação
│   │   │   │   ├── pet/                   # DTOs de pets
│   │   │   │   └── user/                  # DTOs de usuários
│   │   │   │       └── v1/                # Versionamento de API
│   │   │   ├── infra/                     # Infraestrutura e componentes transversais
│   │   │   │   ├── config/                # Configurações de infraestrutura
│   │   │   │   │   ├── CacheConfig.java   # Configuração de cache
│   │   │   │   │   ├── JpaOptimizationConfig.java
│   │   │   │   │   ├── OpenApiConfig.java # Configuração do Swagger
│   │   │   │   │   └── PaginationConfig.java
│   │   │   │   ├── cors/                  # Configurações CORS
│   │   │   │   ├── exception/             # Tratamento global de exceções
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── BusinessException.java
│   │   │   │   │   └── ErrorResponse.java
│   │   │   │   ├── logging/               # Sistema de auditoria e logs
│   │   │   │   │   ├── AuditLogger.java
│   │   │   │   │   ├── CorrelationIdInterceptor.java
│   │   │   │   │   └── ExceptionLogger.java
│   │   │   │   ├── openapi/               # Exemplos para documentação
│   │   │   │   ├── security/              # Segurança e autenticação
│   │   │   │   │   ├── SecurityConfig.java # Configuração Spring Security
│   │   │   │   │   ├── TokenService.java   # Gestão de tokens JWT
│   │   │   │   │   ├── SecurityFilter.java # Filtro de segurança
│   │   │   │   │   ├── RateLimitFilter.java # Rate limiting
│   │   │   │   │   └── CustomUserDetailsService.java
│   │   │   │   └── validation/            # Validadores customizados
│   │   │   │       ├── CPF.java
│   │   │   │       ├── CNPJ.java
│   │   │   │       ├── ValidEmail.java
│   │   │   │       └── ValidPassword.java
│   │   │   ├── mapper/                    # Mapeadores MapStruct
│   │   │   │   ├── DonationMapper.java
│   │   │   │   ├── PetMapper.java
│   │   │   │   └── UserMapper.java
│   │   │   ├── repositories/              # Camada de acesso a dados - Spring Data JPA
│   │   │   │   ├── AdoptionRequestRepository.java
│   │   │   │   ├── BaseUserRepository.java
│   │   │   │   ├── DonationRepository.java
│   │   │   │   ├── ONGRepository.java
│   │   │   │   ├── PetRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── projections/           # Projeções JPA
│   │   │   │       └── UserProjections.java
│   │   │   ├── services/                  # Camada de negócio - Lógica de aplicação
│   │   │   │   ├── AnimalService.java
│   │   │   │   ├── FileStorageService.java
│   │   │   │   ├── auth/                  # Serviços de autenticação
│   │   │   │   │   ├── AuthenticationService.java
│   │   │   │   │   ├── AuthenticationServiceImpl.java
│   │   │   │   │   ├── LoginAttemptService.java
│   │   │   │   │   └── UserRegistrationService.java
│   │   │   │   ├── donation/              # Serviços de doação
│   │   │   │   ├── ong/                   # Serviços específicos de ONGs
│   │   │   │   ├── payment/               # Integração com gateways de pagamento
│   │   │   │   ├── pet/                   # Serviços de pets
│   │   │   │   ├── user/                  # Serviços de usuários
│   │   │   │   └── validation/            # Serviços de validação
│   │   │   │       ├── CommonPasswordService.java
│   │   │   │       ├── EmailValidationService.java
│   │   │   │       └── PasswordPolicyService.java
│   │   │   └── utils/                     # Utilitários e enums
│   │   │       └── enums/
│   │   └── resources
│   │       ├── db/migration/              # Scripts de migração Flyway
│   │       │   ├── V1__initial_schema.sql
│   │       │   ├── V2__security_enhancements.sql
│   │       │   ├── V3__create_pets_and_donations_tables.sql
│   │       │   ├── V4__add_user_profile_fields.sql
│   │       │   └── V5__create_performance_indexes.sql
│   │       ├── application.properties     # Configurações da aplicação
│   │       └── logback-spring.xml         # Configuração de logs
│   └── test/                              # Testes unitários e de integração
│       ├── java/br/edu/utfpr/alunos/webpet
│       │   ├── WebPetApplicationTests.java
│       │   ├── controllers/               # Testes de controllers
│       │   │   └── UserControllerTest.java
│       │   └── services/                  # Testes de serviços
│       │       └── auth/                  # Testes de autenticação
│       │           ├── AuthenticationServiceTest.java
│       │           └── UserRegistrationServiceTest.java
│       └── resources
│           └── application-test.properties # Configurações para testes
├── database/                              # Configurações de banco de dados
│   └── init/                              # Scripts de inicialização
├── .dockerignore
├── .gitignore
├── docker-compose.yml                     # Orquestração Docker
├── Dockerfile                             # Imagem Docker da aplicação
├── mvnw                                   # Maven wrapper (Unix)
├── mvnw.cmd                               # Maven wrapper (Windows)
├── pom.xml                                # Configuração Maven e dependências
├── generate-jwt-secret.sh                 # Script para gerar JWT secret seguro
└── README.md                              # Documentação do projeto

```

## 🐳 **Docker**

### **Pré-requisitos**
- Docker
- Docker Compose

### **Execução**
```bash
docker-compose up --build
```

### **Acesso**
- API: http://localhost:8081
- Swagger: http://localhost:8081/swagger-ui.html

### **Parar**
```bash
docker-compose down
```

## 📊 **Banco de Dados**

### **Migrações com Flyway**

O projeto utiliza Flyway para versionamento do banco de dados. As migrações estão em `src/main/resources/db/migration/`:

- `V1__initial_schema.sql`: Estrutura inicial com tabelas de usuários
- `V2__security_enhancements.sql`: Melhorias de segurança (histórico de senhas, confirmação de email)
- `V3__create_pets_and_donations_tables.sql`: Tabelas principais do negócio

### **Modelo de Dados**

- **users**: Usuários comuns (adotantes)
- **ongs**: Organizações não-governamentais
- **protetores**: Protetores independentes
- **pets**: Animais disponíveis para adoção
- **doacoes**: Transações de doações

## 📝 **Endpoints Principais**

### **Autenticação**
- `POST /auth/login` - Login universal
- `POST /auth/register` - Registro de usuário comum
- `POST /auth/register/ong` - Registro de ONG
- `POST /auth/register/protetor` - Registro de protetor

### **Pets**
- `GET /pets` - Listar pets disponíveis (com filtros)
- `POST /pets` - Cadastrar novo pet (ONGs/Protetores)
- `PUT /pets/{id}` - Atualizar informações do pet
- `DELETE /pets/{id}` - Remover pet

### **Doações**
- `POST /doacoes` - Criar nova doação
- `GET /doacoes/beneficiario/{id}` - Doações recebidas
- `GET /doacoes/doador/{id}` - Doações realizadas

Para a documentação completa, acesse o Swagger UI.

## 👥 **Equipe**

Desenvolvido pelos alunos do 6º período de Engenharia de Software da UTFPR como parte do projeto da disciplina de Fábrica de Software.