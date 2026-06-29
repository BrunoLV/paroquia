# Especificação: Conceito de Matriz, Comunidades e Vínculos

## 1. Visão Geral (Overview)
Esta especificação define a estruturação e a implementação do conceito de **Igreja Matriz** e **Comunidades** subordinadas na Paróquia Nossa Senhora de Fátima. O Clero e a Administração permanecem centralizados na Matriz. As Pastorais e Movimentos são transversais ao nível paroquial, sendo que cada vínculo de membro (associação) deve registrar explicitamente em qual igreja/comunidade (Matriz ou comunidade específica) o membro atua.

## 2. Requisitos Funcionais

### Backend
1. **Modelos de Organização e Enum:**
   * Adicionar os tipos `MATRIZ` e `COMUNIDADE` ao enum `TipoOrganizacao`.
   * Adicionar um campo `parent_id` (UUID, opcional/nullable) à tabela `organizacoes` e à entidade `OrganizacaoEntity` para subordinar Comunidades à Matriz.
2. **Atributo no Vínculo do Membro:**
   * Adicionar a coluna `igreja_vinculo_id` (UUID, opcional, obrigatório para pastorais/movimentos) à tabela `membros_organizacao` e à entidade `MembroOrganizacaoEntity`.
3. **Regras de Negócio e Validações:**
   * Ao criar/editar um vínculo (`AddMembershipUseCase`), se a organização for do tipo `PASTORAL` ou `MOVIMENTO`, a chamada deve exigir o `igreja_vinculo_id`, validando que este ID exista e seja de uma organização do tipo `MATRIZ` ou `COMUNIDADE`.
   * No fluxo de cadastro de usuários (`RegistrarUsuarioUseCase`), permitir informar a `igreja_vinculo_id` para cada pastoral solicitada.

### Frontend
1. **Formulário de Cadastro (Sign Up):**
   * Ao selecionar pastorais/movimentos na tela de cadastro (`Login.tsx`), o usuário deverá selecionar obrigatoriamente a qual igreja/comunidade de atuação ele pertence.
2. **Gerenciamento e Vínculos (Settings):**
   * Permitir que administradores vinculem membros a pastorais selecionando a igreja de atuação (`Matriz` ou comunidade).
   * Exibir a igreja de atuação na listagem de membros vinculados.

## 3. Critérios de Aceite
* O banco de dados suporta `parent_id` nas organizações e `igreja_vinculo_id` nos membros.
* Um usuário comum não pode se cadastrar em uma pastoral sem selecionar a comunidade/matriz correspondente.
* As validações do backend rejeitam vínculos com igrejas inválidas ou nulas para pastorais/movimentos.
* Todos os testes unitários e de integração do backend e frontend passam com sucesso.
