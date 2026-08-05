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
- [x] Task: Escrever teste de integração (Red) que persiste um `MembroGrupo` válido (`fielId` +
      `organizacaoId` + `papel`) `cc730dc`
- [x] Task: Criar migração Flyway que dropa `membros`, `membros_organizacao`, `membros_organizacoes` e cria
      `calendario.membro_grupo` `cc730dc`
- [x] Task: Implementar `MembroGrupoEntity` (infrastructure/persistence/entity) e `MembroGrupoJpaRepository`
      (Green) `cc730dc`
- [x] Task: Refatorar se necessário — não necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `cc730dc`

## Phase 2: `AdicionarMembroUseCase`
- [x] Task: Escrever testes (Red): cria vínculo com sucesso para um `fielId` existente; falha se `fielId` ou
      `organizacaoId` não existirem; teste que verifica explicitamente **zero interação** com o repositório
      de `Usuario` `cc730dc`
- [x] Task: Implementar `AdicionarMembroUseCase`, incluindo a sincronização de liderança (FR5) via nome do
      `Fiel` (Green) `cc730dc`
- [x] Task: Refatorar se necessário — extraído `LiderancaSyncSupport` compartilhado com
      `UpdateMembroUseCase`/`PromoverACoordenadorUseCase` `cc730dc`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `cc730dc`

## Phase 3: `PromoverACoordenadorUseCase`
- [x] Task: Escrever testes (Red): sucesso ao promover um Fiel membro sem Usuario prévio (cria Usuario
      vinculado); sucesso ao promover um Fiel que já tem Usuario (não duplica); falha se o `fielId` não for
      membro ativo do grupo; falha de autorização para actor sem papel PAROCO/VIGARIO/MESA_CPP/ADMIN
      `cc730dc`
- [x] Task: Implementar `PromoverACoordenadorUseCase`, reutilizando a checagem de autorização atual — via
      novo método dedicado `UsuarioAuthorizationService.requireParocoVigarioMesaCppOuAdmin()` (Green)
      `cc730dc`
- [x] Task: Refatorar se necessário — não necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `cc730dc`

## Phase 4: Migração dos consumidores existentes de vínculo
- [x] Task: Escrever/ajustar testes (Red) para `ListMembrosUseCase`, `UpdateMembroUseCase`,
      `GetMembroUseCase`, `AdicionarVinculoUseCase`, `RemoverVinculoUseCase`, `ListarVinculosUseCase`,
      `ListOrganizacaoMembrosUseCase` operando sobre `MembroGrupo`/`fielId` `cc730dc`
- [x] Task: Atualizar esses casos de uso e seus controllers/DTOs (`MembroController`, `MembroRequest`,
      `MembroResponse`, `OrganizacaoController`) para a nova estrutura, resolvendo dados de exibição via
      `Fiel` (Green) `cc730dc`
- [x] Task: Refatorar se necessário — removidos `ParticipanteOrganizacaoEntity` e seus 2 use cases/endpoints
      (participação múltipla vira múltiplas chamadas a `AdicionarMembroUseCase`); `MembroResponse`/
      `OrganizacaoMembroResponse` trocaram `usuarioId/usuarioNome` por `fielId/fielNome` `cc730dc`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `cc730dc` `b6bec1a`

  **Escopo fundido com as Phases 1, 3 e 5 (confirmado com o usuário antes da Phase 1):** a migração V061
  dropa as 3 tabelas legadas na mesma transação lógica em que cria `membro_grupo` — não existe um estado
  intermediário compilável onde só a Phase 1 está pronta, já que toda entidade/repositório/caso de uso que
  referenciava as tabelas antigas (Phase 4) e o mecanismo de autenticação que consultava
  `membros_organizacao` por `usuario_id` (Phase 5) quebrariam imediatamente. Todas as 5 fases foram
  implementadas e commitadas juntas no commit `cc730dc`. 552 testes, 0 falhas (mais detalhes no git note
  desse commit, incluindo a checagem de escopo territorial via `Usuario.igrejaId` removida de
  `AdicionarVinculoUseCase` por não existir equivalente em `Fiel`).

## Phase 5: Ajuste do mecanismo de autenticação
- [x] Task: Escrever teste de integração (Red) que simula login completo (Usuario com fielId vinculado a
      linhas de `membro_grupo`) e verifica que as authorities Spring Security resultantes estão corretas
      `cc730dc`
- [x] Task: Atualizar `LeitorVinculoExterno.findVinculosAtivos`/`findRolesByUserAndOrganization` para
      resolver `fielId` via `usuarios.fiel_id` e consultar `membro_grupo` (Green) `cc730dc`
- [x] Task: Confirmar que `UsuarioDetailsService` continua funcionando sem mudança de contrato externo —
      confirmado: assinatura/contrato inalterados `cc730dc`
- [x] Task: Refatorar se necessário — não necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `cc730dc`

  **Executada junto às Phases 1-4** — ver nota na Phase 4 acima.

## Phase 6: Documentação para agentes de IA
- [x] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante) descrevendo o vínculo `MembroGrupo`
      por `fielId`, e a distinção entre `AdicionarMembroUseCase` (nunca toca Usuario) e
      `PromoverACoordenadorUseCase` (restrito, pode criar Usuario) `f039f70`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `078c8b4`

## Phase 7: Validação final e fechamento da track
- [x] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões `a9cb30b`
- [x] Task: Verificar cobertura de código dos trechos novos/alterados (target >80%) `a9cb30b`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `71f1e82`

  **Resultado da cobertura:** `PromoverACoordenadorUseCase` 100%, `ListarVinculosUseCase` 100%,
  `RemoverVinculoUseCase` 100%, `LiderancaSyncSupport` 100%, `GetMembroUseCase`/`ListMembrosUseCase` 100%,
  `MembroMapper` 96%, `AdicionarVinculoUseCase` 93%, `AdicionarMembroUseCase` 90%,
  `UpdateMembroUseCase`/`ListOrganizacaoMembrosUseCase` 88%, `UsuarioAuthorizationService` 87%,
  `OrganizacaoRepositoryImpl` 81% (todos elevados nesta fase com testes dedicados a
  `PromoverACoordenadorUseCase`, `ListarVinculosUseCase`, `RemoverVinculoUseCase` e
  `UsuarioAuthorizationService.requireParocoVigarioMesaCppOuAdmin`, que estavam sem cobertura própria).
  `MembroGrupoEntity` 78%, `MembroController` 67%, `ApproveUsuarioUseCase` 76%, `LeitorVinculoExterno` 71%
  ficaram abaixo da meta — ramos não cobertos são getters/setters pouco usados da entidade, endpoints
  administrativos de CRUD (get/list/update/toggle) já indiretamente exercitados mas sem asserção dedicada, e
  o método `findRolesByUserAndOrganization` de `LeitorVinculoExterno` (não alterado nesta track). Suíte
  completa: 570 testes, 0 falhas.
