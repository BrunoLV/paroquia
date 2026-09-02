# AGENTS.md - Root Architectural & Technical Overview

Este documento define as regras Mandatórias e Invariantes de arquitetura do monorepo, governança e roteamento de agentes para atuação no **Projeto Paróquia**.

---

## 1. Diretrizes de Comportamento do Agente

* **Comunicação e Texto Explicativo:** Utilize a habilidade `caveman`. Respostas e explicações devem ser extremamente diretas, sem introduções, saudações ou texto conversacional.
* **Geração e Arquitetura de Código:** Utilize a habilidade `ponytail`. O código produzido deve ser estritamente de nível de produção, fortemente tipado, seguro, modular e limpo.
* **Separação de Papéis:** As habilidades `caveman` e `ponytail` são ferramentas de execução de estilo e qualidade, não personas operacionais.

---

## 2. Escopo da Aplicação e Arquitetura do Monorepo

* **Proposta do Sistema:** Sistema para gestão administrativa e pastoral paroquial.
* **Estrutura de Módulos:** Monorepo composto por dois subprojetos estritamente isolados:
  * **Backend (`gestao-paroquia/`):** API REST em Java 25 com Spring Boot 3, estruturada como monólito modular sob arquitetura Hexagonal e Clean Architecture.
  * **Frontend (`gestao-paroquia-ui/`):** Aplicação Web SPA em React 19, TypeScript e Vite 5.
* **Isolamento do Monorepo:**
  * Backend e Frontend **DEVEM** manter dependências, ambientes de build, scripts, testes e governança técnica independentes.
  * **NÃO DEVE** haver acoplamento direto de código ou scripts compartilhados fora das fronteiras de cada módulo.

---

## 3. Governança Tecnológica, Segurança e Privacidade

* **Análise Prévia via Graphify:** **DEVE-SE** consultar a AST e os artefatos de grafo em `../graphify-out/` antes de realizar varreduras manuais no código para mapear dependências e estrutura.
* **Proteção de Dados Pessoais (LGPD):**
  * **NÃO DEVE** haver exposição, log ou persistência insegura de PII em nenhum dos módulos.
  * **DEVE-SE** aplicar rigorosamente as regras do guia `docs/ia/regras_duras_lgpd.md` em auditorias, logs e APIs.

---

## 4. Roteamento de Diretrizes e Agentes (Navegação)

Atuações específicas em subprojetos **DEVEM** obrigatoriamente seguir as regras contidas em seus respectivos arquivos de diretrizes:

* Para tarefas no **Backend**, consulte e siga: [`gestao-paroquia/AGENTS.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia/AGENTS.md)
* Para tarefas no **Frontend**, consulte e siga: [`gestao-paroquia-ui/AGENTS.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia-ui/AGENTS.md)

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
