# Spec: Linguagem Ubíqua — `UnidadeTerritorial` vs. `GrupoParoquial`

## Overview

Track 5 de 7 da iniciativa de aderência ao SDD de domínio. Pressupõe Tracks 1-4 já implementadas.

Hoje `Organizacao`/`TipoOrganizacao` mistura dois conceitos distintos: hierarquia territorial/estrutural da
paróquia (`MATRIZ`, `COMUNIDADE`, `CLERO` — não são agrupamentos de pessoas) e grupos pastorais reais
(`PASTORAL`, `MOVIMENTO`, `CONSELHO_PASTORAL`, `CONSELHO_ECONOMICO`, `CPP`, `CPC`, `OUTRO`). A spec original
define `GrupoParoquial` como "qualquer agrupamento de fiéis", com `TipoGrupo` restrito a `PASTORAL,
MOVIMENTO, CONSELHO, MINISTERIO` — não cabe hierarquia territorial nesse conceito.

Esta track separa os dois: nova entidade `UnidadeTerritorial` (`MATRIZ`, `COMUNIDADE`, `CLERO`) para a
hierarquia estrutural, e `Organizacao`/`TipoOrganizacao` são renomeados para `GrupoParoquial`/`TipoGrupo`,
restritos aos 4 valores da spec. Conselhos (`CONSELHO_PASTORAL`, `CONSELHO_ECONOMICO`, `CPP`, `CPC`) **são**
`GrupoParoquial` com `TipoGrupo = CONSELHO` — a spec inclui `CONSELHO` explicitamente como tipo de grupo, e
um conselho é, de fato, um agrupamento de fiéis com função de governança, na mesma categoria de Pastoral/
Movimento.

## Functional Requirements

- **FR1 — Nova entidade `UnidadeTerritorial`:** `id`, `nome`, `tipo: TipoUnidadeTerritorial` (`CLERO,
  MATRIZ, COMUNIDADE`), `parentId` (auto-referência, mesma hierarquia atual: `CLERO` raiz → `MATRIZ`/
  `COMUNIDADE`), `contato`, `ativo`, + auditoria padrão. Nova tabela `calendario.unidade_territorial`,
  migrada a partir das linhas de `organizacoes` com `tipo IN (MATRIZ, COMUNIDADE, CLERO)`.
- **FR2 — Rename `Organizacao` → `GrupoParoquial`, `TipoOrganizacao` → `TipoGrupo`:** `TipoGrupo` restrito a
  `PASTORAL, MOVIMENTO, CONSELHO, MINISTERIO`. Mapeamento de valores: `CONSELHO_PASTORAL`,
  `CONSELHO_ECONOMICO`, `CPP`, `CPC` → `CONSELHO` (distinção fina entre eles passa a viver só no `nome`/
  `pastoralOficialId` do grupo, igual como Pastoral e Movimento já se distinguem entre si hoje); `OUTRO` →
  `MINISTERIO` (reaproveita o único valor da spec sem uso atual, evitando um 5º valor fora da spec).
- **FR3 — Hierarquia de parentesco pós-split:** `GrupoParoquial.parentId` continua sendo uma referência
  simples (mesma abordagem pragmática de hoje, sem FK tipado/polimórfico) que pode apontar tanto para uma
  `UnidadeTerritorial` (caso de um Conselho, hoje filho direto de Matriz/Comunidade) quanto para outro
  `GrupoParoquial` do tipo `CONSELHO` (caso de Pastoral/Movimento, hoje filhos do Conselho Pastoral) —
  preserva exatamente a hierarquia de negócio já validada em `OrganizacaoService.validateHierarchy` hoje,
  apenas dividida entre as duas tabelas.
- **FR4 — Atualização de regras de autorização que hoje distinguem por `TipoOrganizacao`:**
  `EscopoUsuarioAuthorizationService.canApproveEvents` (e qualquer outro ponto equivalente) hoje distingue
  `CONSELHO_PASTORAL` de `CPC` via `userOrgType`. Como ambos colapsam em `TipoGrupo.CONSELHO`, essas
  checagens são reescritas para não depender mais dessa distinção de tipo (usar `papel`/nome do grupo, ou
  outro critério equivalente que preserve o comportamento observável atual — a decidir na implementação, com
  teste de regressão comprovando que o comportamento não mudou).
- **FR5 — Migração de dados:** migração Flyway que lê `organizacoes`, separa as linhas entre
  `unidade_territorial` (tipos territoriais) e a `grupo_paroquial` renomeada (demais tipos, com o
  remapeamento de FR2), preservando os IDs originais para não quebrar referências existentes
  (`membro_grupo.organizacaoId` → `grupo_paroquial.id`; `papel_global` não referencia organização, não é
  afetado).
- **FR6 — Atualização de todos os consumidores:** `OrganizacaoController`/`OrganizacaoService`/
  `OrganizacaoJpaRepository` e equivalentes renomeados para `GrupoParoquial*`; `MembroGrupo.organizacaoId`
  renomeado para `grupoParoquialId`; qualquer código que hoje itera/filtra por `TipoOrganizacao` revisado
  conforme FR2/FR4.
- **FR7 — Contrato de API:** como esta é uma mudança de nomenclatura no domínio, o contrato de API pode
  mudar nesta track (diferente das Tracks 2/3, que preservaram o formato) — não há consumidor de frontend
  ainda adaptado (isso é Track 6). Os novos nomes de campo/entidade devem refletir `GrupoParoquial`/
  `TipoGrupo`/`UnidadeTerritorial` de forma consistente end-to-end no backend.

## Non-Functional Requirements

- **NFR1:** Migração Flyway idempotente/reversível em ambiente de dev.
- **NFR2:** Nenhuma regra de negócio de hierarquia/autorização muda de comportamento observável — apenas a
  estrutura/nomenclatura interna. Testes de regressão cobrindo FR3 e FR4 são obrigatórios.
- **NFR3:** Cobertura de testes mantida/ampliada para as entidades e serviços renomeados/divididos (>80%).

## Acceptance Criteria

1. Existe `UnidadeTerritorial` (`CLERO`/`MATRIZ`/`COMUNIDADE`) como entidade própria, separada de
   `GrupoParoquial`.
2. `GrupoParoquial`/`TipoGrupo` existem com os 4 valores da spec; `Organizacao`/`TipoOrganizacao` não
   existem mais no código.
3. Um `GrupoParoquial` do tipo `CONSELHO` (ex-`CPP`) e outro ex-`CONSELHO_ECONOMICO` continuam distinguíveis
   pelo `nome`, e as regras de autorização que antes dependiam do tipo continuam se comportando de forma
   equivalente (teste de regressão).
4. A hierarquia de parentesco (Clero → Matriz/Comunidade → Conselho → Pastoral/Movimento) continua validável
   nos mesmos moldes de `OrganizacaoService.validateHierarchy`, agora atravessando as duas entidades.
5. `MembroGrupo.organizacaoId` foi renomeado para `grupoParoquialId` e continua referenciando corretamente
   os grupos migrados.
6. Suíte completa (`./gradlew test`) passa sem regressões.
7. `gestao-paroquia/AGENTS.md`/skills atualizados descrevendo `UnidadeTerritorial` vs. `GrupoParoquial`, e o
   mapeamento de tipos (inclusive `OUTRO`→`MINISTERIO` e o colapso dos 4 tipos de conselho em `CONSELHO`).

## Out of Scope

- Qualquer mudança de UI/frontend — Track 6 (que também absorve o impacto de FR7 no contrato de API).
- Renomeação de `PapelOrganizacional` em si (os valores de papel não mudam nesta track, só o que eles
  referenciam via `grupoParoquialId`).
- Qualquer nova funcionalidade de `UnidadeTerritorial` além do necessário para hospedar os dados territoriais
  hoje presentes em `Organizacao` (sem CRUD novo, sem tela).
