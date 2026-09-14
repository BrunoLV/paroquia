# AGENTS.md - Backend Engineering Standards & Guidelines (`gestao-paroquia/`)

Este documento define as regras Mandatórias, Invariantes e Diretrizes Técnicas para qualquer agente de IA ou desenvolvedor atuando no subprojeto de backend (`gestao-paroquia/`).

---

## 1. Diretrizes Fundamentais de Execução

* **Comunicação e Explicações:** Execute estritamente no estilo `caveman`. Respostas e explicações técnicas devem ser curtas, assertivas, sem introduções, saudações ou texto conversacional.
* **Qualidade e Estilo de Código:** Execute estritamente no estilo `ponytail`. O código deve ser de nível de produção sênior, com tipagem forte, imutável, limpo e com zero warnings.
* **Documentação Viva (*Living Documentation*):**
  * **Classes, Records e Interfaces:** Devem conter Javadoc conceitual explicando o **porquê** da regra de negócio (Linguagem Ubíqua) e as decisões de design (ex.: ADT, Strategy, OCP, LSP).
  * **Proibição de Comentários Triviais:** É expressamente proibido inserir comentários óbvios ou mecânicos (`// construtor`, `// getter`, `// injeta dependência`).

---

## 2. Governança Global de Bounded Contexts & Arquitetura Hexagonal

O backend opera sob uma arquitetura de monólito modular regido por princípios agnósticos de **Domain-Driven Design (DDD)** e **Clean/Hexagonal Architecture**:

* **Autonomia Modular dos Contextos:**
  * Cada contexto de negócio deve ser tratado como um subsistema autônomo com fronteiras explícitas.
  * **PROIBIDO** o acoplamento direto de esquemas de persistência, modelos JPA ou tabelas compartilhadas entre contextos distintos.
  * A comunicação entre contextos distintos **DEVE** ocorrer exclusivamente por:
    1. **Contratos de Aplicação / Interfaces Públicas de Módulo**;
    2. **Eventos de Domínio / Mensageria Interna Assíncrona**;
    3. **Anti-Corruption Layer (ACL):** Qualquer dependência em modelo externo exige adaptadores e mappers traduzindo para conceitos do domínio local.
* **Topologia Interna Canônica Obrigatória (por Módulo/Contexto):**
  Todo e qualquer pacote de contexto delimitado deve aderir à seguinte organização estrita de camadas:

```
/
├── domain/                     <-- CAMADA PURA DE NEGÓCIO (Livre de frameworks)
│   ├── model/                  <-- Entidades, VOs (records), Raízes de Agregado, IDs Tipados
│   ├── repository/             <-- Contratos puros de persistência (Portas de Saída)
│   ├── service/                <-- Domain Services (regras stateless que cruzam entidades)
│   ├── specification/          <-- Regras de negócio encapsuladas e reutilizáveis
│   ├── event/                  <-- Eventos de Domínio imutáveis (records)
│   └── exception/              <-- Exceções semânticas de negócio (DomainException)
│
├── application/                <-- ORQUESTRAÇÃO DE FLUXOS E CASOS DE USO
│   ├── usecase/                <-- Input Ports / Serviços de Aplicação
│   ├── dto/                    <-- Command, Query e Result DTOs imutáveis (records)
│   └── saga/                   <-- Process Managers / Orquestração de múltiplos agregados
│
└── infrastructure/             <-- DETALHES DE TECNOLOGIA E ADAPTADORES
    ├── configuration/          <-- @Configuration Spring (registro de Domain Services como Beans)
    ├── db/                     <-- Entidades JPA (@Entity), Spring Data Repositories e Mappers
    ├── acl/                    <-- Adaptadores e tradutores para outros contextos ou APIs externas
    ├── messaging/              <-- Listeners e Dispatchers de eventos
    └── rest/                   <-- Controllers HTTP (@RestController), DTOs de transporte e OpenAPI
```

---

## 3. Matriz de Ativação de Skills do Agente (`.agents/skills/`)

Antes de iniciar qualquer intervenção, o agente DEVE consultar e respeitar os manuais de skills e seus respectivos checklists:

| Escopo da Tarefa | Skills Mandatórias | Invariantes Críticas |
| :--- | :--- | :--- |
| **Modelagem de Domínio & Casos de Uso** | `ddd.md`<br>`solid.md`<br>`java.md` | • Domínio puro (zero anotações Spring/JPA em `domain/`).<br>• Identificadores fortemente tipados com `record` (ex.: `record EntidadeId(UUID value)`).<br>• Relacionamentos entre agregados distintos exclusivamente por ID.<br>• Mutação limitada a um único agregado por transação.<br>• Modelagem via Sealed Interfaces e Pattern Matching sem `default`.<br>• **Proibição absoluta de Modelo Anêmico:** zero setters públicos em Agregados/Entidades de domínio; o estado muda apenas por métodos da Linguagem Ubíqua. `domain/model/` nunca contém apenas enums. Ver §5.1. |
| **Implementação Java 25 Geral** | `java.md`<br>`solid.md` | • Imutabilidade por padrão (`record` com construtor compacto de validação).<br>• Proibido `float`/`double` para precisão decimal; use `BigDecimal` com `RoundingMode`.<br>• Virtual Threads em I/O sem `synchronized` (anti-pinning; use `ReentrantLock`).<br>• `Optional` apenas para retornos de métodos; coleções retornam vazias, nunca `null`. |
| **Testes Unitários & TDD** | `tdd.md` | • Precedência TDD: Ciclo Red-Green-Refactor estrito.<br>• JUnit 5 + Mockito 5 (`STRICT_STUBS`) + AssertJ fluente.<br>• Isolamento: proibido `@SpringBootTest` ou I/O real em testes unitários.<br>• Zero código defensivo ou de produção sem um teste falho que o exija. |
| **Persistência, SQL & Migrações** | `flyway.md`<br>`sql.md` | • Migrações em `src/main/resources/db/migration/V{NNN}__{descricao}.sql`.<br>• Imutabilidade de migrações commitadas.<br>• Padrão Zero-Downtime: novas colunas como `NULLable` ou com `DEFAULT`.<br>• Proibição de `SELECT *`; constraints nomeadas e `COMMENT ON` mandatório. |
| **Segurança e Privacidade (LGPD)** | `lgpd-privacy-by-design.md` | • Regras uniformes de proteção para qualquer perfil de usuário.<br>• Zero PII em logs, exceções e rastros de pilha.<br>• Criptografia em repouso de dados civis sensíveis.<br>• DTOs segregados; proteção obrigatória contra IDOR via escopo em queries. |
| **Build e Automação** | `gradle.md` | • Kotlin DSL (`.gradle.kts`) exclusivo.<br>• Dependências declaradas exclusivamente em `gradle/libs.versions.toml`.<br>• Java 25 Toolchain e Lazy API universal em tarefas customizadas. |

---

## 4. Diretrizes de Segurança, Autorização e Acesso

* **Segregação de Identidade e Acesso:** Mecanismos de autenticação e tokens JWT cuidam exclusivamente de identidade e credenciais. Papéis, permissões de domínio e vínculos cadastrais residem em suas respectivas fronteiras de negócio.
* **Autorização Centralizada e Declarativa:**
  * Proibido checar regras de autorização inline espalhadas em Controllers ou UseCases.
  * Consuma os serviços especializados de autorização da arquitetura antes de executar a lógica de negócio.
* **Prevenção contra IDOR:** Consultas de busca por identificador devem aplicar escopo institucional/perfil no predicado da busca (`WHERE id = :id AND tenant_id = :currentTenant`).

---

## 5. Padrões de Código e Tipagem Estrita (Java 25)

* **Convenção de Nomenclatura:**
  * Conceitos de domínio, regras e testes: **Português** (Linguagem Ubíqua).
  * Termos técnicos e padrões arquiteturais: **Inglês** (`UseCase`, `Repository`, `Controller`, `Adapter`, `Factory`, `Strategy`).
  * **PROIBIDO** o uso de termos genéricos/vagos: `Data`, `Handler`, `Manager`, `Helper`, `Utility`, bem como o sufixo artificial `*Impl` sem propósito polimórfico comprovado.
  * **Comentários (Javadoc e inline):** SEMPRE em **Português**, independentemente do idioma dos identificadores técnicos ao redor (ex.: um comentário dentro de um método de um `Repository` em inglês ainda deve ser escrito em português).
* **Injeção de Dependências:**
  * Exclusivamente via **construtor com propriedades `private final`**.
  * **ESTRITAMENTE PROIBIDO** o uso de `@Autowired` em campos (*field injection*).
* **Limites de Complexidade:**
  * Arquivos de classe/interface: Máximo de **500 linhas**.
  * Métodos: Máximo de **20 linhas** (decompor métodos que excederem).

### 5.1. REGRAS TÁTICAS DE MODELAGEM: MODELO DE DOMÍNIO RICO (DDD)

* **Erradicação de Anemia:** É PROIBIDO expor setters públicos ou construtores de cópia
  cega (`update(campo1, ..., campoN)`) em Agregados, Raízes de Agregado e Entidades de
  domínio. Toda transição de estado ocorre por um método nomeado na Linguagem Ubíqua
  (`aprovar`, `fechar`, `inativar`), que valida as invariantes internamente antes de mutar.
  Invariante declarada em Javadoc mas enforçada só em Bean Validation de DTO ou em `if` de
  UseCase/Controller é anemia — a regra volta para o domínio.
* **Tell, Don't Ask:** O orquestrador (UseCase/Controller) não lê o estado do agregado para
  decidir a mutação. `if (a.getStatus()==PENDENTE) a.setStatus(APROVADA)` vira `a.aprovar(...)`.
  A guarda de estado e a exceção de transição ilegal pertencem ao agregado.
* **Always-Valid Entity:** O construtor/fábrica rejeita qualquer instância que viole uma
  invariante. Não existe agregado em estado inconsistente esperando validação externa.
* **Obsessão por Primitivos:** `String`/`UUID`/`LocalDate`/`int` que carregam regra viram
  Value Object imutável (`record` com construtor compacto de validação): `Email`, `Cpf`,
  `Cep`, `Endereco`, `IntervaloTemporal`, `Money`. Referência a outro agregado é ID tipado,
  nunca `UUID` cru.
* **Encapsulamento de Coleções:** getters de agregado retornam `List.copyOf(...)` /
  `Collections.unmodifiableList(...)` ou um VO-coleção; nunca a referência interna mutável.
  A coleção só muda por métodos da raiz (`adicionarItem`, `removerItem`).
* **Raiz de Agregado:** Agregados com ciclo de vida e eventos estendem
  `support.domain.AggregateRoot<ID>`, registram `DomainEvent` internamente e são drenados
  pela camada de aplicação via `PublicadorEventosDominio.publicar(raiz)` após o `save`.

---

## 6. Higiene de Código, Qualidade e Governança

### 6.1. Policies versus Services

* **Policies:** Toda variação de cálculo, autorização, seleção ou decisão por tipo/perfil/contexto deve ser uma interface `*Policy` pura, stateless e sem I/O em `domain/policy/` ou `domain/<aggregate>/policy/`.
* **Services:** `*Service` não contém Strategy disfarçada por `if/else` ou `switch/case`; Application Services orquestram portas e Domain Services apenas coordenam conceitos de domínio sem variação algorítmica.
* **Tell, Don't Ask:** Policy retorna uma decisão/VO; a entidade ou agregado executa sua própria mutação por método da Linguagem Ubíqua. Policies não recebem JPA entities, repositórios, DTOs REST, `SecurityContext` ou anotações Spring.

* **Zero Warnings:** O código entregue deve compilar e passar com zero avisos nas ferramentas de verificação estática (`./gradlew check`).
* **Higiene de Importações:**
  * Proibido o uso de caracteres coringa (*wildcards* / `import java.util.*`).
  * **PROIBIDO USO DE IMPORTS INLINE OU NOMES TOTALMENTE QUALIFICADOS (FQCNs):** Todo tipo utilizado no corpo da classe (atributos, parâmetros, variáveis, retornos, chamadas estáticas, casts, exceções em `catch` ou anotações como `@ExceptionHandler`) **DEVE** ser importado explicitamente no cabeçalho do arquivo via `import`. Nunca use referências como `java.util.UUID`, `java.util.stream.Collectors` ou pacotes internos inline no código.
* **Documentação Estrutural:**
  * Todo pacote de módulo/camada deve conter seu respectivo `package-info.java`.
  * Endpoints REST devem expor contratos tipados e descritos via anotações OpenAPI (Swagger).

---

## 7. Padrões de Controllers REST e Exception Handling

* **Controllers REST:**
  * **Anotações OpenAPI Obrigatórias:** `@Tag(name = ..., description = ...)` no nível da classe; `@Operation(summary = ..., description = ...)`, `@ApiResponses` com `@ApiResponse` para cada código HTTP e `@Parameter` para parâmetros de rota/query.
  * **Segurança Declarativa:** Obrigatório `@PreAuthorize` no nível da classe ou método (ex.: `@PreAuthorize("isAuthenticated()")`).
  * **Verbos HTTP Semânticos:** Proibido `@RequestMapping(method = ...)` genérico; use mapeamentos explícitos (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`).
  * **Zero `@ExceptionHandler` Inline:** É terminantemente proibido declarar métodos `@ExceptionHandler` dentro de classes `@RestController`.
* **Exception Handling por Bounded Context:**
  * **Um `@RestControllerAdvice` por Bounded Context:** Todo módulo deve possuir seu próprio advice (ex.: `PatrimonioExceptionHandler`, `ReservaExceptionHandler`, `AprovacaoExceptionHandler`) localizado em `infrastructure/rest/`.
  * **Herança Mandatória:** Todo advice de módulo **DEVE** herdar de `BaseExceptionHandler` e anotar-se com `@Order(2)` (ou prioridade mais específica `@Order(3)`).
  * **Formato de Resposta Unificado:** Exceções capturadas devem retornar `ResponseEntity<ValidationErrorResponse>` consumindo o catálogo `ErrorCodes`.
  * **Handlers Globais:** O `GlobalDomainExceptionHandler` em `support` é reservado apenas como fallback para módulos transversais legados sem advice dedicado. Nunca sobrecarregue o global com regras de Bounded Contexts estruturados.

---

## 8. Referência de Comandos

### Graphify (Executar a partir de `gestao-paroquia/`)
* Consultar base de código: `graphify query "<pergunta>"`
* Rastrear dependências: `graphify path "<ClasseA>" "<ClasseB>"`
* Explicar conceito/módulo: `graphify explain "<conceito>"`
* Atualizar AST após alterar código: `graphify update .`

### Gradle Wrapper (Executar a partir de `gestao-paroquia/`)
* Validação estrita de qualidade (SpotBugs, Checkstyle, PMD): `./gradlew check`
* Compilação completa do backend: `./gradlew build`
* Executar suíte de testes unitários: `./gradlew test`
* Executar teste específico: `./gradlew test --tests "br.com.nsfatima.gestao.<modulo>.<NomeTest>"`
* Executar aplicação localmente: `./gradlew bootRun`
* Executar esteira completa de testes E2E: `./demo_mvp.sh`
