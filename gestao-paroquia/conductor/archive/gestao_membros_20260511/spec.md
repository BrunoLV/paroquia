# Specification: Gestão de Membros (Fiéis/Paroquianos)

## 1. Overview
Atualmente o sistema gerencia `Usuarios` (IAM - quem faz login) e `Organizacoes` (entidades como Pastorais). No entanto, falta o conceito central de **Membro** (a pessoa física, o fiel). Este módulo introduzirá o cadastro de membros, separando a pessoa do seu acesso ao sistema, e permitindo rastrear sua participação na paróquia.

## 2. Modelagem do Domínio (Membro)

### 2.1 Campos Essenciais
*   `id`: UUID (PK)
*   `nome_completo`: VARCHAR(255), Not Null
*   `data_nascimento`: DATE
*   `email`: VARCHAR(255)
*   `telefone`: VARCHAR(20)
*   `endereco`: VARCHAR(500)
*   `usuario_id`: UUID (FK para IAM - Opcional. Permite que um membro acesse o sistema, se possuir login)
*   `ativo`: BOOLEAN (Default: true)
*   `criado_em_utc`, `atualizado_em_utc`, `version` (BaseVersionedEntity)

### 2.2 Relacionamento Sacramentais Básicos
*   `data_batismo`: DATE (Opcional)
*   `local_batismo`: VARCHAR(255) (Opcional)
*   `data_crisma`: DATE (Opcional)
*   `data_matrimonio`: DATE (Opcional)

## 3. Relacionamentos

### 3.1 Membro -> Organização
*   O módulo `membro` precisará gerenciar os vínculos com `organizacao`.
*   *Conflito de Nomenclatura:* Atualmente existe `MembroOrganizacaoEntity` (no pacote `organizacao`) que liga `Usuario` a `Organizacao` (com papel, ex: coordenador).
*   *Refatoração Necessária:* Precisamos diferenciar "Membro de uma Pastoral" (Pessoa que participa) de "Usuário Administrativo de uma Pastoral" (Acesso no IAM).
    *   **Proposta:** Manter o `MembroOrganizacaoEntity` atual para fins de **Acesso/Governança IAM**.
    *   Criar `ParticipanteOrganizacaoEntity` (no novo pacote `membro`) para vincular o `Membro` à `Organizacao`, indicando desde quando participa.

## 4. Funcionalidades (Endpoints REST)

### 4.1 Cadastro Base (`/api/v1/membros`)
*   `POST /api/v1/membros`: Criar novo membro.
*   `GET /api/v1/membros`: Listar membros (paginado, com filtros de busca por nome).
*   `GET /api/v1/membros/{id}`: Detalhar membro.
*   `PATCH /api/v1/membros/{id}`: Atualizar dados.
*   `DELETE /api/v1/membros/{id}`: Inativar (Soft delete).

### 4.2 Integração IAM
*   `PATCH /api/v1/membros/{id}/vincular-usuario`: Vincula um membro a uma conta de usuário existente no IAM.

### 4.3 Histórico de Organizações
*   `GET /api/v1/membros/{id}/organizacoes`: Lista pastorais/movimentos que a pessoa participa.
*   `POST /api/v1/membros/{id}/organizacoes`: Inscreve a pessoa em uma organização.

## 5. Security & Authorization
*   `ROLE_ADMIN`: Acesso total.
*   `ROLE_SECRETARIA`: Acesso total a criação e edição de membros (poderemos criar esta role ou usar ADMIN para simplificar a V1). Por padrão, usaremos `ADMIN`.

## 6. Out of Scope (Para Tracks Futuras)
*   Gestão financeira/dízimo de membros.
*   Emissão de certidões.