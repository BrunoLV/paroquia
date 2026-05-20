# Implementation Plan: Gestão de Membros

## Phase 1: Entidades e Repositórios Base [checkpoint: 389361]
- [x] Task: Criar pacote `br.com.nsfatima.gestao.membro.infrastructure.persistence.entity` e `br.com.nsfatima.gestao.membro.infrastructure.persistence.repository`. 387673
- [x] Task: Implementar `MembroEntity` com os campos especificados (dados pessoais, contato, vínculo usuário e datas sacramentais) e herdar de `BaseVersionedEntity`. 387673
- [x] Task: Criar script Flyway (`V031__create_membros_table.sql`) para a tabela `membros`. 389361
- [x] Task: Implementar `MembroJpaRepository`. 389361
- [x] Task: Criar testes de integração para o repositório garantindo salvar, buscar e atualizar membros. 389361
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Serviços e Casos de Uso Core [checkpoint: 391585]
- [x] Task: Criar pacote `br.com.nsfatima.gestao.membro.application.usecase`. 391378
- [x] Task: Criar `CreateMembroUseCase`, `UpdateMembroUseCase` e `GetMembroUseCase`. 391378
- [x] Task: Implementar testes unitários para os casos de uso de Membro. 391585
- [x] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: API REST e Segurança [checkpoint: 394293]
- [x] Task: Criar pacote `br.com.nsfatima.gestao.membro.api.v1.controller` e `br.com.nsfatima.gestao.membro.api.v1.dto`. 391378
- [x] Task: Criar `MembroController` com endpoints básicos (`POST`, `GET`, `PATCH`, `DELETE`). 391378
- [x] Task: Implementar paginação e filtros (nome) no endpoint de listagem. 391378
- [x] Task: Adicionar anotações de segurança (`@PreAuthorize("hasRole('ADMIN')")`). 391378
- [x] Task: Implementar testes de integração dos endpoints (MockMvc), incluindo auditoria e segurança. 394293
- [x] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)

## Phase 4: Vínculo com Organizações (Participantes) [checkpoint: 399579]
- [x] Task: Implementar `ParticipanteOrganizacaoEntity` para relacionar `Membro` a `Organizacao`. 392373
- [x] Task: Criar script Flyway (`V032__create_participantes_organizacoes_table.sql`). 392373
- [x] Task: Criar `AddParticipanteUseCase` e `ListParticipacoesUseCase`. 392373
- [x] Task: Criar endpoints no `MembroController` para listar e adicionar participação em organizações (`/api/v1/membros/{id}/organizacoes`). 399702
- [x] Task: Testar as regras de associação (evitar duplicidade no mesmo período). 399702
- [x] Task: Conductor - User Manual Verification 'Phase 4' (Protocol in workflow.md)
