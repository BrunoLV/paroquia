# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Monorepo for "Projeto Paróquia" — administrative and pastoral management system for Paróquia Nossa Senhora de Fátima. Backend is a modular monolith in **Java 25 / Spring Boot 3** (`gestao-paroquia/`), frontend is **React 19 / TypeScript / Vite 5** (`gestao-paroquia-ui/`). Full engineering standards live in `AGENTS.md` (root), `gestao-paroquia/AGENTS.md` (backend), and `gestao-paroquia-ui/AGENTS.md` (frontend) — read those before making non-trivial changes; this file only covers commands and architecture.

## Graphify (mandatory before exploring code)

A knowledge graph exists at `graphify-out/` (graph.json, GRAPH_REPORT.md, cache/semantic/). Before grepping or reading multiple source files:
- `graphify query "<question>"` for codebase questions
- `graphify path "<A>" "<B>"` for relationships between two symbols
- `graphify explain "<concept>"` for a focused concept
- Read `graphify-out/GRAPH_REPORT.md` only for broad architecture review, or when the above don't surface enough
- After modifying code: `graphify update .` (AST-only, no API cost)

## Commands

### Backend (`gestao-paroquia/`, Gradle, Java 25 toolchain)
```bash
./gradlew build              # full build (compile + checkstyle + pmd + spotbugs + test)
./gradlew test               # unit + integration + contract tests (H2 in-memory)
./gradlew test --tests "FullyQualifiedClassName"          # single test class
./gradlew test --tests "FullyQualifiedClassName.methodName"  # single test method
./gradlew checkstyleMain checkstyleTest   # style gate (no wildcard/inline-FQN imports)
./gradlew pmdMain pmdTest
./gradlew spotbugsMain
./gradlew jacocoTestReport
```
All commands run from `gestao-paroquia/`. There is one Gradle module (`app`) — no multi-module split.

### Frontend (`gestao-paroquia-ui/`, npm + Vite)
```bash
npm run dev          # dev server
npm run build         # tsc typecheck + vite build (build fails on type errors)
npm test              # vitest run (single run)
npm run test:watch    # vitest watch
npx vitest run path/to/File.test.tsx            # single test file
npx vitest run -t "test name"                   # single test by name
```

### Full stack (from repo root)
```bash
docker compose up -d   # postgres, backend API, frontend UI, pgAdmin, seeder
```
Frontend: http://localhost:3000, API: http://localhost:8080, Swagger UI: http://localhost:8080/swagger-ui.html, pgAdmin: http://localhost:5050.

## Naming convention: domain in Portuguese, technical in English

Domain/business concepts (entity names, enum values, status names, role names, DTO fields, use-case names describing a business action) are named in **Portuguese**, mirrored consistently across backend and frontend — e.g. `AtivoAlocacaoEntity`, `EventoController`, `VinculoResponse` (not `MembershipResponse`). Purely technical/architectural terms stay in **English** (`Entity`, `Controller`, `Repository`, `UseCase`, `DTO`, `Enum`, `Exception`, `Context`, `Provider`, `Policy` — e.g. `PeriodoOperacionalPolicy`), and this also applies to local variables and business-rule comments inside method bodies, not just class/method/field signatures. Prefer an enum or a named constant over a string literal used as a marker in comparisons/`switch` — reuse an existing enum via `.name()` (e.g. `AprovacaoStatus`, `ProjetoStatus`, `EventoStatusInput`) instead of repeating the literal. A JPA entity `String` field with a closed set of values should be mapped as an enum with `@Enumerated(EnumType.STRING)`, unless the persisted value doesn't match `Enum.name()` (use an `AttributeConverter` instead) or the field mixes vocabularies across different lifecycle stages (keep it `String` in that case).

## Architecture

### Backend — DDD + Ports & Adapters, package-by-feature
Each business capability under `gestao-paroquia/app/src/main/java/br/com/nsfatima/gestao/<context>/` (e.g. `calendario`, `organizacao`, `projeto`, `aprovacao`, `membro`, `iam`, `conselho`, `local`, `importacao`, `dashboard`, `governanca`, `observabilidade`, `support`) follows the same internal layering:
- `domain/model` — entities, value objects (Java records), domain events. Zero framework dependencies.
- `domain/service` — domain services spanning multiple aggregates.
- `application/usecase` — one class per use case, orchestrating a transaction.
- `infrastructure` — Spring `@RestController`s (`api/v1/controller`), JPA repositories/entities, mappers, security adapters.
- Bounded contexts do not leak entities/DB structures into each other; cross-context references go through IDs or explicit ports.

Key cross-cutting concerns to be aware of:
- **Aprovação (approval workflow)**: many mutating use cases (event/project create-update-cancel) route through an approval/pending state machine (`AprovacaoEntity`, `DecideSolicitacaoAprovacaoUseCase`, `ApprovalActionPayloadMapper`) rather than applying changes directly — check whether a new mutation needs to integrate with this before writing a plain CRUD path.
- **Evento (calendar events)** is the largest/most connected context (`EventoEntity`, `EventoJpaRepository` are the top "god nodes" in the graph) — repositories here have been intentionally segregated by responsibility (see "EventoJpaRepository Segregation Pattern" in the graph) rather than being one fat repository; follow that pattern for new queries instead of adding methods to a single repository interface.
- **Organizacao / PapelOrganizacional / TipoOrganizacao** model the parish org hierarchy (Matriz, Comunidades, movimentos, clero) and drive authorization scoping (`ProjetoAuthorizationService`, `EventoActorContext`) — most permission checks are actor-role + organization-scope based, not simple ownership checks.
- **Flyway** migrations live under `db/migration`; DB schema changes always go through a new versioned migration, never edited in place.
- LGPD/privacy rules for this backend are in `gestao-paroquia/docs/ia/regra_dura_lgpd.md` — consult before adding logging, DTOs, or exports that touch personal data.
- Skill guides in `.agents/skills/` (`ddd.md`, `java.md`, `solid.md`, `tdd.md`, `flyway.md`, `sql.md`, `gradle.md`) and `gestao-paroquia/conductor/code_styleguides/` codify the conventions above in more depth.

### Frontend — feature pages + typed API client + React Query
- `src/api/` — one module per resource (`projects.ts`, `organizacoes.ts`, `membros.ts`, etc.) wrapping `client.ts` (Axios instance), with shared response/DTO shapes in `types.ts`. Pages/hooks never call Axios directly.
- `src/auth/` — `AuthContext.tsx` (session/user state) and `permissions.ts` (pure functions like `canManageProjects()`, `canTransitionProject()`, `isGlobalCreator()`) — permission checks are centralized here, not scattered inline in components.
- `src/pages/` — route-level components (e.g. `Projects.tsx`, `Calendar.tsx`, `Organograma.tsx`), each with a co-located `*.test.tsx`.
- `src/context/` — cross-cutting UI state (e.g. `NotificationContext.tsx`).
- `AppRoutes.tsx` wires pages to routes and gates them via the permission helpers above.
- Path aliases (`@/...`) are used instead of deep relative imports.

### Conductor workflow
`conductor/` (root) and `gestao-paroquia*/conductor/` hold spec-driven track documents: each feature/track has `spec.md` (requirements), `plan.md` (implementation plan), `index.md`. Completed tracks move to `archive/`. `conductor/tracks.md` / `tracks/` is the registry of in-flight and archived tracks — check it for context on why a given area of code looks the way it does before assuming something is dead or unfinished.
