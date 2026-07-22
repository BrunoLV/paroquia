# Implementation Plan: Padronização do Código e Documentação segundo os Padrões AGENTS.md

## Phase 1: Backend Java Standards & Documentation Audit (TDD & Static Analysis)
- [ ] Task: Audit and Fix Imports, Exception Messages and Javadoc in Backend
    - [ ] Inspect Java source files in `gestao-paroquia` for inline FQN imports and convert them to top-level imports.
    - [ ] Update Javadoc comments on public classes/use-cases to pt-BR detailing intent and usage examples.
    - [ ] Ensure exception messages contain offending value and expected format.
    - [ ] Run `./gradlew test` to verify zero regression in backend tests.
- [ ] Task: Conductor - User Manual Verification 'Backend Java Standards & Documentation Audit' (Protocol in workflow.md)

## Phase 2: Frontend React/TypeScript Standards & Documentation Audit
- [ ] Task: Audit and Refactor TypeScript Types, JSDoc and Imports in Frontend
    - [ ] Inspect TypeScript files in `gestao-paroquia-ui` to replace relative import paths with absolute `@/...` aliases where appropriate.
    - [ ] Add JSDoc comments in pt-BR for utility functions and custom hooks.
    - [ ] Ensure console hygiene by removing any debug `console.log` statements.
    - [ ] Run `npm run build` and `npm test` to verify zero regression in frontend build and unit tests.
- [ ] Task: Conductor - User Manual Verification 'Frontend React/TypeScript Standards & Documentation Audit' (Protocol in workflow.md)

## Phase 3: Conductor System Documentation Language Harmonization
- [ ] Task: Harmonize Conductor Core Files Language to pt-BR
    - [ ] Review `product.md`, `tech-stack.md`, `workflow.md`, and `product-guidelines.md` ensuring consistent terminology in pt-BR.
- [ ] Task: Conductor - User Manual Verification 'Conductor System Documentation Language Harmonization' (Protocol in workflow.md)
