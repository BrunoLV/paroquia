# 📊 Padrão de Logging, LGPD & Métricas

Este documento define os padrões mandatórios de observabilidade do **Projeto Paróquia**, detalhando a infraestrutura assíncrona de logs com Log4j2, os controles de privacidade de dados (LGPD / PII Zero) e a telemetria baseada em Spring Boot Actuator e Prometheus.

---

## 1. Arquitetura de Logging Assíncrono com Log4j2

O backend adota o **Apache Log4j2** configurado exclusivamente em modo assíncrono com o **LMAX Disruptor**:

```
[Virtual Thread / UseCase] 
           │
           ▼
[Log4j2 Event Logger] ──(Zero Bloqueio)──► [Disruptor RingBuffer (Lock-free)]
                                                    │
                                                    ▼ (Background Thread)
                                           [Async Logger Worker]
                                                    │
                                                    ├──► [PII Masking Filter]
                                                    │
                                                    └──► [JSON Output Appender] ──► Console / STDOUT
```

### 1.1. Configuração do LMAX Disruptor
- A propriedade JVM `-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector` garante que todas as chamadas de log ocorram de forma *lock-free* e sem bloquear a execução das Virtual Threads.
- Tamanho do buffer circular: configurado para alta vazão com descarte gracioso sob sobrecarga extrema (*garbage-free logging*).

---

## 2. Rastreabilidade com MDC (Mapped Diagnostic Context)

Toda requisição HTTP que ingressa no backend passa por um filtro de observabilidade (`MdcFilter`) que injeta metadados contextuais padronizados no contexto de log:

```json
{
  "timestamp": "2026-09-16T11:45:02.124Z",
  "level": "INFO",
  "logger": "br.com.nsfatima.gestao.calendario.application.usecase.CriarEventoUseCase",
  "message": "Evento criado com sucesso. ID: 4f1a2380-6bc1-40c2-a7d1-05244b7d19e0",
  "context": {
    "correlationId": "c7a8b920-1e9a-4f51-b847-190284051a23",
    "userId": "usr_99812",
    "username": "secretaria.central",
    "tenantId": "paroquia-ns-fatima",
    "userRoles": ["SECRETARIA"],
    "action": "CRIAR_EVENTO",
    "httpMethod": "POST",
    "requestUri": "/api/v1/eventos"
  }
}
```

### Diretrizes de Uso do MDC:
- **`correlationId`:** Gerado no primeiro ponto de contato (ou propagado pelo cabeçalho HTTP `X-Correlation-ID`) e mantido ao longo de toda a esteira de processamento e respostas de erro (`ProblemDetail`).
- **Limpeza Garantida:** O filtro sempre executa `MDC.clear()` em bloco `finally` para evitar vazamento de contexto entre reusos de thread.

---

## 3. Conformidade LGPD & Política PII Zero em Logs

Em estrita consonância com a **LGPD (Lei 13.709/2018)** e a skill canônica [`lgpd-privacy-by-design`](.agents/skills/lgpd-privacy-by-design/SKILL.md), é **expressamente proibido** imprimir Informações Pessoais Identificáveis (PII) em texto claro nos logs da aplicação.

### 3.1. Dados com Proibição Absoluta de Log
- CPFs, RGs ou números de documentos civis.
- Endereços residenciais, CEPs e números prediais.
- Telefones e números de WhatsApp.
- E-mails pessoais de fiéis ou clérigos.
- Dados de saúde, donativos financeiros nominais ou anotações de aconselhamento espiritual.

### 3.2. Mascaramento Automático (*Sanitization Pattern*)
Sempre que for estritamente necessário registrar a ocorrência de um evento associado a um fiel, utilize identificadores sintéticos (`FielId`) ou padrões de mascaramento (*masking*):

| Dado | Formato de Mascaramento | Exemplo |
| :--- | :--- | :--- |
| **CPF** | `***.NNN.NNN-**` | `***.456.789-**` |
| **E-mail** | `p***@dominio.com` | `j***@gmail.com` |
| **Telefone** | `(NN) *****-NNNN` | `(11) *****-1234` |

---

---

## 4. Métricas e Telemetria com Spring Boot Actuator, Micrometer & Prometheus

O sistema exporta métricas em tempo real no formato aberto do **Prometheus** através da rota `/actuator/prometheus`, além de métricas internas do PostgreSQL via **PostgreSQL Exporter** na porta `9187`.

### 4.1. Catálogo de Métricas de Negócio (Eventos & Aprovações)

| Nome da Métrica | Tipo | Descrição | Tags / Dimensões |
| :--- | :--- | :--- | :--- |
| `gestao.evento.criados.total` | Counter | Total de eventos criados no calendário | `conflito_pendente`, `replay` |
| `gestao.evento.falhas.total` | Counter | Total de falhas ou bloqueios no cadastro | `categoria` |
| `gestao.evento.tempo.cadastro` | Timer | Tempo de processamento do cadastro de evento | - |
| `gestao.evento.lead_time` | Timer | Lead time entre a solicitação e realização do evento | - |
| `gestao.evento.consulta.latencia` | Timer | Latência de consultas e buscas na agenda | `endpoint` |
| `gestao.evento.retrabalho.total` | Counter | Total de eventos de retrabalho administrativo | `horario_alterado`, `cancelamento`, `organizacao_alterada` |
| `gestao.evento.cancelamentos.total` | Counter | Total de cancelamentos de eventos | `modo`, `outcome` |
| `gestao.evento.conflitos.total` | Counter | Conflitos de agenda detectados | `tipo` |
| `gestao.evento.idempotencia.replays.total` | Counter | Reenvios idempotentes repetidos | - |
| `gestao.eventos.confirmados` | Gauge | Quantidade de eventos confirmados ativos no banco | - |
| `gestao.eventos.rascunhos` | Gauge | Quantidade de eventos em rascunho no banco | - |
| `gestao.aprovacao.solicitadas.total` | Counter | Total de solicitações de aprovação abertas | `tipo`, `papel_solicitante` |
| `gestao.aprovacoes.total` | Counter | Total de solicitações deferidas | `tipo` |
| `gestao.rejeicoes.total` | Counter | Total de solicitações indeferidas | `tipo` |
| `gestao.aprovacao.decisoes.total` | Counter | Total de decisões de governança tomadas | `decisao`, `tipo` |
| `gestao.aprovacao.tempo.decisao` | Timer | Tempo decorrido entre criação da solicitação e decisão | `tipo`, `status` |
| `gestao.aprovacao.tempo.execucao` | Timer | Tempo de execução da ação após deferimento | `tipo` |
| `gestao.aprovacao.tempo.total` | Timer | Tempo total de ponta a ponta do ciclo de aprovação | `tipo` |
| `gestao.aprovacoes.pendentes` | Gauge | Quantidade de solicitações aguardando decisão | - |
| `gestao.delecoes.total` | Counter | Total de exclusões/cancelamentos de recursos | `recurso`, `origem` |
| `gestao.edicoes.total` | Counter | Total de atualizações/edições de recursos | `recurso`, `origem` |

### 4.2. Telemetria de Banco de Dados & Pool de Conexões

- **Pool HikariCP (`hikaricp_*`):**
  - `hikaricp_connections_active`: conexões em uso pela aplicação.
  - `hikaricp_connections_idle`: conexões disponíveis no pool.
  - `hikaricp_connections_pending`: threads bloqueadas aguardando conexão.
  - `hikaricp_connections_acquire_seconds`: latência de aquisição de conexão (P95, Max).
  - `hikaricp_connections_usage_seconds`: tempo de retenção de conexão por transação.
  - `hikaricp_connections_timeout_total`: falhas de esgotamento de pool.
- **Servidor PostgreSQL (`pg_stat_*` via postgres-exporter):**
  - `pg_stat_database_numbackends`: conexões concorrentes ativas no banco.
  - `pg_stat_database_xact_commit` / `_rollback`: taxa de transações por segundo.
  - `pg_stat_database_blks_hit` vs `_read`: índice de acertos no buffer de cache (Cache Hit Ratio).
  - `pg_stat_database_tup_*`: tuplas lidas, inseridas, atualizadas e deletadas.
  - `pg_database_size_bytes`: tamanho físico do banco de dados em disco.

### 4.3. Healthchecks & Probes de Orquestração
As rotas de saúde são configuradas para garantir integração imediata com orquestradores de contêineres (Docker / Kubernetes):

- **Liveness Probe (`/actuator/health/liveness`):**
  Indica se o processo Java está vivo e respondendo. Caso falhe, o orquestrador reinicia o contêiner.
- **Readiness Probe (`/actuator/health/readiness`):**
  Verifica se as conexões com o PostgreSQL e migrações do Flyway estão prontas para receber tráfego de usuários.

---

## 5. Dashboards no Grafana

O ecossistema conta com **4 dashboards modulares e especializados**, provisionados automaticamente sob a pasta *Gestão Paróquia*:

1. **Gestão Paroquial - Negócio (Eventos & Aprovações):**
   - *UID:* `paroquia-negocio-eventos-aprovacoes`
   - *Finalidade:* Painel executivo com volume de eventos, taxas de deferimento, cancelamentos, conflitos e SLAs de decisão pastoral.
2. **Gestão Paroquial - Infraestrutura & Performance:**
   - *UID:* `paroquia-infraestrutura-performance`
   - *Finalidade:* Monitoramento técnico de tráfego HTTP, latências P95 por rota, taxas de erro 5xx, memória JVM Heap/Non-Heap, Garbage Collection e threads.
3. **Gestão Paroquial - Banco de Dados (HikariCP & PostgreSQL):**
   - *UID:* `paroquia-banco-dados`
   - *Finalidade:* Visão de integridade e saturação de conexões, fila de requisições pendentes, latência de aquisição, taxa de transações e cache hit ratio.
4. **Logs do Sistema (Loki):**
   - *UID:* `paroquia-logs`
   - *Finalidade:* Centralização de logs do backend e frontend com suporte a filtros de severidade, contexto por `correlationId` e busca textual em tempo real.

---

## 6. Agregação e Centralização de Logs com Grafana Loki & Promtail

O ecossistema conta com **Grafana Loki** e **Promtail** para indexação e visualização unificada de logs de infraestrutura e aplicação.

### 6.1. Arquitetura de Coleta Híbrida
- **Promtail:** Agente leve responsável pelo envio de streams de logs para a API push do Loki (`http://loki:3100/loki/api/v1/push`).
  - **Containers Docker:** Descoberta dinâmica via `/var/run/docker.sock`, etiquetando `gestao-api` com `app="backend"` e `gestao-ui` com `app="frontend"`.
  - **Desenvolvimento Local:** Monitoramento ativo dos arquivos `.run/api.log` e `.run/ui.log` gerados pelo comando `./manage.sh dev`.
  - **Extração de Severidade:** Pipeline regex que extrai os níveis `INFO`, `WARN`, `ERROR` e `DEBUG` para indexação instantânea como label.

### 6.2. Telemetria de Logs da UI (Client-Side)
- O frontend React 19 conta com o módulo singleton `logger.ts` (`src/shared/infrastructure/logging/logger.ts`), que:
  - Captura erros de renderização via `AppErrorBoundary`.
  - Captura falhas de comunicação HTTP via interceptor do Axios.
  - Captura exceções e rejeições globais (`window.addEventListener('error')`, `unhandledrejection`).
  - Enfileira eventos em buffer e descarrega em lote assíncrono para o endpoint `POST /api/v1/observabilidade/logs`.
- O backend sanitiza o lote contra PII (LGPD PII Zero) e emite os registros via Log4j2 sob a tag `[UI_CLIENT]`, permitindo correlação ponta a ponta pelo `correlationId`.

### 6.3. Consultas LogQL Úteis no Grafana
- **Todos os logs do backend:** `{app="backend"}`
- **Erros no backend:** `{app="backend", level="ERROR"}`
- **Logs do frontend / interface:** `{app="frontend"}` ou `{app="backend"} |= "[UI_CLIENT]"`
- **Rastreamento por Correlation ID:** `{app=~"backend|frontend"} |= "corrId=c7a8b920-..."`


