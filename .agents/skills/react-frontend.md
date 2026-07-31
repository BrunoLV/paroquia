# Skill: React Frontend Engineering (gestao-paroquia-ui)

## Context & Purpose
This skill equips the agent with the precise execution patterns, architectural constraints, and quality gates required to develop, refactor, and maintain the `gestao-paroquia-ui` codebase. It ensures absolute alignment with React 19, TypeScript, Vite, Material UI (MUI 9), and Vitest.

## Production Standards & Core Practices

### 1. Component Architecture & Separation of Concerns
* **Business Logic Isolation:** Components must be thin visual layout layers. All state transitions, side effects, and data fetching MUST be encapsulated inside custom hooks (`hooks/`).
* **MUI 9 Semantic Primitives:** Use Material UI semantic layouts (`Box`, `Stack`, `Grid2`, `Container`, `Typography`) instead of raw HTML elements (`div`, `span`, `p`). HTML markup within JSX is treated as a style violation.
* **Component Decomposition:** Strictly adhere to the 10-60 lines constraint for JSX files. If a component exceeds 60 lines, automatically decompose it into atomic sub-components placed within a local `components/` subfolder adjacent to the parent file.
* **Controlled Component Tree:** Favor explicit React Context for global or feature-wide shared state, and local hook state for ephemeral UI state. Global window injection or singleton state stores are forbidden.

### 2. Test-Driven Development (TDD) Mandate
* **Red-Green-Refactor Protocol:** The agent will not generate production code without outputting the corresponding test specification *first*. A feature implementation is incomplete without its comprehensive testing suite.
* **Testing Stack Isolation:**
    - **Hooks & Utils:** Tested via Vitest using `@testing-library/react`'s `renderHook` utility.
    - **UI & Interactivity:** Tested using React Testing Library coupled with `@testing-library/user-event` to simulate genuine browser interactions. Raw `fireEvent` calls are banned.
- **Network Decoupling:** All network communication executed via `axios` or TanStack Query mutations must be intercepted using strict API fakes or mocking abstractions. Unit and integration suites must remain 100% deterministic and isolated from network sockets.

### 3. Server-State Management (TanStack Query v5 & Axios)
* **Custom Hook Customization:** Raw `axios` invocations inside React components are completely prohibited. All HTTP mutations and data fetches must be wrapped into feature-specific custom hooks exposing TanStack Query objects (`useQuery`, `useMutation`).
* **Deterministic Query Keys:** Maintain a strict, predictable query key hierarchy passing structured arrays for cache invalidation (e.g., `['context', 'resource', { parameters }]`).
* **Resilient Boundaries:** Every asynchronous boundary must gracefully handle errors using local React Error Boundaries or standardized fallback states to shield the user from application crashes.

## Anti-Patterns Banned from the Codebase

* **No Inline Logical Conditionals in JSX:** Avoid complex inline ternary operations or short-circuit evaluations (`{condition && <Component />}`) directly in the return block if they exceed 1 level of nesting. Extract the evaluation logic into separate variable guard clauses or specialized presentational sub-components.
* **No `any` or Loose Typings:** The use of `any` or `unknown` without immediate type guards/narrowing will break compilation gates. Every API response schema, callback function, and component prop must be explicitly typed with strict TypeScript definitions.
* **No Primitive State Bloat:** Avoid splitting closely related state properties into individual `useState` calls (e.g., `setIsLoading`, `setIsError`, `setData`). Group related server-state primitives or use declarative status strings (`'idle' | 'loading' | 'success' | 'error'`).
* **No Leaked Side Effects:** Side effects must be explicitly tracked inside React lifecycle boundaries (hooks/effects). Never instantiate loose listeners, micro-tasks, or state mutations outside the functional component lifecycle.
* **No Relative Path Traversals:** Deeply nested parent relative imports (e.g., `../../../../components`) are blocked. The agent must exclusively use absolute path mapping prefixes (`@/components`, `@/hooks`, `@/utils`).

## Verification & Quality Gates
* **Compilation Integrity:** Code changes must cleanly compile via `tsc` and pass strict `ESLint` / `Prettier` syntax audits without requiring modifications to the base rule sets.
* **Zero Technical Log Leaks:** Temporary debug code like `console.log` must be systematically removed prior to committing changes. Telemetry, client-side error outputs, and production logs must contain zero stack traces or PII data, delegating all privacy boundaries to `docs/ia/regras_duras_lgpd.md`.