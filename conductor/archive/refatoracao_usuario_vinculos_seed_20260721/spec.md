# Specification: Refatoração do Modelo de Usuário, Vínculos com Organizações e Seed Docker

## Overview
Esta especificação limpa e atualiza o modelo de dados de Usuários no backend e frontend para suportar a associação com a Igreja (Matriz/Comunidade) sem resíduos de código morto, simplifica o formulário público de cadastro de usuários, implementa a gestão de vínculos com papéis na interface administrativa, garante a filtragem dinâmica de coordenadores e atualiza os dados iniciais do Docker Seed SQL.

## Functional Requirements

1. **Refatoração e Limpeza do Modelo de Dados do Usuário (Backend & Frontend)**:
   - Adicionar o campo `igrejaId` (referenciando `MATRIZ` ou `COMUNIDADE`) e `igrejaNome` diretamente na entidade `UsuarioEntity`, DTOs (`UsuarioResponse`, `RegistrarUsuarioRequest`) e interfaces TypeScript (`Usuario`).
   - Remover código morto e atributos obsoletos em formulários e use cases relacionados a solicitações antigas de pastorais no cadastro.

2. **Simplificação do Formulário de Registro de Usuário**:
   - No cadastro/registro público de novos usuários, o formulário requer obrigatoriamente a seleção da Igreja (`igrejaId` - Matriz ou Comunidade).
   - O campo de solicitação de pastorais é removido do fluxo público de cadastro.

3. **Gestão e Visualização de Vínculos do Usuário na UI**:
   - Na tabela de Usuários e Membros (em Configurações / Administração de Usuários), é disponibilizada a opção "Gerenciar Vínculos".
   - O modal de vínculos exibe a lista completa de organizações às quais o usuário está vinculado, com seus respectivos papéis (`COORDENADOR`, `VICE_COORDENADOR`, `MEMBRO`) e a Igreja/Comunidade correspondente.
   - Administradores e autoridades com escopo de permissão podem adicionar ou remover vínculos do usuário diretamente nesta interface.

4. **Reflexo Automático de Coordenação nos Vínculos do Usuário**:
   - Ao definir um usuário como Coordenador ou Vice-Coordenador no cadastro/edição de uma Pastoral ou Movimento, o novo vínculo com o papel correspondente é refletido automaticamente na visualização de vínculos do usuário.

5. **Filtragem Dinâmica de Coordenadores por Organização Pai**:
   - No modal de cadastro/edição de Pastoral e Movimento, quando a Organização Pai (`parentId`) é alterada ou selecionada, a lista de candidatos a Coordenador e Vice-Coordenador é atualizada dinamicamente para incluir apenas usuários pertencentes àquela Igreja/Comunidade.

6. **Atualização do Docker Seed Data**:
   - O script de carga inicial (Seed SQL) no Docker é atualizado para conter dados completos de teste:
     - 1 Matriz e 1 Comunidade (ex: Comunidade Santo Agostinho).
     - Usuários de teste pré-cadastrados e associados à Matriz ou Comunidade.
     - Pastorais e Movimentos com coordenadores e membros devidamente vinculados.

## Non-Functional Requirements
- **Manutenibilidade & Clean Code**: Garantir ausência de código morto, removendo referências depreciadas.
- **Desempenho e Reatividade da UI**: A atualização do seletor de coordenadores ao mudar a organização pai deve ocorrer de forma instantânea na interface.

## Acceptance Criteria
- O modelo de usuário no backend e frontend possui o atributo `igrejaId` estruturado sem resíduos de código não utilizado.
- O formulário de registro público solicita apenas a Igreja/Comunidade e não exibe o campo de seleção de pastorais.
- A modal "Gerenciar Vínculos" na tela de Usuários exibe a lista de pastorais/movimentos do usuário com papéis corretos (`COORDENADOR`, `VICE_COORDENADOR`, `MEMBRO`).
- Ao mudar a organização pai no formulário de Pastoral, a lista de possíveis coordenadores é filtrada imediatamente para os usuários daquela igreja.
- O ambiente Docker inicializa via seed com dados pré-populados demonstrando a hierarquia completa.
