# AGENTS.md - Backend Engineering Standards & Guidelines

## Domain-Driven Design (DDD) Core Principles
- **Padrões Orientados ao Domínio:** Siga estritamente os princípios puros de Domain-Driven Design para modelar as capacidades de negócio.
- **Linguagem Ubíqua:** Os elementos de código (classes, métodos, variáveis, schemas de banco de dados) devem espelhar exatamente a linguagem do domínio de negócio. Nunca use jargões técnicos onde exista um termo do domínio.
- **Contextos Delimitados (Bounded Contexts):** Mantenha uma separação rigorosa entre contextos de domínio distintos. Não vaze conceitos, entidades ou estruturas de banco de dados de um contexto delimitado para outro.
- **Modelo de Domínio Rico sobre Membros Anêmicos:** As entidades devem conter tanto dados quanto comportamento. Regras de negócio, transições de estado e validações de domínio pertencem ao interior das Entidades de Domínio e Agregados, NUNCA vazadas para os Services.
- **Agregados e Raízes (Roots):** Desenhe Raízes de Agregados claras para manter os limites transacionais e invariantes. Referencie agregados externos estritamente por Identidade (ID), nunca mantendo referências diretas a objetos.
- **Objetos de Valor (Value Objects):** Sempre encapsule atributos que não possuem identidade conceitual em Objetos de Valor imutáveis (implementados como Java `record`). Exemplos: Money, Address, Email.

## Data Privacy & Compliance (Mandato da LGPD)
- **Roteamento de Compliance Estrito:** Este projeto manipula dados pessoais e é apenas para uso interno. O agente DEVE ABSOLUTAMENTE ler, internalizar e aplicar estritamente os limites de privacidade e segurança definidos no arquivo `docs/ia/regras_duras_lgpd.md` antes de autorar, refatorar ou expor qualquer código, contrato de dados ou log interno.

## Otimização de Janela de Contexto
- **Consulta Prévia Mandatória:** Sempre consulte `graphify-out/graph.json` ou execute buscas semânticas locais antes de abrir múltiplos arquivos de código.
- **Eficiência de Tokens:** Nunca consuma tokens carregando arquivos inteiros se a relação mapeada pelo Graphify exigir modificações em apenas algumas linhas isoladas.

## Agent Capabilities, Custom Skills & Graphify Mandate
- **Exploração da Base de Código via Graphify:** A pasta `graphify-out/` localizada na raiz do monorepo contém os grafos de conhecimento e dependências de todo o projeto. O agente DEVE obrigatoriamente consultar `graphify-out/GRAPH_REPORT.md` e os arquivos de grafo antes de explorar classes, pacotes ou repositories do backend via buscas manuais.
- **Economia de Tokens:** É proibida a leitura em lote de múltiplos arquivos fonte inteiros quando a estrutura de dependências puder ser obtida diretamente via consulta aos artefatos do Graphify, garantindo economia de tokens e navegação precisa.
- **Alinhamento Obrigatório de Skills do Projeto:** O agente DEVE obrigatoriamente consultar e aplicar as diretrizes contidas nos guias de skill em `.agents/skills/` (`ddd.md`, `java.md`, `solid.md`, `tdd.md`, `tdd_2.md`, `flyway.md`, `sql.md` e `gradle.md`) antes de gerar ou refatorar qualquer código no backend.

## Estilo de Código & Arquitetura (Java & Segurança de Tipos)
- **Tipos sobre Strings:** Sempre prefira Java `enum`, `sealed classes` ou wrappers de objetos fortemente tipados (Objetos de Valor) em vez de `String` pura para representar estados, categorias ou domínios fixos.
- **Tipagem Estrita:** Nunca use tipos brutos (raw types), coleções não tipadas ou mapas genéricos de `Object` para dados estruturais. Use Genéricos precisos e `java.util.Optional` em vez de retornar `null`.
- **Carregadores de Dados Imutáveis:** Use Java `record` para Objetos de Valor, DTOs e payloads de eventos para garantir a imutabilidade por padrão.
- **Restrições de Funções:** Mantenha os métodos entre 4 e 20 linhas. Se um método exceder 20 linhas, extraia os blocos coesos para métodos auxiliares privados (private helper methods).
- **Limites de Arquivo:** Mantenha os arquivos abaixo de 500 linhas. Divida as classes pelo Princípio da Responsabilidade Única (SRP) quando elas crescerem além desse limite.
- **Convenções de Nomenclatura:** Os nomes devem ser específicos, únicos e orientados ao domínio. Termos ABSOLUTAMENTE PROIBIDOS: `Data`, `Handler`, `Manager`, `Helper`, `Utility`. Prefira nomes precisos que retornem menos de 5 resultados de grep na base de código.
- **Fluxo de Controle:** Prefira retornos antecipados (early returns) e cláusulas de guarda (guard clauses) em vez de blocos `if-else` aninhados. A profundidade máxima de indentação permitida é de 2 níveis dentro de qualquer bloco.
- **Dry & Extract:** Zero duplicação de código permitida. Extraia a lógica compartilhada em componentes reutilizáveis e testáveis ou em funções puras.
- **Exceções Expressivas:** Exceções customizadas devem ser não checadas (`RuntimeException`). As mensagens de exceção devem incluir explicitamente o valor incorreto em tempo de execução, o contexto e o limite/formato esperado.

## Análise Estática & Linters (Quality Gates)
- **Conformidade com Ferramentas:** A base de código impõe rigidamente portões de qualidade (quality gates) via **PMD**, **SpotBugs** e **Checkstyle**. Todo código gerado ou refatorado deve estar em total conformidade com as configurações dessas ferramentas.
- **Política de Zero Avisos:** Nenhum aviso (warning), code smell ou violação de estilo deve ser introduzido. O agente deve verificar se o código não dispara violações de regras do PMD (ex: otimização, propensão a erros), alertas do SpotBugs (ex: riscos de ponteiro nulo, má práticas) ou erros de layout do Checkstyle antes de finalizar as alterações.

## Arquitetura de Documentação
- **Documentação Viva:** Cada mudança arquitetural não trivial, introdução de padrão estrutural ou grande escolha de integração DEVE ser documentada por meio de um **Registro de Decisão Arquitetural (ADR)** em formato Markdown no local designado do repositório.
- **Documentação de API e Contratos:** Os contratos de API pública devem ser totalmente decorados com anotações OpenAPI/Swagger (`@Operation`, `@ApiResponse`) para produzir documentação de API exaustiva, precisa e interativa automaticamente em tempo de execução.
- **Javadoc Público:** Cada interface pública, controller, classe de configuração e método de serviço deve ter um bloco Javadoc declarando sua intenção primária, contexto de negócio e exatamente um exemplo de uso válido.
- **Documentação de Domínio & Fluxo:** Regras de negócio complexas, sagas entre agregados e máquinas de estado devem ser explicitamente documentadas usando diagramas Mermaid.js incorporados diretamente nos arquivos Markdown de acompanhamento ou nos Javadocs em nível de pacote.
- **Rastreabilidade:** Referencie IDs de rastreamento de issues ou SHAs de commit diretamente nos comentários quando uma linha de código existir unicamente para corrigir um bug upstream ou um caso de borda de terceiros.
- **Intenção sobre Mechanism:** Os comentários dentro do código devem explicar o **PORQUÊ**, não o **O QUÊ**. Não comente códigos autoexplicativos (ex: pule `// incrementa o contador` acima de `i++`). Explique a restrição de negócio ou a razão arquitetural por trás de uma lógica não óbvia.
- **Preservar o Contexto:** Retenha os comentários existentes durante a refatoração; eles carregam intenção histórica e proveniência.

## Importações & Higiene de Pacotes
- **Sem Nomes Totalmente Qualificados (No Inline FQNs):** NUNCA use nomes de classes totalmente qualificados em linha (ex: `com.example.domain.MyClass`) dentro do corpo do código. Aplicado estritamente tanto ao código produtivo (`src/main`) quanto ao código de testes (`src/test`) e checado via Checkstyle (`IllegalTokenText`).
- **Importações Limpas no Topo:** SEMPRE use importações explícitas no topo (top-level) no início do arquivo.
- **Sem Caracteres Coringa (Proibição Absoluta de Wildcards/Star Imports):** NÃO use importações com caracteres coringa (ex: `import java.util.*` ou `import static org.mockito.Mockito.*`). Importe cada classe ou membro estático explicitamente. Aplicado estritamente tanto em `src/main` quanto em `src/test` e checado via Checkstyle (`AvoidStarImport` com `allowClassImports=false` e `allowStaticMemberImports=false`).

## Tratamento de Erros & Resiliência
- **Validação Fail-Fast:** Valide as entradas na fronteira (ex: Controllers, Event Listeners) usando Jakarta Validation (`@NotNull`, `@Valid`) ou `Objects.requireNonNull()` explícito antes de passar os dados para os serviços core.
- **Captura Específica:** Nunca capture `Throwable` ou `Exception` genéricos, a menos que esteja construindo explicitamente um manipulador de erros global de fallback. Capture a subclasse mais específica possível.

## Suíte de Testes & Mandato de TDD
- **Aplicação Estrita de TDD:** O Desenvolvimento Orientado a Testes (TDD) é a base absoluta da segurança do código neste projeto. Você DEVE escrever o teste antes de escrever qualquer código de produção (ciclo Red-Green-Refactor). Nenhuma alteração no código de produção é permitida sem uma alteração prévia no teste.
- **Camadas Obrigatórias da Suíte de Testes:** Para cada nova funcionalidade ou comportamento de domínio, o agente DEVE implementar três camadas distintas de teste:
  1. **Testes Unitários:** Testes rápidos e isolados para a lógica central do domínio, Objetos de Valor e invariantes de Agregados. I/O externo deve ser mockado ou simulado (faked).
  2. **Testes de Integração:** Testes que verificam a interação entre componentes e fronteiras de infraestrutura (ex: persistência em banco de dados com `@DataJpaTest`, fatias de contexto do Spring, contratos de API).
  3. **Testes de Ponta a Ponta (E2E):** Testes de fatia completa que atravessam toda a stack da aplicação, desde o ponto de entrada público (ex: requisição HTTP usando `@SpringBootTest` + `WebTestClient`) até o banco de dados/integrações externas, provando que o sistema funciona como um todo coeso.
- **Execução:** Todos os testes em todos os níveis devem rodar perfeitamente através de um único comando na raiz: `./gradlew test`.
- **Isolamento Estrito de I/O no Nível Unitário:** Mocke as fronteiras de I/O externo (APIs REST, Bancos de Dados, Sistemas de Arquivos, Relógios). Prefira simuladores de teste nomeados (`FakeUserRepository`) ou mocks do Mockito. Nunca permita que um teste unitário realize atividades reais de rede ou disco.
- **Princípios F.I.R.S.T.:** Os testes devem ser Rápidos (Fast), Independentes (Independent), Repetíveis (Repeatable), Auto-validáveis (Self-Validating) e Oportunos/Na hora certa (Timely).

## Gerenciamento de Dependências & Desacoplamento
- **Injeção de Dependência:** Injete todas as dependências explicitamente via injeção por construtor. Evite `@Autowired` em atributos para garantir a testabilidade da classe sem a necessidade de orquestração do contexto do Spring.
- **Camada Anticorrupção (ACL):** Envolva bibliotecas de terceiros e SDKs externos atrás de uma interface fina de propriedade deste projeto. Não vaze modelos de bibliotecas externas para a camada core do domínio.

## Framework & Estrutura (Spring Boot & Arquitetura Limpa)
- **Camadas Estratégicas:** Isole o Domínio Core da Infraestrutura. Siga um layout de arquitetura limpa do tipo Portas e Adaptadores (Hexagonal) ou Onion:
  - `domain/model`: Agregados, Entidades, Objetos de Valor e Eventos de Domínio. (Zero dependências de frameworks).
  - `domain/service`: Serviços de domínio puros que lidam com operações que abrangem múltiplos agregados.
  - `application`: Casos de uso, handlers de CQRS e serviços de aplicação que coordenam transações.
  - `infrastructure`: Spring Controllers, Repositories, Mappers de Banco de Dados e clientes de API externa.
- **Spring Boot Idiomático:** Siga estritamente as convenções padrão do Spring Boot dentro da camada de infraestrutura, garantindo que ela implemente as interfaces (Portas) definidas pela camada de domínio.
- **Componentes Granulares:** Prefira classes Spring `@Component` pequenas e altamente focadas em vez de "Serviços Deuses" (God Services) inflados.

## Observabilidade & Formatação
- **Log Estruturado:** Use SLF4J/Logback para emitir o formato JSON estruturado para os logs da aplicação (info, warn, error).
- **Logs Contextuais:** Inclua identificadores de rastreamento relevantes (como MDC / Trace IDs) nos logs e sempre registre contextos que permitam ação, não apenas stack traces genéricos.
- **Zero Debates sobre Estilo:** Use a configuração padrão de formatador do projeto/IDE. Não altere ou debate estilos de formatação além das regras automatizadas do checkstyle.