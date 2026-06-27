# Specification: Cadastro e Vínculo de Usuários com Criptografia

## Overview
Esta funcionalidade visa implementar o fluxo completo de cadastro autônomo de usuários a partir da tela de login, a governança e aprovação desses cadastros, e as regras de autorização para o gerenciamento de vínculos e papéis de liderança (coordenação, vice-coordenador, pároco, etc.) dentro das pastorais e movimentos, garantindo a criptografia de dados sensíveis em repouso no banco de dados.

## Functional Requirements
1. **Cadastro Público (Sign Up)**:
   * Disponibilizar uma opção "Cadastre-se" na tela de login (`Login.tsx`).
   * Formulário de cadastro contendo: Nome completo, E-mail (username), Senha, Telefone/Contato, Data de Nascimento e, opcionalmente, seleção de pastorais/movimentos que deseja se vincular.
   * O usuário é persistido com `enabled = false` no banco de dados, necessitando de aprovação prévia.

2. **Aprovação e Gerenciamento de Usuários (Aba nas Configurações)**:
   * Criar a aba **"Membros / Usuários"** no painel de Configurações (`Settings.tsx`), disponível apenas para perfis de alta liderança (`ADMIN`, `CLERO_PAROCO`, `CONSELHO_COORDENADOR`).
   * Listar todos os usuários. Usuários com `enabled = false` exibirão botão para "Aprovar" (definir `enabled = true` e atribuir `ROLE_USER`) ou "Rejeitar/Excluir".

3. **Regras de Vínculo com Organizações (Membros da Pastoral/Movimento)**:
   * Um usuário pode pertencer a múltiplas pastorais/movimentos.
   * **Quem pode vincular**:
     * Administradores/Párocos/Conselho Coordenador podem vincular qualquer usuário a qualquer pastoral/movimento.
     * Coordenadores de uma pastoral/movimento específica podem vincular usuários **apenas** a essa pastoral/movimento que coordenam.

4. **Governança de Papéis de Liderança (Coordenação)**:
   * Pastorais e movimentos possuem no máximo **1 Coordenador** e **1 Vice-Coordenador**.
   * Ao atribuir o papel de coordenação a um usuário, garante-se a unicidade por organização (substituindo o anterior, se houver).
   * **Hierarquia de Definição de Papéis**:
     * `COORDENADOR` e `VICE` de Conselho só podem ser definidos por `CLERO_PAROCO` (Pároco).
     * `CLERO_PAROCO` só pode ser definido por `ADMIN`.

5. **Exibição na Tela de Pastoral/Movimento**:
   * A aba de Pastorais e Movimentos deve exibir dinamicamente o nome do Coordenador e Vice-Coordenador resolvidos a partir de seus vínculos de papel reais na organização.

## Technical Requirements
* **Criptografia de Dados em Repouso (Field-Level Encryption)**:
  * Os campos `nome`, `telefone` e `data_nascimento` no banco de dados serão armazenados de forma criptografada por meio de conversores JPA (`AttributeConverter`) utilizando o algoritmo AES-256 e uma chave secreta injetada por propriedade.
* **Migração SQL**: Adicionar colunas `nome` (VARCHAR), `telefone` (VARCHAR), `data_nascimento` (VARCHAR - para suportar o formato criptografado) à tabela `calendario.usuarios`.
* **Segurança**: Expor o endpoint `/api/v1/usuarios/registrar` publicamente no `SecurityConfig.java`.
* **Endpoints Administrativos**:
  * `PUT /api/v1/usuarios/{id}/approve` para aprovação de usuários.
  * Validações de regras hierárquicas e de unicidade no backend.
