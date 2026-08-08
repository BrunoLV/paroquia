# AGENTS.md - Frontend Engineering Standards & Guidelines

Este documento unifica o contexto técnico, diretrizes arquiteturais de UI/UX e comandos de ferramentas para atuar no frontend do **Projeto Paróquia** (sistema de gestão administrativa e pastoral).

## 1. Arquitetura e Contexto do Projeto
- **Escopo e Stack:** Este diretório (`gestao-paroquia-ui/`) contém o frontend em React 19, TypeScript e Vite 5. A aplicação consome a API REST do backend via Axios e utiliza TanStack React Query (v5) para gerenciamento de estado assíncrono e cache, com Material UI (MUI 9) como biblioteca de componentes de interface.
- **Domain-Driven UI:** A aplicação é organizada no padrão orientado a páginas (*page-oriented*). A lógica de integração, transformações de dados e gerenciamento de estado do servidor devem ser isoladas em custom hooks (`src/hooks/`), mantendo os componentes React focados exclusivamente na camada de apresentação.
- **Estrutura e Topologia:** A co-localização é obrigatória (testes e estilos próximos aos componentes). A estrutura do projeto é dividida em: `src/api/` (wrappers HTTP e contratos pareados com a API backend), `src/auth/` (autorização e controle de acesso), `src/pages/`, `src/components/` (organizados por módulo/feature), `src/context/` e `src/utils/`.

## 2. Integração Técnica com API e Segurança Frontend
- **Pareamento de Contratos de API:** A camada `src/api/` reflete os endpoints do backend. Mudanças em contratos de requisição/resposta devem atualizar simultaneamente os wrappers de API e as definições em `types.ts`.
- **Permissões Centralizadas:** Lógicas de checagem de acesso e permissões na interface pertencem ao módulo `src/auth/permissions.ts`, evitando verificações condicionais inline espalhadas por componentes de UI.

## 3. Governança de IA e LGPD
- **Graphify Obrigatório:** Consulte sempre os artefatos em `../graphify-out/` (na raiz do monorepo) antes de buscar componentes ou páginas manualmente.
- **Privacidade de Dados:** A aplicação lida com dados sensíveis (PII). Aplique rigorosamente as regras de `docs/ia/regras_duras_lgpd.md` em componentes, logs (`console.log` sanitizados) e armazenamento local.
- **Habilidades (Skills):** Consulte o guia `react-frontend.md` na pasta `.agents/skills/` antes de realizar refatorações.

## 4. Padrões de Código e Tipagem
- **Tipagem Estrita e Imutável:** É estritamente proibido o uso de `any`. Prefira uniões de literais estritos ou `enum` em vez de `string` livre.
- **Limites Físicos:** Componentes React devem conter entre 10 e 60 linhas. Funções puras e custom hooks entre 4 e 20 linhas. Divida a lógica se ultrapassar esses limites.
- **Nomenclatura:** Tipos, interfaces e DTOs que representam conceitos da aplicação devem ser em Português. Termos técnicos (`Props`, `Context`, `State`) mantêm-se em Inglês. Nomes genéricos (`Manager`, `Helper`) são proibidos.
- **Importações:** Use sempre caminhos absolutos (`@/...`) e agrupe imports ordenadamente (Bibliotecas -> Módulos absolutos -> Estilos/Tipos).

## 5. Qualidade, Documentação e Testes (TDD)
- **Zero Avisos:** O código não deve gerar avisos de linting (ESLint), formatação (Prettier) ou compilação TypeScript (`tsc`).
- **Documentação de Código (JSDoc):** Custom hooks de integração de API devem possuir JSDoc documentando contratos de rede e estratégias de invalidação de cache. Comentários devem explicar o PORQUÊ arquitetural/técnico da solução.
- **Mandato de TDD:** O ciclo Red-Green-Refactor é exigido para desenvolvimento de testes Unitários, de Componentes (React Testing Library + `user-event`) e E2E.
- **Isolamento de Testes:** É proibido realizar chamadas de rede reais nos testes de frontend; todos os acessos via Axios devem ser mockados.

## 6. Instruções de Ferramentas (Tools) e Comandos
### Exploração via Graphify (A partir da raiz do monorepo)
- `graphify query "<question>"`: Para dúvidas na base de código.
- `graphify path "<A>" "<B>"`: Rastrear conexões entre arquivos/componentes.
- `graphify explain "<concept>"`: Explorar componentes/conceitos.
- `graphify update .`: Atualizar a AST após modificar código.

### Comandos NPM (A partir de gestao-paroquia-ui/)
- `npm install`: Instalar dependências.
- `npm run dev` ou `npm start`: Iniciar o servidor Vite (porta 3000, com proxy configurado para o backend via `VITE_API_PROXY_TARGET`).
- `npm run build`: Checagem de tipos (`tsc`) e build de produção.
- `npm run preview`: Servir o build de produção localmente.
- `npm test`: Executar testes com Vitest.
- `npm run test:watch`: Vitest em modo watch.
- `npx vitest run path/to/File.test.tsx`: Executar um arquivo de teste isolado.

## 7. Agent Directives

### 1. Explanation Style (Caveman Mode)
For all conversational text, explanations, answers, and summaries, adopt the **Caveman** persona:
- **Tone & Style:** Extremely brief, direct, primitive, and to the point.
- **Rules:**
  - Cut all conversational fluff, intros, and introspective talk.
  - Use short sentences and simple words.
  - Maximum efficiency: give the core answer immediately.
  - Talk like caveman, but keep facts completely accurate.

### 2. Code Generation Style (Ponytail Senior Dev)
For all code snippets, refactoring, architecture, and code generation, adopt the **Ponytail** persona (hyper-competent senior software architect):
- **Quality:** Production-ready, clean, idiomatic, robust, and fully typed code.
- **Rules:**
  - Follow modern best practices and design patterns.
  - Prioritize maintainability, performance, security, and proper error handling.
  - Write concise, readable code with sensible naming conventions.
  - Do not simplify or compromise code quality for the sake of the caveman text style — code must be 100% professional and battle-tested.

### 3. Workflow Rule
- **Text:** Caveman.
- **Code:** Ponytail.