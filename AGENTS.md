# AGENTS.md - Monorepo Root Engineering Governance & Standards

## Monorepo Architecture & Rule Delegation
- **Consciência de Contexto (Context Awareness):** Este repositório é um Monorepo. Antes de modificar, refatorar ou introduzir código dentro de um subdiretório específico de uma aplicação, o agente DEVE ABSOLUTAMENTE ler, respeitar e aplicar estritamente o arquivo `AGENTS.md` local encontrado dentro da pasta desse projeto filho (ex: pastas de serviço backend ou frontend).
- **Sem Contaminação Cruzada (No Cross-Contamination):** Nunca importe componentes internos da aplicação, tipos ou lógica de negócio entre projetos filhos, a menos que o compartilhamento seja feito explicitamente por meio de um pacote de workspace comum/compartilhado (shared) designado.
- **Isolamento de Dependências:** As dependências em nível de raiz (root-level) devem gerenciar estritamente o ferramental do monorepo, linting ou configurações globais de build. As dependências específicas da aplicação pertencem exclusivamente aos arquivos de configuração de seus respectivos projetos filhos.

## Generalized Engineering Standards & Git Hygiene
- **Commits Atômicos:** As alterações de código devem ser divididas em commits pequenos e logicamente atômicos. Um único commit deve abordar exatamente uma intenção (ex: uma correção de bug, uma adição de funcionalidade discreta ou uma refatoração isolada).
- **Mandato de Commits Convencionais (Conventional Commits):** Cada mensagem de commit escrita pelo agente deve aderir estritamente à especificação de Conventional Commits. Estruture todas as mensagens da seguinte forma:
  - `feat(scope): description` para novas capacidades/funcionalidades.
  - `fix(scope): description` para correções de bugs.
  - `test(scope): description` para adição/modificação de testes.
  - `docs(scope): description` para atualizações de documentação.
  - `refactor(scope): description` para alterações estruturais de código que preservam o comportamento.
- **Estratégia de Branches:** O trabalho deve ser sempre realizado em branches de funcionalidade (feature branches) de curta duração, derivadas da branch principal de integração. Nunca envie código diretamente para as branches principais de produção ou main.

## Unified Quality & Monorepo Validation Gates
- **Verificação Abrangente:** Antes de finalizar qualquer tarefa, o agente deve executar os comandos de validação em todos os projetos filhos afetados para garantir um estado de zero regressão.
- **Higiene Pré-Commit:** O agente deve pré-avaliar a sintaxe e a formatação usando as configurações de ferramentas locais. Nenhum código com erros de lint, avisos de análise estática (Checkstyle, PMD, SpotBugs, ESLint) ou estados de compilação quebrados (`tsc`, Gradle build) pode ser proposto.
- **Proposição Estrita de Imports & Proibição de Wildcards (No Inline FQNs / No Star Imports):** É estritamente proibida a utilização de FQN inline imports (ex: `br.com.nsfatima...LiturgicalEngine`) e de importações com caracteres coringa (ex: `import java.util.*` ou `import static ...*`) no código. Todos os tipos e membros devem obrigatoriamente ser declarados através de instruções `import` explícitas no topo do arquivo. Esta regra aplica-se rigorosamente a todo código produtivo (`src/main`) e código de testes (`src/test`) e é checada via Checkstyle (`IllegalTokenText` e `AvoidStarImport`).
- **Alinhamento Global com o Framework TDD:** Em todo o monorepo, o Desenvolvimento Orientado a Testes (TDD) continua sendo a base absoluta da segurança do código. O agente deve entregar sistematicamente as especificações de teste (camadas de Unidade, Integração e E2E, conforme ditado pelas diretrizes dos projetos filhos) juntamente com ou antes das alterações de produção.

## Graphify Navigation & Token Optimization Mandate
- **Obrigatoriedade do Graphify:** A pasta `graphify-out/` contendo os grafos de conhecimento, AST e dependências da aplicação está disponível na raiz. Todos os agentes de IA que atuarem neste repositório DEVEM obrigatoriamente priorizar a consulta aos artefatos do `graphify` (`graphify-out/GRAPH_REPORT.md`, `graphify-out/graph.json`, detecção de comunidades, god nodes e análise de impacto) para compreender a arquitetura e rastrear dependências antes de efetuar buscas manuais cegas.
- **Navegação Inteligente & Economia de Tokens:** É estritamente proibido realizar varredura cega ou leitura em massa de múltiplos arquivos desnecessários (`view_file` em lote sem filtro prévio). O agente deve obrigatoriamente utilizar o conhecimento estruturado do grafo para ir diretamente aos arquivos afetados e trechos específicos de código, otimizando o consumo de tokens e garantindo precisão contextual.

## Otimização de Janela de Contexto
- **Consulta Prévia Mandatória:** Sempre consulte `graphify-out/graph.json` ou execute buscas semânticas locais antes de abrir múltiplos arquivos de código.
- **Eficiência de Tokens:** Nunca consuma tokens carregando arquivos inteiros se a relação mapeada pelo Graphify exigir modificações em apenas algumas linhas isoladas.

## Mandatory Project Skills Consultation & Best Practices Alignment
- **Consulta Obrigatória de Skills:** Todo agente de IA DEVE obrigatoriamente consultar os guias de skills localizados nas pastas `.agents/skills` de cada módulo (`gestao-paroquia/.agents/skills/` e `gestao-paroquia-ui/.agents/skills/`) antes de arquitetar, implementar ou refatorar qualquer código, garantindo alinhamento estrito com as melhores práticas adotadas no projeto.
- **Skills Backend (`gestao-paroquia/.agents/skills/`):**
  - `ddd.md`: Modelagem rica de domínio, agregados, Value Objects e bounded contexts.
  - `java.md`: Padrões Java 25, uso de records, imutabilidade e type-safety.
  - `solid.md`: Princípios de responsabilidade única, inversão de dependência e arquitetura limpa.
  - `tdd.md` & `tdd_2.md`: Ciclo TDD (Red-Green-Refactor) e pirâmide de testes.
  - `flyway.md` & `sql.md`: Governança de migrações e consultas SQL de alta performance.
  - `gradle.md`: Gestão de dependências e automação de build.
- **Skills Frontend (`gestao-paroquia-ui/.agents/skills/`):**
  - `react-frontend.md`: Padrões de React 19, custom hooks, TypeScript estrito (sem `any`), MUI 9, TanStack Query e Vitest.

## Security, Privacy & Global Constraints
- **Isolamento de Privacidade Absoluto (Absolute Privacy Isolation):** Todo este monorepo manipula dados pessoais e é estritamente para uso interno. O agente deve se reportar e aderir estritamente ao arquivo `docs/ia/regras_duras_lgpd.md` antes de escrever estruturas de dados, registrar eventos de log ou estruturar respostas de rede. A prevenção contra o vazamento de informações de identificação pessoal (PII) é aplicada globalmente.
- **Diretório de Skills Compartilhado:** Os comportamentos globais do agente, os paradigmas operacionais de ferramentas customizadas e as skills genéricas estão localizados em `.agents/skill` e `.agents/skills`. Revise essas capacidades globais juntamente com as skills locais dos projetos filhos para garantir uma execução coesa.