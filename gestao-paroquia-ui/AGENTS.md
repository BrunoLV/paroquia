# AGENTS.md - Frontend Engineering Standards & Guidelines (`gestao-paroquia-ui/`)

Este documento define as regras Mandatórias, Invariantes e Diretrizes Técnicas para qualquer agente de IA ou desenvolvedor atuando no subprojeto de frontend (`gestao-paroquia-ui/`).

---

## 1. Diretrizes Fundamentais de Execução

* **Comunicação e Explicações:** Execute estritamente no estilo `caveman`. Respostas e explicações devem ser curtas, diretas, sem saudações, introduções ou prolixidade.
* **Qualidade e Estilo de Código:** Execute estritamente no estilo `ponytail`. O código produzido deve ser de nível de produção sênior, declarativo, fortemente tipado, acessível e sem warnings.
* **Código como Documentação Viva (*Living Documentation*):**
  * Componentes de tela, modais, painéis e hooks customizados **DEVEM** conter JSDoc contextualizando a regra de negócio/canônica atendida e a decisão ergonômica de UX adotada.
  * Justifique no código contenções de propagação (`stopPropagation`), aprisionamentos de foco (*focus traps*) e escolhas semânticas de ARIA. Comentários triviais (`// renderiza botão`, `// hook`) são proibidos.
* **Fluxo de Desenvolvimento Mandatório (`fluxo-desenvolvimento`):**
  * Toda intervenção deve seguir o GitHub Flow (branch a partir de `master` atualizada).
  * Atualizar `docs/business-rules/<modulo>.md` quando houver alteração de regra de negócio ou fluxo operacional. Diretivas técnicas e visuais pertencem ao Design System e às instruções dos agentes.
  * Questionar sobre fechamento de versão ao término e atualizar `CHANGELOG.md` e `package.json`.

---

## 2. Matriz de Ativação de Skills do Agente (`.agents/skills/`)

Antes de iniciar qualquer intervenção no frontend, consulte e respeite integralmente os manuais de skills correspondentes:

| Escopo da Tarefa no Frontend | Skills Mandatórias | Invariantes Críticas |
| :--- | :--- | :--- |
| **Workflow e Ciclo de Vida (ALWAYS-ON)** | [`fluxo-desenvolvimento`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/fluxo-desenvolvimento/SKILL.md) | • GitHub Flow obrigatório (branch semântica saindo de `master` atualizada).<br>• Consulta das regras de negócio e atualização quando houver mudança de negócio; diretivas visuais ficam no Design System.<br>• Pergunta sobre versão e atualização de `CHANGELOG.md`.<br>• Conventional Commits e submissão via Pull Request para `master`. |
| **Componentes, Páginas, Telas & Layout** | [`react-frontend`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/react-frontend/SKILL.md)<br>[`DESIGN_SYSTEM.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia-ui/DESIGN_SYSTEM.md) | • Leitura de `DESIGN_SYSTEM.md` **obrigatória** antes de tocar em qualquer tela/modal/formulário — é a fonte única do padrão visual (botões, busca, diálogos, tabelas, estados assíncronos).<br>• Semântica MUI 9 pura: **PROIBIDAS** tags HTML cruas (`div`, `span`, `p`) para layout/texto.<br>• Uso exclusivo do novo `<Grid2 container spacing={2}>` e `<Grid2 size={{ xs: 12, ... }}>`.<br>• Limites de linhas: componentes puros (10–60 linhas); formulários densos (máximo 120 linhas).<br>• Cantos arredondados padronizados pelo tema (`shape.borderRadius: 12`). |
| **Formulários & Entradas de Dados** | [`react-frontend`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/react-frontend/SKILL.md) | • Schema Zod obrigatório em `src/contextos/<contexto>/presentation/schemas/`.<br>• Integração mandatória via `react-hook-form` + `@hookform/resolvers/zod`.<br>• Proibida proliferação de `useState` para múltiplos inputs.<br>• Máscaras e formatadores em `src/shared/utils/`. |
| **Server State & Integração com API** | [`react-frontend`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/react-frontend/SKILL.md) | • TanStack Query v5 exclusivo em `contextos/<contexto>/presentation/hooks/`.<br>• Chaves centralizadas no contexto proprietário.<br>• Adapters HTTP em `infrastructure/http/` implementam portas de `application`.<br>• Proibidas chamadas diretas de `fetch`/`axios` em componentes ou hooks de apresentação. |
| **Acessibilidade (a11y) & Tabelas** | [`react-frontend`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/react-frontend/SKILL.md) | • Linhas de tabela clicáveis devem ter `role="button"`, `tabIndex={0}` e listeners `onKeyDown` (`Enter`/`Space`).<br>• Botões e menus de ações na linha **DEVEM** conter `event.stopPropagation()`.<br>• Paginação obrigatória em todas as listagens (`SearchableTablePagination`). |
| **Segurança e Privacidade (LGPD)** | [`lgpd-privacy-by-design`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/lgpd-privacy-by-design/SKILL.md) | • Paridade absoluta: dados civis de qualquer papel/clérigo isolados em modais de RH/administrativos.<br>• Proibido `console.log` com payloads ou PII exposta.<br>• Mascaramento obrigatório de dados civis (CPF, e-mail, telefone). |
| **Testes Automatizados de Frontend** | [`react-frontend`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/react-frontend/SKILL.md)<br>[`tdd`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/tdd/SKILL.md) | • Vitest + React Testing Library + `@testing-library/user-event`.<br>• Proibido `fireEvent`; simule interações reais com `userEvent`.<br>• Isolamento de rede: interceptação obrigatória via Mock Service Worker (MSW). |

---

## 3. Padrão Visual Oficial (*Padrão Arrojado*)

* **Listas pesquisáveis obrigatórias:** Toda lista de valores, inclusive filtros, seleção múltipla e Linhas por página, segue a seção 2.1 do `DESIGN_SYSTEM.md`. Use `SearchableSelect`/`SearchableAutocomplete` e `SearchableTablePagination`; proibidos seletores sem pesquisa. A suíte estrutural do frontend valida esta diretiva.
* **Paginação no rodapé:** Aplicar a seção 7 do Design System: `SearchableTablePagination` dentro do `Table`, após `TableBody`, com `colSpan` das colunas visíveis. Seletor e botões anterior/próximo com mesma altura, estilo compacto restrito à paginação. Validar `tablePaginationArchitecture.test.ts`; não modificar a densidade dos formulários.
* **Ações de linha:** `TableActionMenu` obrigatório, inclusive ação única, com gatilho pequeno delineado `⋮`, sem texto visível, tooltip e nome acessível “Ações”. Preservar permissões e estados. Validar teclado/foco e `tableActionsArchitecture.test.ts`.

👉 **Fonte única e obrigatória: [`DESIGN_SYSTEM.md`](./DESIGN_SYSTEM.md).** Leia-o antes de criar ou alterar qualquer tela, modal, formulário ou componente visual. Não duplicar seu conteúdo aqui — qualquer divergência entre este arquivo e `DESIGN_SYSTEM.md` resolve-se a favor do `DESIGN_SYSTEM.md`.

Resumo do que ele cobre: layout de página (`PageHeader`, `Stack spacing={3}`), busca (`SearchField`), botões (`ActionButton`/`IconActionButton` + semântica de cor — nunca `ActionButton` sem `label` como controle só-ícone), estrutura de diálogos (`Card`+`Grid2`+`DIALOG_CONTENT_SCROLL_SX`, um único controle de fechar por Dialog/Drawer), espaçamento fixo de campos (`Stack spacing={2.5}`/`Grid spacing={2}`, nunca `Box`+`gap`), tríade de estados assíncronos, `StatusIcon`, `InfoField` com ícone obrigatório em toda tela de detalhe, tabelas e modo escuro (`useTheme()`, nunca import estático de tema). É especificação global: aplica-se a toda tela/módulo, novo ou existente, sem exceções por página.

### 3.1. Invariantes e Anti-patterns Mandatórios de Modais e Diálogos (`Dialog`)
* **`<DialogTitle>` Limpo:** Sempre string pura (`<DialogTitle>Título</DialogTitle>`). É **TERMINANTEMENTE PROIBIDO** colocar `<Typography>` dentro de `<DialogTitle>`.
* **`<form noValidate>` Obrigatório:** Toda tag `<form>` em modais deve conter `noValidate` para impedir balões HTML5 nativos que quebram o design system.
* **`size="small"` em TODOS os Inputs:** É **EXPRESSAMENTE PROIBIDO** gerar `<TextField>`, autocomplete ou `<FormControl>` sem `size="small"`. O padrão corporativo exige 40px de altura para campos de entrada.
* **`dividers` + `sx={DIALOG_CONTENT_SCROLL_SX}`:** Obrigatórios em `<DialogContent>`.
* **Campos em `<Card variant="outlined" sx={{ borderRadius: 2 }}>`:** Proibido campo solto direto no `DialogContent`. Todo agrupamento tem cabeçalho com ícone e `Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}`.
* **Controle Único de Fechar:** O botão canônico é `ActionButton` "Cancelar" no `DialogActions`. Proibido ícone "X" no cabeçalho quando houver rodapé com ações.
* **Consulte a regra permanente:** [`.agents/rules/frontend-design-system.md`](../.agents/rules/frontend-design-system.md).

---

## 4. Arquitetura de Componentes e Código TypeScript

* **Tipagem Estrita:**
  * O uso de `any` ou `unknown` sem narrowing é **TERMINANTEMENTE PROIBIDO**.
  * Proibido `string` livre para valores finitos; utilize `enum` ou uniões de literais (`type Status = 'ATIVO' | 'INATIVO'`).
* **Regra de Co-localização e Organização:**
  * Testes (`.test.tsx`), schemas (`Schema.ts`) e estilos específicos devem residir junto ao módulo funcional em `contextos/<contexto>/presentation/`.
  * Subcomponentes derivados de telas devem residir na pasta `components/` adjacente ao arquivo de página pai.
* **Estrutura de Importações:**
  * Utilize invariavelmente aliases absolutos: `@/contextos`, `@/shared` e `@/app`.
  * Proibidas importações relativas profundas (`../../../../components`).

## 4.1. Arquitetura DDD e Hexagonal

* Cada capacidade de negócio reside em `src/contextos/<contexto>/`, alinhada ao bounded context do backend.
* `domain/` contém apenas TypeScript puro; não pode importar React, MUI, Axios, TanStack Query, Zod ou código de infraestrutura.
* `application/` declara use cases e portas; depende de `domain`, nunca de adapters concretos ou componentes.
* `infrastructure/http/` concentra adapters, DTOs e mapeadores HTTP; é o único local de cada contexto que conhece o cliente Axios.
* `presentation/` concentra páginas, componentes, hooks Query e schemas Zod; só acessa dados por contratos/use cases da aplicação.
* `src/shared/` não contém regras de negócio. `src/app/` apenas compõe providers e rotas.
* Todo nome de domínio, ação e rota usa português. Apenas convenções técnicas como `Adapter`, `Port`, `Provider`, `Hook`, `Query`, `Mutation`, `DTO` e APIs de bibliotecas ficam em inglês.
* Rotas canônicas: `/acesso`, `/painel`, `/calendario`, `/projetos`, `/aprovacoes` e `/relatorios`. Rotas inglesas são proibidas.

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
