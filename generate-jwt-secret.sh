#!/bin/bash

# Gerador de JWT Secret seguro (256 bits / 64 chars hex)
generate_jwt_secret() {
    openssl rand -hex 32
}

echo "🔐 Gerando JWT Secret seguro..."
SECRET=$(generate_jwt_secret)

echo "✅ Secret gerado:"
echo "JWT_SECRET=$SECRET"
echo ""
echo "📋 Para usar:"
echo "export JWT_SECRET=$SECRET"
echo ""
echo "🐳 Para Docker:"
echo "JWT_SECRET=$SECRET docker compose up -d"