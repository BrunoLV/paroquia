# Plan: Linguagem Ubíqua — `UnidadeTerritorial` vs. `GrupoParoquial`

## Phase 0: Setup
- [ ] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      refactor/linguagem-ubiqua-dominio-<data>`)
- [ ] Task: Confirmar que as Tracks 1-4 estão implementadas na base atual — se não estiverem, HALT e avisar
      o usuário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 1: Entidade `UnidadeTerritorial`
- [ ] Task: Escrever teste de integração (Red) que persiste uma `UnidadeTerritorial` válida e valida a
      hierarquia `CLERO` → `MATRIZ`/`COMUNIDADE`
- [ ] Task: Criar migração Flyway `unidade_territorial` + migração de dados a partir de `organizacoes`
      (tipo IN MATRIZ/COMUNIDADE/CLERO), preservando IDs originais
- [ ] Task: Implementar `UnidadeTerritorial`/`TipoUnidadeTerritorial` (domain/model),
      `UnidadeTerritorialEntity`, `UnidadeTerritorialJpaRepository` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Rename `Organizacao` → `GrupoParoquial`
- [ ] Task: Escrever testes (Red) para o mapeamento de `TipoGrupo` (colapso de CONSELHO_PASTORAL/
      CONSELHO_ECONOMICO/CPP/CPC → CONSELHO; OUTRO → MINISTERIO)
- [ ] Task: Criar migração Flyway que renomeia `organizacoes` → `grupo_paroquial` (excluindo as linhas já
      migradas para `unidade_territorial` na Phase 1) e reescreve os valores de tipo conforme o mapeamento
- [ ] Task: Renomear `Organizacao`→`GrupoParoquial`, `TipoOrganizacao`→`TipoGrupo` (domain/model,
      infrastructure/persistence/entity, repository) (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Hierarquia de parentesco pós-split
- [ ] Task: Escrever testes (Red) cobrindo `GrupoParoquial.parentId` apontando para `UnidadeTerritorial`
      (Conselho) e para outro `GrupoParoquial` tipo CONSELHO (Pastoral/Movimento), replicando os casos hoje
      validados em `OrganizacaoService.validateHierarchy`
- [ ] Task: Atualizar a lógica de validação de hierarquia para operar sobre as duas entidades (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Regras de autorização dependentes de tipo
- [ ] Task: Escrever teste de regressão (Red) reproduzindo o comportamento atual de
      `EscopoUsuarioAuthorizationService.canApproveEvents` para `CONSELHO_PASTORAL` vs. `CPC`
- [ ] Task: Reescrever a checagem para não depender da distinção de tipo perdida no colapso (Green),
      confirmando que o teste de regressão continua passando
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Atualização de todos os consumidores
- [ ] Task: Escrever/ajustar testes (Red) para `OrganizacaoController`/`OrganizacaoService` (renomeados) e
      para `MembroGrupo.grupoParoquialId` (renomeado de `organizacaoId`)
- [ ] Task: Renomear/atualizar controllers, services, repositories, DTOs e todo consumidor de `Organizacao`/
      `TipoOrganizacao`/`MembroGrupo.organizacaoId` em todo o codebase (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Documentação para agentes de IA
- [ ] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante) descrevendo `UnidadeTerritorial` vs.
      `GrupoParoquial`, o mapeamento de tipos, e a atualização de nomenclatura em toda a base
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 7: Validação final e fechamento da track
- [ ] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões
- [ ] Task: Verificar cobertura de código dos trechos novos/alterados (target >80%)
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)
