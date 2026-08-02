# AGENTS.md - Frontend Engineering Standards & Guidelines

## Architecture & State Management (React & TypeScript)
- **Domain-Driven UI:** Mantenha a lógica de negócio separada da representação visual. Transformações de dados pesadas, lógica de validação e gerenciamento de estado pertencem ao interior de custom hooks (`hooks/`), mantendo os componentes focados na apresentação.
- **Tipagem Estrita:** Nunca use `any`. Evite `unknown` sem um estreitamento de tipo (type narrowing) imediato. Todas as props de componentes, custom hooks e respostas de API devem ter definições explícitas e imutáveis de `type` ou `interface` do TypeScript.
- **Tipos sobre Strings:** Sempre prefira Java/TypeScript `enum` ou tipos estritos de união de literais de modelo (ex: `'success' | 'error' | 'pending'`) em vez de `string` pura para representar estados, categorias, status ou domínios fixos.
- **Restrições de Componentes e Funções:** Componentes React devem ter entre 10 e 60 linhas. Funções puras e hooks devem permanecer entre 4 e 20 linhas. Divida a lógica antecipadamente em componentes filhos ou hooks modulares caso os limites sejam excedidos.
- **Convenções de Nomenclatura:** Os nomes devem ser específicos, únicos e orientados ao domínio. Termos genéricos ABSOLUTAMENTE PROIBIDOS: `Data`, `Handler`, `Manager`, `Helper`, `Utility`, `Component`. Prefira nomes precisos que retornem menos de 5 resultados de grep na base de código.
- **Idioma — Domínio em Português, Técnico em Inglês:** Tipos/interfaces/DTOs que representam conceitos de domínio devem ser nomeados em português, espelhando o backend (ex.: `VinculoResponse`, não `MembershipResponse`). Termos puramente técnicos (`Response`, `Request`, `Props`, `Context`, `Provider`) permanecem em inglês.
- **Fluxo de Controle:** Prefira retornos antecipados (early returns) e cláusulas de guarda (guard clauses) em vez de blocos `if-else` aninhados ou operadores ternários de JSX profundamente aninhados. A profundidade máxima de indentação permitida é de 2 níveis dentro de qualquer bloco.

## Data Privacy & Compliance (LGPD Mandate)
- **Roteamento de Compliance Estrito:** Este projeto manipula dados pessoais e é apenas para uso interno. O agente DEVE ABSOLUTAMENTE ler, internalizar e aplicar estritamente os limites de privacidade e segurança definidos no arquivo `docs/ia/regras_duras_lgpd.md` antes de autorar componentes de UI, expor contratos de dados ou lidar com persistência/logs no lado do cliente (client-side).

## Otimização de Janela de Contexto
- **Consulta Prévia Mandatória:** Sempre consulte `graphify-out/graph.json` ou execute buscas semânticas locais antes de abrir múltiplos arquivos de código.
- **Eficiência de Tokens:** Nunca consuma tokens carregando arquivos inteiros se a relação mapeada pelo Graphify exigir modificações em apenas algumas linhas isoladas.

## Agent Capabilities, Custom Skills & Graphify Mandate
- **Exploração da Base de Código via Graphify:** A pasta `graphify-out/` localizada na raiz do monorepo contém os grafos de conhecimento e dependências de todo o projeto. O agente DEVE obrigatoriamente consultar `graphify-out/GRAPH_REPORT.md` e os arquivos de grafo antes de explorar componentes UI, páginas ou hooks via buscas manuais.
- **Economia de Tokens:** É proibida a leitura em lote de múltiplos arquivos fonte inteiros quando a estrutura de dependências puder ser obtida diretamente via consulta aos artefatos do Graphify, garantindo economia de tokens e navegação precisa.
- **Alinhamento Obrigatório de Skills do Projeto:** O agente DEVE obrigatoriamente consultar e aplicar as diretrizes contidas nos guias de skill em `.agents/skills/` (`react-frontend.md`) antes de gerar ou refatorar qualquer componente, hook ou página no frontend.

## Static Analysis, Linters & Tools
- **Conformidade com Ferramentas:** A base de código impõe rigidamente portões de qualidade (quality gates) via **ESLint**, **TypeScript (`tsc`)** e **Prettier**. Todo código gerado ou refatorado deve estar em total conformidade com essas configurações.
- **Política de Zero Avisos:** Nenhum aviso de compilação (warning), code smell ou violação de estilo deve ser introduzido. O agente deve verificar se o código compila perfeitamente (`npm run build`) e passa por todas as validações de linting/formatação antes de finalizar as alterações.
- **Stack de Framework:** Adira estritamente à stack principal do framework do projeto: **Vite** como empacotador/servidor de desenvolvimento, **React 19**, **Material UI (MUI 9)** para o sistema de design/componentes, **Axios** para execução do cliente HTTP e **TanStack React Query (v5)** para cache de estado do servidor.

## Arquitetura de Documentação
- **Documentação de API & Query:** Cada custom hook que encapsula um TanStack Query (`useQuery` / `useMutation`) deve incluir JSDoc detalhando o contrato de rede, estratégia de invalidação de query key e o formato de dados esperado.
- **JSDoc de Componentes:** Primitivos de UI reaproveitáveis públicos e componentes estruturais complexos devem ter um bloco Javadoc/JSDoc declarando sua intenção visual primária, requisitos de acessibilidade e exatamente um exemplo de uso de JSX válido.
- **Intenção sobre Mecanismo:** Os comentários dentro do código devem explicar o **PORQUÊ**, não o **O QUÊ**. Não comente códigos autoexplicativos (ex: pule `// set loading to true` acima de `setIsLoading(true)`).
- **Preservar o Contexto:** Retenha os comentários existentes durante a refatoração; eles carregam intenção histórica e proveniência.

## Importações & Higiene de Caminhos
- **Mapeamentos Absolutos:** SEMPRE use mapeamentos de caminhos absolutos (ex: `@/components/Button` ou `@/hooks/useAuth`) em vez de caminhos relativos (ex: `../../../../components/Button`). Caminhos relativos profundamente aninhados são estritamente proibidos.
- **Importações Limpas:** Agrupe as importações logicamente: React/Bibliotecas de terceiros primeiro, componentes/hooks absolutos do projeto em segundo, estilos/tipos por último.

## Suíte de Testes & Mandato de TDD
- **Aplicação Estrita de TDD:** O Desenvolvimento Orientado a Testes (TDD) é a base absoluta da segurança do frontend neste projeto. Você DEVE escrever o teste antes de escrever qualquer lógica de UI de produção, utilitário ou hook (ciclo Red-Green-Refactor).
- **Execução:** Todos os testes em todos os níveis devem rodar perfeitamente através do comando na raiz: `npm test` (alimentado por **Vitest** e **jsdom**).
- **Camadas Obrigatórias da Suíte de Testes:** Para cada nova funcionalidade, página ou hook, o agente DEVE implementar três camadas distintas de teste:
  1. **Testes Unitários:** Testes isolados para funções utilitárias, lógica pura de domínio e custom hooks (usando `renderHook` do `@testing-library/react`).
  2. **Testes de Integração/Componente:** Testes comportamentais de componentes de UI usando **React Testing Library** e `@testing-library/user-event` para simular interações reais de usuários.
  3. **Testes de Ponta a Ponta (E2E) / Fatia Completa:** Verificação de jornadas completas de usuários, transições de rotas via `react-router-dom` e árvores de componentes completas interagindo com fronteiras de rede mockadas.
- **Isolamento Estrito de I/O:** Mocke todas as camadas de rede HTTP externas usando simuladores de API (fakes) nomeados ou ferramentas de mock. Nunca permita que um teste de frontend realize atividades de rede reais contra o backend real.
- **Princípios F.I.R.S.T.:** Os testes devem ser Rápidos (Fast), Independentes (Independent), Repetíveis (Repeatable), Auto-validáveis (Self-Validating) e Oportunos/Na hora certa (Timely).

## Gerenciamento de Dependências & Desacoplamento
- **Props e Contexto sobre Globais:** Injete todo estado, configurações e manipuladores externos explicitamente através de Props ou React Context. É proibido depender de objetos globais `window` ou singletons não gerenciados espalhados.
- **Isolamento de UI de Terceiros:** Envolva bibliotecas de terceiros pesadas ou altamente voláteis (como motores de gráficos ou utilitários complexos) atrás de uma interface fina ou custom hook de propriedade deste projeto antes de usá-las dentro dos componentes.

## Estrutura & Topologia
- **Co-localização Estrita de Componentes:** Mantenha testes (`__tests__/`), estilos e componentes filhos próximos à rota ou ao componente pai que os utiliza, em vez de pastas globais distantes.
- **Topologia Previsível:** Adira estritamente à estrutura do projeto: `src/components/`, `src/hooks/`, `src/pages/`, `src/utils/`.

## Observabilidade & Logs
- **Higiene do Console:** `console.log` é apenas para desenvolvimento local. Limpe-os completamente antes de commitar. Nunca vaze logs técnicos, stack traces puros ou payloads de erro de API para o console do navegador voltado ao usuário em produção.