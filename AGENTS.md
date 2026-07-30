# AGENTS.md - Monorepo Root Engineering Governance & Standards

## Monorepo Architecture & Rule Delegation
- **Context Awareness:** This repository is a Monorepo. Before modifying, refactoring, or introducing code within a specific application sub-directory, the agent ABSOLUTELY MUST read, respect, and strictly apply the local `AGENTS.md` file found inside that child project (e.g., backend or frontend service folders).
- **No Cross-Contamination:** Never import internal application components, types, or business logic across child projects unless sharing explicitly through a designated common/shared workspace package.
- **Dependency Isolation:** Root-level dependencies must strictly manage monorepo tooling, linting, or global build configurations. Application-specific dependencies belong exclusively to their respective child project configuration files.

## Generalized Engineering Standards & Git Hygiene
- **Atomic Commits:** Code changes must be broken down into small, logically atomic commits. A single commit should address exactly one intent (e.g., one bug fix, one discrete feature addition, or one isolated refactor).
- **Conventional Commits Mandate:** Every commit message authored by the agent must strictly adhere to the Conventional Commits specification. Structure all messages as follows:
    - `feat(scope): description` for new capabilities.
    - `fix(scope): description` for bug patches.
    - `test(scope): description` for adding/modifying tests.
    - `docs(scope): description` for documentation updates.
    - `refactor(scope): description` for structural code changes that preserve behavior.
- **Branch Strategy:** Work must always be performed on short-lived feature branches derived from the main integration branch. Never push code directly to primary production or main branches.

## Unified Quality & Monorepo Validation Gates
- **Comprehensive Verification:** Before finalizing any task, the agent must run the validation commands across all affected child projects to ensure a zero-regression state.
- **Pre-Commit Hygiene:** The agent must pre-evaluate syntax and formatting using the local tooling configurations. No code with lint errors, static analysis warnings (Checkstyle, PMD, SpotBugs, ESLint), or broken compilation states (`tsc`, Gradle build) may be proposed.
- **Proposição Estrita de Imports (No Inline FQN Imports):** É estritamente proibida a utilização de FQN inline imports no código (ex: `br.com.nsfatima...LiturgicalEngine`). Todos os tipos e classes devem obrigatoriamente ser declarados através de instruções `import` no topo do arquivo.
- **Global TDD Framework Alignment:** Across the entire monorepo, Test-Driven Development (TDD) remains the absolute foundation of code safety. The agent must systematically deliver the testing specifications (Unit, Integration, and E2E layers as dictated by child guidelines) alongside or prior to production changes.

## Graphify Navigation & Token Optimization Mandate
- **Obrigatoriedade do Graphify:** A pasta `graphify-out/` contendo os grafos de conhecimento, AST e dependências da aplicação está disponível na raiz. Todos os agentes de IA que atuarem neste repositório DEVEM obrigatoriamente priorizar a consulta aos artefatos do `graphify` (`graphify-out/GRAPH_REPORT.md`, `graphify-out/graph.json`, detecção de comunidades, god nodes e análise de impacto) para compreender a arquitetura e rastrear dependências antes de efetuar buscas manuais cegas.
- **Navegação Inteligente & Economia de Tokens:** É estritamente proibido realizar varredura cega ou leitura em massa de múltiplos arquivos desnecessários (`view_file` em lote sem filtro prévio). O agente deve obrigatoriamente utilizar o conhecimento estruturado do grafo para ir diretamente aos arquivos afetados e trechos específicos de código, otimizando o consumo de tokens e garantindo precisão contextual.

## Security, Privacy & Global Constraints
- **Absolute Privacy Isolation:** This entire monorepo handles personal data and is strictly for internal use. The agent must strictly defer to `docs/ia/regras_duras_lgpd.md` before writing data structures, logging events, or structuring network responses. PII leak prevention is globally enforced.
- **Shared Skills Directory:** Global agent behaviors, custom tool operational paradigms, and generic skills are located within `.agents/skill`. Review these global capabilities alongside local child skills to ensure cohesive execution.