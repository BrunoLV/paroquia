# Specification: Gestão Administrativa de Usuários e Organizações

## 1. Overview
Implementação do gerenciamento administrativo completo para as entidades de Usuário (User/IAM) e Organização (Organization). Seguindo o padrão de modularização estabelecido para 'Local', este track irá refatorar e expandir as capacidades de gestão, permitindo que administradores globais e coordenadores delegados mantenham o cadastro de membros e entidades pastorais da paróquia.

## 2. Functional Requirements

### 2.1 Módulo de Organização (`br.com.nsfatima.gestao.organizacao`)
*   **Gestão (CRUD):** Criar, listar, atualizar e excluir Organizações (Pastorais, Movimentos, etc.).
*   **Regras de Exclusão:**
    *   Impedir a exclusão de uma Organização se houver membros vinculados ou se ela for a responsável por eventos (passados ou futuros).
*   **Permissões:** Apenas usuários com `ROLE_ADMIN` podem criar ou excluir organizações.

### 2.2 Módulo de IAM/Usuário (`br.com.nsfatima.gestao.iam`)
*   **Gestão Administrativa:**
    *   **Criação:** Admin pode cadastrar novos usuários.
    *   **Roles:** Atribuição e alteração de papéis do sistema (ex: `ROLE_ADMIN`, `ROLE_COORDENADOR`, `ROLE_MEMBRO`).
    *   **Lifecycle:** Ativação e inativação de contas.
    *   **Segurança:** Reset de senha administrativo.
*   **Permissões Delegadas:**
    *   `ROLE_ADMIN`: Gestão total de todos os usuários e roles.
    *   `ROLE_COORDENADOR`: Pode gerenciar (adicionar membros, resetar senhas) de usuários vinculados à sua própria Organização.

## 3. Non-Functional Requirements
*   **Modularização:** Separação clara dos conceitos de IAM e Organização do módulo de Calendário.
*   **Segurança:** Auditoria de alterações administrativas (quem alterou qual role/status).
*   **Consistência:** Uso de exceções de domínio específicas (ex: `OrganizationInUseException`).

## 4. Acceptance Criteria
*   [ ] Endpoint `POST /api/v1/organizacoes` operacional para Admins.
*   [ ] Tentativa de deletar organização com membros retorna erro 400/409 claro.
*   [ ] Endpoint para alteração de roles de usuário restrito a perfis autorizados.
*   [ ] Coordenador de pastoral consegue listar e resetar senha de seus liderados.
*   [ ] Refatoração completa dos pacotes para os novos módulos `iam` e `organizacao`.

## 5. Out of Scope
*   Auto-cadastro de usuários (self-service signup).
*   Upload de fotos de perfil.
