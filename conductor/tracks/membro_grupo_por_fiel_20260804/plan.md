# Plan: Vínculo de Membro por `Fiel` (`MembroGrupo`)

## Phase 0: Setup
- [ ] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      feat/membro-grupo-por-fiel-<data>`)
- [ ] Task: Confirmar que as Tracks 1 e 2 estão implementadas na base atual — se não estiverem, HALT e
      avisar o usuário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

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
