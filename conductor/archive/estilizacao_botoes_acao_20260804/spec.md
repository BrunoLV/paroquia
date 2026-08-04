# Spec: Padronização de Cor e Ícone por Ação nos Botões do Sistema

## Overview
Os botões do sistema (`Button` e `IconButton` do MUI) hoje seguem parcialmente uma convenção de cor por ação (ex.: `color="secondary"` em Cancelar, `color="error"` em ações destrutivas), mas a maioria não possui ícone representativo da ação — apenas 21 de ~110 ocorrências de `<Button>` usam `startIcon`/`endIcon`. O menu de navegação lateral (`Layout.tsx`) já segue esse padrão (cada item tem um ícone que representa a página). Esta track estende esse mesmo princípio visual para todos os botões de ação do sistema (formulários, diálogos, tabelas, páginas), definindo um mapeamento semântico **ação → cor → ícone** e aplicando-o de forma consistente em toda a base de código.

## Functional Requirements
1. **Mapeamento semântico ação → cor → ícone**, documentado e aplicado consistentemente:
   - **Salvar / Confirmar (não-destrutivo):** `color="primary"`, ícone `SaveIcon` (Salvar) ou `CheckIcon`/`CheckCircleIcon` (Confirmar genérico).
   - **Cancelar / Fechar / Voltar:** `color="secondary"`, ícone `CloseIcon` (Cancelar/Fechar) ou `ArrowBackIcon` (Voltar).
   - **Excluir / Remover / Vetar / Rejeitar (destrutivo):** `color="error"`, ícone `DeleteIcon` (Excluir/Remover) ou `CloseIcon`/`BlockIcon` (Vetar/Rejeitar).
   - **Adicionar / Novo / Criar:** `color="primary"`, ícone `AddIcon`.
   - **Editar:** `color="secondary"`, ícone `EditIcon`.
   - **Exportar / Download:** `color="info"`, ícone `DownloadIcon` (ou `FileDownloadIcon`).
   - **Importar / Enviar:** `color="primary"`, ícone `UploadIcon`/`SendIcon`.
   - **Aprovar / Homologar:** `color="success"`, ícone `CheckCircleIcon`.
   - **Transferir / Alocar / Reservar:** `color="primary"`, ícone `SwapHorizIcon`/`EventAvailableIcon` conforme o contexto.
   - **Sair (logout):** `color="inherit"` (mantém padrão atual no header), ícone `LogoutIcon` (já aplicado).
   - Ações sem correspondência clara no mapeamento acima devem seguir o princípio geral (cor conforme severidade/intenção da ação; ícone que represente literalmente a ação, buscado em `@mui/icons-material`).
2. **Cobertura total:** todo `<Button>` e `<IconButton>` de ação do sistema (formulários, diálogos de confirmação, páginas de listagem/tabela, menus de contexto) deve ser revisado e, quando aplicável, receber a cor e o ícone correspondentes à sua ação semântica.
3. **Consistência de padrão visual:** ícones seguem o mesmo pacote já usado no projeto (`@mui/icons-material`), sem introduzir nova biblioteca de ícones.
4. **Documentação do padrão:** o mapeamento ação → cor → ícone é adicionado a `.agents/skills/react-frontend.md` (guia de engenharia frontend já consultado pelo agente), como referência obrigatória para botões futuros.

## Non-Functional Requirements
- **Sem quebra de funcionalidade:** a mudança é estritamente visual (props `color`/`startIcon`/`icon` e o ícone renderizado); nenhuma lógica de `onClick`, submissão de formulário ou navegação pode ser alterada.
- **Sem regressão de testes:** nenhum teste existente pode quebrar. Testes que dependem de `getByText`/`getByRole` continuam funcionando pois o texto do botão não muda (ícones são adicionados via `startIcon`, não substituem o label).
- **Consistência com o design system existente:** preservar `borderRadius`, tipografia e demais tokens do `theme.ts` (não sobrepostos por esta track); os botões continuam usando os `styleOverrides` de `MuiButton`/`MuiIconButton` já existentes.
- **Branch dedicada:** o desenvolvimento deve ocorrer em uma branch dedicada criada a partir do `master` atualizado.

## Acceptance Criteria
- [ ] O mapeamento ação → cor → ícone está documentado em `.agents/skills/react-frontend.md`.
- [ ] Todos os botões de Salvar/Confirmar usam `color="primary"` e um ícone de confirmação.
- [ ] Todos os botões de Cancelar/Fechar/Voltar usam `color="secondary"` e um ícone correspondente.
- [ ] Todos os botões de ação destrutiva (Excluir, Remover, Vetar, Rejeitar) usam `color="error"` e um ícone correspondente.
- [ ] Botões de Adicionar/Novo/Criar usam `color="primary"` e `AddIcon`.
- [ ] Botões de Editar (`Button` e `IconButton`) usam `color="secondary"` e `EditIcon`.
- [ ] Botões de Aprovar/Homologar usam `color="success"` e `CheckCircleIcon`.
- [ ] `IconButton`s de ação em tabelas/listas (editar, excluir, etc.) seguem a mesma cor semântica dos `Button` equivalentes.
- [ ] `npx tsc --noEmit` e `CI=true npm test` passam sem novas falhas.
- [ ] QA visual manual confirma a aplicação consistente do padrão nas telas principais (formulários de cadastro, diálogos de confirmação, tabelas de listagem, menus/ações de contexto).

## Out of Scope
- Alterações na estrutura, lógica de negócio ou fluxo de submissão dos formulários/diálogos.
- Alterações no menu de navegação lateral (`Layout.tsx`), que já segue o padrão de ícone por item.
- Introdução de nova biblioteca de ícones (mantém-se `@mui/icons-material`).
- Alterações de tipografia, spacing ou densidade dos componentes (escopo já coberto pela track `ui_densidade_visual_20260804`).
- Redesign de layout/UX além da adição de cor/ícone semânticos aos botões existentes.
