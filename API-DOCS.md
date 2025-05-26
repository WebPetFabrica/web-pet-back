# 📚 API WebPet - Documentação Completa

Esta documentação descreve todos os endpoints disponíveis na API WebPet para gerenciamento de adoção de animais.

## 🔐 Endpoints de Autenticação

### 🔑 Login Universal
Endpoint para autenticação de qualquer tipo de usuário (Usuário, ONG ou Protetor).

```http
POST /auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "usuario@exemplo.com",
  "password": "senha123"
}
```

**Response (200):**
```json
{
  "name": "Nome do Usuário",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

---

### 👤 Cadastro de Usuário Comum
Registro para usuários que desejam adotar animais.

```http
POST /auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "João Silva",
  "email": "joao@exemplo.com",
  "password": "senha123"
}
```

**Response (201):**
```json
{
  "name": "João Silva",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

---

### 🏢 Cadastro de ONG
Registro para organizações não-governamentais.

```http
POST /auth/register/ong
Content-Type: application/json
```

**Request Body:**
```json
{
  "cnpj": "12345678000190",
  "nomeOng": "ONG Amigos dos Animais",
  "email": "contato@ongamigos.org",
  "celular": "41999999999",
  "password": "senha123"
}
```

**Response (201):**
```json
{
  "name": "ONG Amigos dos Animais",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

---

### 🛡️ Cadastro de Protetor
Registro para protetores independentes de animais.

```http
POST /auth/register/protetor
Content-Type: application/json
```

**Request Body:**
```json
{
  "nomeCompleto": "Maria Santos",
  "cpf": "12345678901",
  "email": "maria@exemplo.com",
  "celular": "41888888888",
  "password": "senha123"
}
```

**Response (201):**
```json
{
  "name": "Maria Santos",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

---

## 🔒 Endpoints Protegidos

### ✅ Teste de Autenticação
Endpoint para verificar se o token JWT está válido.

```http
GET /user
Authorization: Bearer {token}
```

**Response (200):**
```json
{
  "message": "Acesso autorizado",
  "user": "usuario@exemplo.com"
}
```

---

## 📋 Códigos de Status HTTP

| Código | Descrição | Exemplo |
|--------|-----------|---------|
| **200** | ✅ Sucesso | Login realizado com sucesso |
| **201** | ✅ Criado | Usuário cadastrado com sucesso |
| **400** | ❌ Erro do Cliente | Email já cadastrado, dados inválidos |
| **401** | 🔒 Não Autorizado | Token inválido ou expirado |
| **500** | ⚠️ Erro do Servidor | Erro interno da aplicação |

---

## 🚨 Tratamento de Erros

### Erro de Validação (400)
```json
{
  "error": "Email já cadastrado",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Erro de Autenticação (401)
```json
{
  "error": "Credenciais inválidas",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Erro do Servidor (500)
```json
{
  "error": "Erro interno do servidor",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 🔧 Configuração de Headers

### Para Endpoints Públicos
```http
Content-Type: application/json
```

### Para Endpoints Protegidos
```http
Content-Type: application/json
Authorization: Bearer {seu_token_jwt}
```

---

## 📝 Regras de Validação

### 📧 Email
- Deve ser um email válido
- Único no sistema (não pode haver duplicatas)

### 🔒 Senha
- Mínimo 6 caracteres
- Será criptografada com BCrypt

### 🏢 CNPJ (ONG)
- Formato válido de CNPJ brasileiro
- Único no sistema

### 🆔 CPF (Protetor)
- Formato válido de CPF brasileiro
- Único no sistema

### 📱 Celular
- Formato brasileiro (opcional)

---

## 🔄 Exemplos de Uso com cURL

### Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@exemplo.com","password":"senha123"}'
```

### Cadastro de ONG
```bash
curl -X POST http://localhost:8081/auth/register/ong \
  -H "Content-Type: application/json" \
  -d '{"cnpj":"12345678000190","nomeOng":"ONG Teste","email":"ong@teste.com","celular":"41999999999","password":"senha123"}'
```

### Acesso Protegido
```bash
curl -H "Authorization: Bearer SEU_TOKEN_JWT" \
  http://localhost:8081/user
```

---

## 🎯 URL Base

**Desenvolvimento:** `http://localhost:8081`
**Produção:** `https://api.webpet.com` *(quando disponível)*
