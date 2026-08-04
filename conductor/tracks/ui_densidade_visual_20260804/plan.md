# Plan: Redução de Densidade Visual da UI

## Phase 0: Setup de Branch
- [x] Task: Sincronizar `master` local com `origin/master` e criar branch dedicada `chore/ui-densidade-visual-20260804` a partir dela `8e489020`
- [x] Task: Commit de checkpoint inicial (estado zero da branch, sem alterações) `1f033f52`

## Phase 1: Redução de Tipografia (até 20%)
- [x] Task: Escrever teste (Red) em `theme.test.tsx` que afirma os novos tamanhos de fonte esperados para as variantes usadas (`h1`, `body1`, `body2`, `button`, `caption`) com redução de até 20% frente aos defaults do MUI, e confirmar que falha `e38cb290`
- [x] Task: Implementar (Green) os novos valores de `typography` em `theme.ts` (fontSize por variante), garantindo `body1` ≥ 14px, e rodar os testes até passarem `e38cb290`
- [x] Task: Refatorar se necessário (extrair fator de escala nomeado, evitar números mágicos duplicados) e re-rodar os testes `e38cb290`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Redução de Espaçamento Base (até 20%)
- [ ] Task: Escrever teste (Red) que afirma o novo valor de `theme.spacing(1)` reduzido em até 20% frente ao default do MUI (8px), e confirmar que falha
- [ ] Task: Implementar (Green) o novo `spacing` em `theme.ts` e rodar os testes até passarem
- [ ] Task: Refatorar se necessário e re-rodar os testes
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Redução de Densidade de Componentes Interativos
- [ ] Task: Escrever teste (Red) que afirma padding/size reduzidos nos `styleOverrides`/`defaultProps` de `MuiButton`, `MuiOutlinedInput`/`MuiTextField`, `MuiTableCell` e `MuiChip` em `theme.ts`, e confirmar que falha
- [ ] Task: Implementar (Green) os overrides necessários em `theme.ts`, preservando `borderRadius` e cores existentes, e rodar os testes até passarem
- [ ] Task: Refatorar se necessário e re-rodar os testes
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Validação Final e QA Visual
- [ ] Task: Rodar `npm run lint`, `npm run typecheck` e `CI=true npm run test:run` no `gestao-paroquia-ui` e corrigir eventuais falhas
- [ ] Task: Subir a aplicação localmente (`./run.sh dev` ou `npm run dev`) e realizar QA visual manual nas telas principais (Login, Dashboard, Calendar, formulários de cadastro, tabelas de listagem, diálogos modais), confirmando redução de densidade sem quebra de layout
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Encerramento da Track
- [ ] Task: Atualizar `conductor/tracks.md` marcando a track como concluída `[x]`
- [ ] Task: Sincronizar documentação de projeto (`product.md`/`tech-stack.md`) se aplicável (Refer to conductor-implement Section 4)
