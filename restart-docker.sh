#!/bin/bash
set -euo pipefail

# Garante navegação para a raiz do projeto independente do diretório de chamada
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Script para reiniciar, reconstruir e subir o ambiente Docker do projeto paróquia

show_help() {
    echo "Uso: ./restart-docker.sh [OPÇÕES]"
    echo ""
    echo "Opções:"
    echo "  -c, --clean       Remove volumes (limpa o banco de dados e os caches do gradle/build)"
    echo "  --no-cache        Reconstrói as imagens do Docker do zero, sem usar cache"
    echo "  -r, --no-build    Apenas reinicia os containers sem reconstruir/buildar as imagens"
    echo "  -h, --help        Mostra esta mensagem de ajuda"
    echo ""
}

CLEAN=false
NO_CACHE=false
BUILD=true

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -c|--clean) CLEAN=true ;;
        --no-cache) NO_CACHE=true ;;
        -r|--no-build) BUILD=false ;;
        -h|--help) show_help; exit 0 ;;
        *) echo "Opção desconhecida: $1"; show_help; exit 1 ;;
    esac
    shift
done

echo "========================================="
echo "   REINICIANDO O AMBIENTE DOCKER         "
echo "========================================="

echo "Step 1: Parando e limpando containers..."
if [ "$CLEAN" = true ]; then
    echo "-> Removendo containers e volumes (banco de dados postgres_data, shared_volume e gradle_cache)..."
    docker compose down -v
else
    echo "-> Parando e removendo apenas os containers..."
    docker compose down
fi

echo ""
if [ "$BUILD" = true ]; then
    echo "Step 2: Reconstruindo as imagens..."
    BUILD_FLAGS=()
    if [ "$NO_CACHE" = true ]; then
        echo "-> Construindo sem cache (--no-cache)..."
        BUILD_FLAGS+=("--no-cache")
    fi
    docker compose build "${BUILD_FLAGS[@]}"
else
    echo "Step 2: Pulando reconstrução das imagens (--no-build)..."
fi

echo ""
echo "Step 3: Subindo o ambiente de serviços..."
docker compose up -d

echo ""
echo "========================================="
echo "   AMBIENTE INICIALIZADO COM SUCESSO!    "
echo "========================================="
echo "Para acompanhar o processo de build do backend e inicialização dos serviços, execute:"
echo "  docker compose logs -f builder api ui"
echo ""
echo "Serviços:"
echo "  - Frontend UI (Vite): http://localhost:3000"
echo "  - API Backend (v1):   http://localhost:8080/api/v1"
echo "  - Swagger UI / Docs: http://localhost:8080/swagger-ui.html"
echo "  - Health Check API:  http://localhost:8080/actuator/health"
echo "  - PgAdmin 4:         http://localhost:5050 (User: admin@admin.com / Senha: admin)"
echo "  - Postgres DB 16:    localhost:5432 (User: postgres / Senha: password / DB: calendario)"
echo "========================================="
