# Plan: Padronização de Cor e Ícone por Ação nos Botões do Sistema

## Phase 0: Setup de Branch
- [x] Task: Sincronizar `master` local com `origin/master` e criar branch dedicada `feat/estilizacao-botoes-acao-20260804` a partir dela `9afe9a57`
- [x] Task: Commit de checkpoint inicial (estado zero da branch, sem alterações) `66e5d94a`

## Phase 1: Documentação do Padrão (Guia de Estilo)
- [x] Task: Adicionar a seção "Action Button Color & Icon Mapping" em `.agents/skills/react-frontend.md`, com a tabela ação → cor → ícone definida no spec.md `71a7459b`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Diálogos de Formulário (Salvar/Cancelar/Confirmar)
- [x] Task: Escrever teste (Red) verificando ícone + cor esperados nos botões de Salvar/Cancelar dos diálogos principais (`CadastroEventoModal`, `EditScopeDialog`, `CancelarEventoDialog`, `ImportModal`, `ExportCalendarDialog`, `AtivoAlocacoesModal`, `ObservacoesEvento`), e confirmar que falha `d986c0bb`
- [x] Task: Implementar (Green) `startIcon`/`color` nos botões desses diálogos conforme o mapeamento, e rodar os testes até passarem `d986c0bb`
- [x] Task: Refatorar se necessário (extrair ícones repetidos, evitar duplicação) e re-rodar os testes `d986c0bb`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Páginas de Listagem/CRUD (Ações Primárias)
- [x] Task: Escrever teste (Red) verificando ícone + cor esperados nos botões de ação primária das páginas (`Ativos.tsx`, `Projects.tsx`, `Calendar.tsx`, `ConselhosAtas.tsx`, `Settings.tsx`, `Reports.tsx`, `Aprovacoes.tsx`, `Membros`/`Organizacoes`/`Eventos` conforme aplicável), e confirmar que falha `c0fc6abd`
- [x] Task: Implementar (Green) `startIcon`/`color` nesses botões conforme o mapeamento, e rodar os testes até passarem `c0fc6abd`
- [x] Task: Refatorar se necessário e re-rodar os testes `c0fc6abd`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: IconButtons de Ação em Tabelas/Linhas
- [x] Task: Escrever teste (Red) verificando a cor semântica esperada (`color="secondary"` para editar, `color="error"` para excluir, etc.) nos `IconButton` de ação em linhas de tabela/lista, e confirmar que falha `f91ae86f`
- [x] Task: Implementar (Green) o `color` correto nesses `IconButton`, e rodar os testes até passarem `f91ae86f`
- [x] Task: Refatorar se necessário e re-rodar os testes `f91ae86f`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Diálogos de Confirmação Destrutiva e Aprovação/Homologação
- [x] Task: Escrever teste (Red) verificando ícone + cor esperados nos botões de confirmação destrutiva (Excluir, Vetar, Rejeitar) e de aprovação/homologação (Aprovar, Homologar), e confirmar que falha `6d257e16` — a maior parte já coberta nas Phases 2-4 (CancelarEventoDialog, veto ConselhosAtas, Aprovar/Rejeitar em Aprovacoes.tsx); varredura final encontrou e corrigiu o botão "Aprovar" de usuários pendentes em Settings.tsx
- [x] Task: Implementar (Green) `startIcon`/`color` nesses botões conforme o mapeamento, e rodar os testes até passarem `6d257e16`
- [x] Task: Refatorar se necessário e re-rodar os testes `6d257e16`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Validação Final e QA Visual
- [x] Task: Rodar `npx tsc --noEmit` e `CI=true npm test` no `gestao-paroquia-ui` e corrigir eventuais falhas `3656f124`
- [x] Task: Subir a aplicação localmente (`npm run dev`) e realizar QA visual manual nas telas principais, confirmando aplicação consistente do padrão `3656f124` — QA encontrou e corrigiu lacunas em Ativos (menu de ações), CpcDecisaoModal, LocalFormModal, OrganizacaoFormModal e diversos pontos de Settings.tsx em 2 rodadas de feedback
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 7: Encerramento da Track
- [ ] Task: Atualizar `conductor/tracks.md` marcando a track como concluída `[x]`
- [ ] Task: Sincronizar documentação de projeto (`product.md`/`tech-stack.md`) se aplicável (Refer to conductor-implement Section 4)
