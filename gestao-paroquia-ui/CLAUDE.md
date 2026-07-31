# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`gestao-paroquia-ui` is the React 19 / TypeScript / Vite 5 frontend for "Projeto Paróquia" — the administrative and pastoral management system for Paróquia Nossa Senhora de Fátima. It talks to the Spring Boot backend in the sibling `gestao-paroquia/` module. Full engineering standards are in `AGENTS.md` (read it before making non-trivial changes) — this file covers commands and architecture only.

## Commands

```bash
npm install            # install dependencies
npm run dev             # Vite dev server, port 3000, proxies /api -> VITE_API_PROXY_TARGET (default http://localhost:8080)
npm start                # alias for npm run dev
npm run build             # tsc typecheck + vite build (fails the build on type errors)
npm run preview            # serve the production build locally
npm test                    # vitest run (single run, jsdom env)
npm run test:watch           # vitest watch mode
npx vitest run path/to/File.test.tsx   # single test file
npx vitest run -t "test name"           # single test by name
```

There is no lint script wired into `package.json`; ESLint/Prettier conformance is still expected per `AGENTS.md` — check for `.eslintrc*`/editor config before assuming there is none to run.

### Environment variables (`VITE_`-prefixed, read by Vite)
| Variable | Purpose | Local default |
| --- | --- | --- |
| `VITE_API_URL` | Base URL for REST calls | `http://localhost:8080/api/v1` |
| `VITE_API_PROXY_TARGET` | Backend target for the Vite dev proxy | `http://localhost:8080` |
| `PORT` | Vite dev/preview server port | `3000` |

## Graphify (mandatory before exploring code)

A knowledge graph exists at the monorepo root (`../graphify-out/`: graph.json, GRAPH_REPORT.md, wiki/). Before grepping or reading multiple source files:
- `graphify query "<question>"` for codebase questions
- `graphify path "<A>" "<B>"` for relationships between two symbols
- `graphify explain "<concept>"` for a focused concept
- Read `graphify-out/GRAPH_REPORT.md` only for broad architecture review, or `graphify-out/wiki/index.md` for navigation, when the above don't surface enough
- After modifying code: `graphify update .` (AST-only, no API cost)

## Architecture

Feature pages + typed API client + React Query — a thin, page-oriented app rather than a component-library-heavy one.

- **`src/api/`** — one module per backend bounded context (`projects.ts`, `organizacoes.ts`, `membros.ts`, `approvals.ts`, `conselhos.ts`, `locais.ts`, `metricas.ts`, `dashboard.ts`, `usuarios.ts`, `importacao.ts`, `solenidades.ts`, `pastoraisOficiais.ts`, `anosParoquiais.ts`, `observacoes.ts`), each wrapping the shared Axios instance in `client.ts`. Shared response/DTO shapes live in `types.ts` (domain-specific extras in `metricasTypes.ts`). `QueryProvider.tsx` wires up TanStack React Query. Pages and hooks never call Axios directly — always go through an `api/` module.
- **`client.ts`** — the single Axios instance: `withCredentials: true`, centralized 401 interceptor and error handling (see `interceptors.test.ts`).
- **`src/auth/`** — `AuthContext.tsx` holds session/user state, `ProtectedRoute.tsx` guards routes, `permissions.ts` centralizes role/permission logic (e.g. `canManageProjects()`, `canTransitionProject()`, `isGlobalCreator()`). Permission checks belong here, not scattered inline in components.
- **`src/pages/`** — route-level components (`Dashboard`, `Calendar`, `Projects`, `Approvals`, `Organograma`, `ConselhosAtas`, `Reports`, `Settings`, `Login`, plus modal pages like `OrganizacaoFormModal`/`LocalFormModal`), each with a co-located `*.test.tsx`.
- **`src/components/`** — reusable UI, with feature subfolders (`dashboard/`, `organograma/`, `reports/`) rather than one flat bucket; `layout/` holds Sidebar/Navbar/base container, `modals/` holds cadastro/edição/confirmação dialogs.
- **`src/context/`** — cross-cutting UI state (e.g. notifications/theme) beyond auth.
- **`src/hooks/`** — custom hooks (e.g. organograma export), tested under `hooks/__tests__/`.
- **`src/utils/`** — export utilities (CSV/PDF) and date/currency formatters.
- **`src/theme.ts`** — centralized MUI theme (Fátima color palette, typography).
- **`AppRoutes.tsx`** — central route map, gated via the `src/auth/permissions.ts` helpers.
- Path alias `@/...` (configured in `vite.config.ts` and `tsconfig.json`) is used instead of deep relative imports (`../../../../`).

### Stack
React 19, Material UI 9 (+ Emotion) as the design system, `@mui/x-charts` for charts, `@dabeng/react-orgchart` for the org chart, TanStack React Query 5 for server-state cache/refetch, Axios for HTTP, React Router DOM 6 for routing, `html-to-image` for exporting the organograma as an image. Tests use Vitest 2 + React Testing Library + `@testing-library/user-event` + jsdom (`setupTests.ts` wires up `@testing-library/jest-dom`).

### Testing conventions
Tests are co-located with the code they cover (`Component.test.tsx` next to `Component.tsx`, `module.test.ts` next to `module.ts` in `api/`) rather than in a separate global test tree. Network calls are mocked — no test hits the real backend.

## Cross-cutting notes
- **LGPD/privacy**: this app handles personal data for internal use only. Consult `docs/ia/regras_duras_lgpd.md` before authoring UI components, exposing data contracts, or adding client-side persistence/logging.
- **Backend pairing**: the API surface in `src/api/` mirrors bounded contexts in the backend (`gestao-paroquia/app/.../<context>/`); when a backend contract changes, the corresponding `api/*.ts` module and `types.ts` need matching updates.
- Many mutating flows (event/project create-update-cancel) route through an approval workflow on the backend — `src/api/approvals.ts` and the `Approvals` page reflect that state machine; don't assume a create/update call applies immediately.
