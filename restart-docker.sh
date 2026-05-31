#!/bin/bash

# Script para reiniciar, reconstruir e subir o ambiente Docker do projeto paróquia

# Exibe ajuda
show_help() {
    echo "Uso: ./restart-docker.sh [OPÇÕES]"
    echo ""
    echo "Opções:"
    echo "  -c, --clean       Remove volumes (limpa o banco de dados e os caches do gradle/build)"
    echo "  --no-cache        Reconstrói as imagens do Docker do zero, sem usar cache"
    echo "  -h, --help        Mostra esta mensagem de ajuda"
    echo ""
}

CLEAN=false
NO_CACHE=""

# Trata argumentos
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -c|--clean) CLEAN=true ;;
        --no-cache) NO_CACHE="--no-cache" ;;
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
    echo "-> Removendo containers e volumes (banco de dados e caches do Gradle)..."
    docker compose down -v
else
    echo "-> Parando e removendo apenas os containers..."
    docker compose down
fi

echo ""
echo "Step 2: Reconstruindo as imagens..."
if [ -n "$NO_CACHE" ]; then
    echo "-> Construindo sem cache (--no-cache)..."
fi
docker compose build $NO_CACHE

echo ""
echo "Step 3: Subindo o ambiente de serviços..."
docker compose up -d

echo ""
echo "========================================="
echo "   AMBIENTE INICIALIZADO COM SUCESSO!    "
echo "========================================="
echo "Para acompanhar o processo de build do backend e inicialização da API, execute:"
echo "  docker compose logs -f builder api"
echo ""
echo "Serviços:"
echo "  - Frontend UI:  http://localhost:3000"
echo "  - API Backend:  http://localhost:8080"
echo "  - PgAdmin:      http://localhost:5050 (User: admin@admin.com / Senha: admin)"
echo "  - Postgres DB:  localhost:5432 (User: postgres / Senha: password)"
echo "========================================="
