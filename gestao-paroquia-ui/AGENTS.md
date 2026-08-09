# AGENTS.md - Frontend Engineering Standards & Guidelines

Este documento define as regras Mandatórias e Invariantes de arquitetura, código, segurança e ferramentas para atuação no frontend do **Projeto Paróquia** (`gestao-paroquia-ui/`).

---

## 1. Diretrizes de Comportamento do Agente

* **Comunicação e Texto Explicativo:** Utilize a habilidade `caveman`. Respostas e explicações devem ser extremamente diretas, sem introduções, saudações ou texto conversacional.
* **Geração e Arquitetura de Código:** Utilize a habilidade `ponytail`. O código produzido deve ser estritamente de nível de produção, fortemente tipado, seguro, modular e limpo.
* **Separação de Papéis:** As habilidades `caveman` e `ponytail` são ferramentas de execução de estilo e qualidade, não personas operacionais.

---

## 2. Arquitetura e Contexto do Sistema

* **Stack Principal:** React 19, TypeScript, Vite 5, Material UI (MUI 9), TanStack React Query (v5), Axios.
* **Padrão Arquitetural:** Domain-Driven UI voltado a páginas (*page-oriented*).
* **Isolamento de Lógica:** Componentes React devem conter exclusivamente lógica visual e de apresentação.
* **Camada de Estado de Servidor:** Toda integração com API, mutação, cache e transformação de dados **DEVE** ser isolada em Custom Hooks dentro de `src/hooks/`.
* **Regra de Co-localização:** Testes e estilos específicos de um componente **DEVEM** residir na mesma pasta do componente.
* **Topologia Direcionada:**
  * `src/api/`: Wrappers HTTP e contratos de requisição/resposta.
  * `src/auth/`: Autorização, controle de acesso e regras de permissão.
  * `src/pages/`: Containers e roteamento de páginas.
  * `src/components/`: Componentes visuais reutilizáveis divididos por módulo/feature.
  * `src/context/`: Contextos globais da aplicação.
  * `src/utils/`: Funções puras utilitárias e helpers genéricos.

---

## 3. Integração com API e Segurança Frontend

* **Sincronia de Contratos:** Qualquer alteração em um contrato de endpoint do backend **DEVE** ser refletida simultaneamente no wrapper em `src/api/` e nas definições em `types.ts`.
* **Permissões Centralizadas:** Testes de autorização e checagens de perfil **NÃO DEVEM** ser feitos via renderização condicional inline com lógica própria. **DEVE-SE** consumir estritamente as abstrações em `src/auth/permissions.ts`.

---

## 4. Governança de IA e Conformidade LGPD

* **Análise Prévia via Graphify:** **DEVE-SE** consultar os artefatos de AST em `../graphify-out/` antes de realizar buscas manuais no código para entender conexões e dependências.
* **Proteção de Dados Pessoais (LGPD):**
  * **NÃO DEVE** haver exposição de dados PII em logs de console (`console.log`), local storage ou estado local não criptografado.
  * **DEVE-SE** cumprir rigorosamente as diretrizes contidas em `docs/ia/regras_duras_lgpd.md`.
* **Guia de Habilidades:** Consultar o arquivo `.agents/skills/react-frontend.md` antes de efetuar qualquer refatoração estrutural.

---

## 5. Padrões de Código e Tipagem Estrita

* **Tipagem:**
  * O uso do tipo `any` é **ESTRITAMENTE PROIBIDO**.
  * **NÃO DEVE** ser utilizada `string` livre para valores finitos; utilize `enum` ou uniões de literais de string (`type Status = 'ATIVO' | 'INATIVO'`).
* **Limites de Tamanho por Arquivo:**
  * Componentes React: Mínimo 10 linhas, Máximo 60 linhas.
  * Custom Hooks e Funções Puras: Mínimo 4 linhas, Máximo 20 linhas.
  * Se o limite for excedido, o arquivo **DEVE** ser decomposto.
* **Nomenclatura:**
  * Entidades de domínio, interfaces de dados e DTOs **DEVEM** ser nomeados em Português (ex: `UsuarioDto`, `ParoquiaResponse`).
  * Termos técnicos e padrões de React **DEVEM** permanecer em Inglês (ex: `Props`, `Context`, `State`, `Handler`).
  * Nomes genéricos ou vazios (`Manager`, `Helper`, `Data`, `Info`) são **PROIBIDOS**.
* **Estrutura de Importação:**
  * **DEVE-SE** utilizar caminhos absolutos baseados no alias `@/`.
  * Importações **DEVEM** seguir a ordem: 1. Bibliotecas externas -> 2. Módulos absolutos do projeto -> 3. Tipos e Estilos locais.

---

## 6. Qualidade, Testes e Documentação

* **Garantia de Zero Warnings:** O código produzido **NÃO DEVE** gerar nenhum warning ou erro no ESLint, Prettier ou no compilador TypeScript (`tsc`).
* **Documentação JSDoc:** Todo Custom Hook em `src/hooks/` **DEVE** possuir bloco JSDoc especificando:
  * O contrato da requisição de rede.
  * As chaves e estratégias de invalidação do TanStack Query.
  * O motivo arquitetural ou técnico da abordagem adotada.
* **Desenvolvimento Guiado por Testes (TDD):**
  * O ciclo **Red-Green-Refactor** é **MANDATÓRIO** para qualquer implementação.
  * Cobertura obrigatória para testes Unitários, de Componentes (React Testing Library + `user-event`) e E2E.
* **Isolamento Total de Rede:** É **PROIBIDO** realizar chamadas HTTP reais durante a execução de testes. Toda a camada Axios/API **DEVE** ser mockada.

---

## 7. Referência de Comandos e Ferramentas

### Graphify (Executar a partir da raiz do monorepo)
* Consultar base de código: `graphify query "<pergunta>"`
* Rastrear dependências entre arquivos: `graphify path "<Caminho_A>" "<Caminho_B>"`
* Explicar conceito/módulo: `graphify explain "<conceito>"`
* Atualizar AST após modificações: `graphify update .`

### NPM / Vitest (Executar a partir de `gestao-paroquia-ui/`)
* Instalação: `npm install`
* Servidor de desenvolvimento: `npm run dev`
* Validação de tipos e Build: `npm run build`
* Execução de testes: `npm test`
* Testes em modo Watch: `npm run test:watch`
* Executar teste específico: `npx vitest run <caminho_do_arquivo>`