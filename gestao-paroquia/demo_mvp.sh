#!/bin/bash

# demo_mvp.sh - A complete lifecycle demonstration of the NS Fatima Calendar MVP
# This script interacts with the API and generates a rich HTML report.
# Default is http://localhost:8080/api/v1 (standard local/docker mapping)
# Requires: curl, jq, uuidgen

set -e

# --- Configuration & Setup ---
BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1}"
HEALTH_URL="${BASE_URL%/api/v1}/actuator/health"
LOG_FILE="demo_results.log"
HTML_FILE="demo_report.html"
COOKIE_JAR="demo_cookies.txt"

# Colors for CLI output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# IDs from init-data.sql
ORG_CATEQUESE="00000000-0000-0000-0000-0000000000aa"
ORG_LITURGIA="00000000-0000-0000-0000-0000000000bb"
ORG_CLERO="00000000-0000-0000-0000-0000000000dd"

# User mapping for real authentication
declare -A USER_MAP
USER_MAP["coord_catequese"]="maria.catequese:senha123"
USER_MAP["coord_liturgia"]="pedro.liturgia:senha123"
USER_MAP["coord_conselho"]="maria.conselho:senha123"
USER_MAP["paroco"]="padre.pedro:senha123"
USER_MAP["membro"]="joao.liturgia:senha123"

# --- Utility Functions ---

log_info() { echo -e "${BLUE}[INFO]${NC} $1" >&2; }
log_step() { 
    echo -e "\n${CYAN}======================================================================${NC}" >&2
    echo -e "${CYAN} STEP: $1${NC}" >&2
    echo -e "${CYAN}======================================================================${NC}" | tee -a $LOG_FILE >&2
    html_append_step "$1"
}
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1" >&2; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1" >&2; }
log_error() { echo -e "${RED}[ERROR]${NC} $1" >&2; html_finish "FAILED"; exit 1; }

pretty_json() {
    local json=$1
    if [ -n "$json" ] && command -v jq >/dev/null 2>&1; then
        echo -e "${MAGENTA}--- SERVICE RESPONSE ---${NC}" >&2
        echo "$json" | jq -C '.' >&2
        echo -e "${MAGENTA}------------------------${NC}" >&2
    else
        echo "$json" >&2
    fi
}

# --- HTML Report Generation ---

html_init() {
    cat <<EOF > $HTML_FILE
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NS Fatima Calendar MVP - Demo Report</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; background-color: #f4f7f6; margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; margin-top: 0; }
        .meta { color: #7f8c8d; font-size: 0.9em; margin-bottom: 20px; }
        .step { margin-top: 30px; border-left: 4px solid #3498db; padding-left: 15px; }
        .step-title { font-size: 1.2em; font-weight: bold; color: #2980b9; margin-bottom: 10px; text-transform: uppercase; }
        .interaction { background: #fafafa; border: 1px solid #eee; border-radius: 4px; padding: 15px; margin-bottom: 15px; }
        .interaction-header { font-weight: bold; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; }
        .method { background: #3498db; color: #fff; padding: 2px 8px; border-radius: 3px; font-size: 0.8em; margin-right: 10px; }
        .payload-box, .response-box { margin-top: 10px; }
        label { font-size: 0.8em; font-weight: bold; color: #95a5a6; display: block; margin-bottom: 3px; }
        pre { background: #272822; color: #f8f8f2; padding: 12px; border-radius: 4px; overflow-x: auto; font-size: 0.9em; margin: 0; }
        .status-success { color: #27ae60; }
        .status-warn { color: #f39c12; }
        .status-error { color: #c0392b; font-weight: bold; }
        .footer { margin-top: 50px; text-align: center; color: #bdc3c7; font-size: 0.8em; }
        .badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 0.8em; font-weight: bold; color: #fff; }
        .badge-role { background: #9b59b6; }
        .badge-status { background: #2ecc71; margin-left: 10px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>NS Fatima Calendar MVP Demonstration</h1>
        <div class="meta">
            Generated on: $(date)<br>
            API Base URL: $BASE_URL
        </div>
        <div id="content">
EOF
}

html_append_step() {
    echo -e "        <div class=\"step\"><div class=\"step-title\">$1</div>" >> $HTML_FILE
}

html_append_interaction() {
    local method=$1
    local endpoint=$2
    local role=$3
    local description=$4
    local payload=$5
    local response=$6
    local status_code=$7

    cat <<EOF >> $HTML_FILE
            <div class="interaction">
                <div class="interaction-header">
                    <span><span class="method">$method</span> $endpoint</span>
                    <span>
                        <span class="badge badge-role">$role</span>
                        <span class="badge badge-status">HTTP $status_code</span>
                    </span>
                </div>
                <div style="font-size: 0.9em; color: #7f8c8d; margin-bottom: 10px;">$description</div>
EOF

    if [ -n "$payload" ] && [ "$payload" != "null" ]; then
        echo "                <div class=\"payload-box\"><label>PAYLOAD</label><pre>$(echo "$payload" | jq '.' 2>/dev/null || echo "$payload")</pre></div>" >> $HTML_FILE
    fi

    cat <<EOF >> $HTML_FILE
                <div class="response-box"><label>RESPONSE</label><pre>$(echo "$response" | jq '.' 2>/dev/null || echo "$response")</pre></div>
            </div>
EOF
}

html_finish() {
    local result=$1
    cat <<EOF >> $HTML_FILE
        </div>
        <div class="footer">
            Execution Result: <span class="status-${result,,}">$result</span> | NS Fatima Pastoral Digital Team
        </div>
    </div>
</body>
</html>
EOF
    log_info "HTML Report generated: $HTML_FILE"
}

# --- Interaction functions ---

wait_for_api() {
    log_info "Checking API Health at $HEALTH_URL..."
    local max_attempts=30
    local attempt=1
    until curl -s "$HEALTH_URL" | grep -q "UP" || [ $attempt -eq $max_attempts ]; do
        echo -n "." >&2
        sleep 2
        attempt=$((attempt + 1))
    done
    echo "" >&2
    if [ $attempt -eq $max_attempts ]; then
        log_error "API failed to start."
    fi
    log_success "API is ONLINE and HEALTHY!"
}

do_login() {
    local role=$1
    local user_pass=${USER_MAP[$role]}
    local username=${user_pass%:*}
    local password=${user_pass#*:}

    rm -f $COOKIE_JAR

    response=$(curl -s -i -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -c $COOKIE_JAR \
        -d "{\"username\": \"$username\", \"password\": \"$password\"}")

    if ! echo "$response" | grep -q "HTTP/1.1 200"; then
        log_error "Login failed for $role ($username)"
    fi
}

do_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local role=$4
    local description=$5

    do_login "$role"

    echo -e ">> $description" >> $LOG_FILE
    echo "Request: $method $endpoint" >> $LOG_FILE
    
    log_info "Action: $description (as $role)"
    log_info "Endpoint: $method $endpoint"

    local extra_headers=()
    if [[ "$method" == "POST" || "$method" == "PATCH" || "$method" == "PUT" ]]; then
        local idempotency_key=$(uuidgen)
        extra_headers+=("-H" "Idempotency-Key: $idempotency_key")
        echo "Idempotency-Key: $idempotency_key" >> $LOG_FILE
    fi

    # body to file, status to stdout
    status_file=$(mktemp)
    http_status=$(curl -s -w "%{http_code}" -o "$status_file" -X $method "$BASE_URL$endpoint" -H "Content-Type: application/json" "${extra_headers[@]}" -b $COOKIE_JAR ${data:+-d "$data"})
    actual_response=$(cat "$status_file")
    rm "$status_file"

    if [ -z "$actual_response" ]; then log_error "Empty response from API during: $description"; fi

    echo "Response ($http_status): $actual_response" >> $LOG_FILE
    
    # Record to HTML
    html_append_interaction "$method" "$endpoint" "$role" "$description" "$data" "$actual_response" "$http_status"

    echo "$actual_response"
}

# --- Demo Lifecycle ---

html_init
clear
echo -e "${YELLOW}"
echo "   _   _ ____    _____ _  _____ ___ __  __    _      "
echo "  | \ | / ___|  |  ___/ \  |_   _|_ _|  \/  |  / \     "
echo "  |  \| \___ \  | |_ / _ \   | |  | || |\/| | / _ \    "
echo "  | |\  |___) | |  _/ ___ \  | |  | || |  | |/ ___ \   "
echo "  |_| \_|____/  |_|/_/   \_\ |_| |___|_|  |_/_/   \_\  "
echo "                                                       "
echo "         CALENDAR MVP - ENRICHED LIVE DEMO             "
echo -e "${NC}"

wait_for_api

# 1. Project Creation
log_step "Project Management: Planning the Year"
PROJ_INICIO="2026-01-01T00:00:00Z"
PROJ_FIM="2027-12-31T23:59:59Z"
PROJETO_PAYLOAD="{\"nome\": \"Plano Bienal 2026-2027\", \"descricao\": \"Planejamento integrado\", \"organizacaoResponsavelId\": \"$ORG_CATEQUESE\", \"inicio\": \"$PROJ_INICIO\", \"fim\": \"$PROJ_FIM\"}"
RESPONSE=$(do_request "POST" "/projetos" "$PROJETO_PAYLOAD" "coord_catequese" "Creating Project")
pretty_json "$RESPONSE"
PROJETO_ID=$(echo "$RESPONSE" | jq -r '.id')
log_success "Project Created: $PROJETO_ID"

# 2. Happy Path: Confirmed Event
log_step "Scheduling: Adding a Standard Confirmed Event"
INICIO_EVENTO="2027-02-10T10:00:00Z"
FIM_EVENTO="2027-02-10T12:00:00Z"
EVENTO_PAYLOAD="{\"titulo\": \"Missa de Abertura\", \"categoria\": \"LITURGICO\", \"organizacaoResponsavelId\": \"$ORG_CATEQUESE\", \"projetoId\": \"$PROJETO_ID\", \"inicio\": \"$INICIO_EVENTO\", \"fim\": \"$FIM_EVENTO\", \"status\": \"CONFIRMADO\"}"
RESPONSE=$(do_request "POST" "/eventos" "$EVENTO_PAYLOAD" "coord_catequese" "Creating Mass Event")
pretty_json "$RESPONSE"
APROV_ID_MASS=$(echo "$RESPONSE" | jq -r '.solicitacaoAprovacaoId')
log_success "Event creation requested (Pending Approval)."

# 2.5 Approve the Mass Event so we can add notes later
log_step "Governance: Approving Mass Event for later collaboration"
DECISION_MASS='{"status": "APROVADA", "observacao": "Ok"}'
RESPONSE=$(do_request "PATCH" "/aprovacoes/$APROV_ID_MASS" "$DECISION_MASS" "paroco" "Approving Mass Event")
EVENTO_ID_MASS=$(echo "$RESPONSE" | jq -r '.actionExecution.eventoId')
log_info "Mass Event activated with ID: $EVENTO_ID_MASS"

# 3. Conflict Detection Scenario
log_step "Validation Rule: Detecting Time Conflicts"
EVENTO_CONFLITO_PAYLOAD="{\"titulo\": \"Treinamento de Coroinhas\", \"categoria\": \"FORMATIVO\", \"organizacaoResponsavelId\": \"$ORG_LITURGIA\", \"inicio\": \"$INICIO_EVENTO\", \"fim\": \"$FIM_EVENTO\", \"status\": \"CONFIRMADO\"}"
RESPONSE=$(do_request "POST" "/eventos" "$EVENTO_CONFLITO_PAYLOAD" "coord_liturgia" "Attempting conflicting event")
pretty_json "$RESPONSE"
log_warn "Event Status: $(echo "$RESPONSE" | jq -r '.status'). Resource conflict detected!"

# 4. Approval Flow: Sensitive Status
log_step "Control Flow: Requesting an EXTRA event"
EXTRA_INICIO="2027-05-20T14:00:00Z"
EXTRA_FIM="2027-05-20T17:00:00Z"
EVENTO_EXTRA_PAYLOAD="{\"titulo\": \"Retiro Extraordinario\", \"categoria\": \"PASTORAL\", \"organizacaoResponsavelId\": \"$ORG_CATEQUESE\", \"projetoId\": \"$PROJETO_ID\", \"inicio\": \"$EXTRA_INICIO\", \"fim\": \"$EXTRA_FIM\", \"status\": \"ADICIONADO_EXTRA\", \"adicionadoExtraJustificativa\": \"Demanda espontanea dos pais\"}"
RESPONSE=$(do_request "POST" "/eventos" "$EVENTO_EXTRA_PAYLOAD" "coord_catequese" "Creating extra event")
pretty_json "$RESPONSE"
APROVACAO_ID=$(echo "$RESPONSE" | jq -r '.solicitacaoAprovacaoId')
log_info "Approval Request: $APROVACAO_ID"

# 5. Paroco Review: Approval
log_step "Governance: Paroco APPROVES the request"
DECISION_PAYLOAD="{\"status\": \"APROVADA\", \"observacao\": \"Autorizado.\"}"
RESPONSE=$(do_request "PATCH" "/aprovacoes/$APROVACAO_ID" "$DECISION_PAYLOAD" "paroco" "Approving request")
pretty_json "$RESPONSE"
log_success "Event activated."

# 6. Rejection Scenario
log_step "Governance: Paroco REJECTS a request"
REJECT_PAYLOAD="{\"titulo\": \"Evento Inoportuno\", \"categoria\": \"SOCIAL\", \"organizacaoResponsavelId\": \"$ORG_LITURGIA\", \"inicio\": \"2027-06-10T10:00:00Z\", \"fim\": \"2027-06-10T12:00:00Z\", \"status\": \"ADICIONADO_EXTRA\", \"adicionadoExtraJustificativa\": \"Nao sei\"}"
RESPONSE=$(do_request "POST" "/eventos" "$REJECT_PAYLOAD" "coord_liturgia" "Creating event to be rejected")
pretty_json "$RESPONSE"
REJECT_APROVACAO_ID=$(echo "$RESPONSE" | jq -r '.solicitacaoAprovacaoId')
REJECT_DECISION="{\"status\": \"REPROVADA\", \"observacao\": \"Justificativa insuficiente.\"}"
RESPONSE=$(do_request "PATCH" "/aprovacoes/$REJECT_APROVACAO_ID" "$REJECT_DECISION" "paroco" "Rejecting request")
pretty_json "$RESPONSE"
log_success "Event rejected."

# 7. Collaboration: Adding Notes
log_step "Collaboration: Social Layer (Notes & Observations)"
NOTE_PAYLOAD="{\"tipo\": \"NOTA\", \"conteudo\": \"Lembrar do coral das crianças.\"}"
RESPONSE=$(do_request "POST" "/eventos/$EVENTO_ID_MASS/observacoes" "$NOTE_PAYLOAD" "coord_catequese" "Adding note")
pretty_json "$RESPONSE"
log_info "Notes allow rich context without modifying formal event attributes."

# 8. Project Visibility & Health
log_step "Observability: Project Health Check"
RESPONSE=$(do_request "GET" "/projetos/$PROJETO_ID/resumo" "" "membro" "Fetching Summary")
pretty_json "$RESPONSE"

# 9. Audit & Metrics
log_step "Analytics: Dashboard Metrics"
RESPONSE=$(do_request "GET" "/auditoria/eventos/extras?organizacaoId=$ORG_CATEQUESE&periodo=anual" "" "coord_catequese" "Querying Extra Rate")
pretty_json "$RESPONSE"
RESPONSE=$(do_request "GET" "/auditoria/eventos/retrabalho?organizacaoId=$ORG_CATEQUESE&granularidade=anual" "" "coord_catequese" "Querying Rework")
pretty_json "$RESPONSE"

log_step "Audit Trail (History Sample)"
RESPONSE=$(do_request "GET" "/auditoria/eventos/trilha?organizacaoId=$ORG_CATEQUESE&granularidade=anual" "" "coord_catequese" "Full Trail")
TRAIL_COUNT=$(echo "$RESPONSE" | jq '.items | length')
log_info "Audit records verified: $TRAIL_COUNT"

# 10. Calendar Lock Governance
log_step "Governance: Locking the 2027 Calendar (Planning Phase End)"
LOCK_PAYLOAD='{"status": "FECHADO"}'
RESPONSE=$(do_request "PATCH" "/anos-paroquiais/2027" "$LOCK_PAYLOAD" "coord_conselho" "Locking 2027")
pretty_json "$RESPONSE"
log_success "Calendar for 2027 is now CLOSED for regular planning."

log_step "Governance Rule: Denying regular events in a LOCKED year"
EVENTO_BLOQUEADO_PAYLOAD=$(cat <<EOF
{
  "titulo": "Evento de Ultima Hora (Tentativa)",
  "categoria": "FORMATIVO",
  "organizacaoResponsavelId": "$ORG_CATEQUESE",
  "projetoId": "$PROJETO_ID",
  "inicio": "2027-08-10T10:00:00Z",
  "fim": "2027-08-10T12:00:00Z",
  "status": "CONFIRMADO"
}
EOF
)
RESPONSE=$(do_request "POST" "/eventos" "$EVENTO_BLOQUEADO_PAYLOAD" "coord_catequese" "Attempting regular event in locked year")
pretty_json "$RESPONSE"
ERROR_CODE=$(echo "$RESPONSE" | jq -r '.errorCode')
log_warn "Notice the error code: $ERROR_CODE. The system prevented a plan violation!"

log_step "Governance Rule: Allowing ADICIONADO_EXTRA in a LOCKED year"
EVENTO_EXTRA_LOCKED_PAYLOAD=$(cat <<EOF
{
  "titulo": "Evento Extra em Ano Travado",
  "categoria": "FORMATIVO",
  "organizacaoResponsavelId": "$ORG_CATEQUESE",
  "projetoId": "$PROJETO_ID",
  "inicio": "2027-09-15T10:00:00Z",
  "fim": "2027-09-15T12:00:00Z",
  "status": "ADICIONADO_EXTRA",
  "adicionadoExtraJustificativa": "Necessidade pastoral detectada apos fechamento"
}
EOF
)
RESPONSE=$(do_request "POST" "/eventos" "$EVENTO_EXTRA_LOCKED_PAYLOAD" "coord_catequese" "Adding extra event in locked year")
pretty_json "$RESPONSE"
log_success "Extra event accepted even with locked calendar (Approval flow triggered)."

html_finish "SUCCESS"
echo -e "\n${GREEN}======================================================================${NC}"
echo -e "${GREEN} ENRICHED DEMO COMPLETED - REPORT GENERATED: $HTML_FILE${NC}"
echo -e "${GREEN}======================================================================${NC}"
rm -f $COOKIE_JAR
