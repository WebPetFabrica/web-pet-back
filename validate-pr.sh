#!/bin/bash
set -euo pipefail

echo "🔍 Validando branch para PR..."

# 1. Limpeza
echo "🧹 Limpando arquivos temporários..."
find . -name "*.class" -delete
find . -name "*.iml" -delete
rm -rf target/

# 2. Build
echo "🔨 Executando build..."
trap 'echo "❌ Build falhou!"' ERR
./mvnw clean compile

# 3. Testes
echo "🧪 Executando testes..."
trap 'echo "❌ Testes falharam!"' ERR
./mvnw test

# 4. Docker
echo "🐳 Testando Docker..."
trap 'echo "❌ Docker build falhou!"' ERR
docker compose build

# 5. Verificar arquivos não commitados
echo "📝 Verificando status do git..."
if [[ -n $(git status -s) ]]; then
    echo "❌ Por favor, commit ou stash suas alterações antes de prosseguir."
    git status -s
    exit 1
fi

echo "✅ Validação concluída!"