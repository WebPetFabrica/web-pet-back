# API WebPet - Documentação

## Endpoints de Autenticação

### Login (todos os tipos)

```
POST /auth/login
```

```json
{
  "email": "string",
  "password": "string"
}
```

### Cadastro Usuário Comum

```
POST /auth/register
```

```json
{
  "name": "string",
  "email": "string",
  "password": "string"
}
```

### Cadastro ONG

```
POST /auth/register/ong
```

```json
{
  "cnpj": "string",
  "nomeOng": "string",
  "email": "string",
  "celular": "string",
  "password": "string"
}
```

### Cadastro Protetor

```
POST /auth/register/protetor
```

```json
{
  "nomeCompleto": "string",
  "cpf": "string",
  "email": "string",
  "celular": "string",
  "password": "string"
}
```

## Endpoints Protegidos

### Teste de Autenticação

```
GET /user
Headers: Authorization: Bearer {token}
```

## Respostas

### Sucesso (200)

```json
{
  "name": "string",
  "token": "string"
}
```

### Erro (400/500)

- 400: Email já cadastrado ou credenciais inválidas
- 500: Erro interno
