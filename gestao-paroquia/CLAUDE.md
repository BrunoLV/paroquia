# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Backend for "Projeto Paróquia" — a modular monolith (Java 25 / Spring Boot 3) providing the REST API for the administrative and pastoral management system of Paróquia Nossa Senhora de Fátima. This directory is one module (`gestao-paroquia/`) inside a larger monorepo; the React/TS frontend lives as a sibling directory (`gestao-paroquia-ui/`) and is out of scope here.

Full engineering standards (DDD rules, TDD mandate, layering, naming bans, quality-gate policy) live in `AGENTS.md` at this directory's root — read it before making non-trivial changes. This file covers only commands and cross-file architecture.

## Graphify (mandatory before exploring code)

A knowledge graph exists at `../graphify-out/` (graph.json, GRAPH_REPORT.md). Before grepping or reading multiple source files:
- `graphify query "<question>"` for codebase questions
- `graphify path "<A>" "<B>"` for relationships between two symbols
- `graphify explain "<concept>"` for a focused concept
- Read `../graphify-out/GRAPH_REPORT.md` only for broad architecture review, or when the above don't surface enough
- After modifying code: `graphify update .` (AST-only, no API cost)

## Commands

All commands run from this directory (`gestao-paroquia/`), using the Gradle wrapper. There is one Gradle module (`app`) — no multi-module split.

```bash
./gradlew build                 # full build: compile + checkstyle + pmd + spotbugs + test
./gradlew check                 # all quality gates + tests, no jar assembly
./gradlew test                  # unit + integration + contract tests (H2 in-memory)
./gradlew test --tests "FullyQualifiedClassName"             # single test class
./gradlew test --tests "FullyQualifiedClassName.methodName"  # single test method
./gradlew checkstyleMain checkstyleTest   # style gate (no wildcard/inline-FQN imports)
./gradlew pmdMain pmdTest
./gradlew spotbugsMain
./gradlew jacocoTestReport
./gradlew bootRun               # run the API locally (needs Postgres, see docker/)
./gradlew bootJar               # build build/libs/app.jar
./demo_mvp.sh                   # scripted end-to-end MVP walkthrough, produces an HTML report
```

Test classes are suffixed by layer — `*Test` (unit), `*IntegrationTest` (Spring/JPA slice or DB-backed), `*ContractTest` (API contract) — use the suffix to target a layer with `--tests "*IntegrationTest"` etc.

## Architecture

### DDD + Ports & Adapters, package-by-feature

Each bounded context lives under `app/src/main/java/br/com/nsfatima/gestao/<context>/`:
`calendario`, `aprovacao`, `local`, `iam`, `governanca`, `organizacao`, `membro`, `projeto`, `importacao`, `dashboard`, `observabilidade`, `conselho`, `support`.

Every context follows the same internal layering:
- `domain/model` — entities, value objects (Java records), domain events. Zero framework dependencies.
- `domain/service` — domain services spanning multiple aggregates.
- `application/usecase` — one class per use case, orchestrating a transaction.
- `infrastructure` — Spring `@RestController`s (`api/v1/controller`), JPA repositories/entities, mappers, security adapters.

Bounded contexts do not leak entities/DB structures into each other; cross-context references go through IDs or explicit ports.

### Cross-cutting concerns worth knowing before touching them

- **Aprovação (approval workflow)**: many mutating use cases (event/project create-update-cancel) route through an approval/pending state machine (`AprovacaoEntity`, `DecideSolicitacaoAprovacaoUseCase`, `ApprovalActionPayloadMapper`) rather than applying changes directly. Check whether a new mutation needs to integrate with this before writing a plain CRUD path.
- **Evento (calendar events)** is the largest/most connected context (`EventoEntity`, `EventoJpaRepository` are the top "god nodes" in the graph). Repositories here are intentionally segregated by responsibility rather than one fat repository — follow that pattern for new queries instead of adding methods to a single repository interface. Related hubs: `EventoRecorrenciaEntity`/`RegraRecorrencia` (recurrence rules), `EventoActorContext`/`EventoActorContextResolver` (actor-scoped authorization for event actions), `EventoDomainService`, `CreateEventoUseCase`, `CancelEventoUseCase`.
- **Organizacao / PapelOrganizacional / TipoOrganizacao** model the parish org hierarchy (Matriz, Comunidades, movimentos, clero) and drive authorization scoping (`ProjetoAuthorizationService`, `EventoActorContext`) — most permission checks are actor-role + organization-scope based, not simple ownership checks.
- **AnoParoquial** (parish year) has its own lifecycle/status machine (`AnoParoquialEntity`, `AnoParoquialStatus`) that gates annual provisioning of solemnities/events for Matriz and Comunidades.
- **Flyway** migrations live under `app/src/main/resources/db/migration` (or equivalent); DB schema changes always go through a new versioned migration, never edited in place.
- **Auditoria/Observabilidade**: mutating flows publish through dedicated audit/metrics publishers (e.g. `AuditLogPersistenceService`, `CadastroEventoMetricsPublisher`, `ObservacaoAuditPublisher`) rather than logging ad hoc — follow the existing publisher pattern for new auditable actions.
- LGPD/privacy rules are in `docs/ia/regras_duras_lgpd.md` — consult before adding logging, DTOs, or exports that touch personal data. This is a hard, non-negotiable gate per `AGENTS.md`, not a suggestion.
- Skill guides in `.agents/skills/` (`ddd.md`, `java.md`, `solid.md`, `tdd.md`, `flyway.md`, `sql.md`, `gradle.md`) codify the conventions in `AGENTS.md` in more depth — consult the relevant one before generating or refactoring code in that area.

### Quality gates

`AGENTS.md` enforces (via Checkstyle/PMD/SpotBugs, zero-warning policy): methods 4–20 lines, files under 500 lines, no wildcard or inline-FQN imports, constructor injection only (no field `@Autowired`), records for value objects, `Optional` instead of `null`, banned generic names (`Data`, `Handler`, `Manager`, `Helper`, `Utility`), and strict TDD (test before production code) across unit/integration/E2E layers. Run `./gradlew check` before considering backend work done.
