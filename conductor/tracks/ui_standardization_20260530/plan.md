# Plan: UI Standardization & Unified Error Presentation

## Phase 1: Configuração do Tema Global (MUI Theme)
- [x] Task: Refinar a configuração do tema em `theme.ts` [a2b4dcb]
    - [x] Adicionar tipografia padrão (família de fontes Roboto/Inter/System) e pesos de fonte consistentes.
    - [x] Padronizar estilos globais de `MuiButton` (variantes, hover transitions, border-radius coerente com design premium).
    - [x] Padronizar estilos de `MuiTextField`, `MuiOutlinedInput`, `MuiSelect`, `MuiInputLabel` para obter inputs uniformes.
    - [x] Estilizar `MuiLink` para links uniformes com hover e cor primária.
- [x] Task: Escrever testes unitários e de integração para validar as propriedades do tema [a2b4dcb]
    - [x] Atualizar/criar `theme.test.tsx` para assegurar que as fontes, cores e customizações de botões/inputs estejam declaradas.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Configuração do Tema Global' (Protocol in workflow.md) [a2b4dcb]

## Phase 2: Padronização Visual de Formulários, Botões e Ícones
- [~] Task: Padronizar a tela de login (`Login.tsx`)
    - [ ] Usar componentes MUI e as fontes/estilos padronizados pelo tema.
- [~] Task: Padronizar a tela de gerenciamento de projetos (`Projects.tsx`)
    - [ ] Alinhar o formulário de cadastro/edição (TextFields, Selects, Buttons) ao tema global.
    - [ ] Uniformizar a tabela/grade, botões de ação e ícones.
- [~] Task: Padronizar o calendário e modais de evento (`Calendar.tsx`)
    - [ ] Unificar os inputs do modal de criação/edição e botões de ação.
    - [ ] Padronizar os ícones de categorias e navegação.
- [~] Task: Padronizar a tela de aprovações (`Approvals.tsx`)
    - [ ] Garantir o alinhamento visual de listagens, botões e ícones.
- [ ] Task: Executar e atualizar testes automatizados existentes
    - [ ] Rodar testes de login, projetos, calendário e aprovações para garantir compatibilidade.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Padronização Visual de Formulários, Botões e Ícones' (Protocol in workflow.md)

## Phase 3: Unificação da Apresentação de Erros nos Formulários via Toast
- [ ] Task: Unificar tratamento de erros na tela de login
    - [ ] Remover alertas na tela e usar exclusivamente `showError(...)` do Toast para erros de credenciais ou rede.
- [ ] Task: Unificar tratamento de erros em projetos (`Projects.tsx`)
    - [ ] Remover campos vermelhos customizados ou alertas estáticos locais. Utilizar validações que disparam Toasts.
- [ ] Task: Unificar tratamento de erros no calendário e modais de evento (`Calendar.tsx`)
    - [ ] Tratar validações de formulário do client-side e erros de API exibindo-os unicamente no Toast.
- [ ] Task: Validar integridade dos testes de comportamento de erro
    - [ ] Escrever/atualizar testes para garantir que as funções `showError` são chamadas sob falha e os erros estáticos locais foram limpos.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Unificação da Apresentação de Erros nos Formulários via Toast' (Protocol in workflow.md)
