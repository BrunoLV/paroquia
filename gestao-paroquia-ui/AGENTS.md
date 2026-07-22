## Code style

* **Types over Strings:** Always prefer `enum` or TypeScript `type` unions (`'success' | 'error'`) over standardized `String` to represent states, categories, or fixed values.
* **Components & Functions:** 10-60 lines for components, 4-20 lines for pure functions. Split if longer.
* **Files:** under 300 lines (frontend files grow fast with JSX). Split by responsibility (logic vs. UI).
* **One thing per function/component:** One logical task per hook/function, one visual/structural responsibility per component.
* **Names:** specific and unique. Avoid generic names like `data`, `handler`, `Manager`, `Component`. Prefer names that return less than 5 grep hits in the codebase.
* **Types:** strict and explicit. No `any`, no `unknown` without narrowing, no untyped props or functions.
* **No code duplication:** Extract shared logic into custom hooks (`useSomething`) or utility functions, and UI repetition into reusable primitives.
* **Early returns over nested ifs:** Especially inside JSX and hooks. Max 2 levels of indentation.
* **Error Boundaries & Messages:** Error messages and UI crash fallbacks must include the context of the failure and expected state.

## Comments

* Keep your own comments. Don't strip them on refactor — they carry intent and provenance.
* Write WHY, not WHAT. Skip `// set loading to true` above `setIsLoading(true)`.
* JSDoc on public utilities/hooks: intent + one usage example.
* Reference issue numbers / commit SHAs when a line exists because of a specific bug or browser-specific layout hack.

## Tests

* Tests run with a single command: `npm test` (or `yarn test` / `pnpm test`).
* Every new hook/utility gets a unit test. Every critical user flow gets a component/integration test (React Testing Library). Bug fixes get a regression test.
* Mock external I/O (API calls, browser storage, window methods) using MSW (Mock Service Worker) or named fakes, not messy inline stubs.
* Tests must be F.I.R.S.T: fast, independent, repeatable, self-validating, timely.

## Dependencies

* **State & Props:** Inject dependencies and configurations through Props or Context, avoid relying on global window variables or scattered state singletons.
* **Third-party wrappers:** Wrap heavy or highly volatile third-party UI libraries or fetching clients behind a thin interface owned by this project.

## Structure

* Follow the framework's convention (**Vite / Next.js / Remix** architecture).
* **Colocation:** Keep tests, styles, and child components close to the route or parent component that uses them, rather than in distant global folders.
* Predictable paths: `components/`, `hooks/`, `pages/` (or `app/`), `utils/`, `__tests__/`.

## Formatting

* Use the project's default formatter (**Prettier + ESLint** strict rules). Don't discuss code style or bike-shed formatting beyond what the automated tools enforce.

## Logging & Observability

* **Error Tracking:** Structured logs sent to your observability tool (e.g., Sentry, LogRocket) for production errors.
* **Console hygiene:** `console.log` is for local development only. Clean them up before committing. Never leak technical logs to the user-facing browser console in production.

## Engineering Standards

* **Imports:** ALWAYS use absolute path mappings (e.g., `@/components/Button` instead of `../../../../components/Button`) to maintain readability and simplify refactoring. Avoid deeply nested relative paths.
