# WebPet 🐾

Aplicação para gerenciamento de adoção de animais desenvolvida com Spring Boot.

## 📚 Descrição

Este projeto tem como objetivo centralizar e facilitar o processo de adoção de animais, oferecendo uma plataforma confiável, atualizada e de fácil acesso. Ele disponibiliza funcionalidades para cadastro, autenticação e gerenciamento de usuários, além de permitir a visualização e administração dos animais disponíveis para adoção.

## 🏗️ Arquitetura

### Decisões Arquiteturais

- **Herança Table Per Class**: Estratégia de herança para usuários (User, ONG, Protetor)
- **UUID como Primary Key**: Melhor escalabilidade e segurança
- **Soft Delete**: Flag `active` para preservar dados de auditoria
- **JWT Authentication**: Tokens com expiração de 2 horas
- **BCrypt Password Encoding**: Criptografia robusta para senhas
- **Rate Limiting**: Proteção contra ataques de força bruta
- **Correlation ID**: Rastreamento de requisições para logs

### Regras de Negócio

#### Autenticação
- Email único entre todos os tipos de usuário
- CNPJ único para ONGs
- CPF único para Protetores
- Bloqueio após 5 tentativas de login falhas
- Lockout de 30 minutos após bloqueio

#### Usuários
- Soft delete preserva dados para auditoria
- Timestamps automáticos (created_at, updated_at)
- Validação de CPF/CNPJ via anotações customizadas

## 🚀 Tecnologias Utilizadas

### ✅ Back-end (Java + Spring Boot)

- **Java 21** - Linguagem principal do projeto
- **Spring Boot 3.4.5** - Framework principal
- **Spring Web** - APIs REST
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Acesso ao banco de dados
- **PostgreSQL** - Banco de dados principal
- **JWT (Auth0)** - Tokens de autenticação
- **MapStruct** - Mapeamento de DTOs
- **SpringDoc OpenAPI** - Documentação da API

### 🧪 Testes

- **Spring Boot Starter Test** - Testes unitários e integração
- **H2 Database** - Banco em memória para testes

### 💻 Desenvolvimento

- **Spring Boot DevTools** - Hot reload
- **Lombok** - Redução de boilerplate

## 📖 Documentação da API

### Swagger UI
- **URL**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api-docs

### Endpoints Principais

#### Autenticação
- `POST /auth/login` - Login universal
- `POST /auth/register` - Cadastro usuário comum
- `POST /auth/register/ong` - Cadastro ONG
- `POST /auth/register/protetor` - Cadastro protetor

#### Usuário (Autenticado)
- `GET /user` - Perfil atual
- `GET /user/{id}` - Usuário por ID
- `PATCH /user/deactivate` - Desativar conta
- `PATCH /user/activate` - Ativar conta

## ⚙️ Configuração do Projeto

### Pré-requisitos

- Java 21
- PostgreSQL
- Maven
- Docker (opcional)

### Variáveis de Ambiente

```bash
# Banco de dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/webpet_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Segurança
JWT_SECRET=sua-chave-secreta-256-bits

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

### Clonando o Repositório

```bash
git clone https://github.com/WebPetFabrica/web-pet-back.git
cd web-pet-back
```

## 🚀 Execução

### Local com Maven
```bash
./mvnw spring-boot:run
```

### Com Docker
```bash
docker compose up -d
```

## 📋 Estrutura do Projeto

```
src/main/java/br/edu/utfpr/alunos/webpet/
├── controllers/              # REST Controllers
├── domain/                   # Entidades de domínio
│   └── user/                # Hierarquia de usuários
├── dto/                     # Data Transfer Objects
│   ├── auth/               # DTOs de autenticação
│   └── user/               # DTOs de usuário
├── infra/                  # Infraestrutura
│   ├── config/            # Configurações
│   ├── exception/         # Tratamento de exceções
│   ├── logging/           # Sistema de logs
│   ├── openapi/           # Documentação OpenAPI
│   ├── security/          # Configurações de segurança
│   └── validation/        # Validadores customizados
├── mapper/                # MapStruct mappers
├── repositories/          # Repositórios JPA
└── services/             # Serviços de negócio
    ├── auth/             # Serviços de autenticação
    ├── cache/            # Serviços de cache
    └── user/             # Serviços de usuário
```

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes com coverage
./mvnw test jacoco:report
```

## 📊 Monitoramento

### Logs
- Correlation ID para rastreamento
- Structured logging com Logback
- Diferentes níveis por ambiente

### Cache
- Redis para cache distribuído
- Métricas de hit/miss ratio
- TTL configurável por tipo de cache

### Segurança
- Rate limiting configurável
- Audit logs para autenticação
- Monitoramento de tentativas de login

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma feature branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Add nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Equipe

**Definições e Acordos**
* Papéis definidos:
   * **PO**: Anna Hellen A. Moura
   * **Líder Técnico**: Juliano Araujo e Rodrigo Fries
   * **Desenvolvedores**: Juliano Araujo, Rodrigo Fries, Victor Galvão, Gabriel Guarnieri, Ana Clara Santana
   * **QA / Testes**: Gabriela Barbieri
   * **UX/UI**: Luis Henrique

---

Para mais informações, consulte a [documentação da API](http://localhost:8081/swagger-ui.html) ou os [guias do Spring Boot](https://spring.io/guides).