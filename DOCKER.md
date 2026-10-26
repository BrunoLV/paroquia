# Arquitetura e Guia Docker - Paróquia

Este documento descreve a infraestrutura de contêineres, orquestração e ferramentas de desenvolvimento do projeto **Paróquia**.

---

## 1. Visão Geral da Arquitetura

O ambiente em contêineres é gerenciado via **Docker Compose** e isolado na rede bridge personalizada `paroquia-net`. Os serviços cobrem o ciclo completo de desenvolvimento, desde a compilação e execução até o monitoramento e observabilidade em tempo real.

```mermaid
flowchart TD
    subgraph Host["Host Machine"]
        User["Desenvolvedor / Browser"]
        DockerSock["/var/run/docker.sock"]
    end

    subgraph ParoquiaNet["Rede Bridge: paroquia-net"]
        subgraph DevInfra["Infraestrutura & Persistência"]
            DB[("PostgreSQL 16\n:5432\n(gestao-db)")]
            DBSeeder["Seeder Inicial\n(gestao-db-seeder)"]
            PGAdmin["pgAdmin 4\n:5050\n(gestao-pgadmin)"]
        end

        subgraph CoreApp["Aplicações Principais"]
            Builder["Builder Container\n(gestao-builder)"]
            API["Backend REST API\nSpring Boot 4 / Java 25\n:8080\n(gestao-api)"]
            UI["Frontend React (nginx)\n:3000\n(gestao-ui)"]
        end

        subgraph Observability["Observabilidade"]
            Prometheus["Prometheus v3\n:9090\n(gestao-prometheus)"]
            Loki["Grafana Loki v3\n:3100\n(gestao-loki)"]
            Promtail["Promtail v3\n(gestao-promtail)"]
            Grafana["Grafana v11\n:3001\n(gestao-grafana)"]
        end
    end

    User -->|":3000"| UI
    User -->|":8080"| API
    User -->|":5050"| PGAdmin
    User -->|":9090"| Prometheus
    User -->|":3001"| Grafana

    UI -->|"Proxy API"| API
    API -->|"JDBC"| DB
    DBSeeder -->|"Carga SQL"| DB
    PGAdmin -->|"Administração"| DB

    Builder -.->|"Compila bootJar"| API
    DockerSock -.->|"Wormhole"| Builder

    Prometheus -->|"Scrape :8080/actuator/prometheus"| API
    Promtail -->|"Coleta logs de contêineres"| Loki
    Grafana -->|"Datasource PromQL"| Prometheus
    Grafana -->|"Datasource LogQL"| Loki
```

---

## 2. Tabela de Serviços e Portas

| Serviço | Contêiner | Imagem Base | Porta Host | Finalidade | Credenciais Padrão |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ui** | `gestao-ui` | `nginxinc/nginx-unprivileged:1.27-alpine` | `3000` | Interface SPA (build estático React servido por nginx) | N/A |
| **api** | `gestao-api` | `eclipse-temurin:25-jre-alpine` | `8080` | Backend REST API (Spring Boot) | N/A |
| **db** | `gestao-db` | `postgres:16-alpine` | `5432` | Banco de Dados Relacional | `postgres` / `POSTGRES_PASSWORD` do `.env` (DB: `calendario`) |
| **builder** | `gestao-builder` | `eclipse-temurin:25-jdk-alpine` | N/A | Build e execução de Testcontainers | Usuário interno: `dev` |
| **db-seeder** | `gestao-db-seeder` | `postgres:16-alpine` | N/A | Carga de dados de teste (somente `--profile dev`) | `postgres` / `POSTGRES_PASSWORD` do `.env` |
| **pgadmin** | `gestao-pgadmin` | `dpage/pgadmin4:9.1` | `5050` | Gerenciador Web do Postgres | `PGADMIN_DEFAULT_EMAIL` / `PGADMIN_DEFAULT_PASSWORD` do `.env` |
| **prometheus**| `gestao-prometheus`| `prom/prometheus:v3.2.1` | `9090` | Coletor TSDB de métricas | N/A |
| **grafana** | `gestao-grafana` | `grafana/grafana:11.5.2` | `3001` | Painéis e Dashboards analíticos | `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` do `.env` |
| **loki** | `gestao-loki` | `grafana/loki:3.2.1` | `3100` | Agregação e Armazenamento de Logs | N/A |
| **promtail** | `gestao-promtail` | `grafana/promtail:3.2.1` | N/A | Coletor e remetente de logs para Loki | N/A |

---

## 3. Padrão Docker Wormhole (Builder + Testcontainers)

Para suportar testes automatizados de integração que sobem bancos de dados reais sem necessidade de Docker-in-Docker (DinD privilegiado), o contêiner `gestao-builder` utiliza o padrão **Docker-out-of-Docker (DooD / Wormhole)**:

1. O socket do Docker do host (`/var/run/docker.sock`) é montado no contêiner `gestao-builder`.
2. O script [`docker/scripts/builder-entrypoint.sh`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/docker/scripts/builder-entrypoint.sh) detecta dinamicamente o GID numérico do socket do host, cria um grupo compatível dentro do contêiner e associa o usuário `dev`.
3. O comando do Gradle ou Testcontainers é executado via `gosu dev`, permitindo que os testes instanciem contêineres irmãos diretamente no daemon Docker do hospedeiro, sem executar código como `root`.

---

## 4. Volumes Persistentes

O ecossistema utiliza volumes nomeados para isolamento e reaproveitamento de caches:

- `paroquia_postgres_data`: Diretório de dados persistentes do PostgreSQL (`/var/lib/postgresql/data`).
- `paroquia_shared_volume`: Compartilhamento do artefato `app.jar` compilado pelo builder para o runtime da API.
- `paroquia_gradle_cache`: Cache persistente de dependências do Gradle (`/root/.gradle`).
- `paroquia_prometheus_data`: Armazenamento de séries temporais do Prometheus (`/prometheus`).
- `paroquia_grafana_data`: Configurações de usuários e preferências do Grafana (`/var/lib/grafana`).

---

## 5. Rotação de Logs e Segurança

- **Rotação de Logs:** Todos os serviços utilizam o driver `json-file` configurado com `max-size: 10m` e `max-file: 3`, prevenindo esgotamento de espaço em disco em ambientes de longa execução.
- **Princípio do Menor Privilégio (Não-Root):**
  - O contêiner de API roda sob o usuário sem privilégios `appuser` (`appgroup`).
  - O contêiner de UI (nginx-unprivileged) roda sem root, na porta interna 8080.
- **Governança de Recursos (Teto de 30% da Máquina):** O processo de build está estritamente limitado a no máximo 30% da capacidade do host: 3 núcleos de CPU (`cpus: '3.0'`, `org.gradle.workers.max=3`) e 4GB de RAM (`mem_limit: 4096M`, `-Xmx1536m`), garantindo que pelo menos 70% dos recursos permaneçam livres e dedicados ao sistema operacional.

---

## 6. Comandos e Operação

O gerenciamento é realizado pelos scripts canônicos:

### Usando o `docker-env.sh` (Focado no ciclo Docker)

```bash
# Subir todo o ambiente em segundo plano
./docker-env.sh up

# Subir com reconstrução forçada sem cache
./docker-env.sh up --no-cache

# Reiniciar ambiente com limpeza total de volumes de dados
./docker-env.sh restart --clean

# Exibir status detalhado e consumo de memória/CPU
./docker-env.sh status

# Acompanhar logs de um serviço específico
./docker-env.sh logs -f -s api

# Derrubar contêineres mantendo dados
./docker-env.sh down
```

### Usando o `manage.sh` (CLI Unificada)

```bash
# Iniciar infraestrutura Docker + rodar API e UI localmente
./manage.sh dev

# Parar todos os processos locais e contêineres
./manage.sh stop

# Executar quality gates rigorosos (Checkstyle, PMD, SpotBugs, ArchUnit)
./manage.sh check

# Executar suíte de testes
./manage.sh test

# Limpar caches de build
./manage.sh clean
```

---

## 7. Resolução de Problemas (Troubleshooting)

### Porta em uso (ex: 5432, 8080 ou 3000)
Verifique se há outro PostgreSQL ou aplicação rodando localmente:
```bash
sudo lsof -i :5432 -i :8080 -i :3000
```
Encerre os processos conflitantes antes de executar `./docker-env.sh up`.

### Permissão no socket Docker
Se o contêiner builder falhar ao acessar o daemon Docker, certifique-se de que o usuário do host faz parte do grupo `docker`:
```bash
sudo usermod -aG docker $USER
newgrp docker
```

### Limpeza Completa e Redefinição
Para resetar completamente o ecossistema Docker (excluindo volumes, imagens órfãs e redes):
```bash
./docker-env.sh clean
```

---

## Segredos, credenciais e perfil dev

- **Sem credenciais padrão:** o `docker-compose.yml` não contém senhas. Copie `.env.example` para `.env` (ignorado pelo controle de versão), substitua todos os valores `<...>` (`openssl rand -hex 32`) e só então suba o ambiente. Variáveis obrigatórias ausentes abortam o `docker compose` (`${VAR:?}`); `docker-env.sh` também recusa executar sem `.env`.
- **Segredos da API:** `APP_CRYPTO_KEY` e `APP_CONSENT_SECRET` (mínimo 32 caracteres) são obrigatórios; a API não sobe sem eles. Trocar `APP_CRYPTO_KEY` depois de gravar dados torna ilegíveis os campos já cifrados. `APP_METRICS_SCRAPE_TOKEN` é o token Bearer com o qual o Prometheus raspa `/actuator/prometheus` (entregue ao Prometheus como secret do compose); as demais métricas, o Swagger e a OpenAPI exigem login ADMIN.
- **Cookie de sessão:** `SESSION_COOKIE_SECURE=false` apenas para HTTP local; atrás de TLS use `true`.
- **Perfil `dev`:** o `docker compose --profile dev` habilita o `db-seeder` (usuários de teste com senha em texto claro `{noop}senha123`, também via `./docker-env.sh seed`) e o `ui-dev` (Vite com hot reload em `:5173`). Nunca use o seeder em ambiente real. Rodando a API localmente (`./manage.sh dev`), o script ativa `SPRING_PROFILES_ACTIVE=dev`, que carrega `application-dev.yml` (chaves de desenvolvimento públicas, Swagger habilitado, cookie sem `Secure`, `baseline-on-migrate`). Esse perfil jamais deve ser ativado em produção.
- **Portas:** infraestrutura (db, pgAdmin, Prometheus, Grafana, Loki, exporter) publica apenas em `127.0.0.1`.
- **Promtail** permanece como root: ler o socket do Docker equivale a root no host e o GID do grupo `docker` varia por máquina. **Loki** mantém logs por 7 dias (`retention_period: 168h` com compactor).
- **UI de produção:** `Dockerfile.ui` (alvo `runtime`) compila o SPA e o serve com `nginx-unprivileged` (porta interna 8080, publicada em `:3000`), com fallback de SPA e proxy de `/api` para a API na mesma origem, além de CSP e demais cabeçalhos de segurança (`docker/nginx/default.conf`). O alvo `dev` (`npm run dev`) existe somente para desenvolvimento.
