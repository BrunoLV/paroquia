# Skill: React Frontend Engineering (gestao-paroquia-ui)

## Context & Purpose
This skill equips the agent with the precise execution patterns, architectural constraints, and quality gates required to develop, refactor, and maintain the `gestao-paroquia-ui` codebase. It ensures absolute alignment with React 19, TypeScript, Vite, Material UI (MUI 9), and Vitest.

## Production Standards & Core Practices

### 1. Component Architecture & Separation of Concerns
* **Business Logic Isolation:** Components must be thin visual layout layers. All state transitions, side effects, and data fetching MUST be encapsulated inside custom hooks (`hooks/`).
* **MUI 9 Semantic Primitives:** Use Material UI semantic layouts (`Box`, `Stack`, `Grid2`, `Container`, `Typography`) instead of raw HTML elements (`div`, `span`, `p`). HTML markup within JSX is treated as a style violation.
* **Component Decomposition:** Strictly adhere to the 10-60 lines constraint for JSX files. If a component exceeds 60 lines, automatically decompose it into atomic sub-components placed within a local `components/` subfolder adjacent to the parent file.
* **Controlled Component Tree:** Favor explicit React Context for global or feature-wide shared state, and local hook state for ephemeral UI state. Global window injection or singleton state stores are forbidden.

### 2. UI Design System & Visual Consistency
A dedicated standardization pass (2026-07) unified header, loading, dialog, and form conventions across every page. New/changed UI MUST follow these established patterns rather than reintroducing the ad-hoc styles that pass removed:

* **Page header:** every route-level page opens with `<PageHeader icon={<XIcon sx={{ fontSize: 32, color: 'primary.main' }} />} title="..." subtitle="..." actions={...} />` (`src/components/PageHeader.tsx`) instead of a hand-rolled icon-badge/title `Box`. Pass right-aligned buttons via `actions`.
* **Theme tokens, never hex:** `src/theme.ts` defines `theme.custom.headerIconBg` and `theme.custom.subtlePanelBg` plus the palette (`primary.main`, `secondary.main`, `error.main`, `divider`). Do not hardcode colors like `#1a237e`, `#37474f`, `#d97706`, `#eee`/`#e0e0e0`, `#f8f9fa`/`#fafafa` in `sx` — use the palette/theme token equivalent. `theme.ts` also carries global `MuiDialog` (paper `borderRadius: 0`), `MuiDialogTitle` (border, padding, `fontWeight: 700`), and `MuiDialogActions` (border, padding) overrides — do not re-declare these per-component; only add `sx` for something the theme default doesn't already provide (e.g. a status chip row via `display:flex, justifyContent:space-between`).
* **Loading state:** use MUI `Skeleton` sized to approximate the real content for page/section-level loading, not `CircularProgress` (reserve `CircularProgress` for small inline/button-level spinners only).
* **Content sections:** use `Card` (with `CardContent` for padded content) for content panels, not bare `Paper` (Login and Drawer-internal content are the only exceptions).
* **Dialogs/forms:**
  - `DialogContent dividers` wrapping a single `<Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>` for stacked fields; two-column rows use `Grid` (`<Grid container spacing={2}><Grid size={{ xs: 6 }}>`), not ad-hoc `Box` flex rows.
  - Mark required fields with the `required` prop only — never append a manual `*` to the label text.
  - Field-level errors use `error`/`helperText` on `TextField`, or `FormHelperText error` inside a `FormControl` for `Select` — never a raw `<span style={...}>`.
  - Validation feedback channel: field-specific → inline `helperText`/`FormHelperText`; submission-level failure → toast via `useNotification().showError`; non-blocking business-rule warning → `Alert severity="warning"` in the dialog content.
  - `maxWidth`: `xs` for 0-1 field confirmations, `sm` for forms up to ~6 fields, `md` for more complex/table-containing dialogs.
  - Every `TextField`/`Select`/`FormControl` in a dialog gets `size="small"`.
  - Submit button labels: "Salvar" for create, "Salvar Alterações" for edit (swap on the component's existing edit/create mode flag), "Confirmar {Ação}" for destructive confirmations; Cancel buttons use `color="secondary"` unless intentionally destructive-adjacent (then `color="error"`, e.g. a veto/cancel confirm).
* **Server state on pages:** every page's data fetching goes through TanStack Query (`useQuery`/`useMutation`), never raw `useState`+`useEffect`+manual `axios`/`api/*` calls — this was retrofitted onto `ConselhosAtas.tsx`, which is now the reference example for this pattern next to `Approvals.tsx`.

### 3. Test-Driven Development (TDD) Mandate
* **Red-Green-Refactor Protocol:** The agent will not generate production code without outputting the corresponding test specification *first*. A feature implementation is incomplete without its comprehensive testing suite.
* **Testing Stack Isolation:**
    - **Hooks & Utils:** Tested via Vitest using `@testing-library/react`'s `renderHook` utility.
    - **UI & Interactivity:** Tested using React Testing Library coupled with `@testing-library/user-event` to simulate genuine browser interactions. Raw `fireEvent` calls are banned.
- **Network Decoupling:** All network communication executed via `axios` or TanStack Query mutations must be intercepted using strict API fakes or mocking abstractions. Unit and integration suites must remain 100% deterministic and isolated from network sockets.

### 4. Server-State Management (TanStack Query v5 & Axios)
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