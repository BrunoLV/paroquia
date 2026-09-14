# Design System — `gestao-paroquia-ui`

Documento canônico e global do padrão visual do sistema. Referência única — não duplicar estes trechos em outros arquivos, apenas apontar para cá. Leitura e aplicação **obrigatórias** em toda tela, componente, modal ou formulário, novo ou existente, sem exceção de módulo.

Toda mudança que altere um destes componentes ou padrões (`ActionButton`, `IconActionButton`, `SearchField`, `InfoField`, `DIALOG_CONTENT_SCROLL_SX`, estrutura de Dialog, `createAppTheme`/`ThemeModeContext`) **DEVE** atualizar este arquivo na mesma alteração.

---

## 1. Layout de Página

```tsx
<Stack spacing={3}>
  <PageHeader
    icon={<PersonOutlineIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
    title="Título da Página"
    subtitle="Descrição objetiva do módulo"
    actions={<ActionButton title="Novo Registro" icon={<AddIcon />} color="primary" onClick={handleOpenCreate} />}
  />
  {/* busca, tabela, etc. */}
</Stack>
```

- Contêiner raiz de toda rota: `<Stack spacing={3}>`.
- Cabeçalho sempre via `PageHeader` (`src/components/PageHeader.tsx`), nunca `Typography` solto.

## 2. Busca — `SearchField`

Componente único: `src/components/SearchField.tsx`. Nunca reimplementar `TextField` + `SearchIcon` à mão.

```tsx
<SearchField value={busca} onChange={setBusca} placeholder="Pesquisar por nome ou CPF..." />
```

- Props: `value`, `onChange(value: string)`, `placeholder?`, `sx?`.
- Internamente já aplica `Box maxWidth:400` + `TextField fullWidth size="small"` + `InputAdornment` com `SearchIcon`.
- Dropdowns de filtro adicionais (Select, Switch) ficam fora do `SearchField`, lado a lado, em `Stack`/`Paper` bespoke por tela — não existe (nem deve ser criado) um `FilterBar` genérico.

## 3. Botões — `ActionButton` / `IconActionButton`

**Proibido** `<Button>`/`<IconButton>` cru do MUI em páginas e componentes de feature. Sempre:

- Texto: `ActionButton` (`src/components/ActionButton.tsx`) — props `title`, `icon`, `color`, `variant`, `type`, `form?`, `disabled`, `fullWidth`, `component`.
- Só ícone: `IconActionButton` (`src/components/IconActionButton.tsx`) — sempre com `Tooltip` (`title`) e `aria-label`.

Semântica de cor obrigatória:

| Ação | Cor | Ícone típico |
| --- | --- | --- |
| Salvar / Criar / Confirmar / Submeter | `primary` | `SaveIcon`, `AddIcon`, `CheckCircleIcon` |
| Cancelar / Fechar / Voltar | `secondary` | `CloseIcon`, `ArrowBackIcon` |
| Inativar / Excluir / Rejeitar / Vetar | `error` | `BlockIcon`, `CancelIcon`, `DeleteIcon` |

Submissão a partir de um botão fora do `<form>` (ex.: rodapé de `Drawer`): usar `form="id-do-form"` no `ActionButton`, nunca sair do componente para isso.

**Nunca usar `ActionButton` para um controle só-ícone.** `ActionButton` sempre renderiza texto visível (`label` ou, na ausência dele, o `title` inteiro por extenso) — usá-lo sem `label` para um botão pequeno (ex.: zoom in/out, seta de navegação) produz ícone espremido com o `title` completo ao lado, quebrando o layout. Controle só-ícone é sempre `IconActionButton`.

## 4. Diálogos (Modais)

Estrutura obrigatória:

```tsx
<Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
  <DialogTitle>Título</DialogTitle>
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
            <Grid2 container spacing={2}>
              {/* campos */}
            </Grid2>
          </CardContent>
        </Card>
      </Stack>
    </DialogContent>
    <DialogActions>
      <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
      <ActionButton title="Salvar" icon={<SaveIcon />} color="primary" type="submit" />
    </DialogActions>
  </form>
</Dialog>
```

Regras:

- `dividers` **sempre** presente em `DialogContent`.
- `sx={DIALOG_CONTENT_SCROLL_SX}` (import de `@/theme/dialogStyles`) **sempre** presente — limita altura e habilita rolagem em telas baixas. Não reimplementar o objeto `sx` inline.
- Todo grupo de campos vive dentro de `Card variant="outlined" + CardContent`, com cabeçalho `Icon + Typography subtitle2` — mesmo quando o modal tem só uma seção. Proibido campo solto direto no `Stack`/`Box` do `DialogContent` (exceções abaixo).
- `maxWidth`: guiado pelo conteúdo, não fixo. `md` para formulários com várias seções/campos; `sm` para formulários curtos ou confirmações com campo de texto; evitar `xs` sempre que houver `multiline`/`textarea`.
- Exceções documentadas ao padrão de Card (não recriar Card para estes casos):
  - Wizards/Steppers (`ImportModal.tsx`): cada step é `Stack spacing={2.5}` flat, sem Card.
  - Modais de tabela/gestão de lista (`OrganizacaoMembrosModal.tsx`): tabela + toolbar inline, sem Card/Grid.
- **Um único controle de fechar por Dialog/Drawer.** O botão canônico é o `ActionButton` "Cancelar"/"Fechar" no `DialogActions`/rodapé. Proibido adicionar também um "X" no cabeçalho/título chamando o mesmo `onClose` — isso duplica a mesma ação. Se o cabeçalho precisar de um controle de fechar (ex.: `Drawer` sem `DialogActions`), esse É o único controle: não replicar embaixo.

## 4.1. Espaçamento de Campos

Mecanismo e valor são fixos, não "ao gosto da tela":

- Agrupamento de seções dentro de `DialogContent` (o wrapper que envolve os `Card`s, ou os campos quando não há Card — wizards/telas de confirmação): sempre `<Stack spacing={2.5}>`.
- Campos dentro de um `Card`/seção: sempre `<Grid container spacing={2}>` (ou `Grid2`).
- **Proibido** `<Box sx={{ display: 'flex', flexDirection: 'column', gap: N }}>` como substituto do `Stack` acima — é o mesmo papel, mas com valor de espaçamento à mercê de quem escreveu a tela. Se já existe um `Box`+`gap` assim em código legado, ao tocar o arquivo migre para `Stack spacing={2.5}`.
- Exceção: containers de resumo/dashboard (cards de métricas, listas de widgets fora de Dialog) não seguem esta regra — ela vale para telas com campos de formulário/detalhe.

## 5. Tríade de Estados Assíncronos

- **Loading:** `<Skeleton>` com dimensões próximas ao conteúdo real. Nunca tela em branco.
- **Empty:** `<EmptyState icon={...} title="..." description="..." action={<ActionButton .../>} />`.
- **Error:** alerta contextual com ação de `refetch()`.

## 6. Indicadores Categóricos — `StatusIcon`

- Categorias, tipos, sacramentos e status de ciclo de vida **sempre** via `<StatusIcon {...get<Propriedade>Meta(valor)} />` (`src/components/StatusIcon.tsx`).
- Nunca exibir enum cru (`EM_MANUTENCAO`) — sempre mapeador de rótulo amigável.

## 6.1. Telas de Visualização/Detalhe — `InfoField`

Toda tela somente-leitura que mostra a ficha completa de uma entidade (`*ViewModal.tsx`, `*DetalhesDrawer.tsx`) usa o componente compartilhado `InfoField` — `export const InfoField` em `src/components/fieis/fielConstants.tsx` — e não uma reimplementação local nem pares soltos de `Typography` caption+valor.

```tsx
<InfoField label="E-mail" icon={<EmailIcon fontSize="inherit" />} value={dados.email} />
```

- **Todo campo tem `icon`**, semanticamente condizente com o dado (e-mail → `EmailIcon`, telefone → `PhoneIcon`/`WhatsAppIcon`, data → `EventIcon`/`CakeIcon`/`ScheduleIcon`, endereço → `LocationOnIcon`, documento/identificação → `BadgeIcon`, categoria/tipo → `CategoryIcon`, descrição → `DescriptionIcon`). Nenhum `InfoField` sem `icon`.
- Import de outro módulo (ex. `AtivoViewModal.tsx` usando o `InfoField` de `fieis/fielConstants.tsx`) é o padrão esperado — não duplicar o componente localmente.
- Não é uma regra "por módulo de negócio": aplica-se a qualquer tela nova de detalhe, independente da entidade.

## 7. Tabelas

- Cabeçalho: `TableHead` com `backgroundColor: 'action.hover'` e células `fontWeight: 600/700`.
- Linha clicável: `role="button"`, `tabIndex={0}`, `onKeyDown` tratando `Enter`/`Espaço`.
- Ações de linha (`IconActionButton`, menu): sempre `event.stopPropagation()` no `onClick`.
- Paginação obrigatória: `<TablePagination rowsPerPageOptions={[5, 10, 25]} labelRowsPerPage="Linhas por página:" />`.

## 8. Tokens e Estilo

- Só tokens semânticos do tema (`primary.main`, `error.main`, `divider`, `theme.custom.*`). Proibido hex cru no JSX.
- `shape.borderRadius: 12` do tema nunca sobrescrito para 0.

## 9. Modo Escuro

O sistema tem tema claro/escuro (`createAppTheme(mode)` em `src/theme.ts`, alternado via `ThemeModeProvider`/`useThemeMode()` em `src/context/ThemeModeContext.tsx`). Regra única e não-negociável:

- **Dentro de um componente, cor de tema sempre via `useTheme()` do MUI** (`const theme = useTheme()`), nunca `import theme from '.../theme'` (essa é a instância clara estática, fixa, que não reage ao modo ativo). O import estático só é aceitável em código que não é componente React (ex.: uma constante de módulo calculada uma vez fora de qualquer render) — e mesmo assim, prefira passar a cor como parâmetro vindo de quem chama (que tem o hook).
- Ao ler `theme.custom.*` num componente, use encadeamento opcional com fallback (`theme.custom?.subtlePanelBg ?? '#F8F9FA'`) — o `useTheme()` de um componente renderizado fora do `ThemeModeProvider` (comum em teste unitário isolado) devolve o tema default do MUI, que não tem `custom`.
- Nunca hardcode um hex ou `theme.palette.grey[N]` assumindo fundo claro — ambos os modos precisam ficar legíveis; se a cor depende do modo, calcule a partir de `theme.palette.mode === 'dark'` ou de um token em `customTokensByMode` (`src/theme.ts`).

---

## Componentes-fonte desta doc

| Componente/constante | Arquivo |
| --- | --- |
| `PageHeader` | `src/components/PageHeader.tsx` |
| `ActionButton` | `src/components/ActionButton.tsx` |
| `IconActionButton` | `src/components/IconActionButton.tsx` |
| `SearchField` | `src/components/SearchField.tsx` |
| `DIALOG_CONTENT_SCROLL_SX` | `src/theme/dialogStyles.ts` |
| `StatusIcon` | `src/components/StatusIcon.tsx` |
| `InfoField` | `src/components/fieis/fielConstants.tsx` |
| `createAppTheme`, `customTokensByMode` | `src/theme.ts` |
| `ThemeModeProvider`, `useThemeMode` | `src/context/ThemeModeContext.tsx` |

## Casos não cobertos

Se um caso não estiver descrito aqui, resolva por analogia às regras das seções acima (semântica de cor, estrutura de Card, tríade de estados) e, se necessário, atualize este documento — não crie um padrão paralelo em uma tela específica.
