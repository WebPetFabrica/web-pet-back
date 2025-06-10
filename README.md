# WebPet 🐾

Aplicação para gerenciamento de adoção de animais desenvolvida com Spring Boot.

## 📚 Descrição

Este projeto tem como objetivo facilitar o processo de adoção de animais, oferecendo funcionalidades para cadastro, gerenciamento e autenticação de usuários, além de visualização e administração dos animais disponíveis para adoção.

## 🚀 Tecnologias Utilizadas

### ✅ Back-end (Java + Spring Boot)

- **Java 21**  
  Linguagem principal do projeto.

- **Spring Boot 3.4.5**  
  Framework que simplifica a criação de aplicações Spring.

- **Spring Web**  
  Permite criar APIs REST de forma simples e organizada.

- **Spring Security**  
  Gerencia autenticação e autorização, protegendo as rotas da aplicação.

- **Spring Data JPA**  
  Abstrai o acesso ao banco de dados, usando a especificação JPA.

- **Spring Modulith**  
  Auxilia na organização modular de aplicações monolíticas, separando responsabilidades e facilitando testes e manutenção.

- **PostgreSQL Driver** (`org.postgresql`)  
  Driver JDBC utilizado para conectar a aplicação com o banco de dados PostgreSQL.

- **Lombok**  
  Biblioteca que reduz o boilerplate de código (como getters, setters e construtores), usando anotações.

- **Auth0 Java JWT** (`com.auth0:java-jwt`)  
  Utilizado para criar, assinar e validar tokens JWT para autenticação baseada em token.

### 🧪 Testes

- **Spring Boot Starter Test**  
  Conjunto de ferramentas para testes de unidade e integração.

- **Spring Security Test**  
  Auxilia na simulação e verificação de autenticação/autorização em testes.

### 💻 Desenvolvimento

- **Spring Boot DevTools**  
  Fornece melhorias para o desenvolvimento local, como reload automático ao salvar arquivos.

### 🛠️ Build

- **Maven**  
  Ferramenta de build e gerenciamento de dependências.

---

## ⚙️ Configuração do Projeto

### Pré-requisitos

- Java 21 instalado
- PostgreSQL configurado
- Maven instalado

### Clonando o Repositório

```bash
git clone https://github.com/WebPetFabrica/web-pet-back.git
```
## 🚀 Execução Local:
#### Abra o arquivo com o IntelliJ ou eclipse(Sugiro IntelliJ) e em seguida basta executar o seguinte comando no terminal:
```bash
./mvnw spring-boot:run
```

#### Ou via IDE como IntelliJ ou Eclipse, basta rodar a classe WebPetApplication.

## 🐳 Com Docker
Para executar o projeto com Docker, consulte as [instruções detalhadas de Docker](DOCKER.md).

## 🔑 API de Autenticação

A API oferece endpoints para registro e login de usuários. Exemplos de JSON para essas operações estão disponíveis na pasta `src/main/resources/`.

### Registro de Usuário

**Endpoint:** `POST /auth/register`

Exemplos de JSON para registro:
- [Usuário Físico](src/main/resources/requestUsers/register-example.json)
- [Usuário Jurídico](src/main/resources/requestUsers/register-juridico-example.json)
- [Usuário Administrador](src/main/resources/requestUsers/register-admin-example.json)

### Login de Usuário

**Endpoint:** `POST /auth/login`

Exemplo de JSON para login:
- [Login](src/main/resources/requestUsers/login-example.json)

### Resposta da API

Após um registro ou login bem-sucedido, a API retorna:
- [Exemplo de Resposta](src/main/resources/requestUsers/auth-response-example.json)

O token retornado deve ser incluído no cabeçalho `Authorization` das requisições subsequentes como um token Bearer:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Para mais detalhes sobre os formatos JSON, consulte a [documentação de exemplos JSON](src/main/resources/requestUsers/json-examples.md).
