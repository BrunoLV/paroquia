# Specification: CRUD de Locais (Localizações)

## Overview (Visão Geral)
Esta especificação define a implementação do CRUD completo (Cadastro, Edição, Remoção Lógica e Listagem) de locais (localizações físicas da paróquia) no sistema. A remoção de locais será estritamente lógica (inativação) e as ações de alteração de estado (cadastro, edição e remoção/inativação) serão restritas a papéis de coordenação e administração. Qualquer usuário logado poderá visualizar a listagem e os detalhes dos locais.

## Requisitos Funcionais

### Backend
1. **Estrutura de Dados (Local):**
   * A entidade `LocalEntity` e o modelo `Local` contêm: `id`, `nome`, `tipo` (ex: `IGREJA`, `SALAO`, `SALA`, `OUTRO`), `endereco`, `capacidade`, `caracteristicas`, `ativo` (status lógico).
2. **Remoção Lógica (Inativação):**
   * Modificar o método `deleteLocal(id)` no `LocalService` para que atualize o status do local para inativo (`ativo = false`) e persista no banco, em vez de excluir a linha fisicamente.
   * A verificação se o local está em uso por eventos (`localRepository.isLocalInUse`) não deve bloquear a inativação, permitindo a inativação de locais já associados a eventos históricos.
3. **Controle de Acesso (RBAC):**
   * Restringir as operações de mutação (`POST /api/v1/locais`, `PUT /api/v1/locais/{id}`, `DELETE /api/v1/locais/{id}`) usando `@PreAuthorize`. Apenas usuários com as seguintes autoridades terão acesso:
     * `ROLE_ADMIN`
     * `ROLE_CLERO_PAROCO`
     * `ROLE_CONSELHO_COORDENADOR`
     * `ROLE_CONSELHO_VICE_COORDENADOR`
   * A visualização (`GET /api/v1/locais` e `GET /api/v1/locais/{id}`) continuará acessível a qualquer usuário logado.

### Frontend
1. **Página de Configurações:**
   * Criar a rota `/configuracoes` e substituir a rota `/anos-paroquiais`.
   * Atualizar o menu lateral (`Layout.tsx`) para exibir a opção "Configurações" (com o ícone `SettingsIcon`) apenas para perfis autorizados.
   * Implementar a página de configurações dividida em duas abas (Tabs):
     * **Aba 1: Ano Paroquial** (exibe a tabela/ações atualmente contidas em `ParishYears.tsx`).
     * **Aba 2: Locais** (exibe a listagem de locais e botões para gerenciar).
2. **Gestão de Locais na UI:**
   * Tabela com colunas: Nome, Tipo, Capacidade, Endereço, Status (Ativo/Inativo com destaque visual).
   * Botão para "Novo Local" que abre modal com formulário.
   * Botão para "Editar" local na linha correspondente da tabela.
   * Botão para "Inativar" (Desativar) ou "Ativar" (Reativar) locais diretamente na lista.
   * Validação dos campos do formulário (Nome obrigatório e demais opcionais).

## Critérios de Aceitação
1. Usuário comum (`MEMBRO_PASTORAL`) não vê a opção de menu "Configurações" e tem o acesso bloqueado se tentar acessar `/configuracoes` diretamente.
2. Usuário com cargo de administração ou coordenação do conselho (`ADMIN`, `COORDENADOR_CONSELHO`, `COORDENADOR_CLERO` atuando como Pároco) tem acesso à tela de Configurações com ambas as abas.
3. É possível criar, editar e inativar/ativar locais por usuários permitidos.
4. Locais inativos continuam aparecendo no banco de dados e listagens com marcador correspondente.
5. Requisições de mutação não autorizadas na API retornam `403 Forbidden`.
