# Specification: Cadastro de Pastorais e Movimentos

## 1. Overview
O objetivo deste track é fornecer uma interface e permissões de acesso para o gerenciamento de Pastorais e Movimentos no sistema de gestão paroquial, incluindo o cadastro e visualização de seus Coordenadores e Vice-Coordenadores se existirem.

## 2. Requisitos Funcionais
### 2.1 Backend / API
- **Banco de Dados**: Criar migração Flyway para adicionar as colunas `coordenador` (VARCHAR) e `vice_coordenador` (VARCHAR) na tabela `calendario.organizacoes`.
- **Modelo & DTOs**: Atualizar o modelo de domínio `Organizacao`, a entidade JPA `OrganizacaoEntity`, e os DTOs `OrganizacaoRequest` / `OrganizacaoResponse` para incluir `coordenador` e `viceCoordenador`.
- **Ajuste de Autorização**: Modificar as anotações `@PreAuthorize` no `OrganizacaoController.java` para permitir que usuários com os papéis `ADMIN`, `CLERO_PAROCO`, `CONSELHO_COORDENADOR` e `CONSELHO_VICE_COORDENADOR` possam gerenciar as organizações (criar, editar e excluir/inativar).
- **Testes de Integração**: Escrever testes integrados para validar as novas regras de segurança (RBAC) e persistência.

### 2.2 Frontend / UI
- **Helper de Permissões**: Adicionar a lógica `canManageOrganizacoes` em `src/auth/permissions.ts`.
- **Aba de Configurações**: Adicionar uma nova aba chamada "Pastorais e Movimentos" no componente `Settings.tsx` da UI.
- **Listagem e Busca**: Exibir uma tabela com todas as organizações, exibindo Nome, Tipo (ex: Pastoral, Movimento), Coordenador, Vice-Coordenador, Contato e Status (Ativo/Inativo). Permitir busca local dinâmica pelo Nome, Tipo ou Coordenadores.
- **Formulário de Cadastro/Edição**: Desenvolver um modal de formulário (`OrganizacaoFormModal.tsx`) para cadastro e edição de organizações com os campos:
  - Nome (Obrigatório)
  - Tipo (Seleção: Pastoral, Movimento, Clero, Conselho, etc.)
  - Coordenador (Opcional, texto)
  - Vice-Coordenador (Opcional, texto)
  - Contato (Opcional, e-mail/telefone)
- **Status (Inativação)**: Integrar botão para ativar/inativar organizações via requisição PUT alterando o campo `ativo` para `true`/`false`.
- **Testes**: Escrever testes unitários e de integração no frontend para validar os novos componentes e controle de acesso.

## 3. Critérios de Aceitação
- Usuários com papéis autorizados conseguem cadastrar novas pastorais/movimentos, atualizar dados e mudar o status de ativo/inativo.
- Usuários não autorizados podem visualizar a listagem, mas não têm acesso aos botões de criar, editar ou inativar.
- O filtro de busca funciona em tempo real.
- Todos os testes de backend e frontend passam com sucesso.
- O build do frontend (`npm run build`) executa sem erros.
