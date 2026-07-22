# Implementation Plan: Padronização do Código e Documentação segundo os Padrões AGENTS.md

## Phase 1: Backend Java Standards & Documentation Audit (TDD & Static Analysis)
- [x] Task: Audit and Fix Imports, Exception Messages and Javadoc in Backend
    - [x] Inspect Java source files in `gestao-paroquia` for inline FQN imports and convert them to top-level imports.
    - [x] Update Javadoc comments on public classes/use-cases to pt-BR detailing intent and usage examples.
    - [x] Ensure exception messages contain offending value and expected format.
    - [x] Run `./gradlew test` to verify zero regression in backend tests.
- [x] Task: Conductor - User Manual Verification 'Backend Java Standards & Documentation Audit' (Protocol in workflow.md)

## Phase 2: Frontend React/TypeScript Standards & Documentation Audit
- [x] Task: Audit and Refactor TypeScript Types, JSDoc and Imports in Frontend
    - [x] Inspect TypeScript files in `gestao-paroquia-ui` to replace relative import paths with absolute `@/...` aliases where appropriate.
    - [x] Add JSDoc comments in pt-BR for utility functions and custom hooks.
    - [x] Ensure console hygiene by removing any debug `console.log` statements.
    - [x] Run `npm run build` and `npm test` to verify zero regression in frontend build and unit tests.
- [x] Task: Conductor - User Manual Verification 'Frontend React/TypeScript Standards & Documentation Audit' (Protocol in workflow.md)

## Phase 3: Conductor System Documentation Language Harmonization
- [x] Task: Harmonize Conductor Core Files Language to pt-BR
    - [x] Review `product.md`, `tech-stack.md`, `workflow.md`, and `product-guidelines.md` ensuring consistent terminology in pt-BR.
- [x] Task: Conductor - User Manual Verification 'Conductor System Documentation Language Harmonization' (Protocol in workflow.md)
