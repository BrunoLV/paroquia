# Plan: Linguagem Ubíqua — `UnidadeTerritorial` vs. `GrupoParoquial`

## Phase 0: Setup
- [x] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      refactor/linguagem-ubiqua-dominio-<data>`)
- [x] Task: Confirmar que as Tracks 1-4 estão implementadas na base atual — se não estiverem, HALT e avisar
      o usuário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 1: Entidade `UnidadeTerritorial`
- [x] Task: Escrever teste de integração (Red) que persiste uma `UnidadeTerritorial` válida e valida a
      hierarquia `CLERO` → `MATRIZ`/`COMUNIDADE`
- [x] Task: Criar migração Flyway `unidade_territorial` + migração de dados a partir de `organizacoes`
      (tipo IN MATRIZ/COMUNIDADE/CLERO), preservando IDs originais
- [x] Task: Implementar `UnidadeTerritorial`/`TipoUnidadeTerritorial` (domain/model),
      `UnidadeTerritorialEntity`, `UnidadeTerritorialJpaRepository` (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Rename `Organizacao` → `GrupoParoquial`
- [x] Task: Escrever testes (Red) para o mapeamento de `TipoGrupo` (colapso de CONSELHO_PASTORAL/
      CONSELHO_ECONOMICO/CPP/CPC → CONSELHO; OUTRO → MINISTERIO)
- [x] Task: Criar migração Flyway que renomeia `organizacoes` → `grupo_paroquial` (excluindo as linhas já
      migradas para `unidade_territorial` na Phase 1) e reescreve os valores de tipo conforme o mapeamento
- [x] Task: Renomear `Organizacao`→`GrupoParoquial`, `TipoOrganizacao`→`TipoGrupo` (domain/model,
      infrastructure/persistence/entity, repository) (Green)
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Hierarquia de parentesco pós-split
- [x] Task: Escrever testes (Red) cobrindo `GrupoParoquial.parentId` apontando para `UnidadeTerritorial`
      (Conselho) e para outro `GrupoParoquial` tipo CONSELHO (Pastoral/Movimento), replicando os casos hoje
      validados em `OrganizacaoService.validateHierarchy`
      — `GrupoParoquialServiceTest.deveCriarGrupoParoquialComConselho`,
      `.deveFalhaParaCriarConselhoSemMatrizOuComunidadePai`, `.deveFalhaParaCriarPastoralSemConselhoPai`,
      `.deveFalhaParaCriarOrgSePaiEInativo`, `.devePermitirCriandoMinisterioComNuloPai`
- [x] Task: Atualizar a lógica de validação de hierarquia para operar sobre as duas entidades (Green)
      — `GrupoParoquialService.validateHierarchy` consulta `UnidadeTerritorialJpaRepository` para CONSELHO e
      `GrupoParoquialRepository` para PASTORAL/MOVIMENTO
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Regras de autorização dependentes de tipo
- [x] Task: Escrever teste de regressão (Red) reproduzindo o comportamento atual de
      `EscopoUsuarioAuthorizationService.canApproveEvents` para `CONSELHO_PASTORAL` vs. `CPC`
      — `EscopoUsuarioAuthorizationServiceTest.conselhoCoordinatorCanApproveEventsInCommunity`
- [x] Task: Reescrever a checagem para não depender da distinção de tipo perdida no colapso (Green),
      confirmando que o teste de regressão continua passando
      — `PRESIDENTE_CPC` já é discriminador suficiente dentro de `TipoGrupo.CONSELHO`
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Atualização de todos os consumidores
- [x] Task: Escrever/ajustar testes (Red) para `OrganizacaoController`/`OrganizacaoService` (renomeados) e
      para `MembroGrupo.grupoParoquialId` (renomeado de `organizacaoId`)
- [x] Task: Renomear/atualizar controllers, services, repositories, DTOs e todo consumidor de `Organizacao`/
      `TipoOrganizacao`/`MembroGrupo.organizacaoId` em todo o codebase (Green)
      — inclui correção de colunas JPA malformadas, rotas REST, queries nativas e migração V066
- [x] Task: Refatorar se necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Documentação para agentes de IA
- [x] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante) descrevendo `UnidadeTerritorial` vs.
      `GrupoParoquial`, o mapeamento de tipos, e a atualização de nomenclatura em toda a base
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 7: Validação final e fechamento da track
- [x] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões
      — 582/582 testes passando
- [x] Task: Verificar cobertura de código dos trechos novos/alterados (target >80%)
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Desvios documentados
- O design de discriminador de propriedade (Evento/Local/AtivoPatrimonial) decidido pelo usuário foi
  resolvido caso a caso em vez de um campo discriminador explícito por entidade: `AtivoPatrimonial` e
  `Local` são exclusivamente `UnidadeTerritorial` (FK direta); `Evento`/`EventoEnvolvido` permanecem
  polimórficos (FK removida, integridade garantida em nível de aplicação via os repositórios corretos antes
  de persistir), já que não há hoje um único ponto de escrita que justifique um discriminador de coluna.
- Descoberto e corrigido durante a track: colunas JPA com nomenclatura camelCase malformada
  (`grupoParoquial_id` em vez de `grupo_paroquial_id`) em 7 entidades, resultado de um bug na renomeação
  mecânica de texto — corrigidas junto com a migração V066.
