# Spec: Redução de Densidade Visual da UI (Tipografia, Espaçamento e Componentes)

## Overview
Ajustar os tokens centrais do tema MUI (`gestao-paroquia-ui/src/theme.ts`) para reduzir tipografia, espaçamento e altura de componentes em até 20%, tornando a interface mais compacta e agradável, sem comprometer legibilidade nem acessibilidade.

## Functional Requirements
1. **Tipografia:** reduzir tamanhos de fonte (`h1`–`h6`, `subtitle1/2`, `body1/2`, `button`, `caption`, `overline`) em até 20%, mantendo hierarquia relativa.
2. **Espaçamento:** reduzir a unidade base `theme.spacing` (padrão MUI 8px) em até 20%.
3. **Altura de componentes:** reduzir densidade de `MuiButton`, `MuiOutlinedInput`/`MuiTextField`, `MuiTableCell`, `MuiChip` via overrides/`defaultProps`.
4. **Consistência global:** mudanças concentradas em `theme.ts`, evitando overrides pontuais espalhados.
5. **Preservação de identidade:** cores, `borderRadius` (12px global / 10px botões/inputs) e demais tokens de marca — já fixados por `theme.test.tsx` — permanecem intactos.

## Non-Functional Requirements
- Fonte de corpo não abaixo de ~14px; alvos de toque dentro dos mínimos de acessibilidade do MUI.
- Escopo de arquivos: `theme.ts` (exceções pontuais só se algum componente hardcodear tamanhos fora do tema).
- Nenhum teste existente pode quebrar.
- Branch dedicada criada a partir do `master` atualizado.

## Acceptance Criteria
- [ ] `theme.ts` define uma escala de tipografia reduzida em até 20% frente aos defaults do MUI, para todas as variantes usadas na aplicação.
- [ ] `theme.ts` define uma unidade de espaçamento (`spacing`) reduzida em até 20% frente ao default do MUI (8px → ~6.4px, arredondado conforme julgamento de implementação).
- [ ] Componentes interativos padrão (`MuiButton`, `MuiOutlinedInput`, `MuiTextField`, `MuiTableCell`, `MuiChip`) têm padding/altura reduzidos de forma consistente com a nova escala.
- [ ] `npm run lint`, `npm run typecheck` e `npm run test:run` passam sem novas falhas.
- [ ] QA visual manual: aplicação rodando localmente (`./run.sh dev` ou `npm run dev`), navegação pelas telas principais (Login, Dashboard, Calendar, formulários de cadastro, tabelas de listagem, diálogos modais) confirmando visualmente a redução de densidade sem quebra de layout, sobreposição de texto ou corte de conteúdo.
- [ ] Nenhum valor de fonte de corpo abaixo de 14px; nenhuma alteração nos tokens de cor, `borderRadius` ou identidade de marca.

## Out of Scope
- Alterações de paleta de cores, border-radius ou identidade visual (logo, ícones).
- Refatoração de componentes React (estrutura, lógica, testes unitários além dos ajustes de snapshot/estilo necessários).
- Alterações no backend (`gestao-paroquia`).
- Redesign de layout/UX (reorganização de telas, novos componentes) — o escopo é puramente de escala/densidade dos tokens existentes.
