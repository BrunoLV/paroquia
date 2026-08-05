# Plan: Vínculo de Membro por `Fiel` (`MembroGrupo`)

## Phase 0: Setup
- [x] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      feat/membro-grupo-por-fiel-<data>`) — branch `feat/membro-grupo-por-fiel-20260805`
- [x] Task: Confirmar que as Tracks 1 e 2 estão implementadas na base atual — se não estiverem, HALT e
      avisar o usuário — confirmado: PR #5 e PR #6 mergeados em master antes desta branch ser criada
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `d1625e3`

  **Decisões de design confirmadas com o usuário antes da Phase 1 (afetam FR4/FR6 diretamente):**
  1. `MembroGrupo.papel` é tipado como `PapelOrganizacional` (reuso do enum existente, sem valor genérico
     novo `COORDENADOR`/`VICE_COORDENADOR`); `PromoverACoordenadorUseCase` recebe um dos valores já
     existentes (`COORD_PASTORAL_LOCAL/GLOBAL`, `COORD_MOVIMENTO`, `PRESIDENTE_CPC`, `SECRETARIO`).
  2. A lógica de negócio de `AdicionarVinculoUseCase` (auto-demoção de titular único, escopo por igreja,
     restrição de liderança de conselhos a ADMIN/PAROCO) é preservada 100%, apenas trocando `usuarioId` por
     `fielId`/`MembroOrganizacaoEntity` por `MembroGrupo`.
  3. `ParticipanteOrganizacaoEntity`/`ListParticipacoesUseCase`/`AddParticipanteUseCase` (participação extra
     em pastorais) são removidos; o mesmo efeito é obtido chamando `AdicionarMembroUseCase` novamente para
     outra `organizacaoId`. Endpoints `POST/GET /membros/{id}/organizacoes` removidos de `MembroController`.
  4. Autorização de `PromoverACoordenadorUseCase` implementada como novo método dedicado em
     `UsuarioAuthorizationService` (checagem de authorities `ROLE_ADMIN`/`ROLE_CLERO_PAROCO`/
     `ROLE_CLERO_VIGARIO`/`ROLE_CONSELHO_MESA_CPP`), reaproveitando o padrão de `isAdmin`.

## Phase 1: Tabela e entidade `MembroGrupo`
- [ ] Task: Escrever teste de integração (Red) que persiste um `MembroGrupo` válido (`fielId` +
      `organizacaoId` + `papel`)
- [ ] Task: Criar migração Flyway que dropa `membros`, `membros_organizacao`, `membros_organizacoes` e cria
      `calendario.membro_grupo`
- [ ] Task: Implementar `MembroGrupo` (domain/model), `MembroGrupoEntity` (infrastructure/persistence/entity)
      e `MembroGrupoJpaRepository` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: `AdicionarMembroUseCase`
- [ ] Task: Escrever testes (Red): cria vínculo com sucesso para um `fielId` existente; falha se `fielId` ou
      `organizacaoId` não existirem; teste que verifica explicitamente **zero interação** com o repositório
      de `Usuario`
- [ ] Task: Implementar `AdicionarMembroUseCase`, incluindo a sincronização de liderança (FR5) via nome do
      `Fiel` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: `PromoverACoordenadorUseCase`
- [ ] Task: Escrever testes (Red): sucesso ao promover um Fiel membro sem Usuario prévio (cria Usuario
      vinculado); sucesso ao promover um Fiel que já tem Usuario (não duplica); falha se o `fielId` não for
      membro ativo do grupo; falha de autorização para actor sem papel PAROCO/VIGARIO/MESA_CPP/ADMIN
- [ ] Task: Implementar `PromoverACoordenadorUseCase`, reutilizando a checagem de autorização atual
      (mecanismo existente em `EscopoUsuarioAuthorizationService`/`UsuarioAuthorizationService`) (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Migração dos consumidores existentes de vínculo
- [ ] Task: Escrever/ajustar testes (Red) para `ListMembrosUseCase`, `UpdateMembroUseCase`,
      `GetMembroUseCase`, `AdicionarVinculoUseCase`, `RemoverVinculoUseCase`, `ListarVinculosUseCase`,
      `ListOrganizacaoMembrosUseCase` operando sobre `MembroGrupo`/`fielId`
- [ ] Task: Atualizar esses casos de uso e seus controllers/DTOs (`MembroController`, `MembroRequest`,
      `MembroResponse`, `OrganizacaoController`) para a nova estrutura, resolvendo dados de exibição via
      `Fiel` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Ajuste do mecanismo de autenticação
- [ ] Task: Escrever teste de integração (Red) que simula login completo (Usuario com fielId vinculado a
      linhas de `membro_grupo`) e verifica que as authorities Spring Security resultantes estão corretas
- [ ] Task: Atualizar `LeitorVinculoExterno.findVinculosAtivos`/`findRolesByUserAndOrganization` para
      resolver `fielId` via `usuarios.fiel_id` e consultar `membro_grupo` (Green)
- [ ] Task: Confirmar que `UsuarioDetailsService` continua funcionando sem mudança de contrato externo
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Documentação para agentes de IA
- [ ] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante) descrevendo o vínculo `MembroGrupo`
      por `fielId`, e a distinção entre `AdicionarMembroUseCase` (nunca toca Usuario) e
      `PromoverACoordenadorUseCase` (restrito, pode criar Usuario)
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 7: Validação final e fechamento da track
- [ ] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões
- [ ] Task: Verificar cobertura de código dos trechos novos/alterados (target >80%)
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)
