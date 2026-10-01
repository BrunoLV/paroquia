---
name: react-frontend
description: "Engenharia de frontend em React 19, TypeScript estrito, Material UI (MUI 9), TanStack Query v5, Zod, React Hook Form, Vite e Vitest, com aderência obrigatória ao gestao-paroquia-ui/DESIGN_SYSTEM.md."
---

# Skill: React Frontend Engineering (`gestao-paroquia-ui`)

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a projetar, refatorar e auditar código de frontend no ecossistema **`gestao-paroquia-ui`**. O agente atua como um Engenheiro Frontend Principal especializado em **React 19**, **TypeScript estrito**, **Material UI (MUI 9)**, **TanStack Query v5**, **Vite** e **Vitest**.

O objetivo é garantir interfaces declarativas com tipagem robusta, acessibilidade por teclado (a11y/WCAG), isolamento completo de regras de negócio em hooks e fidelidade absoluta à especificação global de padrões visuais e ergonômicos em [`gestao-paroquia-ui/DESIGN_SYSTEM.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia-ui/DESIGN_SYSTEM.md) — aplicável a toda tela/módulo, sem exceções.

---

## 2. O Código como Documentação Viva (*Living Documentation*)

O código JSX/TSX deve documentar a intenção de negócio e as decisões ergonômicas da interface, servindo como especificação funcional do módulo.

* **JSDoc de Negócio e UX Obrigatório:** Componentes de página, formulários complexos, modais e hooks customizados DEVEM conter blocos JSDoc explicando a regra de negócio e o papel do fluxo:
```tsx
/**
 * Formulário de homologação e emissão de Certidão de Matrimônio.
 *
 * REGRA CANÔNICA (RN-MAT-04):
 * O processo só pode ser submetido se ambos os nubentes possuírem
 * certidões de batismo averbadas com validade inferior a 6 meses.
 *
 * DECISÃO DE UX (MUI 9 / Drawer):
 * Utiliza Drawer lateral em vez de Dialog modal devido à extensão dos
 * dados de testemunhas e impedimentos canônicos, preservando contexto.
 */
export const MatrimonioFormDrawer: React.FC<MatrimonioFormDrawerProps> = ({ ... }) => { ... };
```

* **Comentários de Acessibilidade e Eventos:** Justifique explicitamente contenções de propagação de eventos (`stopPropagation`), armadilhas de foco (*focus traps*) e escolhas semânticas de ARIA.
* **Proibição de Comentários Triviais:** Comentários como `// Renderiza botão` ou `// Hook de estado` são proibidos.

---

## 3. Arquitetura de Componentes e Separação de Responsabilidades

* **Isolamento de Lógica de Negócios:** Componentes visuais devem ser camadas de renderização finas e declarativas. Todas as transições de estado complexas, efeitos colaterais e requisições HTTP DEVEM ser encapsuladas em hooks customizados (`src/hooks/`).
* **Primitivos Semânticos do MUI 9:** É expressamente proibido o uso de elementos HTML crus (`div`, `span`, `p`) para layout ou tipografia. Utilize exclusivamente `Box`, `Stack`, `Grid2`, `Container` e `Typography`. Elementos nativos só são tolerados quando estritamente necessários para marcação inline semântica acessível.
* **Limites de Tamanho e Decomposição Atômica:**
  * **Componentes visuais puros e atômicos:** Limite estrito de **10–60 linhas** de JSX.
  * **Formulários densos e orquestradores de tabela:** Teto expandido de **até 120 linhas** para evitar a fragmentação artificial de controles e resolvers de formulário.
  * Componentes que excederem esses limites devem ser decompostos em subcomponentes coesos dentro da pasta `components/` adjacente ao arquivo pai.
* **Hierarquia de Estado Controlada:**
  * **Server State:** Gerenciado exclusivamente via TanStack Query v5.
  * **Shared UI State:** Utilize React Context para estados efêmeros de interface compartilhados no módulo (ex.: etapa ativa de wizard multietapas, visibilidade de gavetas).
  * Injeções no objeto global `window` ou stores singleton externas desordenadas são terminantemente proibidas.

---

## 4. Arquitetura de Formulários e Validação (React Hook Form + Zod)

* **Validação Orientada a Esquema (Schema-Driven):** Todo formulário (criação, edição ou filtro complexo) DEVE definir e exportar um schema Zod estritamente tipado em `src/schemas/<entidade>Schema.ts`.
* **Form Controller Centralizado:** Integrar `react-hook-form` acoplado ao `@hookform/resolvers/zod`. Formulários não devem utilizar múltiplos `useState` isolados para campos de entrada.
* **Feedback Visual Uniforme de Erros:** O feedback de validação deve ser repassado diretamente aos componentes de input do MUI:
```tsx
<TextField
  fullWidth
  size="small"
  label="Nome do Fiel"
  {...register('nome')}
  error={Boolean(errors.nome)}
  helperText={errors.nome?.message}
/>
```

* **Máscaras e Formatadores de Dados:** Utilitários de `src/utils/masks.ts` devem ser aplicados sistematicamente a CPF, RG, telefones, CEP, moeda (BRL via `Intl.NumberFormat`) e datas em todas as telas de entrada e edição.

---

## 5. Design System e Padrões Visuais Oficiais (*Padrão Arrojado*)

👉 **Fonte única e obrigatória: [`gestao-paroquia-ui/DESIGN_SYSTEM.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/gestao-paroquia-ui/DESIGN_SYSTEM.md).** Leia-o antes de tocar em qualquer tela, modal, formulário ou componente visual — cobre layout de página, `SearchField`, `ActionButton`/`IconActionButton`, estrutura de `Dialog` (`Card`+`Grid2`+`DIALOG_CONTENT_SCROLL_SX`), tríade de estados assíncronos, `StatusIcon` e tabelas. É especificação global, aplicável igualmente a toda tela/módulo do sistema, novo ou existente.

### 5.1. Layout de Páginas e Cabeçalho

**Listas de valores pesquisáveis:** Aplique a seção 2.1 do [`DESIGN_SYSTEM.md`](../../../gestao-paroquia-ui/DESIGN_SYSTEM.md). Utilize `SearchableSelect` para códigos/IDs, `SearchableAutocomplete` para objetos e `SearchableTablePagination` nas tabelas. A regra inclui filtros, seleção múltipla e Linhas por página, mesmo com poucas opções. Nunca gerar Select, TextField select ou select nativo sem pesquisa. Execute a suíte `searchableListsArchitecture.test.ts`.

* Contêiner raiz de rotas: `<Stack spacing={3}>`.
* Cabeçalho de página mandatório (`src/components/PageHeader.tsx`):
```tsx
<PageHeader
  icon={<PersonOutlineIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
  title="Registro de Fiéis"
  subtitle="Gestão canônica e cadastral dos membros da comunidade paroquial"
  actions={<ActionButton title="Novo Fiel" icon={<AddIcon />} color="primary" onClick={handleOpenCreate} />}
/>
```

* Barra de busca e filtros:
```tsx
<Box sx={{ maxWidth: 400 }}>
  <TextField
    fullWidth
    size="small"
    placeholder="Pesquisar por nome ou CPF..."
    slotProps={{
      input: {
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon color="action" />
          </InputAdornment>
        ),
      },
    }}
  />
</Box>
```

### 5.2. Botões e Ações Padronizadas

* O uso de `<Button>` cru sem padronização visual é proibido. Utilize sempre `<ActionButton icon={<Icon />} title="..." ... />` (`src/components/ActionButton.tsx`).
* **Semântica de Cores e Ícones:**
  * **Ação Primária** (Salvar, Criar, Submeter, Homologar): `color="primary"`, ícone `<SaveIcon/>` / `<AddIcon/>` / `<CheckCircleIcon/>`.
  * **Ação Secundária / Cancelamento** (Fechar, Voltar): `color="secondary"`, ícone `<CloseIcon/>` / `<ArrowBackIcon/>`.
  * **Ação Destrutiva** (Inativar, Excluir, Vetar): `color="error"`, ícone `<BlockIcon/>` / `<CancelIcon/>` / `<DeleteIcon/>`.
* Botões de ação compostos apenas por ícones sem rótulo ou sem `<Tooltip>` acessível são proibidos.

### 5.3. Tabelas de Dados e Acessibilidade (a11y)

* Contêiner estrutural: `<Card><CardContent sx={{ p: 0 }}><TableContainer><Table>...`.
* Cabeçalhos: `<TableHead sx={{ backgroundColor: 'action.hover' }}>` com células configuradas com `sx={{ fontWeight: 600 }}` ou `700`.
* **Linhas Clicáveis Acessíveis via Teclado:**
```tsx
<TableRow
  hover
  tabIndex={0}
  role="button"
  onClick={() => handleRowClick(row.id)}
  onKeyDown={(e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      handleRowClick(row.id);
    }
  }}
  sx={{ cursor: 'pointer' }}
>
```

* **Isolamento de Ações na Linha:** Cliques em botões de ação ou menus internos da linha DEVEM chamar `event.stopPropagation()` para não acionar a navegação do `TableRow`:
```tsx
<TableActionMenu onClick={(e) => e.stopPropagation()} ... />
```

* **Paginação Obrigatória:** Toda tabela do sistema deve implementar `SearchableTablePagination` com:
  * `rowsPerPageOptions={[5, 10, 25]}`
  * `labelRowsPerPage="Linhas por página:"`
  * `labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`}`

### 5.4. Tríade de Estados Assíncronos

* **Loading:** Renderize `<Skeleton>` estruturados com dimensões aproximadas ao conteúdo real. Nunca renderize telas em branco.
* **Empty State:** Se a busca ou a tabela não contiver dados, renderize:
```tsx
<EmptyState
  icon={<InboxOutlinedIcon sx={{ fontSize: 48, color: 'text.secondary' }} />}
  title="Nenhum registro encontrado"
  description="Não encontramos nenhum fiel cadastrado com os critérios informados."
  action={<ActionButton title="Cadastrar Primeiro Fiel" icon={<AddIcon />} color="primary" onClick={handleCreate} />}
/>
```
* **Error State:** Falhas na query devem renderizar alerta contextual com botão de ação acionando `refetch()`.

### 5.5. Diálogos (Modais) e Painéis Laterais (Drawers)

* Estrutura de Diálogo: `<Dialog fullWidth maxWidth="md" open={...} onClose={...}>` contendo `<DialogTitle>`, `<DialogContent dividers sx={{ maxHeight: 'calc(100vh - 200px)', overflowY: 'auto' }}>` e `<DialogActions>`.
* Empacotamento de conteúdo: `<Stack spacing={2.5}>`.
* Agrupamento visual de campos em Cards delineados:
```tsx
<Card variant="outlined" sx={{ borderRadius: 2 }}>
  <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
    <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
      <Icon color="primary" fontSize="small" />
      <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
        Identificação Canônica
      </Typography>
    </Stack>
    <Grid2 container spacing={2}>
      <Grid2 size={{ xs: 12, md: 6 }}>
        <TextField fullWidth size="small" required {...register('nome')} />
      </Grid2>
    </Grid2>
  </CardContent>
</Card>
```

* **Invariantes e Anti-patterns Mandatórios de Diálogos:**
  * `<DialogTitle>` simples: apenas string pura, proibido `<Typography>` aninhada.
  * Tag `<form>` sempre com `noValidate`.
  * `size="small"` mandatório em todos os campos (`TextField`, autocomplete, `FormControl`).
  * `dividers` e `sx={DIALOG_CONTENT_SCROLL_SX}` obrigatórios em `DialogContent`.
  * Botões de ação via `<ActionButton>` (Cancelar = secondary com CloseIcon, Salvar = primary com SaveIcon).
  * Um único botão de fechar (no rodapé, nunca duplicar com 'X' no header).

### 5.6. Exibição Somente Leitura e Indicadores Categóricos

* Utilize o padrão `InfoField` (rótulo em `Typography variant="caption"` cinza + valor em `Typography variant="body2"` com `fontWeight: 500/600`).
* Enums com underscores (`_`) NUNCA devem ser exibidos crus para o usuário; use dicionários descritivos (`SEXO_LABELS`, `ESTADO_CIVIL_LABELS`).
* Categorias, tipos, sacramentos e status devem ser renderizados obrigatoriamente através de `<StatusIcon {...get<Propriedade>Meta(valor)} />` (`src/components/StatusIcon.tsx`):

| Categoria do Domínio | Token Semântico MUI (`color`) | Glifos Típicos |
| --- | --- | --- |
| **Tipos & Classificações** (`tipo`, `localTipo`) | `primary`, `secondary`, `info` | `MeetingRoom`, `Groups`, `Church` |
| **Categorias Funcionais** (`categoria`) | `info`, `secondary`, `warning` | `Celebration`, `VolunteerActivism` |
| **Sacramentos** (`sacramentos`) | Tokens litúrgicos dedicados | Glifos canônicos dedicados |
| **Ativo / Positivo** (`ATIVO`, `CONFIRMADA`) | `success` | `CheckCircle`, `PlayArrow` |
| **Inativo / Cancelado** (`INATIVO`, `CANCELADO`) | `error` | `Block`, `Cancel`, `Delete` |
| **Pendente / Alerta** (`PENDENTE`, `RASCUNHO`) | `warning` | `AccessTime`, `HourglassEmpty` |
| **Neutro / Arquivado** (`CONCLUIDO`, `OUTRO`) | `default` | `Archive`, `HelpOutline` |

### 5.7. Design Tokens e Estilização Global

* Use exclusivamente tokens semânticos do tema: `primary.main`, `secondary.main`, `error.main`, `divider`, `theme.custom.headerIconBg`.
* Cores hexadecimais explícitas no JSX (`#FFFFFF`, `#1976D2`) são terminantemente proibidas.
* O tema define cantos arredondados modernos (`shape.borderRadius: 12`); nunca sobrescreva componentes para `borderRadius: 0`.

---

## 6. Gerenciamento de Estado de Servidor (TanStack Query v5)

* **Encapsulamento Mandatório:** Chamadas diretas de bibliotecas HTTP (`axios`, `fetch`) dentro de componentes React são estritamente proibidas.
* **Fábrica de Chaves de Consulta (Query Key Factory):** As chaves de cache devem ser centralizadas e serializáveis:
```ts
export const fielKeys = {
  all: ['fieis'] as const,
  lists: () => [...fielKeys.all, 'list'] as const,
  list: (filters: FielFilters) => [...fielKeys.lists(), { filters }] as const,
  details: () => [...fielKeys.all, 'detail'] as const,
  detail: (id: string) => [...fielKeys.details(), id] as const,
};
```

* **Tratamento de Mutações e Efeitos Colaterais:**
```ts
export const useSaveFielMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: saveFielRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: fielKeys.lists() });
      showNotification({ message: 'Registro do fiel atualizado com sucesso!', severity: 'success' });
    },
    onError: (error: Error) => {
      showNotification({ message: error.message || 'Erro ao persistir dados do fiel', severity: 'error' });
    },
  });
};
```

---

## 7. Blueprint Estrutural (Exemplo de Formulário Reativo e Declarativo)

```tsx
// src/schemas/dizimoLancamentoSchema.ts
import { z } from 'zod';

export const dizimoLancamentoSchema = z.object({
  fielId: z.string().uuid({ message: 'Selecione um fiel válido' }),
  valor: z.number().positive({ message: 'O valor deve ser superior a zero' }),
  competenciaMesAno: z.string().regex(/^\d{2}\/\d{4}$/, { message: 'Formato deve ser MM/AAAA' }),
  observacoes: z.string().max(255).optional(),
});

export type DizimoLancamentoInput = z.infer<typeof dizimoLancamentoSchema>;
```

```tsx
// src/pages/Dizimos/components/DizimoLancamentoDialog.tsx
import React from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stack,
  Card,
  CardContent,
  Grid2,
  TextField,
  Typography,
} from '@mui/material';
import PaidOutlinedIcon from '@mui/icons-material/PaidOutlined';
import SaveIcon from '@mui/icons-material/Save';
import CloseIcon from '@mui/icons-material/Close';
import { ActionButton } from '@/components/ActionButton';
import { useCreateDizimoMutation } from '@/hooks/useCreateDizimoMutation';
import { dizimoLancamentoSchema, DizimoLancamentoInput } from '@/schemas/dizimoLancamentoSchema';

interface DizimoLancamentoDialogProps {
  open: boolean;
  onClose: () => void;
}

/**
 * Diálogo transacional de lançamento de contribuição paroquial.
 * Segue estritamente o layout arrojado em Cards e isolamento via Zod.
 */
export const DizimoLancamentoDialog: React.FC<DizimoLancamentoDialogProps> = ({ open, onClose }) => {
  const { mutate, isPending } = useCreateDizimoMutation();

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<DizimoLancamentoInput>({
    resolver: zodResolver(dizimoLancamentoSchema),
    defaultValues: { observacoes: '' },
  });

  const onSubmit = (data: DizimoLancamentoInput) => {
    mutate(data, {
      onSuccess: () => {
        reset();
        onClose();
      },
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Lançamento de Dízimo</DialogTitle>
      
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogContent dividers sx={{ maxHeight: 'calc(100vh - 200px)', overflowY: 'auto' }}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
                  <PaidOutlinedIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Dados da Contribuição
                  </Typography>
                </Stack>

                <Grid2 container spacing={2}>
                  <Grid2 size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      size="small"
                      required
                      label="Código de Identificação do Fiel"
                      {...register('fielId')}
                      error={Boolean(errors.fielId)}
                      helperText={errors.fielId?.message}
                    />
                  </Grid2>

                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <Controller
                      name="valor"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          fullWidth
                          size="small"
                          required
                          type="number"
                          label="Valor (R$)"
                          onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                          error={Boolean(errors.valor)}
                          helperText={errors.valor?.message}
                        />
                      )}
                    />
                  </Grid2>

                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      size="small"
                      required
                      label="Competência (MM/AAAA)"
                      placeholder="Ex: 09/2026"
                      {...register('competenciaMesAno')}
                      error={Boolean(errors.competenciaMesAno)}
                      helperText={errors.competenciaMesAno?.message}
                    />
                  </Grid2>

                  <Grid2 size={{ xs: 12 }}>
                    <TextField
                      fullWidth
                      size="small"
                      multiline
                      rows={2}
                      label="Observações"
                      {...register('observacoes')}
                      error={Boolean(errors.observacoes)}
                      helperText={errors.observacoes?.message}
                    />
                  </Grid2>
                </Grid2>
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} disabled={isPending} />
          <ActionButton title="Salvar Lançamento" icon={<SaveIcon />} color="primary" type="submit" loading={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};
```

---

## 8. Testes Automatizados e Garantia de Qualidade

* **Hooks e Utilitários:** Testados via Vitest utilizando `@testing-library/react` com a função `renderHook`.
* **Interatividade e Componentes:** Testados com React Testing Library e `@testing-library/user-event` simulando interações reais de mouse e teclado. Chamadas diretas a `fireEvent` são terminantemente proibidas.
* **Isolamento Total de Rede:** Todas as rotas de API consumidas por TanStack Query ou Axios DEVEM ser interceptadas por mocks declarativos do **MSW (Mock Service Worker)**. Nenhuma suite de teste deve executar sockets reais de rede.

---

## 9. Práticas Banidas e Anti-Patterns

* **Condicionais Inline Complexas no JSX:** Ternários aninhados ou operadores lógicos com mais de um nível de profundidade (`{a && b ? <C/> : <D/>}`) são proibidos no bloco de retorno. Extraia para variáveis guardiãs ou subcomponentes.
* **Uso de `any` ou Tipagens Frouxas:** O uso de `any` ou `unknown` sem narrowing imediato quebrará o pipeline de compilação. Todas as props, respostas de API e payloads devem ser tipados estritamente.
* **Proliferação de Estados Primitivos (*Primitive Bloat*):** Proibido criar múltiplos `useState` para campos de um mesmo fluxo. Utilize `react-hook-form` ou reducers com estados unificados.
* **Importações Relativas Profundas:** Caminhos com múltiplos níveis ascendentes (`../../../../components`) são bloqueados. Use exclusivamente os aliases de importação configurados (`@/components`, `@/hooks`, `@/utils`, `@/schemas`).
* **Vazamento de Dados Pessoais e Logs:** Declarações de `console.log` devem ser expurgadas antes do commit. Mensagens de erro e telemetria não podem expor dados pessoais (PII), respeitando as regras estritas da LGPD.

---

## 10. Checklist de Qualidade Frontend para o Agente IA

Antes de concluir qualquer tarefa ou submeter alterações, valide rigorosamente:

1. [ ] **Documentação Viva:** Todos os componentes de tela, formulários e hooks possuem blocos JSDoc documentando a regra de negócio e as decisões ergonômicas?
2. [ ] **Validação com Zod:** Todo formulário possui schema Zod correspondente em `src/schemas/` com mensagens de erro humanizadas?
3. [ ] **Semântica do MUI 9:** Não existem elementos HTML brutos (`div`, `span`, `p`) soltos no JSX para fins de layout?
4. [ ] **Grid2 Unificado:** Foi utilizado estritamente `<Grid2 container spacing={2}>` com `<Grid2 size={{ xs: 12, ... }}>`, sem tags legadas de Grid?
5. [ ] **Acessibilidade em Tabelas:** As linhas navegáveis possuem `role="button"`, `tabIndex={0}`, controle de tecla `Enter`/`Space` e os menus internos contêm `event.stopPropagation()`?
6. [ ] **Tríade de Estados Assíncronos:** A tela implementa Skeleton para loading, EmptyState visual para ausência de registros e alerta com retry para erros?
7. [ ] **Isolamento de Server-State:** Toda comunicação com a API é intermediada por hooks de TanStack Query com query keys hierárquicas e invalidação pós-mutação?
8. [ ] **Tokens de Design:** Não existem códigos hexadecimais manuais e o `borderRadius: 12` do tema é preservado?
9. [ ] **Listas Pesquisáveis:** Campos, filtros, seleção múltipla e Linhas por página seguem a seção 2.1 do Design System e passam na suíte estrutural?
