# ⛪ Projeto Paróquia

[![Docker Compose](https://img.shields.io/badge/docker-compose-blue.svg?style=flat-square&logo=docker)](./docker-compose.yml)
[![Java 25](https://img.shields.io/badge/Java-25-ED8B00.svg?style=flat-square&logo=openjdk)](./gestao-paroquia)
[![Spring Boot 4](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F.svg?style=flat-square&logo=springboot)](./gestao-paroquia)
[![React 19](https://img.shields.io/badge/React-19-61DAFB.svg?style=flat-square&logo=react)](./gestao-paroquia-ui)
[![Vite 5](https://img.shields.io/badge/Vite-5-646CFF.svg?style=flat-square&logo=vite)](./gestao-paroquia-ui)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-336791.svg?style=flat-square&logo=postgresql)](./docker-compose.yml)

Sistema unificado de gestão administrativa e pastoral, construído para servir com precisão e zelo a rotina de uma paróquia — do agendamento de uma missa à homologação de uma ata do Conselho Paroquial. Backend monolítico modular em **Java 25 (Spring Boot 4 + Jetty 12 + Log4j2)**, frontend em **React 19 (TypeScript + Material UI 9 + Vite)**.

O sistema cobre o ciclo operacional completo da paróquia: agendamento de eventos e espaços, fluxos de aprovação, cadastro de fiéis e gestão de membros na estrutura organizacional (pastorais, movimentos, conselhos), decisões e homologações do Conselho Paroquial (CPC), controle de patrimônio (ativos e alocações), importação e exportação de relatórios (CSV, PDF e Word) e um dashboard executivo com métricas consolidadas — tudo sob rígidos controles de auditoria e conformidade com a LGPD. Tecnologia a serviço da missão, sem abrir mão de rigor.

---

## 🏗️ Estrutura do Monorepo

O repositório é organizado em módulos independentes, cada um com suas próprias regras de arquitetura, testes e documentação:

- **[Backend (`gestao-paroquia`)](./gestao-paroquia):** API RESTful desenvolvida em **Java 25** com **Spring Boot 4**, servidor web **Jetty 12**, **Log4j2**, estruturada sob os princípios de **Domain-Driven Design (DDD)** e **Clean Architecture (Ports & Adapters)**. Organizada em 15 bounded contexts — veja a [lista completa](./gestao-paroquia/README.md).
- **[Frontend (`gestao-paroquia-ui`)](./gestao-paroquia-ui):** SPA em **React 19**, **TypeScript**, **MUI 9**, **TanStack Query** e **Vite 5**, organizada em bounded contexts DDD com camadas Hexagonais (`domain`, `application`, `infrastructure` e `presentation`).
- **[Infraestrutura (`docker-compose.yml`)](./docker-compose.yml):** Orquestração completa em Docker para banco de dados PostgreSQL 16, build do backend (`Dockerfile.builder`), API (`Dockerfile.api`), frontend UI (`Dockerfile.ui`), pgAdmin 4, Prometheus, Grafana e seeder automático de dados. Veja o guia completo em [**`DOCKER.md`**](./DOCKER.md).
- **[Documentação (`docs/guide/`)](./docs/guide):** Portal de documentação técnica via Docsify com suporte a Mermaid — arquitetura, diagramas conceituais, regras de negócio, padrões, observabilidade e infraestrutura Docker.

- **Scripts utilitários (`manage.sh`, `docker-env.sh`):** Interface de linha de comando e gerenciamento do ciclo de vida do ambiente (subir serviços, testar, checar qualidade, compilar e reiniciar os contêineres).

---

## 🧭 Domínios de Negócio

O backend está organizado em 15 bounded contexts sob o pacote `br.com.nsfatima.gestao`. Resumo funcional (detalhes técnicos no [README do backend](./gestao-paroquia/README.md)):

| Domínio | Responsabilidade |
| :--- | :--- |
| `calendario` | Ciclo de vida de eventos paroquiais, agendamento e recorrência |
| `aprovacao` | Workflows de aprovação de eventos e solicitações |
| `local` | Cadastro e controle de espaços físicos |
| `iam` | Identidade, acesso, roles e autenticação JWT |
| `governanca` | Auditoria, políticas de segurança e rastreabilidade |
| `organizacao` | Hierarquia organizacional (pastorais, movimentos, setores, comunidades) |
| `fiel` | Cadastro base da pessoa fiel (identidade, contato, vocação) |
| `membro` | Vínculo do fiel a grupos, papéis e lideranças na estrutura organizacional |
| `projeto` | Vinculação de eventos a projetos paroquiais e métricas |
| `conselho` | Decisões e homologações do Conselho Paroquial (CPC) |
| `patrimonio` | Ativos patrimoniais, alocação, transferência e manutenção |
| `importacao` | Importação/exportação em lote (Excel, PDF) |
| `dashboard` | Métricas executivas e visão consolidada |
| `observabilidade` | Logs estruturados, telemetria e healthchecks |
| `support` | Utilitários transversais e tratamento global de exceções |

---

## 🚀 Como Iniciar

### Pré-requisitos

- **Docker** e **Docker Compose** (v2+)
- Para desenvolvimento fora de contêineres: **Java 25**, **Node.js 18+** e **npm 9+** (veja os READMEs de [backend](./gestao-paroquia/README.md) e [frontend](./gestao-paroquia-ui/README.md))

### Configuração inicial

Antes de subir o ambiente, copie o arquivo de variáveis de ambiente de exemplo e ajuste os valores conforme necessário:

```bash
cp .env.example .env
```

A forma recomendada de executar e desenvolver é usando a CLI unificada **`manage.sh`**:

```bash
# Iniciar infraestrutura Docker (DB, Prometheus, Grafana) e rodar API e UI localmente
./manage.sh dev

# Executar as suítes de teste (backend Gradle + frontend Vitest)
./manage.sh test

# Executar as travas de qualidade (Checkstyle, PMD, SpotBugs, ArchUnit) e testes
./manage.sh check

# Gerar os artefatos de build de produção (backend + frontend)
./manage.sh build

# Parar serviços locais e contêineres
./manage.sh stop
```

Para controle direto do ciclo de vida dos contêineres Docker, utilize **`docker-env.sh`**:

```bash
# Subir todo o ecossistema Docker Compose em background
./docker-env.sh up

# Subir forçando rebuild sem cache
./docker-env.sh up --no-cache

# Reiniciar ambiente com limpeza completa de volumes
./docker-env.sh restart --clean

# Acompanhar logs ou verificar status
./docker-env.sh status
./docker-env.sh logs -f -s api
```

### 🌐 Serviços e Ambientes Disponíveis

| Serviço | URL / Endereço | Credenciais / Notas |
| :--- | :--- | :--- |
| **Frontend Web** | [http://localhost:3000](http://localhost:3000) (ou `:5173`) | `admin` \| Senha: `senha123` |
| **Backend REST API** | [http://localhost:8080/api](http://localhost:8080/api) | API Spring Boot 4 + Jetty 12 + Log4j2 |
| **Swagger UI** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | Documentação interativa OpenAPI 3 |
| **Prometheus** | [http://localhost:9090](http://localhost:9090) | Coleta e armazenamento de métricas do sistema |
| **Grafana** | [http://localhost:3001](http://localhost:3001) | Dashboards em tempo real (`admin` / `admin`) |
| **pgAdmin 4** | [http://localhost:5050](http://localhost:5050) | `admin@paroquia.local` \| Senha: `admin` |
| **PostgreSQL DB** | `localhost:5432` | DB: `calendario` \| User: `postgres` \| Pass: `password` |
| **Portal de Documentação (Docsify)** | [`docs/guide/`](./docs/guide) | Guia arquitetural vivo (`npx docsify-cli serve docs/guide`) |

Além desses serviços expostos, o Docker Compose também orquestra dois serviços de apoio sem porta publicada: **`builder`** (compila o backend antes de subir a API) e **`db-seeder`** (popula o banco com dados de exemplo após o healthcheck da API).

---

## 🛠️ Tecnologias Principais

| Camada | Tecnologias e Ferramentas |
| :--- | :--- |
| **Backend** | Java 25 (Gradle Toolchain), Spring Boot 4, Eclipse Jetty 12, Log4j2 (Async Disruptor), Spring Data JPA, Spring Security, Spring Actuator, Spring Cache + Caffeine, Flyway |
| **Frontend** | React 19, TypeScript, Vite 5, Material UI (MUI 9), TanStack React Query 5, Axios, React Router DOM 6 |
| **Banco de Dados** | PostgreSQL 16 (Ambiente de Execução / Docker), H2 Database (Ambiente de Testes) |
| **Qualidade & Linters** | **Backend:** Checkstyle 13.8.0, PMD 7.16.0, SpotBugs, JaCoCo <br> **Frontend:** Vitest, React Testing Library, TypeScript (`tsc`) |
| **Relatórios / Mídia** | **Backend:** Apache POI (Excel), OpenPDF (PDF), Apache POI XWPF (Word) <br> **Frontend:** `jspdf` + `jspdf-autotable` (PDF), `docx` (Word), CSV nativo, `html-to-image` (exportação de organogramas) |

---

## 🛡️ Governança, Qualidade e Conformidade (LGPD)

- **Engenharia e Padrões de Codificação:** Consulte [`AGENTS.md`](./AGENTS.md) e [`gestao-paroquia/AGENTS.md`](./gestao-paroquia/AGENTS.md) para diretrizes de desenvolvimento, TDD e linters.
- **Travas de Qualidade (Quality Gates):** Todo código de backend passa por Checkstyle, PMD, SpotBugs e JaCoCo antes de ser aceito; o frontend passa por `tsc` e pela suíte Vitest. Rode tudo de uma vez com `./manage.sh check`.
- **Auditoria e Rastreabilidade:** O domínio `governanca` registra logs de auditoria estruturados para operações sensíveis, com histórico consultável.
- **Privacidade e Proteção de Dados (LGPD):** Consulte o guia de observabilidade em [`docs/guide/logging-metricas.md`](./docs/guide/logging-metricas.md) e a skill canônica [`.agents/skills/lgpd-privacy-by-design/SKILL.md`](./.agents/skills/lgpd-privacy-by-design/SKILL.md) antes de criar ou alterar entidades, DTOs ou logs que envolvam dados pessoais.

---

## 📖 Portal de Documentação Técnica Central (`docs/guide/`)

Toda a documentação técnica, conceitual e de arquitetura do monorepo está unificada em [`docs/guide/`](./docs/guide), com renderização interativa via **Docsify** e diagramas **Mermaid v10**:

- [⛪ **Visão Geral & Objetivos**](./docs/guide/README.md): Missão eclesial, pilares arquiteturais e stack tecnológica.
- [📋 **Regras de Negócio & Funcionalidades**](./docs/guide/regras-funcionalidades.md): Catálogo completo dos 15 Bounded Contexts e recursos da UI.
- [🏛️ **Padrões de Arquitetura**](./docs/guide/arquitetura.md): Clean/Hexagonal Architecture, Virtual Threads e **Diagrama Mermaid C4/Container**.
- [🧩 **Padrões de Projeto (Design Patterns)**](./docs/guide/padroes-projeto.md): Domain Policies puras, Records/Value Objects, Tell Don't Ask e **Diagrama Mermaid de Classes**.
- [🔄 **Diagramas de Sequência**](./docs/guide/fluxos-sequencia.md): Fluxos temporais de agendamento de eventos, alocação de ativos e homologação do CPC.
- [⚙️ **Diagramas de Estados**](./docs/guide/estados.md): Máquinas de estado finito de eventos, aprovações, ativos patrimoniais e atas.
- [📊 **Logging, LGPD & Métricas**](./docs/guide/logging-metricas.md): Log4j2 assíncrono (LMAX Disruptor), MDC, PII Zero e métricas Prometheus/Actuator.
- [🐳 **Infraestrutura Docker**](./docs/guide/docker.md): Topologia de contêineres, volumes, segurança e scripts de ciclo de vida.

Para executar o portal localmente:
```bash
npx docsify-cli serve docs/guide
```

---
*Construído com rigor de engenharia e reverência ao propósito que serve: sustentar, com eficiência, alta confiabilidade e conformidade com a LGPD, a gestão administrativa e pastoral da Paróquia Nossa Senhora de Fátima.* ⛪
