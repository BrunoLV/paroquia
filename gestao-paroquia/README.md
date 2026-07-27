# ⚙️ Paróquia - Backend (`gestao-paroquia`)

API RESTful para o sistema de gestão paroquial da Paróquia Nossa Senhora de Fátima. É um monolito modular desenvolvido em **Java 25 (Spring Boot 3)** com foco em alta confiabilidade, segurança, desacoplamento e privacidade de dados (LGPD).

---

## 🚀 Visão Geral e Arquitetura

O projeto adota estritamente os princípios de **Domain-Driven Design (DDD)** e **Clean Architecture (Ports & Adapters / Arquitetura Hexagonal)**, isolando o núcleo de regras de negócio das camadas de infraestrutura e frameworks.

### 🏛️ Bounded Contexts (Contextos Delimitados)

A aplicação está dividida em 12 domínios explicados sob o pacote raiz `br.com.nsfatima.gestao`:

- **`calendario`**: Gestão do ciclo de vida de eventos paroquiais, solicitações de agendamento e regras de recorrência.
- **`aprovacao`**: Workflows de aprovação de eventos e solicitações com histórico e pareceres.
- **`local`**: Cadastro e controle de espaços físicos (salas, auditórios, capacidade e atributos).
- **`iam`**: Identidade e Gestão de Acesso (Usuários, Roles, Permissões, perfis e Autenticação JWT).
- **`governanca`**: Registros de auditoria, políticas de segurança, controle de acesso e rastreabilidade.
- **`organizacao`**: Hierarquia organizacional da paróquia (Pastorais, Movimentos, Setores, Conselho e Comunidades).
- **`membro`**: Cadastro, fichas cadastrais e acompanhamento de fiéis e membros atuantes.
- **`projeto`**: Vinculação de eventos a projetos paroquiais com consolidação de métricas e status.
- **`importacao`**: Processamento batch de importação e exportação de dados (relatórios em Excel via Apache POI e PDF via OpenPDF).
- **`dashboard`**: Métricas executivas, estatísticas gerais e visão consolidada para a gestão paroquial.
- **`observabilidade`**: Emissão de logs estruturados em formato JSON com MDC, telemetria e healthchecks.
- **`support`**: Utilitários transversais, tratamento global de exceções e abstrações de infraestrutura compartilhada.

---

## 🛠️ Tecnologias e Ferramentas

- **Linguagem & Runtime:** **Java 25** (gerenciado nativamente via Gradle Toolchains / Foojay Resolver)
- **Framework Principal:** **Spring Boot 3.x**
  - **Spring Data JPA:** Persistência de dados ORM
  - **Spring Security:** Autenticação stateless (JWT), autorização baseada em roles e controle de acesso fino
  - **Spring Web (MVC):** API RESTful declarativa e tipada
  - **Spring Boot Actuator:** Healthchecks e métricas da aplicação
  - **Spring Cache + Caffeine:** Caching de alto desempenho em memória
- **Banco de Dados:** PostgreSQL 16 (Produção/Docker) & H2 Database (Testes Automatizados)
- **Migrations:** Flyway Database Migrations
- **Documentação de API:** Springdoc OpenAPI 2.x / Swagger UI
- **Relatórios & Exportação:** Apache POI (Excel) & OpenPDF (PDF)
- **Qualidade de Código & Linters (Quality Gates):**
  - **Checkstyle 13.8.0** (padronização de código, layout e importações explícitas)
  - **PMD 7.16.0** (análise estática, otimizações e prevenção de erros)
  - **SpotBugs** (detecção de falhas de segurança e riscos de ponteiro nulo)
  - **JaCoCo** (relatórios de cobertura de código da suíte de testes)

---

## 📏 Diretrizes de Engenharia e Padrões (`AGENTS.md`)

O desenvolvimento no backend deve respeitar rigorosamente as normas de engenharia definidas em [`AGENTS.md`](./AGENTS.md):

- **Tipos Fortes sobre Strings:** Uso obrigatório de `enum`, `sealed classes` e Value Objects (`record`) em vez de `String` genéricas para representar estados ou domínios fixos.
- **Value Objects Imutáveis:** Implementados obrigatoriamente utilizando Java `record` (ex: Email, CPF, Endereço).
- **Limites de Métodos e Arquivos:** Métodos devem ter entre **4 e 20 linhas**. Arquivos devem ter menos de **500 linhas**.
- **Nomenclatura Domínio-Guiada:** Nomes específicos e proibição estrita dos termosgenéricos: `Data`, `Handler`, `Manager`, `Helper`, `Utility`.
- **Indentação e Fluxo:** Máximo de 2 níveis de aninhamento por bloco. Preferência por *early returns* e *guard clauses*.
- **Sem Wildcards em Imports:** Proibido uso de wildcards (ex: `import java.util.*`). Importações devem ser explícitas.

---

## 🧪 Estratégia de Testes & TDD (Test-Driven Development)

A cobertura de testes é estruturada em três camadas obrigatórias (mão dupla TDD Red-Green-Refactor):

1. **Unit Tests (Testes Unitários):** Foco no domínio puro, validação de invariantes em Aggregates e Value Objects, utilizando fakes/mocks para I/O.
2. **Integration Tests (Testes de Integração):** Validação de persistência JPA com H2, repositórios e contextos Spring.
3. **End-to-End Tests (E2E):** Testes de fluxo completo via `MockMvc` / `WebTestClient` cobrindo da controller ao banco de dados.

---

## 🔒 Privacidade e Conformidade (LGPD)

A aplicação manipula dados pessoais e sensíveis sob rígidas regras em conformidade com a LGPD (Lei Geral de Proteção de Dados).
Consulte a documentação mandatória em [`docs/ia/regra_dura_lgpd.md`](./docs/ia/regra_dura_lgpd.md) antes de criar ou alterar entidades, DTOs ou logs.

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

# Gerar o artefato executável otimizado (JAR final em build/libs/app.jar)
./gradlew bootJar

# Executar a demonstração automatizada do ciclo de vida do MVP (gera relatório HTML)
./demo_mvp.sh
```

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
├── docs/                       # Portal de documentação técnica, arquitetura e LGPD
└── AGENTS.md                   # Diretrizes completas de engenharia do backend
```

---

## 📚 Links e Referências Úteis

- **Swagger UI (com a API em execução):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Documentação do Sistema:** [`docs/README.md`](./docs/README.md)
  - [Visão Geral da Arquitetura](./docs/architecture.md)
  - [Guia de Início Rápido](./docs/getting-started.md)
  - [Documentação dos Domínios](./docs/domains/)
  - [Regra Dura LGPD](./docs/ia/regra_dura_lgpd.md)
- **Padrões de Engenharia Backend:** [`AGENTS.md`](./AGENTS.md)

