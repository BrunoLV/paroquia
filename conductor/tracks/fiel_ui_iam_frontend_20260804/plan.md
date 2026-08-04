# Plan: Adaptação do Frontend a Fiel/GrupoParoquial/IAM

## Phase 0: Setup
- [ ] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      feat/fiel-ui-iam-frontend-<data>`)
- [ ] Task: Confirmar que as Tracks 1-5 estão implementadas no backend (endpoints de Fiel/GrupoParoquial/
      papel_global disponíveis) — se não estiverem, HALT e avisar o usuário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 1: Cliente de API `fieis`
- [ ] Task: Escrever teste (Red) para o novo `api/fieis.ts` (list/get/create/update)
- [ ] Task: Implementar `api/fieis.ts` (Green)
- [ ] Task: Escrever/ajustar testes (Red) para `api/usuarios.ts`, `api/membros.ts`, `api/organizacoes.ts`
      refletindo os novos formatos de request/response
- [ ] Task: Atualizar esses clients (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Tela `/fieis`
- [ ] Task: Escrever testes de componente (Red) para listagem e formulário de cadastro/edição de Fiel
- [ ] Task: Implementar a página `/fieis` (listagem + modal de cadastro/edição), sem campos de login (Green)
- [ ] Task: Adicionar rota e item de menu em `Layout.tsx`
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Botão "Promover a Coordenador"
- [ ] Task: Escrever testes (Red) confirmando que o botão só aparece para usuários com papel global
      permitido, e que o modal pede credenciais só quando o Fiel não tem Usuario vinculado
- [ ] Task: Implementar o botão condicional e o modal de promoção na tela `/fieis` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Cadastro de Usuário via seleção de Fiel
- [ ] Task: Escrever testes (Red) para o formulário "Cadastrar Usuário" em `Settings.tsx` com seletor de
      Fiel existente
- [ ] Task: Implementar o seletor/busca de Fiel e ajustar o payload enviado (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Remoção do autocadastro público
- [ ] Task: Remover/ajustar testes (Red) de `Login.tsx` relacionados ao toggle `isRegistering`
- [ ] Task: Remover o toggle e a chamada `POST /usuarios/registrar` de `Login.tsx` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Fluxo de adicionar membro comum
- [ ] Task: Escrever testes (Red) para o fluxo de vínculo (`OrganizacaoFormModal`/tela de grupo) selecionando
      um Fiel em vez de Usuario
- [ ] Task: Implementar o ajuste (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 7: Terminologia GrupoParoquial/UnidadeTerritorial
- [ ] Task: Levantar todas as ocorrências de "Organização"/"Organizacao" em labels/textos visíveis
      (Settings.tsx, menus, formulários) via grep
- [ ] Task: Atualizar os textos/labels para refletir `GrupoParoquial`/`UnidadeTerritorial`, ajustando testes
      de snapshot/texto onde necessário
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 8: Documentação e validação final
- [ ] Task: Atualizar `.agents/skills/react-frontend.md` (ou skill equivalente) descrevendo os novos
      fluxos de Fiel/promoção/terminologia
- [ ] Task: Rodar a suíte completa (`CI=true npm test`) e `npx tsc --noEmit`, confirmar sucesso sem
      regressões
- [ ] Task: Propor plano de verificação manual (`npm run dev`) para os fluxos de FR1-FR4, a ser executado
      pelo usuário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)
