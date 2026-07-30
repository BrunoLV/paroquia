# AGENTS.md - Monorepo Engineering Standards & Guidelines

## Domain-Driven Design (DDD) Core Principles
- **Domain-Driven Standards:** Strictly follow pure Domain-Driven Design principles to model business capabilities.
- **Ubiquitous Language:** Code elements (classes, methods, variables, database schemas) must mirror the business domain language exactly. Never use technical jargon where a domain term exists.
- **Bounded Contexts:** Maintain strict separation between distinct domain contexts. Do not leak concepts, entities, or database structures from one bounded context into another.
- **Rich Domain Model over Anemic Members:** Entities must contain both data and behavior. Business rules, state transitions, and domain validations belong inside the Domain Entities and Aggregates, NEVER leaked into Services.
- **Aggregates & Roots:** Design clear Aggregate Roots to maintain transactional boundaries and invariants. Reference external aggregates strictly by Identity (ID), never by holding direct object references.
- **Value Objects:** Always encapsulate attributes that have no conceptual identity into immutable Value Objects (implemented as Java `record`). Examples: Money, Address, Email.

## Data Privacy & Compliance (LGPD Mandate)
- **Strict Compliance Routing:** This project handles personal data and is for internal use only. The agent ABSOLUTELY MUST read, internalize, and strictly enforce the privacy and security boundaries defined in the `docs/ia/regras_duras_lgpd.md` file before authoring, refactoring, or exposing any code, data contract, or internal log.

## Agent Capabilities, Custom Skills & Graphify Mandate
- **Graphify Codebase Exploration:** A pasta `graphify-out/` localizada na raiz do monorepo contém os grafos de conhecimento e dependências de todo o projeto. O agente DEVE obrigatoriamente consultar `graphify-out/GRAPH_REPORT.md` e os arquivos de grafo antes de explorar classes, pacotes ou repositories do backend via buscas manuais.
- **Economia de Tokens:** É proibida a leitura em lote de múltiplos arquivos fonte inteiros quando a estrutura de dependências puder ser obtida diretamente via consulta aos artefatos do Graphify, garantindo economia de tokens e navegação precisa.
- **Mandatory Project Skills Alignment:** O agente DEVE obrigatoriamente consultar e aplicar as diretrizes contidas nos guias de skill em `.agents/skills/` (`ddd.md`, `java.md`, `solid.md`, `tdd.md`, `tdd_2.md`, `flyway.md`, `sql.md` e `gradle.md`) antes de gerar ou refatorar qualquer código no backend.

## Code Style & Architecture (Java & Type Safety)
- **Types over Strings:** Always prefer Java `enum`, `sealed classes`, or strongly-typed object wrappers (Value Objects) over raw `String` to represent states, categories, or fixed domains.
- **Strict Typing:** Never use raw types, untyped collections, or generic `Object` maps for structural data. Use precise Generics and `java.util.Optional` instead of returning `null`.
- **Immutable Data Carriers:** Use Java `record` for Value Objects, DTOs, and event payloads to enforce immutability by default.
- **Function Constraints:** Keep methods between 4-20 lines. If a method exceeds 20 lines, extract cohesive blocks into private helper methods.
- **File Limits:** Keep files under 500 lines. Split classes by Single Responsibility Principle (SRP) when they grow beyond this limit.
- **Naming Conventions:** Names must be specific, unique, and domain-driven. ABSOLUTELY BANNED terms: `Data`, `Handler`, `Manager`, `Helper`, `Utility`. Prefer precise names that return less than 5 grep hits in the codebase.
- **Control Flow:** Prefer early returns and guard clauses over nested `if-else` blocks. Maximum allowed indentation depth is 2 levels within any block.
- **Dry & Extract:** Zero code duplication allowed. Extract shared logic into reusable, testable components or pure functions.
- **Expressive Exceptions:** Custom exceptions must be unchecked (`RuntimeException`). Exception messages must explicitly include the offending runtime value, the context, and the expected boundary/shape.

## Static Analysis & Linters (Quality Gates)
- **Tooling Compliance:** The codebase strictly enforces quality gates via **PMD**, **SpotBugs**, and **Checkstyle**. All generated or refactored code must comply fully with these tool configurations.
- **Zero Warnings Policy:** No warnings, code smells, or style violations should be introduced. The agent must verify that the code does not trigger PMD rule violations (e.g., optimization, error prone), SpotBugs alerts (e.g., null pointer risks, bad practices), or Checkstyle layout errors before finalizing changes.

## Documentation Architecture
- **Living Documentation:** Every non-trivial architectural change, structural pattern introduction, or major integration choice MUST be documented via an **Architectural Decision Record (ADR)** in Markdown format within the designated repository location.
- **API and Contract Documentation:** Public API contracts must be fully decorated with OpenAPI/Swagger annotations (`@Operation`, `@ApiResponse`) to produce exhaustive, accurate, and interactive API documentation automatically at runtime.
- **Public Javadoc:** Every public interface, controller, configuration class, and service method must have a Javadoc block stating its primary intent, business context, and exactly one valid usage example.
- **Domain & Flow Documentation:** Complex business rules, cross-aggregate sagas, and state machines must be explicitly documented using Mermaid.js diagrams embedded directly in accompanying Markdown files or package-level Javadocs.
- **Traceability:** Reference issue tracking IDs or commit SHAs directly in the comments when a line of code exists solely to patch an upstream bug or third-party edge case.
- **Intent over Mechanism:** Comments within the code must explain the **WHY**, not the **WHAT**. Do not comment self-explanatory code (e.g., skip `// increment counter` above `i++`). Explain the business constraint or architectural reason behind non-obvious logic.
- **Preserve Context:** Retain existing comments during refactoring; they carry historical intent and provenance.

## Imports & Package Hygiene
- **No Fully Qualified Names:** NEVER use inline fully qualified class names (e.g., `com.example.domain.MyClass`) inside the code body.
- **Clean Top-Level Imports:** ALWAYS use explicit, standard top-level imports at the beginning of the file.
- **No Wildcards:** Do NOT use wildcard imports (e.g., `import java.util.*`). Import each class explicitly to keep compilation dependencies transparent.

## Error Handling & Resilience
- **Fail-Fast Validation:** Validate inputs at the boundary (e.g., Controllers, Event Listeners) using Jakarta Validation (`@NotNull`, `@Valid`) or explicit `Objects.requireNonNull()` before passing data to core services.
- **Catch Specifics:** Never catch generic `Throwable` or `Exception` unless explicitly building a global fallback error handler. Catch the most specific subclass possible.

## Test Harness & TDD Mandate
- **Strict TDD Enforcement:** Test-Driven Development (TDD) is the absolute foundation of code safety in this project. You MUST write the test before writing any production code (Red-Green-Refactor cycle). No production code changes are allowed without a preceding test change.
- **Mandatory Test Suite Layers:** For every new feature or domain behavior, the agent MUST implement three distinct testing layers:
  1. **Unit Tests:** Fast, isolated tests for core domain logic, Value Objects, and Aggregate invariants. External I/O must be mocked or faked.
  2. **Integration Tests:** Tests verifying the interplay between components and infrastructure boundaries (e.g., Database persistence with `@DataJpaTest`, Spring context slices, API contracts).
  3. **End-to-End (E2E) Tests:** Full-slice tests traversing the entire application stack from the public entry point (e.g., HTTP request using `@SpringBootTest` + `WebTestClient`) down to the database/external integrations, proving the system works as a cohesive whole.
- **Execution:** All tests across all levels must run cleanly via a single root command: `./gradlew test`.
- **Strict I/O Isolation for Unit Level:** Mock external I/O boundaries (REST APIs, Databases, Filesystems, Clocks). Prefer named test fakes (`FakeUserRepository`) or Mockito mocks. Never let a unit test perform actual network or disk activity.
- **F.I.R.S.T. Principles:** Tests must be Fast, Independent, Repeatable, Self-Validating, and Timely.

## Dependency Management & Decoupling
- **Dependency Injection:** Inject all dependencies explicitly via constructor injection. Avoid `@Autowired` on fields to ensure class testability without Spring context orchestration.
- **Anti-Corruption Layer:** Wrap third-party libraries and external SDKs behind a thin interface owned by this project. Do not leak external library models into the core domain layer.

## Framework & Structure (Spring Boot & Clean Architecture)
- **Strategic Layering:** Isolate the Core Domain from Infrastructure. Follow a clean Ports & Adapters (Hexagonal) or Onion architecture layout:
  - `domain/model`: Aggregates, Entities, Value Objects, and Domain Events. (Zero framework dependencies).
  - `domain/service`: Pure domain services handling operations spanning multiple aggregates.
  - `application`: Use cases, CQRS handlers, and application services coordinating transactions.
  - `infrastructure`: Spring Controllers, Repositories, DB Mappers, and external API clients.
- **Idiomatic Spring Boot:** Follow standard Spring Boot conventions strictly within the infrastructure layer, ensuring it implements the interfaces (Ports) defined by the domain layer.
- **Granular Components:** Prefer small, highly focused Spring `@Component` classes over bloated "God Services".

## Observability & Formatting
- **Structured Logging:** Use SLF4J/Logback to output structured JSON format for application logs (info, warn, error).
- **Contextual Logs:** Include relevant trace identifiers (like MDC / Trace IDs) in logs, and always log actionable context, not just generic stack traces.
- **Zero Style Debates:** Use the default project/IDE formatter configuration. Do not alter or debate formatting styles beyond the automated checkstyle rules.
