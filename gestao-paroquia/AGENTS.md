# AGENTS.md - Backend Engineering Standards & Guidelines

Este documento define as regras Mandatórias e Invariantes de arquitetura, código, segurança e ferramentas para atuação no backend do **Projeto Paróquia** (`gestao-paroquia/`).

---

## 1. Diretrizes de Comportamento do Agente

* **Comunicação e Texto Explicativo:** Utilize a habilidade `caveman`. Respostas e explicações devem ser extremamente diretas, sem introduções, saudações ou texto conversacional.
* **Geração e Arquitetura de Código:** Utilize a habilidade `ponytail`. O código produzido deve ser estritamente de nível de produção, fortemente tipado, seguro, modular e limpo.
* **Separação de Papéis:** As habilidades `caveman` e `ponytail` são ferramentas de execução de estilo e qualidade, não personas operacionais.

---

## 2. Arquitetura e Contexto do Sistema

* **Stack Principal:** Java 25, Spring Boot 3, Gradle Wrapper.
* **Padrão Arquitetural:** Domain-Driven Design (DDD) e Arquitetura Hexagonal (Portas e Adaptadores).
* **Isolamento de Domínio:** O Domínio Core **DEVE** ser estritamente isolado da Infraestrutura e frameworks.
* **Contextos Delimitados (Bounded Contexts):** O sistema é dividido por módulos contextuais (`calendario`, `aprovacao`, `local`, `organizacao`, `membro`, `iam`, `fiel`).
* **Isolamento entre Contextos:** É **ESTRITAMENTE PROIBIDO** o acoplamento direto de esquemas de banco de dados ou entidades de infraestrutura entre contextos distintos.
* **Topologia Interna por Contexto:**
  * `domain/model`: Entidades, Objetos de Valor e regras puras sem dependência de frameworks.
  * `domain/service`: Serviços de domínio puramente de negócio.
  * `application/usecase`: Orquestração de casos de uso e gerenciamento transacional.
  * `infrastructure`: Controllers REST, Adapters JPA, Repositórios, DTOs e mapeadores.

---

## 3. Padrões de Segurança, Autorização e Infraestrutura

* **Segregação de Repositórios:** Interfaces de repositórios JPA **DEVEM** ser segregadas por responsabilidade e contrato, evitando interfaces monolíticas.
* **Autorização Centralizada:** O controle de acesso e verificação de perfil **NÃO DEVEM** ser feitos inline nos Controllers ou UseCases. **DEVE-SE** consumir estritamente os serviços dedicados em `src/main/java/.../auth/` (ex: `PapelGlobalAuthorizationService`).
* **Segregação do IAM:** Módulos do `iam/` gerenciam estritamente credenciais e autenticação. Dados cadastrais e de domínio **DEVEM** residir exclusivamente em seus respectivos contextos.

---

## 4. Governança de IA e Conformidade LGPD

* **Análise Prévia via Graphify:** **DEVE-SE** consultar os artefatos de AST em `../graphify-out/` antes de realizar buscas manuais no código para entender conexões e dependências.
* **Otimização de Leitura:** É **PROIBIDA** a leitura em lote de arquivos inteiros quando a estrutura puder ser obtida via consultas ao Graphify.
* **Proteção de Dados Pessoais (LGPD):**
  * **NÃO DEVE** haver exposição de PII em logs, tabelas de auditoria não criptografadas ou respostas REST expostas.
  * **DEVE-SE** cumprir rigorosamente as diretrizes contidas em `docs/ia/regras_duras_lgpd.md`.
* **Guia de Habilidades:** Consultar os manuais em `.agents/skills/` (ex: `ddd.md`, `java.md`, `tdd.md`) antes de efetuar refatorações estruturais no backend.

---

## 5. Padrões de Código e Tipagem Estrita

* **Nomenclatura:**
  * Conceitos de domínio, testes e variáveis locais **DEVEM** ser nomeados em Português.
  * Termos técnicos e arquiteturais **DEVEM** permanecer em Inglês (ex: `UseCase`, `Repository`, `Controller`, `Adapter`).
  * O uso de sufixos genéricos (`Data`, `Handler`, `Manager`, `Helper`, `Utility`) é **ESTRITAMENTE PROIBIDO**.
* **Imutabilidade e Tipagem:**
  * **DEVE-SE** utilizar Java `record` para Objetos de Valor e DTOs.
  * **DEVE-SE** utilizar `java.util.Optional` para representar ausência de valor em retornos de métodos.
  * **NÃO DEVE** ser utilizado tipo bruto (*raw type*) ou `String` livre para valores finitos; utilize `enum`.
* **Injeção de Dependências:**
  * **DEVE-SE** utilizar injeção via construtor.
  * O uso de `@Autowired` em atributos (*field injection*) é **ESTRITAMENTE PROIBIDO**.
* **Limites de Tamanho por Arquivo:**
  * Arquivos de classe/interface: Máximo 500 linhas.
  * Métodos: Mínimo 4 linhas, Máximo 20 linhas.
  * Arquivos ou métodos excedentes **DEVEM** ser decompostos.

---

## 6. Qualidade, Testes e Documentação

* **Garantia de Zero Warnings:** Todo o código produzido **NÃO DEVE** gerar nenhum aviso nos portões de qualidade (PMD, SpotBugs e Checkstyle).
* **Higiene de Código:**
  * **É PROIBIDO** o uso de nomes totalmente qualificados inline (FQNs) no meio do código.
  * **É PROIBIDO** o uso de importações com caracteres coringa (*star imports* / `import static ...*`).
* **Documentação Obrigatória:**
  * Todo pacote de contexto **DEVE** possuir um arquivo `package-info.java`.
  * Interfaces públicas e endpoints **DEVEM** conter documentação Swagger/OpenAPI.
  * Comentários **DEVEM** explicar a razão arquitetural/técnica (**PORQUÊ**) e **NÃO** a mecânica óbvia do código (**O QUÊ**).
* **Desenvolvimento Guiado por Testes (TDD):**
  * O ciclo **Red-Green-Refactor** é **MANDATÓRIO** para qualquer implementação.
  * Cobertura obrigatória para testes Unitários, de Integração e End-to-End (E2E).

---

## 7. Referência de Comandos e Ferramentas

### Graphify (Executar a partir de `gestao-paroquia/`)
* Consultar base de código: `graphify query "<pergunta>"`
* Rastrear dependências entre classes: `graphify path "<Símbolo_A>" "<Símbolo_B>"`
* Explicar conceito/módulo: `graphify explain "<conceito>"`
* Atualizar AST após modificações: `graphify update .`

### Gradle Wrapper (Executar a partir de `gestao-paroquia/`)
* Executar validações e portões de qualidade: `./gradlew check`
* Build completo do projeto: `./gradlew build`
* Executar todas as suítes de teste: `./gradlew test`
* Executar teste específico: `./gradlew test --tests "NomeDaClasseTest"`
* Executar aplicação localmente: `./gradlew bootRun`
* Executar bateria de testes E2E: `./demo_mvp.sh`