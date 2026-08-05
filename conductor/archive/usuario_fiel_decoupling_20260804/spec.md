# Spec: Desacoplamento de `UsuarioEntity` dos Dados Pessoais do `Fiel`

## Overview

Track 2 de 7 da iniciativa de aderência ao SDD de domínio. Pressupõe que a Track 1
(`fiel_vocacao_aggregate_20260804`) já foi implementada: o agregado `Fiel`, o enum `Vocacao`, o campo
`UsuarioEntity.fielId` (nullable) e os campos de auditoria já existem.

Esta track implementa efetivamente a "Regra de Ouro" no schema: remove de `UsuarioEntity` os dados pessoais
que hoje estão embutidos nela, tornando `fielId` obrigatório, e atualiza todo consumidor que hoje lê esses
dados diretamente do `Usuario`. É um refactor — nenhuma capacidade nova é exposta ao usuário final; o
contrato de API (`UsuarioResponse`) permanece com o mesmo formato de campos, apenas resolvido internamente
via `Fiel`.

Um efeito colateral necessário: o autocadastro público (`POST /api/v1/usuarios/registrar`) hoje cria um
`Usuario` com dados pessoais embutidos, sem nenhum `Fiel` pré-existente para vincular — isso deixa de ser
possível com `fielId` obrigatório. Como não há decisão de produto ainda sobre como reformular esse fluxo (ver
Track 3, que trata a criação de vínculos/promoção a partir de um `Fiel` já cadastrado), o autocadastro público
é **removido** nesta track, não substituído.

## Functional Requirements

- **FR1 — Remoção de campos pessoais de `UsuarioEntity`:** remover `nome`, `telefone`, `dataNascimento`,
  `batizado`, `primeiraComunhao`, `crismado`, `casadoIgrejaCatolica` (e os respectivos converters de
  criptografia LGPD, que passam a existir só em `FielEntity`).
- **FR2 — `fielId` obrigatório:** `UsuarioEntity.fielId` passa de nullable para `NOT NULL`. Migração Flyway
  correspondente (próximo número livre); como o sistema não está em produção, não há necessidade de
  estratégia de backfill para linhas legadas sem `fielId` — a migração pode assumir base de dev/teste vazia
  ou recriada.
- **FR3 — Atualização de consumidores:** todo código que hoje lê `nome`/`telefone`/`dataNascimento`/
  sacramentos diretamente de `UsuarioEntity` passa a resolver via `Fiel` (join/lookup por `fielId`), mantendo
  o mesmo comportamento observável. Inclui, no mínimo: `MembroMapper`, `UsuarioResponse`/mapeamento em
  `UsuarioController`/`UsuarioAdminController`, `UpdateUsuarioUseCase`, `GetUsuarioUseCase`,
  `ListUsuariosUseCase`, `ApproveUsuarioUseCase`, `UsuarioDetails`/`UsuarioDetailsService` (se aplicável).
- **FR4 — Contrato de API preservado:** `UsuarioResponse` continua expondo os mesmos campos
  (`nome`/`telefone`/`dataNascimento`/sacramentos) no mesmo formato plano — só a origem interna do dado
  muda. Nenhuma mudança de contrato é visível para o frontend nesta track.
- **FR5 — Criação de `Usuario` exige `Fiel` pré-existente:** `CreateUsuarioUseCase` (via admin) passa a
  receber um `fielId` obrigatório apontando para um `Fiel` já cadastrado, em vez de aceitar dados pessoais
  soltos. Falha com erro claro (`IllegalArgumentException`) se o `fielId` não existir.
- **FR6 — Remoção do autocadastro público:** remover `RegistrarUsuarioUseCase`, `RegistrarUsuarioRequest`, o
  endpoint `POST /api/v1/usuarios/registrar` em `UsuarioController` e a regra `permitAll()` correspondente em
  `SecurityConfig`, junto com os testes associados. **Nota para tracks futuras:** o frontend
  (`gestao-paroquia-ui`) possivelmente tem uma tela pública de autocadastro que consumia esse endpoint — isso
  fica registrado como pendência para a Track 6 (frontend), não é resolvido aqui.
- **FR7 — `ApproveUsuarioUseCase` sem alterações de contrato:** o fluxo de aprovação de usuários pendentes
  continua existindo (ele hoje é usado para o caminho antigo de autocadastro pendente); revisar se ainda faz
  sentido após a remoção de FR6, e se sim, ajustar apenas a leitura de dados pessoais para vir do `Fiel`.

## Non-Functional Requirements

- **NFR1:** Nenhum teste da suíte atual deve quebrar por remoção de campos sem que o consumidor correspondente
  seja atualizado no mesmo commit/task.
- **NFR2:** Migração Flyway idempotente/reversível em ambiente de dev.
- **NFR3:** Cobertura de testes mantida/ampliada para os casos de uso e mapeadores alterados (>80% no código
  novo/alterado, conforme workflow.md).

## Acceptance Criteria

1. `UsuarioEntity` não possui mais `nome`, `telefone`, `dataNascimento`, `batizado`, `primeiraComunhao`,
   `crismado`, `casadoIgrejaCatolica`; `fielId` é `NOT NULL`.
2. `UsuarioResponse` retorna os mesmos campos de antes, com os valores corretos, resolvidos via `Fiel`.
3. `CreateUsuarioUseCase` recusa criar um `Usuario` com `fielId` inexistente.
4. `POST /api/v1/usuarios/registrar` não existe mais (404/405); `RegistrarUsuarioUseCase` e
   `RegistrarUsuarioRequest` foram removidos do código.
5. Suíte completa (`./gradlew test`) passa sem regressões.
6. `gestao-paroquia/AGENTS.md`/skills atualizados refletindo que `Usuario` não carrega mais dados pessoais e
   que a criação de `Usuario` sempre parte de um `Fiel` existente.

## Out of Scope

- Qualquer alteração no frontend (`gestao-paroquia-ui`), incluindo a tela pública de autocadastro que ficará
  quebrada — registrado como pendência para Track 6.
- Redesenho do fluxo de aprovação de usuários pendentes além do necessário para compilar/passar nos testes.
- Alteração do modelo de vínculo/membership (`MembroEntity`/`MembroOrganizacaoEntity`) — Track 3.
- Qualquer endpoint novo de CRUD de `Fiel`.
