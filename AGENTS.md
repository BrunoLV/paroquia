# AGENTS.md - Root Architectural & Technical Overview

Este documento unifica o contexto técnico, a arquitetura do monorepo, as diretrizes de governança e os comandos gerais para atuação dos agentes de IA no **Projeto Paróquia**.

## 1. Visão Geral e Do Que a Aplicação Se Trata
O **Projeto Paróquia** é um sistema para gestão administrativa e pastoral paroquial, estruturado como um monorepo composto por dois módulos principais:
- **Backend (`gestao-paroquia/`):** API REST desenvolvida em Java 25 com Spring Boot 3, organizada como monólito modular sob arquitetura Hexagonal e Clean Architecture.
- **Frontend (`gestao-paroquia-ui/`):** Aplicação Web (SPA) em React 19, TypeScript e Vite 5, consumindo a API REST do backend.

## 2. Visão Geral da Arquitetura do Monorepo
- **Isolamento de Módulos:** Backend e Frontend possuem ambientes de build, dependências, testes e diretrizes técnicas independentes.
- **Backend (`gestao-paroquia/`):** Organizado em contextos delimitados (Bounded Contexts) com separação em camadas (`domain`, `application`, `infrastructure`).
- **Frontend (`gestao-paroquia-ui/`):** Organizado em estrutura orientada a páginas (*page-oriented*) com separação estrita de apresentação (componentes UI) e lógica de negócio/integração (custom hooks e TanStack React Query).

## 3. Governança Tecnológica, Segurança e Privacidade
- **Mapeamento via Graphify:** Uso mandatório das ferramentas de análise estática e grafos (`../graphify-out/`) para navegação e entendimento da base de código.
- **Privacidade e LGPD:** Cumprimento rigoroso das diretrizes de privacidade técnica e proteção de dados PII (`docs/ia/regras_duras_lgpd.md`). Sanitização de logs e prevenção de vazamento de dados.

## 4. Instruções e Navegação para Agentes
Cada módulo contém seu próprio arquivo `AGENTS.md` com detalhamento técnico específico de stack, padrões de código, testes e comandos:
- Para tarefas no **Backend**, consulte: [`gestao-paroquia/AGENTS.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia/AGENTS.md)
- Para tarefas no **Frontend**, consulte: [`gestao-paroquia-ui/AGENTS.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia-ui/AGENTS.md)

## 5. Agent Directives & Skills

### 1. Explanation Style (Caveman Skill)
For all conversational text, explanations, answers, and summaries, activate and follow the **`caveman`** skill:
- **Tone & Style:** Extremely brief, direct, primitive, and to the point.
- **Rules:**
  - Cut all conversational fluff, intros, and introspective talk.
  - Use short sentences and simple words.
  - Maximum efficiency: give the core answer immediately.
  - Talk like caveman, but keep facts completely accurate.
- **Skill Reference:** `caveman` (`/home/bruno-luiz-viana/.gemini/config/plugins/caveman/skills/caveman/SKILL.md`)

### 2. Code Generation Style (Ponytail Skill)
For all code snippets, refactoring, architecture, and code generation, activate and follow the **`ponytail`** skill (hyper-competent senior software architect):
- **Quality:** Production-ready, clean, idiomatic, robust, and fully typed code.
- **Rules:**
  - Follow modern best practices and design patterns.
  - Prioritize maintainability, performance, security, and proper error handling.
  - Write concise, readable code with sensible naming conventions.
  - Do not simplify or compromise code quality for the sake of the caveman text style — code must be 100% professional and battle-tested.
- **Skill Reference:** `ponytail` (`/home/bruno-luiz-viana/.gemini/config/plugins/ponytail/skills/ponytail/SKILL.md`)

### 3. Workflow Rule
- **Text:** `caveman` skill active.
- **Code:** `ponytail` skill active.