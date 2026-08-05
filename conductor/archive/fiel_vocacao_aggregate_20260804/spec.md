# Spec: Agregado `Fiel` e Enum `Vocacao` (Contexto Paroquial)

## Overview

Track 1 de 7 de uma iniciativa de aderência do backend (`gestao-paroquia`) a uma especificação de domínio
(SDD) que separa o **Contexto Paroquial** (pessoa física / grupos pastorais) do **Contexto de Acesso/IAM**
(login/roles). Hoje não existe nenhum agregado representando a pessoa física — os dados pessoais estão
embutidos em `UsuarioEntity` (a entidade de login). Esta track introduz o agregado `Fiel`, isolado do IAM,
como fundação para as tracks seguintes (que vão desacoplar `Usuario` desses dados e trocar o vínculo de
membro de `usuarioId` para `fielId`).

Este é um passo aditivo: cria a nova entidade/tabela/enum sem alterar nenhum comportamento ou consumidor
existente. `UsuarioEntity` ganha uma referência `fielId` (nullable), mas nada além disso muda em `Usuario`
nesta track.

## Functional Requirements

- **FR1 — Entidade `Fiel`:** novo pacote de domínio `fiel/` (mesma estrutura em camadas das entidades
  existentes: `domain/model`, `infrastructure/persistence/entity`, `infrastructure/persistence/repository`)
  com os campos:
  - `id: UUID`
  - `nome: String` — obrigatório
  - `cpf: String` — obrigatório, único
  - `dataNascimento: LocalDate` — obrigatório
  - `email: String` — obrigatório (dado de contato do Fiel; independente do `username`/login de um eventual
    `Usuario` associado — não deve haver nenhuma lógica de sincronização entre os dois)
  - `telefone: String` — obrigatório
  - `endereco: String` — obrigatório
  - `vocacao: Vocacao` — obrigatório
  - `ativo: boolean` (default `true`), seguindo o padrão de soft-delete já usado em outras entidades do
    domínio (`Organizacao.ativo`, `MembroEntity.ativo`)
  - `criadoEmUtc: Instant` — obrigatório, preenchido automaticamente na criação (mesmo nome/tipo/convenção
    já usado em `AprovacaoEntity`/`CpcDecisaoEntity`/`ProjetoSolicitacaoAprovacaoEntity`)
  - `atualizadoEmUtc: Instant` — obrigatório, atualizado a cada modificação (campo novo; não existe ainda
    em nenhuma entidade do projeto, mas segue a mesma convenção de nome/tipo de `criadoEmUtc`)
  - `criadoPor: String` — obrigatório, actor (username) que cadastrou o Fiel — mesmo padrão simples de
    "actor" já usado nos publishers de auditoria do projeto (ex.: `MembroAuditPublisher`), não uma FK
  - `atualizadoPor: String` — obrigatório, actor (username) da última modificação
  - Estende `BaseVersionedEntity` (mesmo padrão de auditoria/versionamento das demais entidades JPA).
- **FR2 — Enum `Vocacao`:** valores `LEIGO, SACERDOTE, DIACONO, RELIGIOSO`, seguindo exatamente o padrão já
  estabelecido em `TipoOrganizacao`/`PapelOrganizacional`: método `fromStoredValue(String)` /
  `fromJson(String)` com `@JsonCreator`, normalização de caixa/separador, sem depender de `ordinal()`.
- **FR3 — Criptografia LGPD:** `nome`, `cpf`, `telefone`, `dataNascimento` e `endereco` usam os mesmos
  converters já aplicados em `UsuarioEntity` (`EncryptedStringConverter` / `EncryptedLocalDateConverter`),
  mantendo consistência com a regra dura de LGPD do projeto (`docs/ia/regra_dura_lgpd.md`).
- **FR4 — Tabela `fieis`:** nova migração Flyway (`V056__create_fieis_table.sql` ou próximo número livre),
  schema `calendario`, com constraint de unicidade em `cpf`.
- **FR5 — `UsuarioEntity.fielId`:** novo campo `fielId: UUID`, **nullable** nesta track (migração Flyway
  adiciona a coluna; nenhuma linha existente é preenchida — não há backfill, dado que o sistema ainda não
  está em produção e os bancos de dev/teste podem ser recriados do zero).
- **FR6 — Nenhuma mudança de comportamento em consumidores existentes:** `MembroMapper`, `UsuarioResponse`,
  telas/endpoints de cadastro de usuário e qualquer outro código que hoje lê dados pessoais de `UsuarioEntity`
  permanecem inalterados. Endpoints/casos de uso de CRUD de `Fiel` NÃO são criados nesta track (isso pode ser
  avaliado nas próximas tracks, quando `Fiel` passar a ser efetivamente consumido).
- **FR7 — Documentação para agentes de IA:** atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante em
  `.agents/skills/`) descrevendo a existência do agregado `Fiel`, a distinção Fiel (pessoa/contato) vs.
  Usuario (login/credenciais), e que dados de contato do Fiel nunca devem ser tratados como credencial.
- **FR8 — Auditoria em `UsuarioEntity`:** adicionar os mesmos 4 campos de auditoria de FR1
  (`criadoEmUtc`, `atualizadoEmUtc`, `criadoPor`, `atualizadoPor`, todos `String`/`Instant` conforme o caso)
  em `UsuarioEntity`. Migração Flyway adiciona as colunas; para linhas já existentes, `criadoEmUtc` e
  `atualizadoEmUtc` são preenchidos com o timestamp da migração (`now()`), e `criadoPor`/`atualizadoPor`
  ficam nullable apenas para as linhas legadas (sem actor histórico disponível) — daí em diante são
  obrigatórios em toda escrita nova. Os casos de uso existentes de criação/atualização de `Usuario`
  (`CreateUsuarioUseCase`, `RegistrarUsuarioUseCase`, `UpdateUsuarioUseCase`, `ApproveUsuarioUseCase`) passam
  a popular esses campos a partir do actor autenticado.

## Non-Functional Requirements

- **NFR1:** Migração Flyway deve ser idempotente e reversível dentro do próprio ambiente de dev (sem dados de
  produção a preservar).
- **NFR2:** Segue rigorosamente o padrão arquitetural (Clean Architecture / DDD) já em uso no projeto —
  camadas `domain/model`, `infrastructure/persistence`, sem acoplar `Fiel` a nada do pacote `iam`.
- **NFR3:** Cobertura de testes unitários para `Vocacao` (parsing/normalização) e testes de persistência/
  integração para `Fiel` (criação, constraint de unicidade de CPF), seguindo o padrão de testes já usado nas
  entidades análogas (`OrganizacaoDynamicResolverIntegrationTest`, etc.).

## Acceptance Criteria

1. Existe uma entidade `Fiel` persistida na tabela `calendario.fieis`, com todos os campos de FR1
   obrigatórios e criptografados conforme FR3.
2. O enum `Vocacao` existe com os 4 valores especificados e suporta `fromStoredValue`/`fromJson` como os
   enums análogos do projeto.
3. Duas tentativas de criar `Fiel` com o mesmo CPF falham por violação de constraint de unicidade.
4. `UsuarioEntity` possui o campo `fielId` (nullable), sem quebrar nenhum teste existente.
5. `Fiel` e `UsuarioEntity` possuem `criadoEmUtc`/`atualizadoEmUtc`/`criadoPor`/`atualizadoPor`; criar ou
   atualizar um `Usuario` via `CreateUsuarioUseCase`/`RegistrarUsuarioUseCase`/`UpdateUsuarioUseCase`/
   `ApproveUsuarioUseCase` popula esses campos corretamente com o actor autenticado.
6. Nenhum teste do conjunto atual da suíte quebra após a mudança (execução completa do `./gradlew test`
   passa).
7. `gestao-paroquia/AGENTS.md` (ou skill correspondente) documenta o novo agregado `Fiel` e a distinção
   Fiel vs. Usuario.

## Out of Scope

- Endpoints REST/casos de uso de CRUD para `Fiel`.
- Backfill/migração de dados pessoais existentes de `UsuarioEntity` para `Fiel`.
- Qualquer alteração em `UsuarioEntity` além da adição do campo `fielId` (remoção de `nome`/`telefone`/etc. é
  a Track 2, `usuario_fiel_decoupling`).
- Alteração do modelo de vínculo/membership (`MembroEntity`/`MembroOrganizacaoEntity`) — Track 3.
- Qualquer mudança de UI/frontend.
