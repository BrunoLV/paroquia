# Spec: Formalização de Roles Globais (`papel_global`)

## Overview

Track 4 de 7 da iniciativa de aderência ao SDD de domínio. Pressupõe Tracks 1, 2 e 3 já implementadas: `Fiel`
existe, `Usuario` não tem mais dados pessoais (`fielId` obrigatório), e o vínculo de membro é `MembroGrupo`
(`fielId` + `organizacaoId` + `papel`).

Hoje o "acesso irrestrito" de Pároco/Vigário/Admin é reimplementado como um `if (role == PAROCO || role ==
VIGARIO || role == ADMIN) return true;` copiado em pelo menos 5 lugares diferentes
(`EscopoUsuarioAuthorizationService`, `UsuarioAuthorizationService`, `AdicionarVinculoUseCase`,
`EventoPatchAuthorizationService`, `EventoActorContextResolver`), e mesmo esses papéis ainda exigem uma linha
de `MembroGrupo` com `organizacaoId` não-nulo (uma "organização fictícia" só para satisfazer o schema).
Existe ainda uma inconsistência real hoje: `UsuarioEntity.roles` (CSV usado em `@PreAuthorize`) reconhece
`ROLE_ADMIN` e `ROLE_CLERO_PAROCO`, mas **não tem nenhuma authority equivalente para Vigário** — um Vigário
pode passar em `EscopoUsuarioAuthorizationService` e ainda assim ser barrado por um `@PreAuthorize` que só
reconhece `CLERO_PAROCO`. `SECRETARIA_PAROQUIAL` (papel global previsto na spec original) não existe em
lugar nenhum do código hoje.

Esta track formaliza "Role Global" como conceito de primeiro nível: uma nova tabela `papel_global`
(`fielId` + `papel`, **sem** `organizacaoId`) para os 4 papéis que a spec define como globais — `PAROCO`,
`VIGARIO`, `ADMIN`, `SECRETARIA_PAROQUIAL` — e um único serviço central que os ~5 pontos de bypass hoje
duplicados passam a consultar. `MESA_CPP` e `MEMBRO_CPP` **não** entram nesse domínio — continuam sendo
papéis escopados normais, atribuídos via `MembroGrupo`, sem nenhuma mudança de comportamento.

## Functional Requirements

- **FR1 — Tabela e entidade `papel_global`:** nova tabela `calendario.papel_global` e entidade
  `PapelGlobal` com `id`, `fielId` (NOT NULL, FK para `fieis`), `papel` (enum restrito a `PAROCO, VIGARIO,
  ADMIN, SECRETARIA_PAROQUIAL`), `ativo` (default true), e os 4 campos de auditoria padronizados nas Tracks
  anteriores. **Sem** coluna `organizacaoId` — essa é a diferença estrutural chave frente a `MembroGrupo`.
  Constraint de unicidade em (`fielId`, `papel`) para evitar duplicidade do mesmo papel global para a mesma
  pessoa.
- **FR2 — Validação cruzada:** `AdicionarMembroUseCase`/`PromoverACoordenadorUseCase` (Track 3) passam a
  rejeitar a atribuição de `PAROCO`, `VIGARIO`, `ADMIN` ou `SECRETARIA_PAROQUIAL` via `MembroGrupo` — esses
  papéis só podem ser concedidos através do novo fluxo desta track (FR3). `MESA_CPP`/`MEMBRO_CPP` continuam
  liberados normalmente via `MembroGrupo`, sem qualquer alteração.
- **FR3 — `AtribuirPapelGlobalUseCase`/`RevogarPapelGlobalUseCase`:** novos casos de uso para conceder/
  revogar um papel global a um `Fiel` já existente. **Restritos a `ADMIN`** (é a única forma hoje de existir
  um "primeiro" Pároco/Vigário/Secretaria no sistema; um `ADMIN` inicial é assumido como pré-existente via
  seed/configuração, fora do escopo desta track).
- **FR4 — Serviço central `PapelGlobalAuthorizationService`:** novo serviço com um método único (ex.:
  `isGlobal(fielId): boolean` e/ou `papeisGlobaisDe(fielId): Set<PapelGlobal>`) consultando a tabela
  `papel_global`. Os 5 pontos hoje com checagem ad-hoc (`EscopoUsuarioAuthorizationService`,
  `UsuarioAuthorizationService`, `AdicionarVinculoUseCase`, `EventoPatchAuthorizationService`,
  `EventoActorContextResolver`) passam a delegar a esse serviço em vez de reimplementar o `if` localmente.
  `PromoverACoordenadorUseCase` (Track 3) também passa a usar este serviço para sua checagem de autorização
  (substituindo o mecanismo ad-hoc que a Track 3 introduziu como provisório).
- **FR5 — Correção do gap do Vigário / adição de Secretaria Paroquial no `@PreAuthorize`:** nos endpoints
  onde `@PreAuthorize` hoje testa `hasRole('ADMIN') or hasRole('CLERO_PAROCO')` (ou equivalente) como forma
  de dar acesso irrestrito, a expressão passa a refletir corretamente os 4 papéis globais (incluindo Vigário
  e Secretaria Paroquial), preferencialmente via um método customizado que consulta
  `PapelGlobalAuthorizationService` em vez de authorities estáticas do CSV `UsuarioEntity.roles`.
  **Fora de escopo:** os demais `@PreAuthorize` que testam authorities específicas de conselho/pastoral
  (`CONSELHO_COORDENADOR`, `PRESIDENTE_CPC`, `MESA_CPP`, `MEMBRO_CPP`, `MEMBRO_CPC`, `COORDENADOR`, `USER`
  etc.) permanecem exatamente como estão.
- **FR6 — Resolução de authorities no login:** `LeitorVinculoExterno`/`UsuarioDetailsService` (ajustados na
  Track 3 para ler `membro_grupo` via `fielId`) passam também a incluir as authorities correspondentes aos
  papéis globais do `Fiel` do usuário autenticado (formato a definir na implementação, ex.:
  `ROLE_GLOBAL_<PAPEL>`), preservando o formato já existente para papéis escopados
  (`ROLE_<TIPO_ORGANIZACAO>_<PAPEL>`).

## Non-Functional Requirements

- **NFR1:** Migração Flyway idempotente/reversível em ambiente de dev (sem dados de produção a preservar).
- **NFR2:** Nenhuma authority/`@PreAuthorize` fora do escopo de FR5 é tocada — mudança cirúrgica, não uma
  reescrita geral do sistema de permissões.
- **NFR3:** Cobertura de testes para o novo serviço central e para os 5 pontos de bypass migrados,
  incluindo um teste específico que comprova a correção do gap do Vigário.

## Acceptance Criteria

1. Existe a tabela/entidade `papel_global` (`fielId` + `papel`, sem `organizacaoId`), com unicidade em
   (`fielId`, `papel`).
2. `AdicionarMembroUseCase`/`PromoverACoordenadorUseCase` rejeitam `PAROCO`/`VIGARIO`/`ADMIN`/
   `SECRETARIA_PAROQUIAL` via `MembroGrupo`.
3. `AtribuirPapelGlobalUseCase` só executa com sucesso para actor com papel `ADMIN`; `RevogarPapelGlobalUseCase`
   idem.
4. Os 5 pontos de bypass hoje ad-hoc consultam `PapelGlobalAuthorizationService` em vez de reimplementar a
   checagem localmente (comprovável por teste unitário que injeta um `Fiel` com/sem papel global e verifica o
   resultado, sem depender de string literal de authority).
5. Um `Usuario` cujo `Fiel` tem papel `VIGARIO` em `papel_global` passa em um endpoint protegido pelo
   `@PreAuthorize` corrigido de FR5 (teste de regressão específico do gap identificado).
6. Suíte completa (`./gradlew test`) passa sem regressões.
7. `gestao-paroquia/AGENTS.md`/skills atualizados descrevendo `papel_global`, os 4 papéis globais, e a
   distinção com papéis escopados (`MembroGrupo`, incluindo `MESA_CPP`/`MEMBRO_CPP`, que permanecem lá).

## Out of Scope

- Reescrita dos ~43 demais usos de `@PreAuthorize` não relacionados a Role Global.
- Qualquer mudança em `MESA_CPP`/`MEMBRO_CPP` — permanecem exatamente como definidos na Track 3.
- Alinhamento de nomenclatura `Organizacao` → `GrupoParoquial` — Track 5.
- Qualquer mudança de UI/frontend — Track 6.
- Seed/bootstrap do primeiro usuário `ADMIN` do sistema (assumido como pré-existente).
