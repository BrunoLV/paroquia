# Specification: Technical Debt Refactoring & Code Quality

## Goal
Improve codebase maintainability and readability by refactoring large classes, standardizing patterns, and reducing coupling.

## Requirements
- Refactor `AuditLogService` to split responsibilities (e.g., separation of storage and query logic).
- Review `EventoController` for SRP (Single Responsibility Principle) violations and extract logic if necessary.
- Standardize exception handling across all new modules.
- Ensure all public APIs have Javadoc and usage examples (continuing the work started).

## Success Criteria
- `AuditLogService` size reduced below 200 lines.
- `EventoController` remains focused on HTTP mapping, delegating all logic to use cases.
- 100% compliance with `GEMINI.md` style guide.
