# 🐳 Executando WebPet com Docker

Este documento descreve como executar o projeto WebPet utilizando Docker, facilitando o desenvolvimento e implantação sem necessidade de configurar o ambiente local.

## 📋 Pré-requisitos

- Docker instalado
- Docker Compose instalado

## 🚀 Passo a Passo

### 1. Clone o Repositório

```bash
git clone https://github.com/WebPetFabrica/web-pet-back.git
cd web-pet-back
```

### 2. Execute os Containers

```bash
docker compose up -d
```

Este comando irá:
- Construir o container da aplicação Spring Boot
- Iniciar um container PostgreSQL 
- Configurar a rede entre os containers
- Expor a aplicação na porta 8080
- Expor o banco na porta 5433 (para evitar conflitos com instalações locais)

### 3. Verifique o Status

```bash
docker compose ps
```

Você deverá ver dois containers em execução:
- `web-pet-back_app_1`
- `web-pet-back_db_1`

### 4. Acesse a Aplicação

A API estará disponível em: http://localhost:8080

### 5. Endpoints Disponíveis

- **POST /auth/register** - Registrar novo usuário
  ```bash
  curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d '{"name":"Teste", "email":"teste@exemplo.com", "password":"senha123"}'
  ```

- **POST /auth/login** - Autenticar usuário
  ```bash
  curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email":"teste@exemplo.com", "password":"senha123"}'
  ```

- **GET /user** - Testar autenticação (requer token JWT)
  ```bash
  curl -H "Authorization: Bearer SEU_TOKEN_JWT" http://localhost:8080/user
  ```

### 6. Parar os Containers

Para interromper a execução:

```bash
docker compose down
```

### 7. Logs e Diagnóstico

Para ver os logs da aplicação:

```bash
docker compose logs app
```

Para ver os logs do banco de dados:

```bash
docker compose logs db
```

## 💾 Persistência de Dados

Os dados do banco PostgreSQL são persistidos através de um volume Docker chamado `postgres_data`. Isso significa que mesmo que você pare e reinicie os containers, os dados continuarão disponíveis.

## 🔧 Configurações

Se você precisar realizar ajustes nas configurações:

- **Portas**: Altere no arquivo `docker compose.yml`
- **Variáveis de ambiente**: Modifique a seção `environment` no `docker compose.yml`
- **Configurações JPA**: Ajuste o arquivo `application.properties`

## 🧪 Ambiente de Desenvolvimento

Para desenvolvimento, você pode:

1. Fazer alterações no código
2. Reconstruir e reiniciar os containers:
   ```bash
   docker compose down
   docker compose up --build -d
   ```

---