import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, Container, FormControl, MenuItem, Stack, TextField, useTheme } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import EditIcon from '@mui/icons-material/Edit';
import EditNoteIcon from '@mui/icons-material/EditNote';
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff';
import HistoryEduIcon from '@mui/icons-material/HistoryEdu';
import PostAddIcon from '@mui/icons-material/PostAdd';
import ReplayIcon from '@mui/icons-material/Replay';
import SendIcon from '@mui/icons-material/Send';
import VerifiedIcon from '@mui/icons-material/Verified';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { canRedigirAta } from '@/contextos/iam/presentation/auth/permissions';
import { getOrganizacoes } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import { obterAta } from '@/contextos/conselho/infrastructure/http/atas';
import { Ata, AtaResumo, FiltroAtas, OpcoesExportacao, STATUS_ATA_LABEL, StatusAta, TipoReuniao } from '@/contextos/conselho/domain/ata';
import {
  useAtas, useAtualizarAta, useExportarAta, useExportarCompilacao, useHomologarAta, useRascunharAta,
  useReabrirAta, useRetificarAta, useSubmeterAta, useTiposReuniao, useVetarAta,
} from '@/contextos/conselho/presentation/hooks/useAtas';
import { AtaFormInput, paraConteudoPayload, paraRascunhoPayload } from '@/contextos/conselho/presentation/schemas/ataSchema';
import AtaExportMenu from '@/contextos/conselho/presentation/components/AtaExportMenu';
import AtaFormModal from '@/contextos/conselho/presentation/components/AtaFormModal';
import AtasTable from '@/contextos/conselho/presentation/components/AtasTable';
import AtaViewModal from '@/contextos/conselho/presentation/components/AtaViewModal';
import JustificativaDialog from '@/contextos/conselho/presentation/components/JustificativaDialog';
import ActionButton from '@/shared/presentation/components/ActionButton';
import PageHeader from '@/shared/presentation/components/PageHeader';
import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import { TableAction } from '@/shared/presentation/components/TableActionMenu';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';

type Alvo = { id: string; titulo: string } | null;

const FILTRO_VAZIO = { organizacaoId: '', tipo: '', status: '', de: '', ate: '' };

/**
 * Atas das reuniões da paróquia e das comunidades. O servidor decide o escopo de leitura e as ações permitidas em
 * cada ata (`acoes`); esta tela apenas oferece o que ele autorizou. Atas seladas são imutáveis e corrigidas por ata
 * retificadora; toda exportação, em Word ou PDF, fica registrada na trilha de auditoria.
 */
export const ConselhosAtas: React.FC = () => {
  const theme = useTheme();
  const { user } = useAuth();
  const { showError } = useNotification();

  const [filtro, setFiltro] = useState(FILTRO_VAZIO);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [visualizarId, setVisualizarId] = useState<string | null>(null);
  const [form, setForm] = useState<{ aberto: boolean; edicao: Ata | null }>({ aberto: false, edicao: null });
  const [vetar, setVetar] = useState<Alvo>(null);
  const [retificar, setRetificar] = useState<Alvo>(null);

  const consulta: FiltroAtas = {
    organizacaoId: filtro.organizacaoId || undefined,
    tipo: (filtro.tipo as TipoReuniao) || undefined,
    status: (filtro.status as StatusAta) || undefined,
    de: filtro.de || undefined,
    ate: filtro.ate || undefined,
  };
  const { data: pagina, isLoading } = useAtas({ ...consulta, pagina: page, tamanho: rowsPerPage });
  const { data: tipos = [] } = useTiposReuniao();
  const { data: organizacoes = [] } = useQuery({ queryKey: ['organizacoes'], queryFn: getOrganizacoes });

  const rascunhar = useRascunharAta();
  const atualizar = useAtualizarAta();
  const submeter = useSubmeterAta();
  const homologar = useHomologarAta();
  const vetarAta = useVetarAta();
  const reabrir = useReabrirAta();
  const retificarAta = useRetificarAta();
  const exportar = useExportarAta();
  const exportarCompilacao = useExportarCompilacao();

  const orgaosParaFiltro = organizacoes.filter((o) => o.ativo && o.tipo === 'CONSELHO');
  const temFiltro = Object.values(filtro).some(Boolean);

  const alterarFiltro = (campo: keyof typeof FILTRO_VAZIO, valor: string) => {
    setFiltro((atual) => ({ ...atual, [campo]: valor }));
    setPage(0);
  };

  const abrirEdicao = async (id: string) => {
    try {
      setForm({ aberto: true, edicao: await obterAta(id) });
    } catch (e) {
      showError(extractErrorMessage(e, 'Erro ao carregar o rascunho.'));
    }
  };

  const salvar = (dados: AtaFormInput, regras: Parameters<typeof paraRascunhoPayload>[1]) => {
    const fechar = { onSuccess: () => setForm({ aberto: false, edicao: null }) };
    if (form.edicao) {
      atualizar.mutate({ id: form.edicao.id, conteudo: paraConteudoPayload(dados, regras) }, fechar);
    } else {
      rascunhar.mutate(paraRascunhoPayload(dados, regras), fechar);
    }
  };

  const exportarAta = (id: string, opcoes: OpcoesExportacao) => exportar.mutate({ id, opcoes });

  const acoesDaLinha = (a: AtaResumo): TableAction[] => {
    const tem = (acao: AtaResumo['acoes'][number]) => a.acoes.includes(acao);
    const linhas: TableAction[] = [
      { label: 'Visualizar', icon: <VisibilityIcon fontSize="small" color="primary" />, onClick: () => setVisualizarId(a.id) },
    ];
    if (tem('REDIGIR')) linhas.push({ label: 'Editar rascunho', icon: <EditIcon fontSize="small" color="primary" />, onClick: () => void abrirEdicao(a.id) });
    if (tem('SUBMETER')) linhas.push({ label: 'Submeter', icon: <SendIcon fontSize="small" color="primary" />, onClick: () => submeter.mutate(a.id) });
    if (tem('HOMOLOGAR')) linhas.push({ label: 'Homologar', icon: <VerifiedIcon fontSize="small" color="success" />, onClick: () => homologar.mutate(a.id) });
    if (tem('VETAR')) linhas.push({ label: 'Vetar', icon: <BlockIcon fontSize="small" color="error" />, onClick: () => setVetar({ id: a.id, titulo: a.titulo }) });
    if (tem('REABRIR')) linhas.push({ label: 'Reabrir para correção', icon: <ReplayIcon fontSize="small" color="primary" />, onClick: () => reabrir.mutate(a.id) });
    if (tem('RETIFICAR')) linhas.push({ label: 'Retificar', icon: <EditNoteIcon fontSize="small" color="primary" />, onClick: () => setRetificar({ id: a.id, titulo: a.titulo }) });
    return linhas;
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <PageHeader
        icon={<HistoryEduIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Atas e decisões dos conselhos"
        subtitle="Registre as atas das reuniões da paróquia e das comunidades, acompanhe a homologação do Pároco e exporte em Word ou PDF."
        actions={(
          <Stack direction="row" spacing={1}>
            <AtaExportMenu
              rotulo="Exportar livro de atas"
              disabled={exportarCompilacao.isPending}
              onExportar={(opcoes) => exportarCompilacao.mutate({ filtro: consulta, opcoes })}
            />
            {canRedigirAta(user) && (
              <ActionButton title="Nova ata" icon={<PostAddIcon />} color="primary" onClick={() => setForm({ aberto: true, edicao: null })} />
            )}
          </Stack>
        )}
      />

      <Card elevation={0} sx={{ p: 2, mb: 3, border: '1px solid', borderColor: 'divider', backgroundColor: theme.custom?.subtlePanelBg ?? '#F8F9FA' }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
          <FormControl size="small" sx={{ minWidth: 240 }}>
            <SearchableSelect labelId="filtro-orgao" label="Órgão" value={filtro.organizacaoId}
              onChange={(e) => alterarFiltro('organizacaoId', e.target.value)}>
              <MenuItem value="">Todos os órgãos</MenuItem>
              {orgaosParaFiltro.map((o) => <MenuItem key={o.id} value={o.id}>{o.nome}</MenuItem>)}
            </SearchableSelect>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 260 }}>
            <SearchableSelect labelId="filtro-tipo" label="Tipo de reunião" value={filtro.tipo}
              onChange={(e) => alterarFiltro('tipo', e.target.value)}>
              <MenuItem value="">Todos os tipos</MenuItem>
              {tipos.map((t) => <MenuItem key={t.codigo} value={t.codigo}>{t.rotulo}</MenuItem>)}
            </SearchableSelect>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <SearchableSelect labelId="filtro-status" label="Situação" value={filtro.status}
              onChange={(e) => alterarFiltro('status', e.target.value)}>
              <MenuItem value="">Todas as situações</MenuItem>
              {Object.entries(STATUS_ATA_LABEL).map(([v, l]) => <MenuItem key={v} value={v}>{l}</MenuItem>)}
            </SearchableSelect>
          </FormControl>
          <TextField label="De" type="date" size="small" value={filtro.de} slotProps={{ inputLabel: { shrink: true } }}
            onChange={(e) => alterarFiltro('de', e.target.value)} />
          <TextField label="Até" type="date" size="small" value={filtro.ate} slotProps={{ inputLabel: { shrink: true } }}
            onChange={(e) => alterarFiltro('ate', e.target.value)} />
          {temFiltro && (
            <ActionButton title="Limpar filtros" icon={<FilterAltOffIcon fontSize="small" />} color="secondary"
              onClick={() => { setFiltro(FILTRO_VAZIO); setPage(0); }} />
          )}
        </Stack>
      </Card>

      <AtasTable
        atas={pagina?.itens ?? []}
        total={pagina?.total ?? 0}
        isLoading={isLoading}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={setPage}
        onRowsPerPageChange={(linhas) => { setRowsPerPage(linhas); setPage(0); }}
        onAbrir={(a) => setVisualizarId(a.id)}
        rowActions={acoesDaLinha}
      />

      <AtaViewModal
        ataId={visualizarId}
        onClose={() => setVisualizarId(null)}
        onEditar={(a) => { setVisualizarId(null); setForm({ aberto: true, edicao: a }); }}
        onSubmeter={(a) => submeter.mutate(a.id)}
        onHomologar={(a) => homologar.mutate(a.id)}
        onVetar={(a) => setVetar({ id: a.id, titulo: a.conteudo.titulo })}
        onReabrir={(a) => reabrir.mutate(a.id)}
        onRetificar={(a) => setRetificar({ id: a.id, titulo: a.conteudo.titulo })}
        onExportar={(a, opcoes) => exportarAta(a.id, opcoes)}
      />

      <AtaFormModal
        open={form.aberto}
        edicao={form.edicao}
        tipos={tipos}
        organizacoes={organizacoes}
        isPending={rascunhar.isPending || atualizar.isPending}
        onClose={() => setForm({ aberto: false, edicao: null })}
        onSubmit={salvar}
      />

      <JustificativaDialog
        open={vetar !== null}
        isPending={vetarAta.isPending}
        titulo="Vetar ata"
        rotulo="Motivo do veto"
        ajuda={`O motivo fica na trilha e volta ao redator${vetar ? ` da ata “${vetar.titulo}”` : ''}.`}
        icone={<BlockIcon />}
        cor="error"
        onClose={() => setVetar(null)}
        onConfirm={(motivo) => vetar && vetarAta.mutate({ id: vetar.id, motivo }, { onSuccess: () => setVetar(null) })}
      />

      <JustificativaDialog
        open={retificar !== null}
        isPending={retificarAta.isPending}
        titulo="Abrir ata retificadora"
        rotulo="Justificativa da retificação"
        ajuda={`A ata original${retificar ? ` “${retificar.titulo}”` : ''} permanece intacta; a retificadora nasce em rascunho.`}
        icone={<EditNoteIcon />}
        cor="primary"
        onClose={() => setRetificar(null)}
        onConfirm={(justificativa) => retificar && retificarAta.mutate({ id: retificar.id, justificativa }, {
          onSuccess: (nova) => { setRetificar(null); setVisualizarId(nova.id); },
        })}
      />
    </Container>
  );
};

export default ConselhosAtas;
