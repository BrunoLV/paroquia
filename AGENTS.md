# Monorepo Engineering Standards & Guidelines

## Monorepo & Root Infrastructure

### Scripts & Automation

* **Single Entry Point:** Root scripts (`.sh` or `.bat`) must centralize environment orchestration. Prefer abstract commands like `./run.sh dev` or `./run.sh test` instead of requiring developers to run commands inside subfolders.
* **Fail Fast:** All shell scripts must start with `set -e` (and ideally `set -uo pipefail`) to immediately abort execution if any command fails.
* **No Hardcoded Paths:** Scripts must not assume absolute paths of the local machine; use relative references to the project root (`$(dirname "$0")`).

### Docker & Local Environment

* **Multi-stage Builds:** The production `Dockerfile` for each subproject must use multi-stage builds to ensure lightweight images and security (keeping build tools out of the final image).
* **Compose Isolation:** The root `docker-compose.yml` must manage only the services necessary to run the local environment (DB, LocalStack, application containers).
* **Named Volumes:** Always use named volumes for local database persistence to avoid data loss on `docker compose down`.
* **Environment Variables:** Never hardcode credentials or URLs into Docker files. Use a `.env.example` file at the root as documentation for the required parameters.

---

## Code Style

### General

* **One responsibility:** One responsibility per module/component, one clear single task per function (SRP).
* **Names:** Specific and unique. Avoid generic terms like `data`, `handler`, `Manager`, `Component`. Prefer names that return less than 5 grep hits in the codebase.
* **No code duplication:** Extract shared logic into reusable functions, hooks, or utility modules.
* **Control Flow:** Early returns over nested `if`s. Maximum of 2 levels of indentation.

### Backend (Java)

* **Types over Strings:** Always prefer `enum` over standardized `String` to represent states, categories, or fixed values.
* **Functions & Files:** Functions between 4-20 lines (split if longer). Files under 500 lines.
* **Types:** Explicit and strict. No generic signatures or hidden typing.
* **Exceptions:** Exception messages must include the offending value and the expected shape.

### Frontend (React + TypeScript)

* **Types over Strings:** Prefer `enum` or TypeScript `type` unions (`'success' | 'error'`) over free strings to represent states or categories.
* **Components & Functions:** 10-60 lines for visual components; 4-20 lines for pure functions. Split if they exceed this.
* **Files:** Under 300 lines (UI files grow fast with JSX). Split logic (hooks) from rendering (UI).
* **Types:** Strict and explicit. The use of `any` is forbidden; avoid `unknown` without proper type narrowing. No untyped props or functions.

---

## Comments

* **Intent & Provenance:** Keep your own comments and do not strip them during refactoring. They carry historical context.
* **WHY, not WHAT:** Write *why* the code was written that way, not *what* it does. Avoid `// increment counter` above `i++`.
* **Documentation:** Docstrings (Java) or JSDoc (TS) on public functions: describe the intent and include a practical usage example.
* **Context Linkage:** Always reference commit SHAs, issues, or ticket numbers when a line of code exists purely to bypass a specific bug or a third-party constraint.

---

## Tests

### General

* **F.I.R.S.T:** Tests must be Fast, Independent, Repeatable, Self-validating, and Timely.
* **Coverage:** Every new function/flow requires a test. Bug fixes require a regression test.

### Backend (Java)

* **Execution:** Run with a single command inside the backend folder: `./gradlew test`.
* **Isolation:** External I/O (APIs, Database, Filesystem) must be mocked using named fake classes; avoid inline stubs.

### Frontend (React)

* **Execution:** Run with a single command inside the frontend folder: `npm test` (or `yarn`/`pnpm`).
* **Isolation:** Mock API calls and network contracts using MSW (Mock Service Worker). Use named fake classes/functions for browser storages or `window` methods.
* **Scope:** Unit tests for every custom hook/utility. Integration/component tests (React Testing Library) for critical user flows.

---

## Structure & Dependencies

### Backend (Java / Spring Boot)

* **Structure:** Strictly follow Spring Boot conventions. Focused and small modules instead of god files. Predictable paths (`controller/`, `model/`, `repository/`).
* **Dependencies:** Inject dependencies strictly via constructor or parameters, never through global state or hidden static imports.
* **Abstraction:** Wrap third-party libraries behind a thin interface owned by the project.

### Frontend (React)

* **Structure:** Follow the established architecture of the framework (**Vite / Next.js / Remix**). Predictable paths: `components/`, `hooks/`, `pages/` (or `app/`), `utils/`.
* **Colocation:** Keep test files, styles, and child components as close as possible to the route or parent component that uses them. Avoid distant global folders.
* **Dependencies:** Inject configurations and states via Props or Context. Avoid scattered singletons or direct dependence on global `window` variables. Wrap heavy UI libraries or data clients behind internal interfaces.

---

## Formatting

* **Automated Only:** Strictly use the default formatter for each stack.
* **Backend:** Native IDE formatting / Java standard.
* **Frontend:** Strict **Prettier + ESLint** configuration.


* **Zero Debates:** Do not waste time discussing visual style in code reviews beyond what automated tools already block or fix.

---

## Logging & Observability

### Backend (Java)

* **Format:** Structured JSON for debugging and observability logs in production.
* **CLI:** Plain text only for outputs directed at command-line tools.

### Frontend (React)

* **Error Tracking:** Structured logs sent directly to the observability tool (e.g., Sentry) in case of production errors.
* **Console Hygiene:** `console.log` is strictly for local development. Remove all of them before opening a Pull Request. Never leak technical logs into the user-facing browser console in production environments.

---

## Engineering Standards (Imports)

* **Backend (Java):** NEVER use inline imports (fully qualified class names like `br.com.package.Class` in the middle of the code). Always use standard top-level imports at the beginning of the file.
* **Frontend (React):** ALWAYS use absolute path mappings (e.g., `@/components/Button` instead of `../../../../components/Button`) to maintain readability and simplify refactoring. Avoid deeply nested relative paths.