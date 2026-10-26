#!/bin/bash
# ==============================================================================
# manage.sh - CLI Unificada de Desenvolvimento e Operação - Paróquia
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PID_DIR="$SCRIPT_DIR/.run"
API_PID_FILE="$PID_DIR/api.pid"
UI_PID_FILE="$PID_DIR/ui.pid"
API_LOG_FILE="$PID_DIR/api.log"
UI_LOG_FILE="$PID_DIR/ui.log"

# Cores para saída amigável
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

ensure_dirs() {
    mkdir -p "$PID_DIR"
}

is_running() {
    local pid_file="$1"
    if [ -f "$pid_file" ]; then
        local pid
        pid=$(cat "$pid_file" 2>/dev/null || true)
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            return 0
        fi
    fi
    return 1
}

wait_for_port() {
    local host="$1"
    local port="$2"
    local timeout="${3:-30}"
    local count=0

    log_info "Aguardando serviço em $host:$port (máx: ${timeout}s)..."
    while ! nc -z "$host" "$port" >/dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
        if [ "$count" -ge "$timeout" ]; then
            log_error "Tempo limite excedido aguardando $host:$port."
            return 1
        fi
    done
    return 0
}

show_endpoints() {
    echo ""
    echo -e "${BOLD}Endereços dos Serviços:${NC}"
    echo -e "  • Frontend (UI):       ${CYAN}http://localhost:3000${NC}"
    echo -e "  • Backend (API):       ${CYAN}http://localhost:8080/api${NC}"
    echo -e "  • Swagger UI:          ${CYAN}http://localhost:8080/swagger-ui/index.html${NC} (requer login ADMIN)"
    echo -e "  • Actuator Health:     ${CYAN}http://localhost:8080/actuator/health${NC}"
    echo -e "  • Actuator Metrics:    ${CYAN}http://localhost:8080/actuator/metrics${NC}"
    echo -e "  • Prometheus:          ${CYAN}http://localhost:9090${NC}"
    echo -e "  • Grafana:             ${CYAN}http://localhost:3001${NC} (admin / admin)"
    echo -e "  • Loki:                ${CYAN}http://localhost:3100${NC}"
    echo -e "  • pgAdmin 4:           ${CYAN}http://localhost:5050${NC} (admin@paroquia.com / admin)"
    echo -e "  • PostgreSQL:          ${CYAN}localhost:5432${NC}"
    echo -e "  • Postgres Exporter:   ${CYAN}http://localhost:9187/metrics${NC}"
    echo ""
}

show_help() {
    echo -e "${BOLD}Uso:${NC} ./manage.sh [COMANDO] [OPÇÕES]"
    echo ""
    echo -e "${BOLD}Comandos de Desenvolvimento:${NC}"
    echo "  dev      Inicia infraestrutura Docker (DB/Métricas/Logs) e executa API e UI localmente."
    echo "  stop     Interrompe todos os processos locais em execução e containers."
    echo "  restart  Reinicia o ambiente de desenvolvimento local completo."
    echo "  status   Exibe o status dos serviços locais e containers."
    echo "  doctor   Diagnostica integridade do ambiente, conflitos de portas e processos zumbis."
    echo "  logs     Acompanha os logs da API ou UI local (ex: ./manage.sh logs api)."
    echo "  seed     Aplica o script seed-data.sql no banco de dados ativo."
    echo ""
    echo -e "${BOLD}Comandos de Qualidade e Build:${NC}"
    echo "  test     Executa a suíte de testes unitários e de integração (Gradle / UI)."
    echo "  check    Executa os quality gates completos (Checkstyle, PMD, SpotBugs, ArchUnit)."
    echo "  build    Compila o backend e frontend gerando os artefatos de distribuição."
    echo "  clean    Limpa caches de build locais (Gradle e npm)."
    echo ""
    echo -e "${BOLD}Operações Docker:${NC}"
    echo "  docker   Delega comandos diretamente para ./docker-env.sh (ex: ./manage.sh docker up)."
    echo ""
    echo -e "${BOLD}Ajuda:${NC}"
    echo "  help     Exibe esta mensagem de ajuda."
    echo ""
}

start_infra() {
    log_info "Verificando dependências de infraestrutura no Docker..."
    ./docker-env.sh up -s db -s postgres-exporter -s prometheus -s grafana -s loki -s promtail -r
    wait_for_port "localhost" 5432 30 || true
}


cmd_dev() {
    ensure_dirs
    start_infra

    # 1. Iniciar Backend (API) se não estiver rodando
    if is_running "$API_PID_FILE"; then
        log_warn "API já está em execução (PID $(cat "$API_PID_FILE"))."
    else
        log_info "Iniciando Backend Spring Boot em segundo plano..."
        (
            cd gestao-paroquia
            # Perfil dev: chaves de desenvolvimento explícitas, Swagger habilitado e cookie sem Secure (HTTP local).
            # A senha do banco vem do .env (mesma do container db); demais valores do .env não são carregados.
            export SPRING_PROFILES_ACTIVE=dev
            DB_PASSWORD="$(grep -E '^POSTGRES_PASSWORD=' ../.env | cut -d= -f2-)"
            export DB_PASSWORD
            nohup ./gradlew :app:bootRun --no-daemon > "$API_LOG_FILE" 2>&1 &
            echo $! > "$API_PID_FILE"
        )
        log_success "API iniciada (PID: $(cat "$API_PID_FILE")). Logs em $API_LOG_FILE"
    fi

    # 2. Iniciar Frontend (UI) se não estiver rodando
    if is_running "$UI_PID_FILE"; then
        log_warn "UI já está em execução (PID $(cat "$UI_PID_FILE"))."
    else
        log_info "Iniciando Frontend Vite em segundo plano..."
        (
            cd gestao-paroquia-ui
            if [ ! -d "node_modules" ]; then
                log_info "Instalando dependências do frontend (npm ci)..."
                npm ci --prefer-offline
            fi
            nohup npm run dev > "$UI_LOG_FILE" 2>&1 &
            echo $! > "$UI_PID_FILE"
        )
        log_success "UI iniciada (PID: $(cat "$UI_PID_FILE")). Logs em $UI_LOG_FILE"
    fi

    show_endpoints
}

cmd_stop() {
    local stopped_any=false

    if is_running "$API_PID_FILE"; then
        local pid
        pid=$(cat "$API_PID_FILE")
        log_info "Encerrando API (PID: $pid)..."
        kill "$pid" 2>/dev/null || true
        rm -f "$API_PID_FILE"
        stopped_any=true
    else
        rm -f "$API_PID_FILE"
    fi

    if is_running "$UI_PID_FILE"; then
        local pid
        pid=$(cat "$UI_PID_FILE")
        log_info "Encerrando UI (PID: $pid)..."
        kill "$pid" 2>/dev/null || true
        rm -f "$UI_PID_FILE"
        stopped_any=true
    else
        rm -f "$UI_PID_FILE"
    fi

    log_info "Encerrando containers do ecossistema..."
    ./docker-env.sh down

    if [ "$stopped_any" = true ]; then
        log_success "Processos locais encerrados com sucesso."
    fi
}

cmd_restart() {
    cmd_stop
    sleep 2
    cmd_dev
}

cmd_status() {
    echo -e "${BOLD}Status dos Processos Locais:${NC}"
    if is_running "$API_PID_FILE"; then
        echo -e "  • Backend API:  ${GREEN}EM EXECUÇÃO${NC} (PID: $(cat "$API_PID_FILE"))"
    else
        echo -e "  • Backend API:  ${RED}PARADO${NC}"
    fi

    if is_running "$UI_PID_FILE"; then
        echo -e "  • Frontend UI:  ${GREEN}EM EXECUÇÃO${NC} (PID: $(cat "$UI_PID_FILE"))"
    else
        echo -e "  • Frontend UI:  ${RED}PARADO${NC}"
    fi

    echo ""
    ./docker-env.sh status
}

cmd_doctor() {
    echo -e "${BOLD}=== Diagnóstico do Ambiente Paróquia (Doctor) ===${NC}\n"

    local has_issue=false

    # 1. Ferramentas Básicas do Host
    echo -e "${BOLD}1. Ferramentas e Runtime:${NC}"
    if command -v java >/dev/null 2>&1; then
        local java_version
        java_version=$(java -version 2>&1 | head -n 1)
        echo -e "  ${GREEN}✔${NC} Java: $java_version"
    else
        echo -e "  ${RED}✖${NC} Java não encontrado no PATH"
        has_issue=true
    fi

    if command -v node >/dev/null 2>&1; then
        local node_version
        node_version=$(node -v)
        echo -e "  ${GREEN}✔${NC} Node.js: $node_version"
    else
        echo -e "  ${RED}✖${NC} Node.js não encontrado no PATH"
        has_issue=true
    fi

    if command -v docker >/dev/null 2>&1; then
        local docker_version
        docker_version=$(docker --version)
        echo -e "  ${GREEN}✔${NC} Docker: $docker_version"
    else
        echo -e "  ${RED}✖${NC} Docker não encontrado no PATH"
        has_issue=true
    fi

    # 2. Daemon do Docker
    echo -e "\n${BOLD}2. Daemon do Docker:${NC}"
    if docker info >/dev/null 2>&1; then
        echo -e "  ${GREEN}✔${NC} Daemon do Docker está ativo e acessível"
    else
        echo -e "  ${RED}✖${NC} Daemon do Docker inacessível (verifique permissões do usuário ou status do serviço)"
        has_issue=true
    fi

    # 3. Varredura de Portas do Ecossistema
    echo -e "\n${BOLD}3. Varredura de Portas do Ecossistema:${NC}"
    local -A port_map=(
        ["3000"]="Frontend (UI)"
        ["3001"]="Grafana"
        ["3100"]="Loki"
        ["5050"]="pgAdmin"
        ["5432"]="PostgreSQL"
        ["8080"]="Backend (API)"
        ["9090"]="Prometheus"
        ["9187"]="Postgres Exporter"
    )

    local zombie_detected=false

    for port in "${!port_map[@]}"; do
        local desc="${port_map[$port]}"
        if (exec 3<>/dev/tcp/127.0.0.1/"$port") 2>/dev/null; then
            exec 3<&-
            exec 3>&-
            local running_container
            running_container=$(docker ps --filter "publish=$port" --filter "status=running" --format "{{.Names}}" 2>/dev/null || true)
            if [ -n "$running_container" ]; then
                echo -e "  ${GREEN}✔${NC} Porta $port ($desc): Ocupada pelo container ativo [${CYAN}$running_container${NC}]"
            else
                echo -e "  ${RED}✖${NC} Porta $port ($desc): OCUPADA por processo externo ou proxy zumbi!"
                has_issue=true
                zombie_detected=true
            fi
        else
            echo -e "  ${GREEN}✔${NC} Porta $port ($desc): Livre"
        fi
    done

    # 4. Verificação de Processos Zumbis do docker-proxy
    if [ "$zombie_detected" = true ]; then
        local zombie_count
        zombie_count=$(pgrep -f "docker-proxy" | wc -l || echo "0")
        if [ "$zombie_count" -gt 0 ]; then
            echo -e "\n${YELLOW}[ALERTA]${NC} Foram detectados $zombie_count processos 'docker-proxy' ativos no sistema sem containers correspondentes."
            echo -e "  Recomendação de correção imediata:"
            echo -e "  ${BOLD}sudo systemctl restart docker${NC}  (ou ${BOLD}sudo killall docker-proxy${NC})"
        fi
    fi

    echo ""
    if [ "$has_issue" = true ]; then
        log_warn "O diagnóstico encontrou inconsistências no ambiente local."
    else
        log_success "Todos os diagnósticos passaram. Ambiente saudável para desenvolvimento!"
    fi
}

cmd_logs() {
    local target="${1:-api}"
    case "$target" in
        api|backend)
            if [ -f "$API_LOG_FILE" ]; then
                log_info "Streaming de logs da API ($API_LOG_FILE)..."
                tail -n 50 -f "$API_LOG_FILE"
            else
                log_warn "Nenhum arquivo de log da API encontrado em $API_LOG_FILE"
            fi
            ;;
        ui|frontend)
            if [ -f "$UI_LOG_FILE" ]; then
                log_info "Streaming de logs da UI ($UI_LOG_FILE)..."
                tail -n 50 -f "$UI_LOG_FILE"
            else
                log_warn "Nenhum arquivo de log da UI encontrado em $UI_LOG_FILE"
            fi
            ;;
        docker)
            shift || true
            ./docker-env.sh logs "$@"
            ;;
        *)
            log_error "Alvo de log desconhecido: '$target'. Use: api, ui ou docker."
            exit 1
            ;;
    esac
}

cmd_test() {
    log_info "Executando suíte de testes backend..."
    (cd gestao-paroquia && ./gradlew test)
    log_success "Testes do backend concluídos com sucesso."

    if [ -d "gestao-paroquia-ui" ]; then
        log_info "Executando testes/typecheck do frontend..."
        (
            cd gestao-paroquia-ui
            if [ -f "package.json" ]; then
                npm run build || true
            fi
        )
    fi
}

cmd_check() {
    log_info "Executando quality gates rigorosos (Checkstyle, PMD, SpotBugs, ArchUnit)..."
    (cd gestao-paroquia && ./gradlew check)
    log_success "Todos os quality gates foram aprovados!"
}

cmd_build() {
    log_info "Compilando Backend e gerando JAR..."
    (cd gestao-paroquia && ./gradlew build -x test)
    log_success "Backend compilado com sucesso."

    if [ -d "gestao-paroquia-ui" ]; then
        log_info "Compilando Frontend..."
        (
            cd gestao-paroquia-ui
            npm ci --prefer-offline
            npm run build
        )
        log_success "Frontend compilado com sucesso."
    fi
}

cmd_seed() {
    log_info "Aplicando semente de dados via docker-env..."
    ./docker-env.sh seed
}

cmd_clean() {
    log_info "Limpando artefatos de build do Gradle..."
    (cd gestao-paroquia && ./gradlew clean)
    if [ -d "gestao-paroquia-ui/dist" ]; then
        log_info "Limpando dist do frontend..."
        rm -rf gestao-paroquia-ui/dist
    fi
    rm -rf "$PID_DIR"
    log_success "Limpeza local finalizada."
}

# Roteador principal
COMMAND="${1:-help}"
shift || true

case "$COMMAND" in
    dev)     cmd_dev "$@" ;;
    stop)    cmd_stop "$@" ;;
    restart) cmd_restart "$@" ;;
    status)  cmd_status "$@" ;;
    doctor)  cmd_doctor "$@" ;;
    logs)    cmd_logs "$@" ;;
    seed)    cmd_seed "$@" ;;
    test)    cmd_test "$@" ;;
    check)   cmd_check "$@" ;;
    build)   cmd_build "$@" ;;
    clean)   cmd_clean "$@" ;;
    docker)  ./docker-env.sh "$@" ;;
    help|-h|--help) show_help ;;
    *)
        log_error "Comando desconhecido: '$COMMAND'"
        echo ""
        show_help
        exit 1
        ;;
esac
