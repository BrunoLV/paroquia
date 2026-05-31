# Especificação: Tela de Gerenciamento de Projetos no Frontend

## 1. Visão Geral
Este track consiste em implementar a interface de gerenciamento de projetos paroquiais no frontend (`gestao-paroquia-ui`). Ele integrará os endpoints já existentes na API e implementará um novo padrão de navegação com menu lateral (Drawer) retrátil via botão hambúrguer.

## 2. Requisitos Funcionais

### 2.1 Navegação Lateral (Sidebar Drawer)
* Transferir todos os itens do menu superior (`AppBar`) para uma barra lateral esquerda retrátil (`Drawer`).
* Adicionar um ícone de menu hambúrguer na `AppBar` para expandir/ocultar a barra lateral.
* A barra lateral deve conter atalhos para:
  - Dashboard
  - Calendário
  - Projetos (Novo)
  - Aprovações (Exibido apenas para usuários com permissão `canApprove`)

### 2.2 Gerenciamento de Projetos (CRUD)
* **Listagem de Projetos**: Exibir uma tabela ou lista de cards com nome, descrição, datas de início e fim (formatadas localmente), organização responsável e status (`ATIVO` / `INATIVO`).
* **Criação de Projetos**: Formulário para preenchimento de:
  - Nome (obrigatório, máximo 160 caracteres)
  - Descrição (opcional, máximo 2000 caracteres)
  - Organização Responsável (seleção de lista de organizações válidas)
  - Período (data/hora de início e fim)
* **Edição de Projetos**: Permitir alteração de todos os campos do projeto, incluindo a alteração de status (`ATIVO` / `INATIVO`).

### 2.3 Visualização de Resumo e Indicadores (Dashboard do Projeto)
* Ao selecionar um projeto, deve ser exibido o resumo de indicadores obtido de `/api/v1/projetos/{id}/resumo`:
  - **Saúde Temporal**: Percentual de tempo decorrido e indicação visual se está em risco.
  - **Execução**: Total de eventos, eventos realizados e pendentes.
  - **Colaboração**: Lista de pastorais/organizações envolvidas.

### 2.4 Controle de Acesso (Permissões)
* As ações de criação e edição serão visíveis e autorizadas para:
  - Administradores (`ROLE_ADMIN`)
  - Clero / Párocos (`ROLE_CLERO_PAROCO`)
  - Coordenadores e Vice-coordenadores do Conselho (`CONSELHO_COORDENADOR`, `CONSELHO_VICE_COORDENADOR`).
  - Coordenadores e Vice-coordenadores de pastorais/organizações locais.
* **Regra de Seleção de Organização no Formulário**:
  - **Usuários Globais** (Administradores, Clero e Coordenadores do Conselho) podem selecionar qualquer organização paroquial.
  - **Usuários Locais** (Coordenadores locais) terão a seleção do formulário travada e restrita apenas às organizações às quais eles estão vinculados na sessão.

## 3. Critérios de Aceitação
1. Menus de todas as telas unificados no Drawer lateral responsivo.
2. Tabela de listagem de projetos exibe corretamente dados reais e status.
3. Formulários de criação e edição realizam chamadas para `POST /api/v1/projetos` e `PATCH /api/v1/projetos/{id}` respectivamente.
4. O resumo do projeto renderiza os dados agregados da API com indicadores visuais e barra de progresso.
5. Regras de restrição de organização e permissões aplicadas e validadas no formulário.

## 4. Fora de Escopo
* Exclusão física de projetos.
