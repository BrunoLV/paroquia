# AGENTS.md - Frontend Engineering Standards & Guidelines

## Architecture & State Management (React & TypeScript)
- **Domain-Driven UI:** Keep business logic separated from visual representation. Heavy data transformations, validation logic, and state management belong inside custom hooks (`hooks/`), keeping components focused on presentation.
- **Strict Typing:** Never use `any`. Avoid `unknown` without immediate type narrowing. All component props, custom hooks, and API responses must have explicit, immutable TypeScript `type` or `interface` definitions.
- **Types over Strings:** Always prefer TypeScript `enum` or strict template literal union types (e.g., `'success' | 'error' | 'pending'`) over raw `string` to represent states, categories, status, or fixed domains.
- **Component & Function Constraints:** React components must be between 10-60 lines. Pure functions and hooks should remain between 4-20 lines. Split logic early into child components or modular hooks if limits are exceeded.
- **Naming Conventions:** Names must be specific, unique, and domain-driven. ABSOLUTELY BANNED generic terms: `Data`, `Handler`, `Manager`, `Helper`, `Utility`, `Component`. Prefer precise names that return less than 5 grep hits in the codebase.
- **Control Flow:** Prefer early returns and guard clauses over nested `if-else` blocks or deeply nested JSX ternary operators. Maximum allowed indentation depth is 2 levels within any block.

## Data Privacy & Compliance (LGPD Mandate)
- **Strict Compliance Routing:** This project handles personal data and is for internal use only. The agent ABSOLUTELY MUST read, internalize, and strictly enforce the privacy and security boundaries defined in the `docs/ia/regras_duras_lgpd.md` file before authoring UI components, exposing data contracts, or handling client-side persistence/logs.

## Agent Capabilities, Custom Skills & Graphify Mandate
- **Graphify Codebase Exploration:** A pasta `graphify-out/` localizada na raiz do monorepo contém os grafos de conhecimento e dependências de todo o projeto. O agente DEVE obrigatoriamente consultar `graphify-out/GRAPH_REPORT.md` e os arquivos de grafo antes de explorar componentes UI, páginas ou hooks via buscas manuais.
- **Economia de Tokens:** É proibida a leitura em lote de múltiplos arquivos fonte inteiros quando a estrutura de dependências puder ser obtida diretamente via consulta aos artefatos do Graphify, garantindo economia de tokens e navegação precisa.
- **Mandatory Project Skills Alignment:** O agente DEVE obrigatoriamente consultar e aplicar as diretrizes contidas nos guias de skill em `.agents/skills/` (`react-frontend.md`) antes de gerar ou refatorar qualquer componente, hook ou página no frontend.

## Static Analysis, Linters & Tools
- **Tooling Compliance:** The codebase strictly enforces quality gates via **ESLint**, **TypeScript (`tsc`)**, and **Prettier**. All generated or refactored code must comply fully with these configurations.
- **Zero Warnings Policy:** No compilation warnings, code smells, or style violations should be introduced. The agent must verify that the code compiles perfectly (`npm run build`) and passes all linting/formatting validations before finalizing changes.
- **Framework Stack:** Strictly adhere to the project's core framework stack: **Vite** as the bundler/dev server, **React 19**, **Material UI (MUI 9)** for design system/components, **Axios** for HTTP client execution, and **TanStack React Query (v5)** for server-state caching.

## Documentation Architecture
- **API & Query Documentation:** Every custom hook wrapping a TanStack Query (`useQuery` / `useMutation`) must include JSDoc detailing the network contract, query key invalidation strategy, and expected data shape.
- **Component JSDoc:** Public reusable UI primitives and complex structural components must have a Javadoc/JSDoc block stating their primary visual intent, accessibility requirements, and exactly one valid JSX usage example.
- **Intent over Mechanism:** Comments within the code must explain the **WHY**, not the **WHAT**. Do not comment self-explanatory code (e.g., skip `// set loading to true` above `setIsLoading(true)`).
- **Preserve Context:** Retain existing comments during refactoring; they carry historical intent and provenance.

## Imports & Path Hygiene
- **Absolute Mappings:** ALWAYS use absolute path mappings (e.g., `@/components/Button` or `@/hooks/useAuth`) instead of relative paths (e.g., `../../../../components/Button`). Deeply nested relative paths are strictly prohibited.
- **Clean Imports:** Group imports logically: React/Third-party libraries first, absolute project components/hooks second, styles/types last.

## Test Harness & TDD Mandate
- **Strict TDD Enforcement:** Test-Driven Development (TDD) is the absolute foundation of frontend safety in this project. You MUST write the test before writing any production UI logic, utility, or hook (Red-Green-Refactor cycle).
- **Execution:** All tests across all levels must run cleanly via the root command: `npm test` (powered by **Vitest** and **jsdom**).
- **Mandatory Test Suite Layers:** For every new feature, page, or hook, the agent MUST implement three distinct testing layers:
    1. **Unit Tests:** Isolated tests for utility functions, pure domain logic, and custom hooks (using `@testing-library/react`'s `renderHook`).
    2. **Component/Integration Tests:** Behavioral testing of UI components using **React Testing Library** and `@testing-library/user-event` to simulate real user interactions.
    3. **End-to-End (E2E) / Full-Slice Tests:** Verifying full user journeys, route transitions via `react-router-dom`, and complete component trees interacting with mocked network boundaries.
- **Strict I/O Isolation:** Mock all external HTTP network layers using named API fakes or mocking tools. Never let a frontend test perform actual network activity against the real backend.
- **F.I.R.S.T. Principles:** Tests must be Fast, Independent, Repeatable, Self-Validating, and Timely.

## Dependency Management & Decoupling
- **Props & Context Over Globals:** Inject all state, configurations, and external handlers explicitly through Props or React Context. Relying on global `window` objects or scattered unmanaged singletons is forbidden.
- **Third-Party UI Isolation:** Wrap heavy or highly volatile third-party libraries (such as charting engines or complex utilities) behind a thin interface or custom hook owned by this project before using them inside components.

## Structure & Topology
- **Strict Component Colocation:** Keep tests (`__tests__/`), styles, and child components close to the route or parent component that uses them, rather than in distant global folders.
- **Predictable Topology:** Adhere strictly to the project structure: `src/components/`, `src/hooks/`, `src/pages/`, `src/utils/`.

## Observability & Logging
- **Console Hygiene:** `console.log` is for local development only. Clean them up completely before committing. Never leak technical logs, raw stack traces, or API error payloads to the user-facing browser console in production.