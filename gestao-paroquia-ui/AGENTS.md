# AGENTS.md - Frontend Engineering Standards & Guidelines (`gestao-paroquia-ui/`)

Este documento define as regras Mandatórias, Invariantes e Diretrizes Técnicas para qualquer agente de IA ou desenvolvedor atuando no subprojeto de frontend (`gestao-paroquia-ui/`).

---

## 1. Diretrizes Fundamentais de Execução

* **Comunicação e Explicações:** Execute estritamente no estilo `caveman`. Respostas e explicações devem ser curtas, diretas, sem saudações, introduções ou prolixidade.
* **Qualidade e Estilo de Código:** Execute estritamente no estilo `ponytail`. O código produzido deve ser de nível de produção sênior, declarativo, fortemente tipado, acessível e sem warnings.
* **Código como Documentação Viva (*Living Documentation*):**
  * Componentes de tela, modais, painéis e hooks customizados **DEVEM** conter JSDoc contextualizando a regra de negócio/canônica atendida e a decisão ergonômica de UX adotada.
  * Justifique no código contenções de propagação (`stopPropagation`), aprisionamentos de foco (*focus traps*) e escolhas semânticas de ARIA. Comentários triviais (`// renderiza botão`, `// hook`) são proibidos.

---

## 2. Matriz de Ativação de Skills do Agente (`.agents/skills/`)

Antes de iniciar qualquer intervenção no frontend, consulte e respeite integralmente os manuais de skills correspondentes:

| Escopo da Tarefa no Frontend | Skills Mandatórias | Invariantes Críticas |
| :--- | :--- | :--- |
| **Componentes, Páginas, Telas & Layout** | `react-frontend.md`<br>`DESIGN_SYSTEM.md` | • Leitura de `DESIGN_SYSTEM.md` **obrigatória** antes de tocar em qualquer tela/modal/formulário — é a fonte única do padrão visual (botões, busca, diálogos, tabelas, estados assíncronos).<br>• Semântica MUI 9 pura: **PROIBIDAS** tags HTML cruas (`div`, `span`, `p`) para layout/texto.<br>• Uso exclusivo do novo `<Grid2 container spacing={2}>` e `<Grid2 size={{ xs: 12, ... }}>`.<br>• Limites de linhas: componentes puros (10–60 linhas); formulários densos (máximo 120 linhas).<br>• Cantos arredondados padronizados pelo tema (`shape.borderRadius: 12`). |
| **Formulários & Entradas de Dados** | `react-frontend.md` | • Schema Zod obrigatório em `src/schemas/<entidade>Schema.ts`.<br>• Integração mandatória via `react-hook-form` + `@hookform/resolvers/zod`.<br>• Proibida proliferação de `useState` para múltiplos inputs.<br>• Máscaras e formatadores sistemáticos (`src/utils/masks.ts`). |
| **Server State & Integração com API** | `react-frontend.md` | • TanStack Query v5 exclusivo em hooks dedicados (`src/hooks/`).<br>• Chaves centralizadas via Query Key Factories serializáveis (`fielKeys.all`).<br>• Proibidas chamadas diretas de `fetch`/`axios` dentro do corpo de componentes.<br>• Invalidação de queries e feedback visual com notificações após mutações. |
| **Acessibilidade (a11y) & Tabelas** | `react-frontend.md` | • Linhas de tabela clicáveis devem ter `role="button"`, `tabIndex={0}` e listeners `onKeyDown` (`Enter`/`Space`).<br>• Botões e menus de ações na linha **DEVEM** conter `event.stopPropagation()`.<br>• Paginação obrigatória em todas as listagens (`<TablePagination>`). |
| **Segurança e Privacidade (LGPD)** | `lgpd-privacy-by-design.md` | • Paridade absoluta: dados civis de qualquer papel/clérigo isolados em modais de RH/administrativos.<br>• Proibido `console.log` com payloads ou PII exposta.<br>• Mascaramento obrigatório de dados civis (CPF, e-mail, telefone). |
| **Testes Automatizados de Frontend** | `react-frontend.md`<br>`tdd.md` | • Vitest + React Testing Library + `@testing-library/user-event`.<br>• Proibido `fireEvent`; simule interações reais com `userEvent`.<br>• Isolamento de rede: interceptação obrigatória via Mock Service Worker (MSW). |

---

## 3. Padrão Visual Oficial (*Padrão Arrojado*)

👉 **Fonte única e obrigatória: [`DESIGN_SYSTEM.md`](./DESIGN_SYSTEM.md).** Leia-o antes de criar ou alterar qualquer tela, modal, formulário ou componente visual. Não duplicar seu conteúdo aqui — qualquer divergência entre este arquivo e `DESIGN_SYSTEM.md` resolve-se a favor do `DESIGN_SYSTEM.md`.

Resumo do que ele cobre: layout de página (`PageHeader`, `Stack spacing={3}`), busca (`SearchField`), botões (`ActionButton`/`IconActionButton` + semântica de cor — nunca `ActionButton` sem `label` como controle só-ícone), estrutura de diálogos (`Card`+`Grid2`+`DIALOG_CONTENT_SCROLL_SX`, um único controle de fechar por Dialog/Drawer), espaçamento fixo de campos (`Stack spacing={2.5}`/`Grid spacing={2}`, nunca `Box`+`gap`), tríade de estados assíncronos, `StatusIcon`, `InfoField` com ícone obrigatório em toda tela de detalhe, tabelas e modo escuro (`useTheme()`, nunca import estático de tema). É especificação global: aplica-se a toda tela/módulo, novo ou existente, sem exceções por página.

---

## 4. Arquitetura de Componentes e Código TypeScript

* **Tipagem Estrita:**
  * O uso de `any` ou `unknown` sem narrowing é **TERMINANTEMENTE PROIBIDO**.
  * Proibido `string` livre para valores finitos; utilize `enum` ou uniões de literais (`type Status = 'ATIVO' | 'INATIVO'`).
* **Regra de Co-localização e Organização:**
  * Testes (`.test.tsx`), schemas (`Schema.ts`) e estilos específicos devem residir junto ao componente ou módulo funcional correspondente.
  * Subcomponentes derivados de telas devem residir na pasta `components/` adjacente ao arquivo de página pai.
* **Estrutura de Importações:**
  * Utilize invariavelmente os aliases absolutos configurados: `@/components`, `@/hooks`, `@/utils`, `@/schemas`, `@/api`.
  * Proibidas importações relativas profundas (`../../../../components`).

---

## 5. Governança de Qualidade, Linting e Execução

* **Garantia de Zero Warnings:** O código produzido **NÃO DEVE** gerar nenhum warning ou erro no ESLint, Prettier ou `tsc --noEmit`.
* **Segurança e Higiene de Console:** Proibido comitar declarações de `console.log` ou expor tokens e identificadores em rotas e formulários inseguros.

---

## 6. Referência de Comandos

### Graphify (Executar a partir da raiz do monorepo)
* Consultar base de código: `graphify query "<pergunta>"`
* Rastrear dependências entre componentes/arquivos: `graphify path "<CaminhoA>" "<CaminhoB>"`
* Explicar conceito/módulo: `graphify explain "<conceito>"`
* Atualizar AST após modificações: `graphify update .`

### NPM / Vitest (Executar a partir de `gestao-paroquia-ui/`)
* Instalar dependências: `npm install`
* Iniciar servidor de desenvolvimento: `npm run dev`
* Validação de tipos TypeScript e Build: `npm run build`
* Executar testes automatizados: `npm test`
* Executar teste específico: `npx vitest run <caminho_do_arquivo>`