# Specification: Registro de Tipos Pastorais e Movimentos Oficiais

## Overview
Esta especificação descreve a implementação de um cadastro administrativo sistêmico de **Pastorais e Movimentos Oficiais** no sistema. Este cadastro servirá como um catálogo oficial para categorizar as organizações reais criadas na paróquia ou comunidades (ex: Catequese Matriz e Catequese São José vincular-se-ão ao tipo oficial "Pastoral da Catequese").
Além disso, serão efetuadas alterações no enum `TipoOrganizacao` para renomear `CONSELHO` para `CONSELHO_PASTORAL` e deletar `LAICATO`.

## Functional Requirements
1. **Modelagem de Dados e Banco de Dados**:
   - Criar a tabela `calendario.pastorais_movimentos_oficiais` com colunas:
     - `id` (UUID, primary key)
     - `nome` (VARCHAR, obrigatório, único)
     - `tipo` (VARCHAR/ENUM: 'PASTORAL' ou 'MOVIMENTO', obrigatório)
     - `version` (INTEGER, para controle de concorrência)
   - Adicionar a coluna `pastoral_oficial_id` (UUID, foreign key, nula para outros tipos, obrigatória para PASTORAL e MOVIMENTO) na tabela `calendario.organizacoes`.
2. **Enum `TipoOrganizacao`**:
   - Renomear o valor `CONSELHO` para `CONSELHO_PASTORAL`.
   - Deletar o valor `LAICATO`.
3. **CRUD API (Apenas ADMIN)**:
   - Endpoint `GET /api/v1/pastorais-oficiais` para listar (com paginação e filtros opcionais por tipo).
   - Endpoints `POST`, `PUT`, `DELETE` sob `/api/v1/pastorais-oficiais` protegidos para a role `ADMIN`.
4. **UI Cadastro Administrativo**:
   - Nova aba/tela nas configurações administrativas (`/settings`) sob controle do ADMIN para gerenciar o CRUD das Pastorais e Movimentos Oficiais.
5. **Vínculo no Formulário de Organização**:
   - No cadastro/edição de Organizações, se o tipo for `PASTORAL` ou `MOVIMENTO`, exibir o campo de seleção "Pastoral/Movimento Oficial".
   - Este campo deve filtrar e listar apenas os oficiais ativos que correspondam ao tipo selecionado (PASTORAL para PASTORAL, MOVIMENTO para MOVIMENTO).
   - O campo é **obrigatório** para estes tipos.
6. **Busca/Filtro por Tipo Oficial**:
   - Adicionar suporte na API e UI para filtrar a listagem de Organizações pela Pastoral/Movimento Oficial associada.
7. **Migrações e Sementes**:
   - Criar arquivo de migração V0xx do Flyway para as alterações de schema.
   - Atualizar `seed-data.sql` populando a lista oficial e vinculando todas as pastorais/movimentos previamente cadastrados aos seus respectivos oficiais.

## Non-Functional Requirements
- **Segurança**: Operações de escrita no catálogo oficial devem exigir a role `ADMIN`.
- **Experiência do Usuário**: Seleção do tipo oficial na UI deve ser reativa ao tipo de organização selecionado.

## Acceptance Criteria
- Tabela criada e Flyway executando sem problemas.
- Usuários comuns não conseguem criar/editar tipos oficiais (retorna 403).
- Salvar uma organização do tipo `PASTORAL` ou `MOVIMENTO` sem informar a correspondente oficial retorna erro de validação (tanto backend quanto frontend).
- A tela de Organograma e listagens filtram e exibem as organizações de acordo com o enum atualizado (`CONSELHO_PASTORAL`).

## Out of Scope
- Gestão de categorias adicionais ou subdivisões pastorais além de tipo (PASTORAL/MOVIMENTO).
