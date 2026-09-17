# ⚙️ Paróquia — Backend (`gestao-paroquia`)

API RESTful para o sistema de gestão paroquial da Paróquia Nossa Senhora de Fátima. É um monolito modular desenvolvido em **Java 25 (Spring Boot 4 + Jetty 12 + Log4j2)** com foco em alta confiabilidade, segurança, desacoplamento e privacidade de dados (LGPD).

---

## 🚀 Visão Geral e Arquitetura

O projeto adota estritamente os princípios de **Domain-Driven Design (DDD)** e **Clean Architecture (Ports & Adapters / Arquitetura Hexagonal)**, isolando o núcleo de regras de negócio das camadas de infraestrutura e frameworks. Cada bounded context segue internamente a mesma separação em camadas:

* **`domain/`** — Entidades, agregados, value objects e regras de negócio puras, sem dependência de frameworks.
* **`application/usecase/`** — Casos de uso que orquestram o domínio (um use case por operação de negócio).
* **`api/v1/`** — Controllers REST, DTOs de request/response e mapeamento HTTP.
* **`infrastructure/`** — Adapters de persistência (JPA), observabilidade, agendamento (Quartz) e integrações externas.

### 🏛️ Bounded Contexts (Contextos Delimitados)

A aplicação está dividida em 14 domínios explicados sob o pacote raiz `br.com.nsfatima.gestao`:

* **`calendario`**: Gestão do ciclo de vida de eventos paroquiais, solicitações de agendamento e regras de recorrência.
* **`aprovacao`**: Workflows de aprovação de eventos e solicitações com histórico e pareceres.
* **`local`**: Cadastro e controle de espaços físicos (salas, auditórios, capacidade e atributos).
* **`iam`**: Identidade e Gestão de Acesso (Usuários, Roles, Permissões, perfis e Autenticação JWT).
* **`governanca`**: Registros de auditoria, políticas de segurança, controle de acesso e rastreabilidade.
* **`organizacao`**: Hierarquia organizacional da paróquia (Pastorais, Movimentos, Setores, Conselho e Comunidades).
* **`membro`**: Cadastro, fichas cadastrais e acompanhamento de fiéis e membros atuantes.
* **`projeto`**: Vinculação de eventos a projetos paroquiais com consolidação de métricas e status.
* **`conselho`**: Decisões e homologações do Conselho Paroquial para Assuntos Econômicos (CPC), com histórico de pareceres.
* **`patrimonio`**: Cadastro de ativos patrimoniais, alocação/reserva por evento, transferência entre organizações, manutenção e métricas de ociosidade.
* **`importacao`**: Processamento batch de importação e exportação de dados (relatórios em Excel via Apache POI e PDF via OpenPDF).
* **`dashboard`**: Métricas executivas, estatísticas gerais e visão consolidada para a gestão paroquial.
* **`observabilidade`**: Emissão de logs estruturados via Log4j2 com MDC, rastreio de ações HTTP, telemetria e healthchecks.
* **`support`**: Utilitários transversais, tratamento global de exceções e abstrações de infraestrutura compartilhada.

---

## 🛠️ Tecnologias e Ferramentas

* **Linguagem & Runtime:** **Java 25** (gerenciado nativamente via Gradle Toolchains / Foojay Resolver)
* **Framework Principal:** **Spring Boot 4.x** (servidor web embarcado **Jetty 12**, logging **Log4j2**)
* **Spring Data JPA:** Persistência de dados ORM
* **Spring Security:** Autenticação stateless (JWT), autorização baseada em roles e controle de acesso fino
* **Spring Web (MVC):** API RESTful declarativa e tipada
* **Spring Boot Actuator:** Healthchecks e métricas da aplicação
* **Spring Cache + Caffeine:** Caching de alto desempenho em memória


* **Banco de Dados:** PostgreSQL 16 (Produção/Docker e Testes Automatizados via Testcontainers)
* **Migrations:** Flyway Database Migrations
* **Documentação de API:** Springdoc OpenAPI 2.x / Swagger UI
* **Relatórios & Exportação:** Apache POI (Excel) & OpenPDF (PDF)
* **Qualidade de Código & Linters (Quality Gates):**
* **Checkstyle 13.8.0** (padronização de código, layout e importações explícitas)
* **PMD 7.16.0** (análise estática, otimizações e prevenção de erros)
* **SpotBugs** (detecção de falhas de segurança e riscos de ponteiro nulo)
* **JaCoCo** (relatórios de cobertura de código da suíte de testes)
* **ArchUnit 1.4.0** (validação automatizada de integridade e fronteiras arquiteturais)

* **Agendamento:** Quartz Scheduler (jobs de snapshot de métricas e alocação de ativos)
* **Observabilidade:** Logs estruturados em JSON com MDC, publicadores de métricas e healthchecks via Spring Boot Actuator

---

## 📏 Diretrizes de Engenharia e Padrões (`AGENTS.md`)

O desenvolvimento no backend deve respeitar rigorosamente as normas de engenharia definidas em [`AGENTS.md`](./AGENTS.md):

* **Tipos Fortes sobre Strings:** Uso obrigatório de `enum`, `sealed classes` e Value Objects (`record`) em vez de `String` genéricas para representar estados ou domínios fixos.
* **Value Objects Imutáveis:** Implementados obrigatoriamente utilizando Java `record` (ex: Email, CPF, Endereço).
* **Limites de Métodos e Arquivos:** Métodos devem ter entre **4 e 20 linhas**. Arquivos devem ter menos de **500 linhas**.
* **Nomenclatura Domínio-Guiada:** Nomes específicos e proibição estrita dos termos genéricos: `Data`, `Handler`, `Manager`, `Helper`, `Utility`.
* **Indentação e Fluxo:** Máximo de 2 níveis de aninhamento por bloco. Preferência por *early returns* e *guard clauses*.
* **Sem Wildcards em Imports:** Proibido uso de wildcards (ex: `import java.util.*`). Importações devem ser explícitas.

---

## 🧪 Estratégia de Testes e TDD (Test-Driven Development)

A cobertura de testes é estruturada em camadas obrigatórias (mão dupla TDD Red-Green-Refactor):

1. **Unit Tests (Testes Unitários):** Foco no domínio puro, validação de invariantes em Aggregates e Value Objects, utilizando fakes/mocks para I/O.
2. **Integration Tests (Testes de Integração):** Validação de persistência JPA com PostgreSQL 16 real gerenciado via Testcontainers, repositórios e contextos Spring.
3. **Architecture Tests (Testes Arquiteturais):** Guardrails contínuos com ArchUnit garantindo Clean Architecture e isolamento hexagonal.
4. **End-to-End Tests (E2E):** Testes de fluxo completo via `MockMvc` / `WebTestClient` cobrindo da controller ao banco de dados.

Os testes ficam em `app/src/test/java`, espelhando o pacote do bounded context correspondente (ex.: `integration/eventos`, `integration/metrics`, `support/fake` para fakes reutilizáveis de infraestrutura). O relatório de cobertura JaCoCo é gerado em `build/reports/jacoco/` após `./gradlew check`.

---

## 🔒 Privacidade e Conformidade (LGPD)

A aplicação manipula dados pessoais e sensíveis sob rígidas regras em conformidade com a LGPD (Lei Geral de Proteção de Dados).
Consulte a documentação mandatória em [`../docs/guide/logging-metricas.md`](../docs/guide/logging-metricas.md) e na skill canônica [`.agents/skills/lgpd-privacy-by-design/SKILL.md`](../.agents/skills/lgpd-privacy-by-design/SKILL.md) antes de criar ou alterar entidades, DTOs ou logs — a diretiva cobre minimização de dados, mascaramento em logs e critérios de retenção.

---

## 🛠️ Comandos de Execução e Qualidade

Todos os comandos devem ser executados na raiz do diretório `gestao-paroquia`:

```bash
# Executar a suíte completa de testes (Unitários, Integração e E2E)
./gradlew test

# Executar todas as travas de qualidade (Checkstyle, PMD, SpotBugs, JaCoCo e Testes)
./gradlew check

# Subir a API Spring Boot em modo desenvolvimento
./gradlew bootRun

# Gerar o artefato executável otimizado (JAR final em app/build/libs/app.jar)
./gradlew bootJar
```

### Baseline Flyway e seed local

O projeto mantém a criação inicial do schema organizada por contexto em
`app/src/main/resources/db/migration/`. As migrações criam o schema e os dados
litúrgicos institucionais necessários ao calendário a partir de uma base vazia.

Para um ambiente Docker novo, suba a API e carregue a massa sintética completa:

```bash
docker compose up -d db api
docker compose run --rm db-seeder
```

O seed é exclusivo do desenvolvimento, recria os dados do schema `calendario` e
oferece credenciais sintéticas como `admin` / `senha123`.

---

## 📂 Estrutura de Diretórios

```
gestao-paroquia/
├── app/                        # Módulo principal da aplicação Spring Boot
│   ├── src/main/java/          # Código-fonte Java (organizado por bounded contexts)
│   └── src/test/java/          # Suíte de testes automatizados (Unit, Integration, E2E)
├── config/                     # Configurações dos Linters e Quality Gates
│   ├── checkstyle/checkstyle.xml
│   ├── pmd/pmd.xml
│   └── spotbugs/spotbugs-filter.xml
├── docker/                     # Scripts SQL de inicialização e dados iniciais
│   ├── init-data.sql           # Schema inicial do banco de dados
│   └── seed-data.sql           # Dados de semente para ambiente local/dev
├── build.gradle.kts            # Configuração raiz do Gradle
└── AGENTS.md                   # Diretrizes completas de engenharia do backend
```

---

## 📚 Referências

* **Swagger UI (com a API em execução):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
* **Portal de Documentação Central (Docsify):** [`../docs/guide/`](../docs/guide/)
* [Visão Geral da Arquitetura](../docs/guide/arquitetura.md)
* [Regras de Negócio e Funcionalidades](../docs/guide/regras-funcionalidades.md)
* [Padrões de Projeto e Classes](../docs/guide/padroes-projeto.md)
* [Fluxos e Diagramas de Sequência](../docs/guide/fluxos-sequencia.md)
* [Máquinas de Estado](../docs/guide/estados.md)
* [Logging, LGPD e Métricas](../docs/guide/logging-metricas.md)
* **Padrões de Engenharia Backend:** [`AGENTS.md`](./AGENTS.md)
