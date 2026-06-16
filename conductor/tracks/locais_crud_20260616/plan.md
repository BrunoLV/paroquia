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
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend Implementation & Security' (Protocol in workflow.md) [checkpoint: 9cd4b52]

## Phase 2: Frontend Pages, Routing & Layout [checkpoint: f1d418c]
- [x] Task: Implementar helpers de permissões no frontend [8b85c2e]
    - [x] Criar ou expandir o helper `canManageLocais` em `permissions.ts` utilizando a mesma regra de papéis do Ano Paroquial.
    - [x] Escrever testes unitários em `permissions.test.ts` para validar a função `canManageLocais`.
- [x] Task: Reestruturar as rotas e o menu lateral do frontend [8b85c2e]
    - [x] Substituir o item "Ano Paroquial" por "Configurações" no menu lateral (`Layout.tsx`), apontando para a nova rota `/settings` (ou `/configuracoes`).
    - [x] Atualizar a rota `/anos-paroquiais` para `/settings` (ou `/configuracoes`) em `AppRoutes.tsx`, protegendo-a com um validador de rota de configurações gerais.
- [x] Task: Criar a página de Configurações unificada com abas [8b85c2e]
    - [x] Criar o componente `Settings.tsx` (em `src/pages/Settings.tsx`) estruturado com abas do Material UI (`Tabs`, `Tab`).
    - [x] Mover a tabela e lógicas do `ParishYears.tsx` para a aba 1 ("Ano Paroquial").
    - [x] Criar um esqueleto básico para a aba 2 ("Locais") que exibirá a listagem futuramente.
- [x] Task: Validar testes existentes do frontend [8b85c2e]
    - [x] Atualizar os testes do `Layout.test.tsx` e `AppRoutes` que referenciam o link antigo para verificar a nova aba de configurações.
    - [x] Rodar `npm test` para certificar que os testes unitários do frontend passam com sucesso.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Frontend Pages, Routing & Layout' (Protocol in workflow.md) [checkpoint: f1d418c]

## Phase 3: Frontend Locations CRUD & Final Integration [checkpoint: c831d83]
- [x] Task: Implementar a listagem de locais na aba correspondente [bb9e5dd]
    - [x] Criar a tabela de locais na aba "Locais" da página de configurações, exibindo Nome, Tipo, Capacidade, Endereço e Status.
    - [x] Adicionar formatação visual clara para locais ativos (etiqueta verde) e inativos (etiqueta vermelha/cinza).
- [x] Task: Implementar a modal e o formulário de cadastro/edição de locais [bb9e5dd]
    - [x] Criar componente de modal de formulário (`LocalFormModal.tsx`) para cadastro e edição de localizações, contendo campos Nome, Tipo, Capacidade, Endereço e Características.
    - [x] Implementar validação dos campos obrigatórios (Nome).
- [x] Task: Integrar as chamadas de API no frontend para mutações de locais [bb9e5dd]
    - [x] Implementar as funções de chamada de API em um novo arquivo `src/api/locais.ts` (ou existente) para cadastrar, editar e inativar/ativar locais.
    - [x] Conectar os botões da listagem ("Novo Local", "Editar", "Inativar" / "Ativar") às mutações do React Query, exibindo notificações de sucesso/erro.
- [x] Task: Validar o fluxo de ponta a ponta e compilar [bb9e5dd]
    - [x] Rodar `npm run build` no frontend para garantir que não há erros de compilação ou de tipos TypeScript.
    - [x] Executar a suíte de testes unitários do frontend (`npm test`).
- [x] Task: Conductor - User Manual Verification 'Phase 3: Frontend Locations CRUD & Final Integration' (Protocol in workflow.md) [checkpoint: c831d83]
