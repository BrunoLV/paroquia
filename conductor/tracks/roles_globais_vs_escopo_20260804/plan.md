# Plan: Formalização de Roles Globais (`papel_global`)

## Phase 0: Setup
- [ ] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      refactor/roles-globais-vs-escopo-<data>`)
- [ ] Task: Confirmar que as Tracks 1, 2 e 3 estão implementadas na base atual — se não estiverem, HALT e
      avisar o usuário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 1: Tabela e entidade `papel_global`
- [ ] Task: Escrever teste de integração (Red) que persiste um `PapelGlobal` válido e falha ao duplicar
      (`fielId`, `papel`)
- [ ] Task: Criar migração Flyway para `calendario.papel_global`
- [ ] Task: Implementar `PapelGlobal` (domain/model), `PapelGlobalEntity` (infrastructure/persistence/entity)
      e `PapelGlobalJpaRepository` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Validação cruzada em MembroGrupo
- [ ] Task: Escrever testes (Red) confirmando que `AdicionarMembroUseCase`/`PromoverACoordenadorUseCase`
      rejeitam `PAROCO`/`VIGARIO`/`ADMIN`/`SECRETARIA_PAROQUIAL`, e continuam aceitando `MESA_CPP`/
      `MEMBRO_CPP` normalmente
- [ ] Task: Implementar a validação (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Atribuição/revogação de papel global
- [ ] Task: Escrever testes (Red): sucesso ao atribuir/revogar por actor `ADMIN`; falha de autorização para
      qualquer outro actor; falha ao duplicar papel já atribuído
- [ ] Task: Implementar `AtribuirPapelGlobalUseCase`/`RevogarPapelGlobalUseCase` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Serviço central `PapelGlobalAuthorizationService`
- [ ] Task: Escrever testes (Red) unitários para o novo serviço (`isGlobal`/`papeisGlobaisDe`) cobrindo
      Fiel com e sem papel global
- [ ] Task: Implementar o serviço (Green)
- [ ] Task: Escrever testes (Red) para cada um dos 5 pontos de bypass ad-hoc migrados
      (`EscopoUsuarioAuthorizationService`, `UsuarioAuthorizationService`, `AdicionarVinculoUseCase`,
      `EventoPatchAuthorizationService`, `EventoActorContextResolver`, e `PromoverACoordenadorUseCase` da
      Track 3), confirmando que continuam se comportando como antes
- [ ] Task: Migrar os 5(+1) pontos para delegar ao novo serviço, removendo a lógica duplicada (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Correção do `@PreAuthorize` (gap do Vigário + Secretaria Paroquial)
- [ ] Task: Escrever teste de regressão (Red) que comprova que um `Usuario` com `Fiel` `VIGARIO` é hoje
      barrado num endpoint `@PreAuthorize("hasRole('ADMIN') or hasRole('CLERO_PAROCO')")`
- [ ] Task: Ajustar os endpoints afetados para usar uma expressão/método customizado que consulta
      `PapelGlobalAuthorizationService`, cobrindo os 4 papéis globais (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Authorities de papel global no login
- [ ] Task: Escrever teste de integração (Red) simulando login de um Usuario com Fiel `VIGARIO`/`SECRETARIA_
      PAROQUIAL` e verificando as authorities resultantes
- [ ] Task: Atualizar `LeitorVinculoExterno`/`UsuarioDetailsService` para incluir authorities de papel global
      (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 7: Documentação para agentes de IA
- [ ] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante) descrevendo `papel_global`, os 4
      papéis globais, e a distinção com papéis escopados (`MembroGrupo`, incluindo `MESA_CPP`/`MEMBRO_CPP`)
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 8: Validação final e fechamento da track
- [ ] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões
- [ ] Task: Verificar cobertura de código dos trechos novos/alterados (target >80%)
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)
