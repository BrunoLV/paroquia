# Plano de Implementação: Tela de Gerenciamento de Projetos no Frontend

## Fase 1: Refatoração da Navegação Lateral (Sidebar Drawer) [checkpoint: be50fca]
- [x] Task: Refatorar o cabeçalho superior para usar uma barra lateral esquerda responsiva (Drawer) com ícone hambúrguer
    - [x] Criar testes unitários para o Layout com Drawer, validando exibição de links e botão de alternância (hambúrguer)
    - [x] Substituir o menu horizontal do `AppBar` por um `Drawer` lateral retrátil em `Layout.tsx`
- [x] Task: Conductor - User Manual Verification 'Fase 1: Refatoração da Navegação Lateral (Sidebar Drawer)' (Protocol in workflow.md)

## Fase 2: Ajuste dos Tipos (Types) e Modelo do Projeto [checkpoint: 3afd57b]
- [x] Task: Ajustar a interface `Projeto` em `types.ts` para bater com as chaves reais da API
    - [x] Alterar propriedades `dataInicio`/`dataFim`/`ativo` para `inicio`/`fim`/`status` (enum ATIVO/INATIVO) em `src/api/types.ts`
- [x] Task: Conductor - User Manual Verification 'Fase 2: Ajuste dos Tipos (Types) e Modelo do Projeto' (Protocol in workflow.md)

## Fase 3: Listagem de Projetos e CRUD (Criação/Edição)
- [x] Task: Implementar tela de visualização e listagem dos projetos [4bc1587]
    - [x] Criar testes unitários da listagem de projetos, garantindo exibição de colunas e dados formatados
    - [x] Criar a página de listagem de projetos `src/pages/Projects.tsx`
- [x] Task: Implementar diálogos de criação e edição com controle de acesso e regras de organização [4bc1587]
    - [x] Criar testes unitários do formulário de criação/edição, incluindo o bloqueio de seleção de organização para coordenadores locais
    - [x] Criar modal de criação/edição de projeto integrado com a API (`POST /api/v1/projetos` e `PATCH /api/v1/projetos/{id}`)
- [x] Task: Conductor - User Manual Verification 'Fase 3: Listagem de Projetos e CRUD (Criação/Edição)' [checkpoint: c20fdb1]

## Fase 4: Indicadores e Painel de Resumo do Projeto
- [x] Task: Implementar a exibição de indicadores consolidados (Dashboard do Projeto) [4bc1587]
    - [x] Criar testes unitários da renderização do resumo do projeto (Saúde Temporal, Execução e Colaboração)
    - [x] Integrar e renderizar o componente de resumo de indicadores do endpoint `/api/v1/projetos/{id}/resumo` na interface
- [ ] Task: Conductor - User Manual Verification 'Fase 4: Indicadores e Painel de Resumo do Projeto' (Protocol in workflow.md)
