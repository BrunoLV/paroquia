# Plan: Formalização de Roles Globais (`papel_global`)

## Phase 0: Setup
- [x] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      refactor/roles-globais-vs-escopo-<data>`)
- [x] Task: Confirmar que as Tracks 1, 2 e 3 estão implementadas na base atual — se não estiverem, HALT e
      avisar o usuário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `22d1dcf`

## Phase 1: Tabela e entidade `papel_global`
- [x] Task: Escrever teste de integração (Red) que persiste um `PapelGlobal` válido e falha ao duplicar
      (`fielId`, `papel`)
- [x] Task: Criar migração Flyway para `calendario.papel_global`
- [x] Task: Implementar `PapelGlobal` (domain/model), `PapelGlobalEntity` (infrastructure/persistence/entity)
      e `PapelGlobalJpaRepository` (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `22d1dcf`

## Phase 2: Validação cruzada em MembroGrupo
- [x] Task: Escrever testes (Red) confirmando que `AdicionarMembroUseCase`/`PromoverACoordenadorUseCase`
      rejeitam `PAROCO`/`VIGARIO`/`ADMIN`/`SECRETARIA_PAROQUIAL`, e continuam aceitando `MESA_CPP`/
      `MEMBRO_CPP` normalmente
- [x] Task: Implementar a validação (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `22d1dcf`

## Phase 3: Atribuição/revogação de papel global
- [x] Task: Escrever testes (Red): sucesso ao atribuir/revogar por actor `ADMIN`; falha de autorização para
      qualquer outro actor; falha ao duplicar papel já atribuído
- [x] Task: Implementar `AtribuirPapelGlobalUseCase`/`RevogarPapelGlobalUseCase` (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `22d1dcf`

## Phase 4: Serviço central `PapelGlobalAuthorizationService`
- [x] Task: Escrever testes (Red) unitários para o novo serviço (`isGlobal`/`papeisGlobaisDe`) cobrindo
      Fiel com e sem papel global
- [x] Task: Implementar o serviço (Green)
- [x] Task: Escrever testes (Red) para cada um dos 5 pontos de bypass ad-hoc migrados
      (`EscopoUsuarioAuthorizationService`, `UsuarioAuthorizationService`, `AdicionarVinculoUseCase`,
      `EventoPatchAuthorizationService`, `EventoActorContextResolver`, e `PromoverACoordenadorUseCase` da
      Track 3), confirmando que continuam se comportando como antes
- [x] Task: Migrar os 5(+1) pontos para delegar ao novo serviço, removendo a lógica duplicada (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `22d1dcf`

  **Desvio de escopo confirmado com o usuário antes da Phase 1:** apenas 4(+1) dos 5 pontos foram migrados —
  `UsuarioAuthorizationService`, `AdicionarVinculoUseCase`, `EventoPatchAuthorizationService`
  (`assertCanBypassAgendaConflict`), `EventoActorContextResolver`, e `PromoverACoordenadorUseCase` (Track 3).
  `EscopoUsuarioAuthorizationService` (o 5º) foi deliberadamente deixado como estava: seus métodos recebem
  apenas um `PapelOrganizacional` já resolvido, sem `fielId`/`usuarioId` — diferente dos outros 4, que têm
  acesso ao ator autenticado via `SecurityContextHolder`. Migrá-lo de verdade exigiria mudar a assinatura de
  5 métodos e propagar `fielId` para todos os seus callers (vários use cases de evento/organização),
  contrariando NFR2 ("mudança cirúrgica"). O comportamento ADMIN/PAROCO/VIGARIO continua correto ali, apenas
  não centralizado — dívida documentada, não um bug.

## Phase 5: Correção do `@PreAuthorize` (gap do Vigário + Secretaria Paroquial)
- [x] Task: Escrever teste de regressão (Red) que comprova que um `Usuario` com `Fiel` `VIGARIO` é hoje
      barrado num endpoint `@PreAuthorize("hasRole('ADMIN') or hasRole('CLERO_PAROCO')")`
- [x] Task: Ajustar os endpoints afetados para usar uma expressão/método customizado que consulta
      `PapelGlobalAuthorizationService`, cobrindo os 4 papéis globais (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `22d1dcf`

## Phase 6: Authorities de papel global no login
- [x] Task: Escrever teste de integração (Red) simulando login de um Usuario com Fiel `VIGARIO`/`SECRETARIA_
      PAROQUIAL` e verificando as authorities resultantes
- [x] Task: Atualizar `LeitorVinculoExterno`/`UsuarioDetailsService` para incluir authorities de papel global
      (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `22d1dcf`

## Phase 7: Documentação para agentes de IA
- [x] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante) descrevendo `papel_global`, os 4
      papéis globais, e a distinção com papéis escopados (`MembroGrupo`, incluindo `MESA_CPP`/`MEMBRO_CPP`)
      `fa8ec60`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `fa8ec60`

  **Nota de governança (aplicada durante esta fase):** o usuário corrigiu que arquivos de configuração de
  agentes de IA (`AGENTS.md` etc.) devem conter apenas padrões técnicos/arquiteturais, nunca regras de
  negócio — o bullet de `PapelGlobal` foi escrito como ponteiro técnico (onde consultar, o que nunca
  duplicar), sem restatar as regras de autorização em si.

## Phase 8: Validação final e fechamento da track
- [x] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões
- [x] Task: Verificar cobertura de código dos trechos novos/alterados (target >80%)
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `22d1dcf`

## Phase: Review Fixes
- [x] Task: Apply review suggestions `6d9f469`

  Revisão formal (conductor-review) encontrou 1 achado Medium: o `@PreAuthorize` de
  `MembroController.promoverACoordenador` (FR5) substituiu `CLERO_PAROCO`/`CLERO_VIGARIO` pelos novos papéis
  `GLOBAL_*` em vez de adicioná-los — diferente dos dois endpoints de `UsuarioController`, que corretamente
  preservaram os papéis existentes. Como não há migração de dados populando `papel_global` a partir de
  `MembroGrupo` (V062 só cria a tabela), isso quebraria silenciosamente qualquer Fiel que ainda dependesse do
  vínculo CLERO antigo. Corrigido para ser aditivo, igual aos outros dois endpoints.
