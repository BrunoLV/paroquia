# Regras Mandatórias de Engenharia Frontend & Design System (`gestao-paroquia-ui`)

Esta regra é ALWAYS-ON e DEVE ser rigorosamente cumprida por qualquer agente de IA ao criar, refatorar ou revisar arquivos do frontend (`gestao-paroquia-ui/`). Nenhuma exceção ou atalho é permitido.

---

## 1. Modais e Diálogos (`Dialog`) — Invariantes Não Negociáveis

Todo componente de modal/diálogo (`*Modal.tsx`, `*Dialog.tsx`) DEVE seguir estritamente o layout canônico abaixo:

```tsx
<Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
  <DialogTitle>{isEditing ? 'Editar registro' : 'Novo registro'}</DialogTitle>
  <form onSubmit={handleSubmit(onSubmit)} noValidate>
    <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
      <Stack spacing={2.5}>
        <Card variant="outlined" sx={{ borderRadius: 2 }}>
          <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
              <Icon color="primary" fontSize="small" />
              <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                Nome da Seção
              </Typography>
            </Stack>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  size="small"
                  label="Campo"
                  required
                  error={Boolean(errors.campo)}
                  helperText={errors.campo?.message}
                  {...register('campo')}
                />
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </Stack>
    </DialogContent>
    <DialogActions>
      <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={handleClose} />
      <ActionButton title="Salvar" icon={<SaveIcon />} color="primary" type="submit" disabled={isPending} />
    </DialogActions>
  </form>
</Dialog>
```

### 1.1. Anti-patterns Terminantemente Proibidos em Modais
1. **PROIBIDO `<Typography>` aninhada dentro de `<DialogTitle>`:** `<DialogTitle>` já é um elemento tipográfico padronizado do MUI com variante h6. Passe apenas a string direta.
2. **PROIBIDO omitir `size="small"` em campos de formulário:** TODOS os inputs (`TextField`, autocomplete, `FormControl`) DEVEM ter `size="small"`. Inputs no tamanho default (56px) quebram a harmonia visual corporativa do sistema.
3. **PROIBIDO omitir `noValidate` na tag `<form>`:** O controle de validação é de responsabilidade exclusiva do Zod + React Hook Form. O balão nativo do navegador nunca deve ser exibido.
4. **PROIBIDO omitir `dividers` e `sx={DIALOG_CONTENT_SCROLL_SX}` em `<DialogContent>`:** Obrigatórios para garantir rolagem segura em telas menores e viewport de baixa resolução.
5. **PROIBIDO campos soltos fora de `<Card variant="outlined">`:** Todo grupo de campos deve viver dentro de um Card delineado com `CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}` e cabeçalho com ícone e `Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}`.
6. **PROIBIDO controle de fechar duplicado:** Não adicione "X" no cabeçalho se já existir o botão canônico "Cancelar" no `DialogActions`.
7. **PROIBIDO overrides arbitrários de padding em `<DialogActions>`:** Use `<DialogActions>` limpo com espaçamento padrão do tema.
8. **PROIBIDO `<Button>` ou `<IconButton>` crus:** Use exclusivamente `<ActionButton>` e `<IconActionButton>`.

### 1.2. Padronização Obrigatória de Ícones e Cores em Botões (`ActionButton` / `IconActionButton`)
Todos os botões devem aderir estritamente à semântica de cores e catálogo de ícones de `DESIGN_SYSTEM.md`:
1. **Criação / Cadastro (`color="primary"` variant="contained"):** Ícone específico da entidade:
   - Fiel / Clérigo: `PersonAddIcon`
   - Pastoral / Movimento: `GroupAddIcon`
   - Unidade Territorial: `AddHomeWorkIcon`
   - Local / Espaço: `AddLocationAltIcon`
   - Evento de Calendário: `EventNoteIcon`
   - Assento / Ata / Projeto: `PostAddIcon`
   - Genérico / Parâmetro: `AddIcon`
2. **Gravação e Submissão (`color="primary"`):** `SaveIcon` para salvar/gravar; `CheckCircleIcon` para confirmar/submeter; `LoginIcon` para autenticação.
3. **Cancelamento e Fechamento (`color="secondary"`):** `CloseIcon` para cancelar/fechar; `ArrowBackIcon` para voltar; `ClearAllIcon`/`FilterAltOffIcon` para limpar filtros.
4. **Ações de Sucesso e Homologação (`color="success"`):** `CheckCircleIcon` (aprovar/concluir), `VerifiedIcon` (homologar), `ToggleOnIcon` (ativar/reativar).
5. **Ações Destrutivas e Restritivas (`color="error"`):** `DeleteIcon` (excluir), `BlockIcon`/`CancelIcon` (vetar/rejeitar), `ToggleOffIcon`/`PersonOffIcon` (inativar/desativar).
6. **Ações Neutras e Utilitárias (`color="primary"` ou default):** `VisibilityIcon` (detalhes/visualizar), `EditIcon`/`EditNoteIcon` (editar/ressalva), `LinkIcon` (vincular), `DownloadIcon` (exportar), `RefreshIcon` (atualizar).

### 1.3. Padronização Obrigatória de Ícones para Tipo, Encargo e Grau
1. **Tipo (Categoria e Classificação):**
   - Campo geral e casos não classificados (`OUTRO`): `CategoryIcon`. Proibido `HelpOutlineIcon` ("?").
   - Tipo de Sacramento: `MenuBookIcon` (Sagrados Livros de Assentos).
   - Tipo de Unidade Territorial: `ChurchIcon` (Matriz) e `PlaceIcon` (Comunidade).
   - Tipo de Local Físico: `ChurchIcon` (Igreja), `MeetingRoomIcon` (Salão), `DoorFrontIcon` (Sala).
   - Tipo de Aquisição de Solo: `ReceiptLongIcon`.
2. **Encargo (Ofício e Função Eclesiástica):**
   - Campo geral: `AssignmentIndIcon`.
   - Pároco: `ChurchIcon` (pastor titular e autoridade paroquial). Proibido `AdminPanelSettingsIcon` (TI).
   - Administrador Paroquial: `AssignmentIndIcon` (provisão administrativa temporária). Proibido `ManageAccountsIcon` (TI).
   - Vigário Paroquial: `SupervisorAccountIcon` (cooperador pastoral).
   - Diácono Auxiliar: `CoPresentIcon` (assistência ao Altar e ministério da Palavra).
   - Bispo / Emérito: `WorkspacePremiumIcon` (ordem episcopal e mérito canônico).
3. **Grau (Sacramento da Ordem):**
   - Campo geral: `WorkspacePremiumIcon` (grau ministerial da Ordem).
   - Diaconato (Transitório / Permanente): `CoPresentIcon` (1º grau da Ordem). Proibido `VolunteerActivismIcon` (pastorais).
   - Presbiterado Diocesano: `BadgeIcon` (2º grau da Ordem incardinado).
   - Presbiterado Religioso: `ShieldIcon` (2º grau da Ordem regular).
   - Episcopado: `WorkspacePremiumIcon` (3º grau e plenitude da Ordem).

---

## 2. Formulários, Validação e Máscaras

**Toda lista de valores permite pesquisa por autocomplete**, inclusive filtros, seleção múltipla e Linhas por página. Aplicar a seção 2.1 do [`DESIGN_SYSTEM.md`](../../gestao-paroquia-ui/DESIGN_SYSTEM.md): `SearchableSelect`, `SearchableAutocomplete` e `SearchableTablePagination`. Proibidos Select, TextField select e select nativo sem pesquisa, independentemente do número de opções. Validar a suíte `searchableListsArchitecture.test.ts`.

1. **Zod + React Hook Form:** Obrigatórios para qualquer formulário. Nunca use múltiplos `useState` para inputs.
2. **Máscaras Obrigatórias:**
   * CPF: `formatCpf`
   * CNPJ: `formatCnpj`
   * Telefone/Celular: `formatPhone`
   * CEP: `formatCep`
   * UF: Sempre 2 caracteres e convertido para maiúsculas (`.toUpperCase()`).
3. **Confirmação de Descarte:** Modais de edição/criação devem chamar `confirmDiscard(isDirty, onClose)` ao fechar.

---

## 3. Código como Documentação Viva (*Living Documentation*)

Todo modal e componente de tela DEVE conter bloco JSDoc especificando:
- A regra de negócio ou canônica atendida.
- A justificativa da decisão de UX (ex.: tamanho de modal, agrupamento de seções, acessibilidade).

## 4. Tabelas e Seção de Pesquisa Externa

1. **Seção de Pesquisa e Filtros Obrigatória por Fora da Tabela:**
   - A barra de pesquisa e filtros deve ficar **sempre por fora** do quadro da tabela, renderizada em um `<Paper sx={{ p: 2, mb: 3 }}>` (ou `FilterBar` dedicado), utilizando o componente canônico `SearchField` (`size="small"`, ícone de lupa embutido) e `SearchableSelect`.
   - A tabela deve residir em seu próprio `<TableContainer component={Paper}>`.
   - **Terminantemente proibido:** incorporar `<TextField>` ou `<SearchField>` dentro do cabeçalho ou corpo do mesmo `Card` que engloba a tabela.
2. **Paginação de Tabelas:**
   - Aplicar a seção 7 do [`DESIGN_SYSTEM.md`](../../gestao-paroquia-ui/DESIGN_SYSTEM.md). `SearchableTablePagination` é o rodapé semântico: renderizar dentro do `Table`, após `TableBody`, com `colSpan` das colunas visíveis. Nunca posicionar fora da tabela ou do seu contêiner. Linhas por página mantém pesquisa e a mesma altura dos botões anterior/próximo; o estilo compacto pertence somente à paginação e não altera formulários, filtros ou modais. Validar `tablePaginationArchitecture.test.ts` e dimensões no navegador nos temas claro/escuro.
3. **Clique na Linha para Visualização de Detalhes:**
   - Toda tabela do sistema deve permitir abrir o modal ou drawer de detalhes ao clicar na linha (`TableRow`), em paridade com o botão "Visualizar" do menu de ações.
   - A linha deve conter: `role="button"`, `tabIndex={0}`, `sx={{ cursor: 'pointer' }}`, `onClick={() => onVisualizar(item)}` e `onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); onVisualizar(item); } }}`.
   - A célula do menu de ações (`TableActionMenu`) deve conter **obrigatoriamente** `onClick={(e) => e.stopPropagation()}` para evitar acionamento inadvertido do detalhamento ao clicar no menu.

## 5. Ações de Linha

Toda ação em célula de tabela usa `TableActionMenu`, mesmo quando única. Gatilho pequeno e delineado com `⋮` vertical, sem texto visível; tooltip e nome acessível “Ações”. Proibidos `… Ações`, botões textuais e menus locais duplicados. Preservar permissões, callbacks e estados desabilitados. O componente comum gerencia teclado, foco e isolamento dos eventos da linha.
- **Cores semânticas obrigatórias nos itens de ação:**
  - Visualização e edição (`Visualizar`, `Editar`, `Membros`, `Vínculos`): `color="primary"` (ex.: `<VisibilityIcon fontSize="small" color="primary" />`, `<EditIcon fontSize="small" color="primary" />`, `<GroupsIcon fontSize="small" color="primary" />`).
  - Ações destrutivas / restritivas (`Excluir`, `Inativar`, `Desativar`, `Vetar`, `Rejeitar`, `Cancelar`): `color="error"` (ex.: `DeleteIcon color="error"`, `ToggleOffIcon color="error"`, `BlockIcon color="error"`).
  - Ações de sucesso / homologação (`Aprovar`, `Homologar`, `Ativar`, `Reativar`, `Concluir`): `color="success"` (ex.: `CheckCircleIcon color="success"`, `VerifiedIcon color="success"`, `ToggleOnIcon color="success"`).
  - Ações de processo e fluxo (`Saída`, `Retirada`, `Certidão`): `color="info"`.
  - Ações de alerta / atenção (`Manutenção`): `color="warning"`.
- Executar `TableActionMenu.test.tsx` e `tableActionsArchitecture.test.ts`.

## 6. Navegação Lateral

Aplicar a seção 10 do Design System e a configuração central `navigation.ts`. Agrupar por finalidade, sem ordenação alfabética global nem menus paralelos. Preservar rotas/permissões, omitir grupos vazios e validar `SidebarNavigation.test.tsx`/`navigation.test.ts`. Cabeçalhos legíveis e recolhíveis, recuo dos itens, links acessíveis e mesma organização no desktop, compacto e mobile.
