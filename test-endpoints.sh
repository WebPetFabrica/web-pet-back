#!/bin/bash

# Testar cadastro ONG
curl -X POST http://localhost:8081/auth/register/ong \
  -H "Content-Type: application/json" \
  -d '{
    "cnpj": "12345678000190",
    "nomeOng": "Abrigo Esperança",
    "email": "contato@abrigo.com",
    "celular": "44999887766",
    "password": "senha123"
  }'

# Testar cadastro Protetor
curl -X POST http://localhost:8081/auth/register/protetor \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCompleto": "Maria Silva",
    "cpf": "12345678901",
    "email": "maria@email.com",
    "celular": "44998877665",
    "password": "senha123"
  }'

# Testar login ONG
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "contato@abrigo.com",
    "password": "senha123"
  }'