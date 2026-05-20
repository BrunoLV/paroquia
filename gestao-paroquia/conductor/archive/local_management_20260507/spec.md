# Specification: Gerenciamento de Locais

## 1. Overview
Implementação do gerenciamento completo de cadastro de Locais (Locations) da Paróquia. O Local é uma entidade fundamental, pois eventos e atividades ocorrem em espaços físicos específicos. A gestão de locais permitirá catalogar os espaços disponíveis, sua capacidade, e características, garantindo que não sejam removidos ou inativados indevidamente se estiverem em uso.

## 2. Functional Requirements
*   **Gestão (CRUD):** Criar, ler, atualizar e excluir (ou inativar) Locais.
*   **Atributos de Local:**
    *   **Nome:** (Obrigatório, Único) Nome do local.
    *   **Endereço:** (Opcional) Endereço completo.
    *   **Capacidade:** (Opcional) Número máximo de pessoas suportadas.
    *   **Status:** Ativo ou Inativo.
    *   **Características:** (Opcional) Texto livre ou lista de facilidades (ex: projetor, ar condicionado).
*   **Hierarquia:** Lista plana (não hierárquica). Cada local é independente.
*   **Regras de Exclusão/Inativação:**
    *   Um Local **não pode** ser excluído ou inativado se possuir eventos associados a ele. O sistema deve retornar um erro de validação (Bloqueio se em uso).
*   **Permissões (RBAC):**
    *   Apenas usuários com a role de Administrador podem gerenciar (criar, atualizar, excluir/inativar) Locais.
    *   Usuários autenticados podem visualizar os Locais em listas/combos de seleção.

## 3. Non-Functional Requirements
*   **Performance:** Consultas de listagem de locais (ex: para popular um dropdown na tela de criação de evento) devem ser rápidas e cacheadas se aplicável.
*   **Integração:** Seguir o padrão RESTful e arquitetura Clean Architecture/DDD já estabelecida.

## 4. Acceptance Criteria
*   [ ] O endpoint `POST /api/v1/locais` permite a criação de um local por um Administrador.
*   [ ] O endpoint `GET /api/v1/locais` retorna a lista de locais cadastrados.
*   [ ] O endpoint `PUT /api/v1/locais/{id}` permite a atualização dos dados do local (incluindo status e capacidade).
*   [ ] O endpoint `DELETE /api/v1/locais/{id}` (ou a inativação via PUT) falha com uma mensagem clara se o local estiver vinculado a um ou mais eventos.
*   [ ] Usuários não administradores recebem `403 Forbidden` ao tentar criar, alterar ou excluir um local.

## 5. Out of Scope
*   Hierarquia de locais (Salas dentro de Prédios).