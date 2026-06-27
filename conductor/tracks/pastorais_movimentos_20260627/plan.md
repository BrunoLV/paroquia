# Implementation Plan: Cadastro de Pastorais e Movimentos

## Phase 1: Database Migration & Backend Refactoring (TDD)
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
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Database Migration & Backend Refactoring (TDD)' (Protocol in workflow.md)

## Phase 2: Frontend API & Permissions Setup (TDD)
- [ ] Task: Criar helper de permissões `canManageOrganizacoes` em `permissions.ts` e seus testes em `permissions.test.ts`
    - [ ] Implementar `canManageOrganizacoes` retornando verdadeiro para papéis ADMIN, CLERO_PAROCO, CONSELHO_COORDENADOR, CONSELHO_VICE_COORDENADOR e suas variações
    - [ ] Escrever casos de teste que validem cada papel
- [ ] Task: Atualizar tipo `Organization` no `types.ts`
    - [ ] Adicionar campos `coordenador` e `viceCoordenador` como opcionais em `Organization` e DTOs correspondentes
- [ ] Task: Criar cliente de API para organizações em `src/api/organizacoes.ts` com testes/mocks
    - [ ] Implementar chamadas para buscar listagem, criar, editar e inativar/deletar organizações
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend API & Permissions Setup (TDD)' (Protocol in workflow.md)

## Phase 3: Frontend UI Implementation & Settings Tab Integration
- [ ] Task: Desenvolver o componente `OrganizacaoFormModal.tsx`
    - [ ] Criar modal similar ao `LocalFormModal.tsx` com campos Nome, Tipo, Coordenador, Vice-Coordenador, Contato e validação do Nome
- [ ] Task: Integrar a aba "Pastorais e Movimentos" no componente `Settings.tsx`
    - [ ] Adicionar a terceira aba "Pastorais e Movimentos"
    - [ ] Implementar a tabela com listagem das organizações
    - [ ] Exibir botões "Novo", "Editar" e "Inativar" / "Ativar" controlados por `canManageOrganizacoes`
- [ ] Task: Implementar busca e filtragem local na listagem
    - [ ] Adicionar caixa de texto de busca para filtrar a tabela dinamicamente por Nome, Tipo ou Coordenador
- [ ] Task: Rodar `npm run build` e testes do frontend `npm test` para assegurar qualidade
    - [ ] Corrigir eventuais erros de tipos TypeScript e assegurar que todos os testes da aplicação passam com sucesso
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend UI Implementation & Settings Tab Integration' (Protocol in workflow.md)
