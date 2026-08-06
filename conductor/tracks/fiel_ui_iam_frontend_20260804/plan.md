# Plan: Adaptação do Frontend a Fiel/GrupoParoquial/IAM

## Phase 0: Setup
- [x] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      feat/fiel-ui-iam-frontend-20260806`)
- [x] Task: Confirmar que as Tracks 1-5 estão implementadas no backend (endpoints de Fiel/GrupoParoquial/
      papel_global disponíveis) — se não estiverem, HALT e avisar o usuário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 1: Cliente de API `fieis`
- [x] Task: Escrever teste (Red) para o novo `api/fieis.ts` (list/get/create/update)
- [x] Task: Implementar `api/fieis.ts` (Green)
- [x] Task: Escrever/ajustar testes (Red) para `api/usuarios.ts`, `api/membros.ts`, `api/organizacoes.ts`
      refletindo os novos formatos de request/response
- [x] Task: Atualizar esses clients (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Tela `/fieis`
- [x] Task: Escrever testes de componente (Red) para listagem e formulário de cadastro/edição de Fiel
- [x] Task: Implementar a página `/fieis` (listagem + modal de cadastro/edição), sem campos de login (Green)
- [x] Task: Adicionar rota e item de menu em `Layout.tsx`
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Botão "Promover a Coordenador"
- [x] Task: Escrever testes (Red) confirmando que o botão só aparece para usuários com papel global
      permitido, e que o modal pede credenciais só quando o Fiel não tem Usuario vinculado
- [x] Task: Implementar o botão condicional e o modal de promoção na tela `/fieis` (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Cadastro de Usuário via seleção de Fiel
- [x] Task: Escrever testes (Red) para o formulário "Cadastrar Usuário" em `Settings.tsx` com seletor de
      Fiel existente
- [x] Task: Implementar o seletor/busca de Fiel e ajustar o payload enviado (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Remoção do autocadastro público
- [x] Task: Remover/ajustar testes (Red) de `Login.tsx` relacionados ao toggle `isRegistering`
- [x] Task: Remover o toggle e a chamada `POST /usuarios/registrar` de `Login.tsx` (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Fluxo de adicionar membro comum
- [x] Task: Escrever testes (Red) para o fluxo de vínculo (`OrganizacaoFormModal`/tela de grupo) selecionando
      um Fiel em vez de Usuario
- [x] Task: Implementar o ajuste (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 7: Terminologia GrupoParoquial/UnidadeTerritorial
- [x] Task: Levantar todas as ocorrências de "Organização"/"Organizacao" em labels/textos visíveis
      (Settings.tsx, menus, formulários) via grep
- [x] Task: Atualizar os textos/labels para refletir `GrupoParoquial`/`UnidadeTerritorial`, ajustando testes
      de snapshot/texto onde necessário
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 8: Documentação e validação final
- [x] Task: Atualizar `.agents/skills/react-frontend.md` (ou skill equivalente) descrevendo os novos
      fluxos de Fiel/promoção/terminologia
- [x] Task: Rodar a suíte completa (`CI=true npm test`) e `npx tsc --noEmit`, confirmar sucesso sem
      regressões
- [x] Task: Propor plano de verificação manual (`npm run dev`) para os fluxos de FR1-FR4, a ser executado
      pelo usuário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)
