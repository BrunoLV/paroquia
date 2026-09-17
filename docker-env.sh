#!/bin/bash
# ==============================================================================
# docker-env.sh - Gerenciador do Ambiente Docker para Paróquia
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Paleta de cores para saída formatada
if [ -t 1 ]; then
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    BLUE='\033[0;34m'
    CYAN='\033[0;36m'
    BOLD='\033[1m'
    NC='\033[0m'
else
    RED=''
    GREEN=''
    YELLOW=''
    BLUE=''
    CYAN=''
    BOLD=''
    NC=''
fi

log_info()    { echo -e "${BLUE}[INFO]${NC} $*"; }
log_success() { echo -e "${GREEN}[OK]${NC} $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $*"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; }

check_docker() {
    if ! command -v docker >/dev/null 2>&1; then
        log_error "Docker não está instalado ou não está no PATH."
        exit 1
    fi
    if ! docker info >/dev/null 2>&1; then
        log_error "O daemon do Docker não está acessível. Verifique se o serviço está ativo ou se seu usuário tem permissão (grupo docker)."
        exit 1
    fi
}

show_endpoints() {
    echo ""
    echo -e "${BOLD}Endereços dos Serviços Locais:${NC}"
    echo -e "  • Frontend (UI):       ${CYAN}http://localhost:3000${NC}"
    echo -e "  • Backend (API):       ${CYAN}http://localhost:8080/api${NC}"
    echo -e "  • Swagger UI:          ${CYAN}http://localhost:8080/swagger-ui/index.html${NC}"
    echo -e "  • Actuator Health:     ${CYAN}http://localhost:8080/actuator/health${NC}"
    echo -e "  • Actuator Metrics:    ${CYAN}http://localhost:8080/actuator/metrics${NC}"
    echo -e "  • Prometheus:          ${CYAN}http://localhost:9090${NC}"
    echo -e "  • Grafana:             ${CYAN}http://localhost:3001${NC} (admin / admin)"
    echo -e "  • pgAdmin 4:           ${CYAN}http://localhost:5050${NC} (admin@paroquia.local / admin)"
    echo -e "  • PostgreSQL:          ${CYAN}localhost:5432${NC} (banco: calendario, user: postgres)"
    echo ""
}

show_help() {
    echo -e "${BOLD}Uso:${NC} ./docker-env.sh [COMANDO] [OPÇÕES]"
    echo ""
    echo -e "${BOLD}Comandos:${NC}"
    echo "  up       Sobe o ecossistema Docker Compose."
    echo "  down     Derruba os containers mantendo volumes de dados."
    echo "  restart  Reinicia os serviços (executa down e up)."
    echo "  status   Exibe o status atual e consumo de recursos dos containers."
    echo "  logs     Exibe logs dos containers (ou de um serviço específico)."
    echo "  clean    Remove containers, volumes locais e dados de banco."
    echo "  help     Mostra esta ajuda detalhada."
    echo ""
    echo -e "${BOLD}Opções (Flags):${NC}"
    echo "  -c, --clean     Em 'up' ou 'restart', limpa volumes e cache antes de subir."
    echo "  --no-cache      Força reconstrução das imagens sem cache (docker compose build --no-cache)."
    echo "  -r, --no-build  Sobe os serviços sem tentar reconstruir imagens locais."
    echo "  -s, --service   Especifica um serviço específico para logs ou rebuild (ex: -s api)."
    echo "  -f, --follow    Em 'logs', acompanha o streaming contínuo de logs (tail)."
    echo ""
    echo -e "${BOLD}Exemplos:${NC}"
    echo "  ./docker-env.sh up"
    echo "  ./docker-env.sh restart --clean"
    echo "  ./docker-env.sh logs -f -s api"
    echo "  ./docker-env.sh status"
    echo "  ./docker-env.sh down"
}

cmd_up() {
    local clean=false
    local no_cache=false
    local no_build=false
    local service=""

    while [ $# -gt 0 ]; do
        case "$1" in
            -c|--clean) clean=true; shift ;;
            --no-cache) no_cache=true; shift ;;
            -r|--no-build) no_build=true; shift ;;
            -s|--service) service="${2:-}"; shift 2 ;;
            *) shift ;;
        esac
    done

    check_docker

    if [ "$clean" = true ]; then
        log_warn "Executando limpeza completa de containers e volumes antes de subir..."
        docker compose down -v --remove-orphans || true
    fi

    local build_opts=()
    if [ "$no_cache" = true ]; then
        build_opts+=(--no-cache)
    fi

    if [ "$no_build" = false ]; then
        log_info "Construindo imagens necessárias..."
        docker compose build "${build_opts[@]}" ${service:+"$service"}
    fi

    log_info "Iniciando containers do ecossistema..."
    docker compose up -d ${service:+"$service"}

    log_success "Ecossistema iniciado com sucesso!"
    show_endpoints
}

cmd_down() {
    check_docker
    log_info "Encerrando containers..."
    docker compose down --remove-orphans
    log_success "Containers finalizados."
}

cmd_restart() {
    check_docker
    log_info "Reiniciando ecossistema Docker..."
    cmd_down
    cmd_up "$@"
}

cmd_status() {
    check_docker
    echo -e "${BOLD}Status dos Containers:${NC}"
    docker compose ps
    echo ""
    echo -e "${BOLD}Consumo de Recursos (docker stats resumido):${NC}"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}" || true
    show_endpoints
}

cmd_logs() {
    check_docker
    local follow=false
    local service=""

    while [ $# -gt 0 ]; do
        case "$1" in
            -f|--follow) follow=true; shift ;;
            -s|--service) service="${2:-}"; shift 2 ;;
            *)
                if [ -z "$service" ] && [[ "$1" != -* ]]; then
                    service="$1"
                fi
                shift
                ;;
        esac
    done

    local log_args=()
    if [ "$follow" = true ]; then
        log_args+=(-f)
    fi

    if [ -n "$service" ]; then
        log_info "Exibindo logs do serviço: $service"
        docker compose logs "${log_args[@]}" "$service"
    else
        log_info "Exibindo logs de todos os serviços..."
        docker compose logs "${log_args[@]}"
    fi
}

cmd_clean() {
    check_docker
    log_warn "ATENÇÃO: Todos os containers e volumes do projeto (PostgreSQL, Grafana, Prometheus) serão excluídos!"
    read -r -p "Deseja prosseguir com a limpeza total? [s/N]: " confirm
    if [[ "$confirm" =~ ^[sS]([iI][mM])?$ ]]; then
        log_info "Removendo containers e volumes..."
        docker compose down -v --remove-orphans
        log_success "Limpeza concluída com sucesso."
    else
        log_info "Operação cancelada pelo usuário."
    fi
}

# Roteador de comandos
COMMAND="${1:-help}"
shift || true

case "$COMMAND" in
    up)       cmd_up "$@" ;;
    down)     cmd_down "$@" ;;
    restart)  cmd_restart "$@" ;;
    status)   cmd_status "$@" ;;
    logs)     cmd_logs "$@" ;;
    clean)    cmd_clean "$@" ;;
    help|-h|--help) show_help ;;
    *)
        log_error "Comando desconhecido: '$COMMAND'"
        echo ""
        show_help
        exit 1
        ;;
esac
