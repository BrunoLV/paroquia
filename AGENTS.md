# AGENTS.md - Monorepo Root Engineering Governance & Standards

## Monorepo Architecture & Rule Delegation
- **Consciência de Contexto (Context Awareness):** Este repositório é um Monorepo. Antes de modificar, refatorar ou introduzir código dentro de um subdiretório específico de uma aplicação, o agente DEVE ABSOLUTAMENTE ler, respeitar e aplicar estritamente as regras de governança locais encontradas dentro da pasta do projeto filho (ex: `gestao-paroquia/` para o backend Java ou `gestao-paroquia-ui/` para o frontend React).
- **Sem Contaminação Cruzada (No Cross-Contamination):** Nunca importe componentes internos da aplicação, tipos ou lógica de negócio entre projetos filhos, a menos que o compartilhamento seja feito explicitamente por meio de um pacote de workspace comum/compartilhado (shared) designado.
- **Isolamento de Dependências:** As dependências em nível de raiz (root-level) devem gerenciar estritamente o ferramental do monorepo, linting ou configurações globais de build. As dependências específicas da aplicação pertencem exclusivamente aos arquivos de configuração de seus respectivos projetos filhos (ex: `build.gradle` no backend e `package.json` no frontend).

## Generalized Engineering Standards & Git Hygiene
- **Commits Atômicos:** As alterações de código devem ser divididas em commits pequenos e logicamente atômicos. Um único commit deve abordar exatamente uma intenção (ex: uma correção de bug, uma adição de funcionalidade discreta ou uma refatoração isolada).
- **Mandato de Commits Convencionais (Conventional Commits):** Cada mensagem de commit escrita pelo agente deve aderir estritamente à especificação de Conventional Commits. Estruture todas as mensagens da seguinte forma:
  - `feat(scope): description` para novas capacidades/funcionalidades.
  - `fix(scope): description` para correções de bugs.
  - `test(scope): description` para adição/modificação de testes.
  - `docs(scope): description` para atualizações de documentação.
  - `refactor(scope): description` para alterações estruturais de código que preservam o comportamento.
- **Estratégia de Branches:** O trabalho deve ser sempre realizado em branches de funcionalidade (feature branches) de curta duração, derivadas da branch principal de integração. Nunca envie código diretamente para as branches principais de produção ou main.

## Convenção de Nomenclatura: Domínio em Português, Técnico em Inglês
- **Regra:** Conceitos de domínio (nomes de entidades, enums, campos, DTOs, use cases que descrevem uma ação de negócio) DEVEM ser nomeados em português. Termos puramente técnicos/arquiteturais (sufixos como `Entity`, `Controller`, `Repository`, `Service`, `UseCase`, `DTO`, `Exception`, `Policy`, `Event`/`Listener` como padrão técnico, `Enum`, `Cache`) permanecem em inglês. Exemplo já consolidado no backend: `AtivoAlocacaoEntity`, `EventoController`, `VinculoResponse` (não `MembershipResponse`). No frontend, o mesmo padrão vale para tipos/DTOs/identificadores (ex.: `VinculoResponse`, não `MembershipResponse`).
- **Strings mágicas:** Prefira enum ou constante nomeada a string literal usada como marcador em comparações (`.equals("PENDENTE")`) ou `switch`/`case`. Quando já existir um enum para o conceito (`AprovacaoStatus`, `ProjetoStatus`, `EventoStatusInput`, etc.), reutilize-o via `Enum.CONSTANTE.name()` em vez de repetir o literal.

## Unified Quality & Monorepo Validation Gates
- **Verificação Abrangente:** Antes de finalizar qualquer tarefa, o agente deve executar os comandos de validação em todos os projetos filhos afetados para garantir um estado de zero regressão.
- **Higiene Pré-Commit:** O agente deve pré-avaliar a sintaxe e a formatação usando as configurações de ferramentas locais. Nenhum código com erros de lint, avisos de análise estática (Checkstyle, PMD, SpotBugs, ESLint) ou estados de compilação quebrados (`tsc`, Gradle build) pode ser proposto.
- **Proposição Estrita de Imports & Proibição de Wildcards (No Inline FQNs / No Star Imports):** É estritamente proibida a utilização de FQN inline imports (ex: `br.com.nsfatima...LiturgicalEngine`) e de importações com caracteres coringa (ex: `import java.util.*` ou `import static ...*`) no código. Todos os tipos e membros devem obrigatoriamente ser declarados através de instruções `import` explícitas no topo do arquivo. Esta regra aplica-se rigorosamente a todo código produtivo (`src/main`) e código de testes (`src/test`) e é checada via Checkstyle (`IllegalTokenText` e `AvoidStarImport`).
- **Alinhamento Global com o Framework TDD:** Em todo o monorepo, o Desenvolvimento Orientado a Testes (TDD) continua sendo a base absoluta da segurança do código. O agente deve entregar sistematicamente as especificações de teste (camadas de Unidade, Integração e E2E, conforme ditado pelas diretrizes dos projetos filhos) juntamente com ou antes das alterações de produção.

## Graphify Navigation & Token Optimization Mandate
- **Obrigatoriedade do Graphify:** A pasta `graphify-out/` contendo os grafos de conhecimento, AST e dependências da aplicação está disponível na raiz. Todos os agentes de IA que atuarem neste repositório DEVEM obrigatoriamente priorizar a consulta aos artefatos do `graphify` (como o cache semântico em `graphify-out/cache/semantic/`) para compreender a arquitetura e rastrear dependências antes de efetuar buscas manuais cegas.
- **Navegação Inteligente & Economia de Tokens:** É estritamente proibido realizar varredura cega ou leitura em massa de múltiplos arquivos desnecessários. O agente deve obrigatoriamente utilizar o conhecimento estruturado do grafo para ir diretamente aos arquivos afetados e trechos específicos de código, otimizando o consumo de tokens e garantindo precisão contextual.

## Otimização de Janela de Contexto
- **Consulta Prévia Mandatória:** Sempre consulte a estrutura analítica gerada ou execute buscas semânticas locais em `graphify-out/` antes de abrir múltiplos arquivos de código.
- **Eficiência de Tokens:** Nunca consuma tokens carregando arquivos inteiros se a relação mapeada exigir modificações em apenas algumas linhas isoladas.

## Mandatory Project Skills Consultation & Best Practices Alignment
- **Consulta Obrigatória de Diretrizes:** Todo agente de IA DEVE obrigatoriamente consultar os guias de estilo e padrões arquiteturais localizados nos diretórios de governança de cada módulo antes de arquitetar, implementar ou refatorar qualquer código, garantindo alinhamento estrito com as melhores práticas adotadas no projeto.
- **Diretrizes Backend (`gestao-paroquia/conductor/code_styleguides/` & `gestao-paroquia/docs/`):**
  - **DDD & Arquitetura Limpa:** Modelagem rica de domínio, agregados, Value Objects, bounded contexts e separação rígida de portas/adaptadores (conforme visto na estrutura estruturada em `api/v1/controller`, `application/usecase`, `domain/model` e `infrastructure/persistence`).
  - **Java Moderno:** Padrões modernos de desenvolvimento Java, uso de records, imutabilidade e type-safety (validado via SpotBugs, PMD e Checkstyle configurados em `gestao-paroquia/config/`).
  - **TDD Rigoroso:** Ciclo Red-Green-Refactor com suite completa de testes unitários, de integração e de contratos (localizados em `src/test/java/br/com/nsfatima/gestao/`).
  - **Database Governance:** Controle estrito de migrações via Flyway (armazenadas em `db/migration/`) e consultas SQL de alta performance.
- **Diretrizes Frontend (`gestao-paroquia-ui/conductor/code_styleguides/`):**
  - **React Frontend:** Padrões de React, custom hooks, TypeScript estrito, arquitetura de componentes reutilizáveis (MUI, TanStack Query) e cobertura de testes robusta usando Vitest (conforme mapeado na árvore de `src/components/`, `src/hooks/` e `src/context/`).

## Security, Privacy & Global Constraints
- **Isolamento de Privacidade Absoluto (Absolute Privacy Isolation):** Todo este monorepo manipula dados e regras de negócio eclesiais internos e sensíveis. O agente deve se reportar e aderir estritamente às diretrizes de privacidade e LGPD mapeadas em `gestao-paroquia/docs/ia/` antes de escrever estruturas de dados, registrar eventos de log ou estruturar respostas de rede. A prevenção contra o vazamento de informações de identificação pessoal (PII) é aplicada globalmente em todo o código produtivo.
- **Rastreabilidade de Alterações:** Toda modificação histórica e refinamento técnico deve ser documentada nos respectivos trackers de progresso dentro de `conductor/tracks/` e nas pastas `conductor/` locais das aplicações.