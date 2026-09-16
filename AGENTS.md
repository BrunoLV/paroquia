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
* **Backend (`gestao-paroquia/`):** API REST em Java 25 com Spring Boot 4, servidor embarcado Jetty 12, Log4j2, arquitetura Hexagonal (Ports & Adapters) e Clean Architecture.
* **Frontend (`gestao-paroquia-ui/`):** SPA Web em React 19, TypeScript estrito, Vite 5 e MUI 9.
* **Isolamento de Fronteiras:** 
  * Backend e Frontend **DEVEM** manter dependências, ambientes de build, scripts, testes e governança técnica 100% independentes.
  * **PROIBIDO** acoplamento direto de código ou scripts compartilhados fora das fronteiras de cada módulo.

### 2.1. Regras Táticas de Arquitetura: Policies vs Services (DDD)

1. **PROIBIÇÃO DE REGRAS VARIÁVEIS EM SERVICES**
   * NUNCA crie ou expanda classes `*Service` com encadeamentos de `if/else` ou `switch/case` para variações de cálculo, taxa, desconto, juros, autorização ou decisão de negócio.
   * Se a regra variar por tipo, perfil, campanha ou contexto, modele uma interface `*Policy` e Strategies concretas em `domain/policy/` ou `domain/<aggregate>/policy/`.
2. **ISOLAMENTO DE POLICIES NO DOMÍNIO**
   * Toda Policy é estritamente stateless e zero-I/O: não pode injetar repositórios, APIs, banco, cache nem carregar anotações de framework.
   * O UseCase carrega os dados e entrega à Policy apenas Value Objects, agregados ou snapshots imutáveis em memória.
3. **TELL, DON'T ASK E ANEMIA**
   * A Policy devolve uma decisão ou Value Object; nunca altera entidade/agregado por setters.
   * A mutação é comandada pelo próprio agregado por método semântico, como `aggregate.applyRule(policy)`.

---

## 3. Matriz Mandatória de Governança de Skills (`.agents/skills/`)

Antes de gerar ou refatorar qualquer arquivo, o agente **DEVE** identificar a natureza da tarefa e ativar rigorosamente os padrões, restrições e checklists das skills canônicas localizadas em `.agents/skills/`:

| Escopo / Gatilho | Skills Mandatórias | Diretrizes Críticas |
| :--- | :--- | :--- |
| **Manipulação de PII, Usuários, Fiéis ou Clérigos** | [`lgpd-privacy-by-design`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/lgpd-privacy-by-design/SKILL.md) | • **Paridade Clérigo/Fiel:** regras idênticas de privacidade para clérigos e leigos.<br>• PII Zero em logs.<br>• Criptografia em repouso de dados civis e prevenção contra IDOR.<br>• DTOs segregados por permissão. |
| **Modelagem de Domínio Java & Regras de Negócio** | [`ddd`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/ddd/SKILL.md)<br>[`solid`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/solid/SKILL.md)<br>[`java`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/java/SKILL.md) | • Domínio em Java puro sem frameworks (zero anotações Spring/JPA em `domain/`).<br>• Identificadores fortemente tipados com `record` (ex: `FielId`).<br>• Agregados associam-se apenas por ID.<br>• Mutações de estado limitadas a 1 agregado por transação.<br>• Sealed Hierarchies com Pattern Matching sem `default` artificial. |
| **Implementação de Código Java 25 Geral** | [`java`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/java/SKILL.md)<br>[`solid`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/solid/SKILL.md) | • `record` imutável para DTOs/VOs com validação no construtor compacto.<br>• Proibido `float`/`double` para valores financeiros (use `BigDecimal`).<br>• Virtual Threads para I/O sem `synchronized` (anti-pinning).<br>• `Optional` restrito a retornos de métodos; coleções retornam vazias, nunca `null`.<br>• **Zero imports inline:** proibido usar nomes totalmente qualificados (FQCNs) no corpo do código; declare todos os imports explicitamente no topo do arquivo sem wildcards. |
| **Controllers REST e Exception Handling** | [`java`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/java/SKILL.md)<br>[`solid`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/solid/SKILL.md) | • Zero `@ExceptionHandler` inline em controllers.<br>• `@RestControllerAdvice` modular por Bounded Context estendendo `BaseExceptionHandler` com `@Order(2)`.<br>• Contratos OpenAPI completos (`@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`).<br>• Segurança declarativa `@PreAuthorize` e verbos HTTP semânticos explícitos. |
| **Testes Unitários no Backend** | [`tdd`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/tdd/SKILL.md) | • Ciclo Red-Green-Refactor estrito.<br>• JUnit 5 + Mockito + AssertJ.<br>• Isolamento total: Proibido `@SpringBootTest` ou I/O físico em testes unitários.<br>• Zero código defensivo sem teste prévio motivador. |
| **Scripts DDL, DML ou Consultas SQL** | [`sql`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/sql/SKILL.md)<br>[`flyway`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/flyway/SKILL.md) | • Identificadores em `snake_case`, palavras-chave em MAIÚSCULO.<br>• Proibido `SELECT *` e junções implícitas.<br>• Sargabilidade estrita no `WHERE` (sem funções em colunas indexadas).<br>• `COMMENT ON` obrigatório em tabelas e colunas.<br>• UUIDs e `DECIMAL` para precisão monetária. |
| **Persistência JPA & Auditoria de Entidades** | [`ddd`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/ddd/SKILL.md)<br>[`solid`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/solid/SKILL.md)<br>[`java`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/java/SKILL.md) | • **Modelo Único:** Todas as entidades JPA mapeadas estendem `BaseAuditableEntity`.<br>• Padrão estrito de 4 colunas: `criado_em_utc`, `atualizado_em_utc`, `criado_por`, `atualizado_por`.<br>• Auditoria 100% automática via Spring Data JPA Auditing (`AuditingEntityListener`).<br>• Proibida auditoria manual (`@PrePersist`, `@PreUpdate` ou setters em use cases).<br>• Conceitos de framework restritos à persistência (zero vazamento para `domain/model/`). |
| **Migrações e Evolução de Esquema** | [`flyway`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/flyway/SKILL.md) | • Local: `src/main/resources/db/migration/V{NNN}__{descricao}.sql`.<br>• Padrão Expand & Contract (Zero-Downtime): novas colunas como `NULLable` ou `DEFAULT`.<br>• Imutabilidade absoluta: nunca editar scripts `V` já existentes.<br>• DMLs idempotentes (`ON CONFLICT DO NOTHING`). |
| **Compilação, Dependências e Tarefas Gradle** | [`gradle`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/gradle/SKILL.md) | • Kotlin DSL (`.gradle.kts`) exclusivo, sem Groovy.<br>• Zero `allprojects {}` / `subprojects {}`: use Convention Plugins em `build-logic`.<br>• Dependências centralizadas exclusivamente em `gradle/libs.versions.toml`.<br>• Java 25 Toolchain fixado e Lazy API em todas as tarefas. |
| **Desenvolvimento Frontend (React/UI)** | [`react-frontend`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/react-frontend/SKILL.md)<br>[`lgpd-privacy-by-design`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/lgpd-privacy-by-design/SKILL.md) | • React 19 + TypeScript estrito + MUI 9.<br>• Componentes finos (10–60 linhas JSX); regras em hooks customizados.<br>• Formulários controlados via Zod + React Hook Form.<br>• Proibidas tags HTML cruas para layout (`div`, `span`, `p`); use primitivos do MUI.<br>• Server State exclusivo via TanStack Query v5.<br>• Fiel respeito à especificação visual [`gestao-paroquia-ui/DESIGN_SYSTEM.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia-ui/DESIGN_SYSTEM.md). |
| **Exploração e Arquitetura do Repositório** | [`graphify`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/graphify/SKILL.md) | • Prioridade máxima ao grafo em `graphify-out/` antes de varreduras lineares com grep/find.<br>• Navegação orientada a nós e dependências. |

---

## 4. Navegação e Roteamento de Agentes

Atuações específicas nos submódulos devem obedecer às regras locais complementares:

* Para tarefas no **Backend**, consulte: [`gestao-paroquia/AGENTS.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia/AGENTS.md)
* Para tarefas no **Frontend**, consulte: [`gestao-paroquia-ui/AGENTS.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia-ui/AGENTS.md)

---

## 5. Diretrizes de Grafo de Conhecimento (Graphify)

O projeto possui um grafo de conhecimento em `graphify-out/` mapeando dependências estruturais e relacionamentos cruzados de todo o monorepo.

* **Comando `/graphify`:** Ao receber o comando, utilize a skill `graphify` antes de qualquer outra ação.
* **Exploração Racional:**
  * Perguntas gerais da base: `graphify query "<pergunta>"` (requer `graphify-out/graph.json`).
  * Relacionamentos: `graphify path "<Origem>" "<Destino>"`.
  * Conceitos isolados: `graphify explain "<conceito>"`.
  * Navegação ampla: priorize `graphify-out/wiki/index.md` em vez de varreduras manuais no código-fonte.
* **Manutenção Contínua:** Após alterar código estrutural, execute `graphify update .` a partir da raiz do monorepo para atualizar a AST.
