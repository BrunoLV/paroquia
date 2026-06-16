# Plan: CRUD de Locais (Localizações)

## Phase 1: Backend Implementation & Security (TDD)
- [x] Task: Escrever testes automatizados de segurança (RBAC) e remoção lógica para locais [16f4c83]
    - [x] Criar testes integrados em `LocalControllerIntegrationTest` simulando requisições POST/PUT/DELETE com usuários de diferentes papéis (ADMIN, COORDENADOR_CONSELHO, COORDENADOR_CLERO/Pároco) para validar sucesso e falha (403).
    - [x] Criar testes unitários em `LocalServiceTest` para validar que `deleteLocal` inativa o local (ativo = false) e não lança exceção caso o local esteja associado a eventos.
- [x] Task: Implementar as regras de segurança nos endpoints do controller de locais [16f4c83]
    - [x] Atualizar as anotações `@PreAuthorize` nos métodos `create`, `update` e `delete` do `LocalController.java` para permitir acesso a `ADMIN`, `CLERO_PAROCO`, `CONSELHO_COORDENADOR` e `CONSELHO_VICE_COORDENADOR`.
- [x] Task: Implementar a remoção lógica (inativação) no serviço de locais [16f4c83]
    - [x] Modificar o método `deleteLocal(UUID id)` no `LocalService.java` para buscar o local, alterar seu status para `ativo = false` e salvar a entidade, ao invés de excluí-la fisicamente do banco de dados.
    - [x] Remover ou contornar a validação de uso físico `localRepository.isLocalInUse(id)` na exclusão lógica, garantindo integridade de dados históricos.
- [x] Task: Executar e validar testes do backend [16f4c83]
    - [x] Rodar `./gradlew test` no backend para assegurar que todas as regras de negócio de locais e seus testes integrados passam com sucesso.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend Implementation & Security' (Protocol in workflow.md)

## Phase 2: Frontend Pages, Routing & Layout
- [ ] Task: Implementar helpers de permissões no frontend
    - [ ] Criar ou expandir o helper `canManageLocais` em `permissions.ts` utilizando a mesma regra de papéis do Ano Paroquial.
    - [ ] Escrever testes unitários em `permissions.test.ts` para validar a função `canManageLocais`.
- [ ] Task: Reestruturar as rotas e o menu lateral do frontend
    - [ ] Substituir o item "Ano Paroquial" por "Configurações" no menu lateral (`Layout.tsx`), apontando para a nova rota `/settings` (ou `/configuracoes`).
    - [ ] Atualizar a rota `/anos-paroquiais` para `/settings` (ou `/configuracoes`) em `AppRoutes.tsx`, protegendo-a com um validador de rota de configurações gerais.
- [ ] Task: Criar a página de Configurações unificada com abas
    - [ ] Criar o componente `Settings.tsx` (em `src/pages/Settings.tsx`) estruturado com abas do Material UI (`Tabs`, `Tab`).
    - [ ] Mover a tabela e lógicas do `ParishYears.tsx` para a aba 1 ("Ano Paroquial").
    - [ ] Criar um esqueleto básico para a aba 2 ("Locais") que exibirá a listagem futuramente.
- [ ] Task: Validar testes existentes do frontend
    - [ ] Atualizar os testes do `Layout.test.tsx` e `AppRoutes` que referenciam o link antigo para verificar a nova aba de configurações.
    - [ ] Rodar `npm test` para certificar que os testes unitários do frontend passam com sucesso.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend Pages, Routing & Layout' (Protocol in workflow.md)

## Phase 3: Frontend Locations CRUD & Final Integration
- [ ] Task: Implementar a listagem de locais na aba correspondente
    - [ ] Criar a tabela de locais na aba "Locais" da página de configurações, exibindo Nome, Tipo, Capacidade, Endereço e Status.
    - [ ] Adicionar formatação visual clara para locais ativos (etiqueta verde) e inativos (etiqueta vermelha/cinza).
- [ ] Task: Implementar a modal e o formulário de cadastro/edição de locais
    - [ ] Criar componente de modal de formulário (`LocalFormModal.tsx`) para cadastro e edição de localizações, contendo campos Nome, Tipo, Capacidade, Endereço e Características.
    - [ ] Implementar validação dos campos obrigatórios (Nome).
- [ ] Task: Integrar as chamadas de API no frontend para mutações de locais
    - [ ] Implementar as funções de chamada de API em um novo arquivo `src/api/locais.ts` (ou existente) para cadastrar, editar e inativar/ativar locais.
    - [ ] Conectar os botões da listagem ("Novo Local", "Editar", "Inativar" / "Ativar") às mutações do React Query, exibindo notificações de sucesso/erro.
- [ ] Task: Validar o fluxo de ponta a ponta e compilar
    - [ ] Rodar `npm run build` no frontend para garantir que não há erros de compilação ou de tipos TypeScript.
    - [ ] Executar a suíte de testes unitários do frontend (`npm test`).
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend Locations CRUD & Final Integration' (Protocol in workflow.md)
