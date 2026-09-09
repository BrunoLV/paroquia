# AGENTS.md - Root Architectural, Technical Overview & Skills Governance

Este documento define as regras Mandatórias, Invariantes de arquitetura do monorepo, governança de segurança e a **matriz de execução de Skills** para atuação no **Projeto Paróquia**.

---

## 1. Diretrizes Fundamentais de Execução

* **Comunicação e Texto Explicativo:** Execute sob o modo `caveman`. Respostas e explicações devem ser extremamente diretas, sem saudações, introduções ou prolixidade.
* **Geração e Arquitetura de Código:** Execute sob o modo `ponytail`. O código produzido deve ser estritamente de nível de produção, fortemente tipado, seguro, modular e limpo.
* **Código como Documentação Viva (*Living Documentation*):** Todo código (Java, TypeScript, SQL, Gradle) deve carregar Javadoc/JSDoc conceitual ou metadados DDL (`COMMENT ON`) justificando regras de negócio e decisões de arquitetura. Comentários triviais e óbvios são expressamente proibidos.

---

## 2. Escopo do Monorepo e Isolamento

* **Proposta do Sistema:** Sistema para gestão administrativa e pastoral paroquial.
* **Backend (`gestao-paroquia/`):** API REST em Java 25 com Spring Boot 3, arquitetura Hexagonal (Ports & Adapters) e Clean Architecture.
* **Frontend (`gestao-paroquia-ui/`):** SPA Web em React 19, TypeScript estrito, Vite 5 e MUI 9.
* **Isolamento de Fronteiras:** 
  * Backend e Frontend **DEVEM** manter dependências, ambientes de build, scripts, testes e governança técnica 100% independentes.
  * **PROIBIDO** acoplamento direto de código ou scripts compartilhados fora das fronteiras de cada módulo.

---

## 3. Matriz Mandatória de Governança de Skills (`.agents/skills/`)

Antes de gerar ou refatorar qualquer arquivo, o agente **DEVE** identificar a natureza da tarefa e ativar rigorosamente os padrões, restrições e checklists das skills correspondentes localizadas em `.agents/skills/`:

| Escopo / Gatilho | Skills Mandatórias | Diretrizes Críticas |
| :--- | :--- | :--- |
| **Manipulação de PII, Usuários, Fiéis ou Clérigos** | `lgpd-privacy-by-design.md` | • **Paridade Clérigo/Fiel:** regras idênticas de privacidade para clérigos e leigos.<br>• PII Zero em logs.<br>• Criptografia em repouso de dados civis e prevenção contra IDOR.<br>• DTOs segregados por permissão. |
| **Modelagem de Domínio Java & Regras de Negócio** | `ddd.md`<br>`solid.md`<br>`java.md` | • Domínio em Java puro sem frameworks (zero anotações Spring/JPA em `domain/`).<br>• Identificadores fortemente tipados com `record` (ex: `FielId`).<br>• Agregados associam-se apenas por ID.<br>• Mutações de estado limitadas a 1 agregado por transação.<br>• Sealed Hierarchies com Pattern Matching sem `default` artificial. |
| **Implementação de Código Java 25 Geral** | `java.md`<br>`solid.md` | • `record` imutável para DTOs/VOs com validação no construtor compacto.<br>• Proibido `float`/`double` para valores financeiros (use `BigDecimal`).<br>• Virtual Threads para I/O sem `synchronized` (anti-pinning).<br>• `Optional` restrito a retornos de métodos; coleções retornam vazias, nunca `null`. |
| **Controllers REST e Exception Handling** | `java.md`<br>`solid.md` | • Zero `@ExceptionHandler` inline em controllers.<br>• `@RestControllerAdvice` modular por Bounded Context estendendo `BaseExceptionHandler` com `@Order(2)`.<br>• Contratos OpenAPI completos (`@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`).<br>• Segurança declarativa `@PreAuthorize` e verbos HTTP semânticos explícitos. |
| **Testes Unitários no Backend** | `tdd.md` | • Ciclo Red-Green-Refactor estrito.<br>• JUnit 5 + Mockito + AssertJ.<br>• Isolamento total: Proibido `@SpringBootTest` ou I/O físico em testes unitários.<br>• Zero código defensivo sem teste prévio motivador. |
| **Scripts DDL, DML ou Consultas SQL** | `sql.md`<br>`flyway.md` | • Identificadores em `snake_case`, palavras-chave em MAIÚSCULO.<br>• Proibido `SELECT *` e junções implícitas.<br>• Sargabilidade estrita no `WHERE` (sem funções em colunas indexadas).<br>• `COMMENT ON` obrigatório em tabelas e colunas.<br>• UUIDs e `DECIMAL` para precisão monetária. |
| **Migrações e Evolução de Esquema** | `flyway.md` | • Local: `src/main/resources/db/migration/V{NNN}__{descricao}.sql`.<br>• Padrão Expand & Contract (Zero-Downtime): novas colunas como `NULLable` ou `DEFAULT`.<br>• Imutabilidade absoluta: nunca editar scripts `V` já existentes.<br>• DMLs idempotentes (`ON CONFLICT DO NOTHING`). |
| **Compilação, Dependências e Tarefas Gradle** | `gradle.md` | • Kotlin DSL (`.gradle.kts`) exclusivo, sem Groovy.<br>• Zero `allprojects {}` / `subprojects {}`: use Convention Plugins em `build-logic`.<br>• Dependências centralizadas exclusivamente em `gradle/libs.versions.toml`.<br>• Java 25 Toolchain fixado e Lazy API em todas as tarefas. |
| **Desenvolvimento Frontend (React/UI)** | `react-frontend.md`<br>`lgpd-privacy-by-design.md` | • React 19 + TypeScript estrito + MUI 9.<br>• Componentes finos (10–60 linhas JSX); regras em hooks customizados.<br>• Formulários controlados via Zod + React Hook Form.<br>• Proibidas tags HTML cruas para layout (`div`, `span`, `p`); use primitivos do MUI.<br>• Server State exclusivo via TanStack Query v5.<br>• Fiel respeito ao padrão visual arrojado da página `Fieis.tsx`. |

---

## 4. Navegação e Roteamento de Agentes

Atuações específicas nos submódulos devem obedecer às regras locais complementares:

* Para tarefas no **Backend**, consulte: [`gestao-paroquia/AGENTS.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia/AGENTS.md)
* Para tarefas no **Frontend**, consulte: [`gestao-paroquia-ui/AGENTS.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia-ui/AGENTS.md)

---

## 5. Diretrizes de Grafo de Conhecimento (Graphify)

O projeto possui um grafo de conhecimento em `../graphify-out/` mapeando dependências estruturais e relacionamentos cruzados.

* **Comando `/graphify`:** Ao receber o comando, utilize a skill `graphify` antes de qualquer outra ação.
* **Exploração Racional:**
  * Perguntas gerais da base: `graphify query "<pergunta>"` (requer `graphify-out/graph.json`).
  * Relacionamentos: `graphify path "<Origem>" "<Destino>"`.
  * Conceitos isolados: `graphify explain "<conceito>"`.
  * Navegação ampla: priorize `graphify-out/wiki/index.md` em vez de varreduras manuais no código-fonte.
* **Manutenção Contínua:** Após alterar código estrutural, execute `graphify update .` para atualizar a AST.