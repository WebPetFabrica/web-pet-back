#!/bin/bash

# ===================================================================
# SCRIPT DE TESTE MANUAL AUTOMATIZADO - WEB PET API
# ===================================================================
# Este script testa os fluxos principais da API WebPet
# Executa testes de conectividade, autenticação e recursos protegidos
# ===================================================================

# Configurações
API_BASE="http://localhost:8081"
TIMEOUT=10

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para imprimir mensagens coloridas
print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO") echo -e "${BLUE}[INFO]${NC} $message" ;;
        "SUCCESS") echo -e "${GREEN}[✓ SUCCESS]${NC} $message" ;;
        "ERROR") echo -e "${RED}[✗ ERROR]${NC} $message" ;;
        "WARNING") echo -e "${YELLOW}[⚠ WARNING]${NC} $message" ;;
    esac
}

# Função para verificar se jq está instalado
check_dependencies() {
    print_status "INFO" "Verificando dependências..."
    
    if ! command -v curl &> /dev/null; then
        print_status "ERROR" "curl não está instalado. Instale com: sudo apt-get install curl"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_status "ERROR" "jq não está instalado. Instale com: sudo apt-get install jq"
        exit 1
    fi
    
    print_status "SUCCESS" "Dependências verificadas"
}

# Função para testar conectividade básica
test_connectivity() {
    print_status "INFO" "Testando conectividade com a API..."
    
    # Teste de conectividade básica
    local response
    response=$(curl -s -w "%{http_code}" -o /tmp/api_test_response -m $TIMEOUT "$API_BASE/animal/animals" 2>/dev/null)
    
    if [ $? -eq 0 ] && [ "$response" = "200" ]; then
        print_status "SUCCESS" "API está respondendo (endpoint público /animal/animals)"
        local animals_count
        animals_count=$(cat /tmp/api_test_response | jq '. | length' 2>/dev/null || echo "0")
        print_status "INFO" "Animais cadastrados: $animals_count"
    else
        print_status "ERROR" "API não está respondendo. Verifique se a aplicação está rodando em $API_BASE"
        print_status "INFO" "Para iniciar a aplicação: ./mvnw spring-boot:run"
        exit 1
    fi
}

# Função para testar login
test_login() {
    local user_type=$1
    local email=$2
    local password=$3
    local description=$4
    
    print_status "INFO" "Testando login para $description..."
    
    local login_data="{\"email\":\"$email\",\"password\":\"$password\"}"
    local response
    local http_code
    
    response=$(curl -s -w "%{http_code}" -o /tmp/login_response \
        -H "Content-Type: application/json" \
        -d "$login_data" \
        -m $TIMEOUT \
        "$API_BASE/auth/login" 2>/dev/null)
    
    http_code="${response: -3}"
    
    if [ "$http_code" = "200" ]; then
        local token
        token=$(cat /tmp/login_response | jq -r '.token' 2>/dev/null)
        
        if [ "$token" != "null" ] && [ -n "$token" ]; then
            print_status "SUCCESS" "Login realizado com sucesso para $description"
            echo "$token"
            return 0
        else
            print_status "ERROR" "Token não encontrado na resposta de login para $description"
            return 1
        fi
    else
        print_status "ERROR" "Falha no login para $description (HTTP: $http_code)"
        print_status "INFO" "Resposta: $(cat /tmp/login_response 2>/dev/null || echo 'Sem resposta')"
        return 1
    fi
}

# Função para testar endpoint protegido
test_protected_endpoint() {
    local token=$1
    local endpoint=$2
    local method=$3
    local description=$4
    local data=$5
    
    print_status "INFO" "Testando $description..."
    
    local curl_opts="-s -w %{http_code} -o /tmp/protected_response -m $TIMEOUT"
    local response
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        response=$(curl $curl_opts \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$data" \
            -X POST \
            "$API_BASE$endpoint" 2>/dev/null)
    else
        response=$(curl $curl_opts \
            -H "Authorization: Bearer $token" \
            -X "$method" \
            "$API_BASE$endpoint" 2>/dev/null)
    fi
    
    local http_code="${response: -3}"
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        print_status "SUCCESS" "$description (HTTP: $http_code)"
        return 0
    else
        print_status "ERROR" "Falha em $description (HTTP: $http_code)"
        print_status "INFO" "Resposta: $(cat /tmp/protected_response 2>/dev/null || echo 'Sem resposta')"
        return 1
    fi
}

# Função principal de teste
run_tests() {
    print_status "INFO" "=== INICIANDO TESTES DA API WEB PET ==="
    echo
    
    # Problema conhecido com registro
    print_status "WARNING" "PROBLEMA IDENTIFICADO: Os endpoints de registro não estão funcionando"
    print_status "WARNING" "O AuthController tem dependências não existentes (RegisterRequestDTO, AuthService)"
    print_status "WARNING" "Para testar completamente, será necessário:"
    print_status "WARNING" "1. Corrigir o AuthController para usar AuthenticationService"
    print_status "WARNING" "2. Criar endpoints específicos: /auth/register/user, /auth/register/ong, /auth/register/protetor"
    echo
    
    # Fase 1: Conectividade
    print_status "INFO" "=== FASE 1: TESTE DE CONECTIVIDADE ==="
    test_connectivity
    echo
    
    # Fase 2: Testes de Login (assumindo usuários existentes)
    print_status "INFO" "=== FASE 2: TESTES DE LOGIN ==="
    print_status "INFO" "NOTA: Para estes testes funcionarem, usuários devem estar previamente cadastrados no banco"
    echo
    
    # Usuários de teste (você precisará criar estes no banco primeiro)
    local user_token=""
    local ong_token=""
    local protetor_token=""
    
    # Teste login usuário comum
    user_token=$(test_login "USER" "user@test.com" "123456" "Usuário Comum")
    if [ $? -eq 0 ]; then
        print_status "INFO" "Token do usuário obtido: ${user_token:0:30}..."
    fi
    echo
    
    # Teste login ONG
    ong_token=$(test_login "ONG" "ong@test.com" "123456" "ONG")
    if [ $? -eq 0 ]; then
        print_status "INFO" "Token da ONG obtido: ${ong_token:0:30}..."
    fi
    echo
    
    # Teste login Protetor
    protetor_token=$(test_login "PROTETOR" "protetor@test.com" "123456" "Protetor")
    if [ $? -eq 0 ]; then
        print_status "INFO" "Token do protetor obtido: ${protetor_token:0:30}..."
    fi
    echo
    
    # Fase 3: Testes de Endpoints Protegidos
    print_status "INFO" "=== FASE 3: TESTES DE ENDPOINTS PROTEGIDOS ==="
    
    # Teste /user/me para cada tipo de usuário logado
    if [ -n "$user_token" ]; then
        test_protected_endpoint "$user_token" "/user/me" "GET" "Perfil do usuário comum"
        echo
    fi
    
    if [ -n "$ong_token" ]; then
        test_protected_endpoint "$ong_token" "/user/me" "GET" "Perfil da ONG"
        echo
    fi
    
    if [ -n "$protetor_token" ]; then
        test_protected_endpoint "$protetor_token" "/user/me" "GET" "Perfil do protetor"
        echo
    fi
    
    # Teste criação de animal (deve funcionar para qualquer usuário autenticado)
    if [ -n "$ong_token" ]; then
        local animal_data='{"name":"Rex","description":"Cão amigável","category":"DOG","status":"AVAILABLE"}'
        test_protected_endpoint "$ong_token" "/animal/createAnimal" "POST" "Criação de animal pela ONG" "$animal_data"
        echo
    fi
    
    # Teste outros endpoints protegidos de animal
    if [ -n "$user_token" ]; then
        test_protected_endpoint "$user_token" "/animal/animalStatus/AVAILABLE" "GET" "Busca animais disponíveis"
        test_protected_endpoint "$user_token" "/animal/animalCategory/DOG" "GET" "Busca cães"
        echo
    fi
    
    # Limpeza
    rm -f /tmp/api_test_response /tmp/login_response /tmp/protected_response
    
    print_status "INFO" "=== TESTES CONCLUÍDOS ==="
    echo
    print_status "INFO" "PRÓXIMOS PASSOS PARA TESTES COMPLETOS:"
    print_status "INFO" "1. Corrigir AuthController para implementar endpoints de registro"
    print_status "INFO" "2. Adicionar usuários de teste ao banco de dados"
    print_status "INFO" "3. Configurar endpoints de health/actuator se necessário"
}

# Função para criar usuários de teste via SQL (opcional)
create_test_users_sql() {
    print_status "INFO" "Para criar usuários de teste, execute os seguintes comandos SQL:"
    echo
    echo "-- Usuário comum"
    echo "INSERT INTO users (id, name, email, password, celular, user_type, active) VALUES"
    echo "('user-test-id', 'User Test', 'user@test.com', '\$2a\$10\$...(senha encriptada)', '11999999999', 'USER', true);"
    echo
    echo "-- ONG"
    echo "INSERT INTO ong (id, nome_ong, cnpj, email, password, celular, user_type, active) VALUES"
    echo "('ong-test-id', 'ONG Test', '12345678000195', 'ong@test.com', '\$2a\$10\$...(senha encriptada)', '11888888888', 'ONG', true);"
    echo
    echo "-- Protetor"
    echo "INSERT INTO protetor (id, nome_completo, cpf, email, password, celular, user_type, active) VALUES"
    echo "('protetor-test-id', 'Protetor Test', '12345678901', 'protetor@test.com', '\$2a\$10\$...(senha encriptada)', '11777777777', 'PROTETOR', true);"
    echo
    print_status "INFO" "NOTA: Substitua '...(senha encriptada)' pela senha '123456' encriptada com BCrypt"
}

# Menu principal
main() {
    case "${1:-test}" in
        "test")
            check_dependencies
            run_tests
            ;;
        "sql")
            create_test_users_sql
            ;;
        "help"|"-h"|"--help")
            echo "Uso: $0 [comando]"
            echo
            echo "Comandos:"
            echo "  test (padrão)  - Executa os testes da API"
            echo "  sql           - Mostra comandos SQL para criar usuários de teste"
            echo "  help          - Mostra esta ajuda"
            echo
            echo "Exemplo:"
            echo "  $0 test"
            echo "  $0 sql"
            ;;
        *)
            print_status "ERROR" "Comando desconhecido: $1"
            print_status "INFO" "Use '$0 help' para ver os comandos disponíveis"
            exit 1
            ;;
    esac
}

# Execução
main "$@"