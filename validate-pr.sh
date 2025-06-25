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
./mvnw clean compile
if [ $? -ne 0 ]; then
    echo "❌ Build falhou!"
    exit 1
fi

# 3. Testes
echo "🧪 Executando testes..."
./mvnw test
if [ $? -ne 0 ]; then
    echo "❌ Testes falharam!"
    exit 1
fi

# 4. Docker
echo "🐳 Testando Docker..."
docker compose build
if [ $? -ne 0 ]; then
    echo "❌ Docker build falhou!"
    exit 1
fi

# 5. Verificar arquivos não commitados
echo "📝 Verificando status do git..."
if [[ -n $(git status -s) ]]; then
    echo "❌ Por favor, commit ou stash suas alterações antes de prosseguir."
    git status -s
    exit 1
fi

echo "✅ Validação concluída!"