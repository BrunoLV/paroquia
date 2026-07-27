#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

show_help() {
    echo "Uso: ./run.sh [COMANDO] [OPÇÕES]"
    echo ""
    echo "Comandos disponíveis:"
    echo "  dev           Inicializa o ambiente de desenvolvimento local (Docker Compose)"
    echo "  test          Executa as suítes de teste de backend (Gradle) e frontend (npm test)"
    echo "  check         Executa as checagens estáticas (Checkstyle, PMD, SpotBugs) e suítes de teste"
    echo "  build         Realiza o build completo dos projetos de backend e frontend"
    echo "  docker        Reinicia e recompõe o ambiente de contêineres Docker"
    echo "  help          Exibe esta mensagem de ajuda"
    echo ""
}

COMMAND="${1:-help}"

case "$COMMAND" in
    dev)
        echo "========================================="
        echo "   INICIALIZANDO AMBIENTE DEV (DOCKER)   "
        echo "========================================="
        docker compose up -d
        ;;
    test)
        echo "========================================="
        echo "   EXECUTANDO TESTES DO BACKEND          "
        echo "========================================="
        (cd gestao-paroquia && ./gradlew test)
        
        echo ""
        echo "========================================="
        echo "   EXECUTANDO TESTES DO FRONTEND         "
        echo "========================================="
        (cd gestao-paroquia-ui && npm test -- --run)
        ;;
    check)
        echo "========================================="
        echo "   EXECUTANDO QUALITY GATES (BACKEND)    "
        echo "========================================="
        (cd gestao-paroquia && ./gradlew check)
        
        echo ""
        echo "========================================="
        echo "   EXECUTANDO TESTES DO FRONTEND         "
        echo "========================================="
        (cd gestao-paroquia-ui && npm test -- --run)
        ;;
    build)
        echo "========================================="
        echo "   BUILD DO BACKEND (BOOTJAR)            "
        echo "========================================="
        (cd gestao-paroquia && ./gradlew :app:bootJar)
        
        echo ""
        echo "========================================="
        echo "   BUILD DO FRONTEND (VITE)              "
        echo "========================================="
        (cd gestao-paroquia-ui && npm run build)
        ;;
    docker)
        shift
        ./restart-docker.sh "$@"
        ;;
    help|-h|--help)
        show_help
        ;;
    *)
        echo "Comando desconhecido: $COMMAND"
        show_help
        exit 1
        ;;
esac
