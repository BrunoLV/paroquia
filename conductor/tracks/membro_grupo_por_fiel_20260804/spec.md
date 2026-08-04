# Spec: Vínculo de Membro por `Fiel` (`MembroGrupo`)

## Overview

Track 3 de 7 da iniciativa de aderência ao SDD de domínio. Pressupõe Tracks 1 e 2 já implementadas: o
agregado `Fiel` existe, e `UsuarioEntity` não tem mais dados pessoais, com `fielId` obrigatório.

Esta é a mudança de maior impacto comportamental da iniciativa: hoje três tabelas paralelas e redundantes
(`membros`, `membros_organizacao`, `membros_organizacoes`) ligam um `usuarioId` a uma organização — ou seja,
só é possível registrar alguém como membro de um grupo pastoral se essa pessoa já tiver login no sistema.
Esta track consolida essas três tabelas em um único vínculo `MembroGrupo`, ligando `fielId` +
`organizacaoId` + papel, eliminando de vez a exigência de conta de login para participação pastoral —
implementando o Cenário 1 da spec original (cadastrar um Fiel/dizimista sem tocar em `Usuario`).

Como consequência direta, o mecanismo de autenticação (`LeitorVinculoExterno`/`UsuarioDetailsService`), que
hoje monta as authorities do Spring Security consultando o vínculo por `usuario_id` diretamente, precisa ser
ajustado para resolver via `Usuario.fielId` — sem isso, login deixaria de funcionar assim que o schema
mudasse. Esse ajuste é parte inseparável desta track, não pode ficar para depois.

## Functional Requirements

- **FR1 — Tabela e entidade `membro_grupo`:** nova tabela `calendario.membro_grupo` e entidade `MembroGrupo`
  (pacote `membro/` ou `organizacao/`, a definir na implementação seguindo a estrutura em camadas do
  projeto) com `id`, `fielId` (NOT NULL, FK para `fieis`), `organizacaoId` (NOT NULL), `papel` (tipado,
  seguindo o padrão `PapelOrganizacional`), `ativo` (default true), `dataEntrada`, e os 4 campos de
  auditoria já padronizados nas Tracks 1/2 (`criadoEmUtc`, `atualizadoEmUtc`, `criadoPor`, `atualizadoPor`).
- **FR2 — Remoção das 3 tabelas/entidades legadas:** `MembroEntity` (`membros`), `MembroOrganizacaoEntity`
  (`membros_organizacao`) e `ParticipanteOrganizacaoEntity` (`membros_organizacoes`) são removidas — dropadas
  via migração Flyway, sem migração/backfill de dados (sistema não produtivo). O conceito de "participação em
  múltiplos grupos" (hoje coberto por `ParticipanteOrganizacaoEntity`) passa a ser simplesmente múltiplas
  linhas de `MembroGrupo` para o mesmo `fielId`.
- **FR3 — `AdicionarMembroUseCase`:** novo caso de uso que recebe `fielId` + `organizacaoId` + `papel`
  (papéis de membro comum, ex.: `MEMBRO_PASTORAL`, `MEMBRO_MOVIMENTO`, `VOLUNTARIO_PASTORAL_LOCAL`) e cria o
  vínculo `MembroGrupo`. **Nunca lê, cria ou modifica `UsuarioEntity`.** Autorização: qualquer papel já
  autorizado hoje a gerenciar a organização-alvo (via `EscopoUsuarioAuthorizationService`/
  `UsuarioAuthorizationService`, mecanismo atual, sem alterações).
- **FR4 — `PromoverACoordenadorUseCase`:** novo caso de uso, dado um `fielId` que já é membro ativo de um
  grupo (via `MembroGrupo`), atribui um papel de liderança (`COORDENADOR`, `VICE_COORDENADOR`, `SECRETARIO`)
  e, se esse `Fiel` ainda não tiver `Usuario` vinculado, cria um `Usuario` novo apontando `fielId` para ele
  (username/senha fornecidos na chamada). **Restrito** a quem hoje possui papel `PAROCO`, `VIGARIO`,
  `MESA_CPP` ou `ADMIN` (checagem usando o mecanismo de autorização atual — `PapelOrganizacional`/roles CSV
  —, sem antecipar a formalização de Roles Globais da Track 4).
- **FR5 — Sincronização de liderança:** ambos os casos de uso continuam sincronizando
  `Organizacao.coordenador`/`viceCoordenador` quando o papel atribuído for `COORDENADOR`/`VICE_COORDENADOR`
  (mesmo comportamento hoje presente em `CreateMembroUseCase.syncLeadership`), agora usando o nome resolvido
  a partir do `Fiel`.
- **FR6 — Atualização dos demais casos de uso de vínculo:** `ListMembrosUseCase`, `UpdateMembroUseCase`,
  `GetMembroUseCase`, `AdicionarVinculoUseCase`, `RemoverVinculoUseCase`, `ListarVinculosUseCase`,
  `ListOrganizacaoMembrosUseCase` e seus controllers/DTOs (`MembroController`, `MembroRequest`,
  `MembroResponse`, `OrganizacaoController`) passam a operar sobre `MembroGrupo`/`fielId` em vez de
  `usuarioId`. Os campos de exibição hoje resolvidos via `Usuario` (nome, etc.) passam a vir do `Fiel`.
- **FR7 — Ajuste do mecanismo de autenticação:** `LeitorVinculoExterno.findVinculosAtivos` (e
  `findRolesByUserAndOrganization`) passam a resolver o `fielId` do usuário autenticado
  (`usuarios.fiel_id`) e consultar `membro_grupo` por esse `fielId`, preservando o mesmo formato de
  authority (`ROLE_<TIPO_ORGANIZACAO>_<PAPEL>`) e o mesmo comportamento de filtro por organização ativa.
  `UsuarioDetailsService` não muda de assinatura/contrato externo.
- **FR8 — Nenhuma mudança de contrato de API observável fora do necessário:** `MembroResponse` mantém os
  mesmos campos de exibição (nome, papel, organização); apenas a origem interna dos dados muda.

## Non-Functional Requirements

- **NFR1:** Migração Flyway idempotente/reversível em ambiente de dev (sem dados de produção a preservar).
- **NFR2:** O ajuste de `LeitorVinculoExterno` é coberto por teste de integração que simula login completo e
  verifica a resolução correta das authorities via `fielId`.
- **NFR3:** Cobertura de testes mantida/ampliada para os casos de uso novos e alterados (>80%).

## Acceptance Criteria

1. Existe `MembroGrupo`/tabela `membro_grupo` ligando `fielId` + `organizacaoId` + `papel`; as 3 tabelas
   legadas (`membros`, `membros_organizacao`, `membros_organizacoes`) não existem mais.
2. `AdicionarMembroUseCase` cria um vínculo de membro comum para um `Fiel` sem nunca consultar/criar/alterar
   `UsuarioEntity` — comprovável por teste que verifica zero interação com o repositório de `Usuario`.
3. `PromoverACoordenadorUseCase` só executa com sucesso quando o actor autenticado tem papel `PAROCO`,
   `VIGARIO`, `MESA_CPP` ou `ADMIN`; falha com erro de autorização para qualquer outro papel.
4. `PromoverACoordenadorUseCase`, ao promover um Fiel sem `Usuario` prévio, cria um `Usuario` com `fielId`
   apontando corretamente para ele.
5. Login continua funcionando ponta a ponta: um `Usuario` com vínculos em `membro_grupo` (via seu `fielId`)
   recebe as authorities Spring Security corretas.
6. Suíte completa (`./gradlew test`) passa sem regressões.
7. `gestao-paroquia/AGENTS.md`/skills atualizados descrevendo o vínculo `MembroGrupo` por `fielId` e os dois
   casos de uso (`AdicionarMembroUseCase` vs. `PromoverACoordenadorUseCase`).

## Out of Scope

- Formalização de Roles Globais (schema com `organizacaoId` opcional para papéis globais) — Track 4.
- Alinhamento de nomenclatura `Organizacao` → `GrupoParoquial` — Track 5.
- Qualquer mudança de UI/frontend — Track 6.
- Endpoints REST novos além dos necessários para expor `AdicionarMembroUseCase`/`PromoverACoordenadorUseCase`
  de forma minimamente funcional (design detalhado de API fica a critério da implementação, seguindo o
  padrão REST já usado em `MembroController`).
