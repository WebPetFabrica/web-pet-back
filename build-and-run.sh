echo "🧹 Limpando ambiente..."
docker compose down -v
docker system prune -f
rm -rf target/

echo "🔨 Compilando projeto..."
mvn clean compile

echo "🐳 Construindo imagens Docker..."
docker compose build --no-cache

echo "🚀 Iniciando containers..."
docker compose up -d

echo "⏳ Aguardando aplicação iniciar..."
sleep 10

echo "📊 Status dos containers:"
docker compose ps

echo "📝 Logs da aplicação:"
docker compose logs app --tail=50

echo "✅ Aplicação disponível em http://localhost:8081"