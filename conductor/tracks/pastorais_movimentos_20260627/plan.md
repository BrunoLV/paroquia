# Implementation Plan: Cadastro de Pastorais e Movimentos

## Phase 1: Database Migration & Backend Refactoring (TDD) [checkpoint: 6eb29f3]
- [x] Task: Escrever migração de banco de dados Flyway `V036__add_coordenadores_to_organizacoes.sql` [9ab14e0]
    - [x] Criar arquivo de migração na pasta do backend adicionando as colunas `coordenador` e `vice_coordenador` à tabela `calendario.organizacoes`
- [x] Task: Atualizar o modelo de domínio `Organizacao` e entidade `OrganizacaoEntity` [fad796d]
    - [x] Atualizar `Organizacao.java` com campos, construtor e método `update`
    - [x] Atualizar `OrganizacaoEntity.java` adicionando as propriedades e anotações `@Column` JPA
- [x] Task: Atualizar DTOs e ajustar o mapeador/service para suportar coordenador e vice-coordenador [b189a30]
    - [x] Atualizar `OrganizacaoRequest.java` e `OrganizacaoResponse.java` com os novos campos
    - [x] Atualizar `OrganizacaoController.java` e `OrganizacaoService.java` para salvar os campos
- [x] Task: Atualizar permissões `@PreAuthorize` no `OrganizacaoController` [b189a30]
    - [x] Ajustar `@PreAuthorize` dos métodos `create`, `update` e `delete` para usar `hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')`
- [x] Task: Escrever testes unitários e de integração no backend e rodar `./gradlew test` [e2cb183]
    - [x] Adicionar testes em `OrganizacaoServiceTest.java` e `OrganizacaoApiIntegrationTest.java` para verificar persistência e validação de permissões RBAC
- [x] Task: Conductor - User Manual Verification 'Phase 1: Database Migration & Backend Refactoring (TDD)' (Protocol in workflow.md) 6eb29f3

## Phase 2: Frontend API & Permissions Setup (TDD) [checkpoint: 5a48960]
- [x] Task: Criar helper de permissões `canManageOrganizacoes` em `permissions.ts` e seus testes em `permissions.test.ts` [0dc0f32]
    - [x] Implementar `canManageOrganizacoes` retornando verdadeiro para papéis ADMIN, CLERO_PAROCO, CONSELHO_COORDENADOR, CONSELHO_VICE_COORDENADOR e suas variações
    - [x] Escrever casos de teste que validem cada papel
- [x] Task: Atualizar tipo `Organization` no `types.ts` [9f130d5]
    - [x] Adicionar campos `coordenador` e `viceCoordenador` como opcionais em `Organization` e DTOs correspondentes
- [x] Task: Criar cliente de API para organizações em `src/api/organizacoes.ts` com testes/mocks [f3c3fbd]
    - [x] Implementar chamadas para buscar listagem, criar, editar e inativar/deletar organizações
- [x] Task: Conductor - User Manual Verification 'Phase 2: Frontend API & Permissions Setup (TDD)' (Protocol in workflow.md) 5a48960

## Phase 3: Frontend UI Implementation & Settings Tab Integration
- [x] Task: Desenvolver o componente `OrganizacaoFormModal.tsx` [70c3cd0]
    - [x] Criar modal similar ao `LocalFormModal.tsx` com campos Nome, Tipo, Coordenador, Vice-Coordenador, Contato e validação do Nome
- [x] Task: Integrar a aba "Pastorais e Movimentos" no componente `Settings.tsx` [b807769]
    - [x] Adicionar a terceira aba "Pastorais e Movimentos"
    - [x] Implementar a tabela com listagem das organizações
    - [x] Exibir botões "Novo", "Editar" e "Inativar" / "Ativar" controlados por `canManageOrganizacoes`
- [x] Task: Implementar busca e filtragem local na listagem [b807769]
    - [x] Adicionar caixa de texto de busca para filtrar a tabela dinamicamente por Nome, Tipo ou Coordenador
- [x] Task: Rodar `npm run build` e testes do frontend `npm test` para assegurar qualidade [b7c5963]
    - [x] Corrigir eventuais erros de tipos TypeScript e assegurar que todos os testes da aplicação passam com sucesso
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend UI Implementation & Settings Tab Integration' (Protocol in workflow.md)
