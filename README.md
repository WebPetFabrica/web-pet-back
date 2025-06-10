# **WebPet Backend 🐾**

[](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[](https://spring.io/projects/spring-boot)
[](https://www.google.com/search?q=pom.xml)
[](https://www.google.com/search?q=%23-destaques-de-seguran%C3%A7a)
[](https://www.google.com/search?q=LICENSE)

Backend robusto para a plataforma de adoção de animais WebPet. Desenvolvido com Java 21 e Spring Boot, o projeto oferece uma API RESTful completa, segura e performática para gerenciar usuários, pets para adoção e doações.

## ✨ **Funcionalidades Principais**

  * **Sistema Multi-Usuário:** Suporte para três tipos de perfis com papéis distintos:
      * `USER`: Adotantes em busca de um pet.
      * `ONG`: Organizações que gerenciam múltiplos animais.
      * `PROTETOR`: Protetores independentes.
  * **Gerenciamento Completo de Pets:** CRUD completo para animais, incluindo detalhes como espécie, raça, porte, gênero, idade e status de adoção.
  * **Busca e Filtragem Avançada:** API de listagem de pets com filtros por espécie, porte, gênero, idade e busca por texto livre.
  * **Sistema de Doações:** Endpoint para receber doações destinadas a ONGs e Protetores.
  * **Autenticação e Autorização:** Sistema seguro baseado em `JWT` com controle de acesso por papel.
  * **Documentação de API:** Documentação interativa e automatizada com SpringDoc (OpenAPI 3).

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

| Categoria | Tecnologia | Propósito |
| :--- | :--- | :--- |
| **Linguagem & Framework** | Java 21 & Spring Boot 3.4.5 | Core da aplicação |
| **Segurança** | Spring Security, JWT (java-jwt) | Autenticação, autorização e segurança de endpoints |
| **Acesso a Dados** | Spring Data JPA, Hibernate | Persistência de dados e ORM |
| **Banco de Dados** | PostgreSQL (Produção), H2 (Desenvolvimento/Testes) | Armazenamento de dados relacional |
| **Migrações de BD** | Flyway | Versionamento e controle do schema do banco de dados |
| **Documentação da API**| SpringDoc (OpenAPI 3) | Geração de documentação interativa da API |
| **Mapeamento de DTOs** | MapStruct | Mapeamento de objetos entre camadas (Entidade ↔ DTO) |
| **Validação** | Hibernate Validator | Validação de dados de entrada |
| **Cache** | Caffeine | Cache em memória para alta performance |
| **Containerização** | Docker, Docker Compose | Empacotamento e orquestração de ambientes |

## 🔒 **Destaques de Segurança**

A segurança é um pilar fundamental deste projeto.

  * **Autenticação Robusta:** Implementação de JWT com expiração e validação.
  * **Hashing de Senhas:** As senhas são protegidas usando o algoritmo BCrypt.
  * **Políticas de Senha:** Validação de complexidade e checagem contra senhas comuns.
  * **Histórico de Senhas:** Prevenção de reuso das últimas 5 senhas.
  * **Proteção contra Brute-Force:** Limite de tentativas de login com bloqueio temporário de conta.
  * **Rate Limiting:** Controle de requisições por IP para evitar abuso da API.
  * **Logging de Auditoria:** Logs detalhados para eventos de segurança e autenticação.

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

Se você tiver o Java 21 e o Maven configurados localmente.

```bash
# Inicie um banco de dados PostgreSQL (pode ser via Docker)
docker run --name postgres-webpet -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15

# Execute a aplicação
./mvnw spring-boot:run
```

## 🧪 **Testes**

Para garantir a qualidade e a estabilidade do código, execute a suíte de testes automatizados.

```bash
./mvnw clean test
```

O script `validate-pr.sh` também executa a limpeza, compilação e os testes, simulando um ambiente de CI.

## 📁 **Estrutura do Projeto**

```
.
├── src
│   ├── main
│   │   ├── java/br/edu/utfpr/alunos/webpet
│   │   │   ├── controllers/       # Endpoints da API REST
│   │   │   ├── domain/            # Entidades JPA (User, Pet, etc.)
│   │   │   ├── dto/               # Objetos de Transferência de Dados
│   │   │   ├── infra/             # Infraestrutura (Segurança, Config, Exceções)
│   │   │   ├── mapper/            # Mapeadores (MapStruct)
│   │   │   ├── repositories/      # Interfaces do Spring Data JPA
│   │   │   └── services/          # Lógica de negócio
│   │   └── resources
│   │       ├── db/migration/      # Scripts de migração (Flyway)
│   │       ├── application.properties
│   │       └── logback-spring.xml # Configuração de logs
│   └── test/                    # Testes unitários e de integração
├── .dockerignore
├── .gitignore
├── docker-compose.yml
├── pom.xml                      # Dependências e build do projeto
└── README.md
```