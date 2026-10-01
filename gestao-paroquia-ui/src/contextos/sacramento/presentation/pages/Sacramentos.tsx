import { useMemo, useState } from 'react';
import { Stack, Tab, Tabs } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DescriptionIcon from '@mui/icons-material/Description';
import EditIcon from '@mui/icons-material/Edit';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import VerifiedIcon from '@mui/icons-material/Verified';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import {
  canAcessarLivroReservado, canHomologarSacramento, canManageSacramentos,
} from '@/contextos/iam/presentation/auth/permissions';
import ActionButton from '@/shared/presentation/components/ActionButton';
import PageHeader from '@/shared/presentation/components/PageHeader';
import SearchField from '@/shared/presentation/components/SearchField';
import { TableAction } from '@/shared/presentation/components/TableActionMenu';
import {
  Assento, AssentoResumo, Averbacao, Certidao, Participante, TIPOS_SACRAMENTO, TIPO_SACRAMENTO_LABEL, TipoSacramento,
} from '@/contextos/sacramento/domain/sacramento';
import { emitirCertidao, obterAssento } from '@/contextos/sacramento/infrastructure/http/sacramentos';
import { useAssentoMutations, useAssentos } from '@/contextos/sacramento/presentation/hooks/useAssentos';
import {
  AssentoFormInput, assentoParaForm, paraLavrarPayload, paraTeorPayload, participanteVazio,
} from '@/contextos/sacramento/presentation/schemas/assentoSchema';
import { PAPEIS_POR_TIPO } from '@/contextos/sacramento/domain/sacramento';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import AssentosTable from '@/contextos/sacramento/presentation/components/AssentosTable';
import AssentoFormModal from '@/contextos/sacramento/presentation/components/AssentoFormModal';
import AssentoViewModal from '@/contextos/sacramento/presentation/components/AssentoViewModal';
import AverbacaoDialog from '@/contextos/sacramento/presentation/components/AverbacaoDialog';
import CertidaoDialog from '@/contextos/sacramento/presentation/components/CertidaoDialog';
import VinculoDialog from '@/contextos/sacramento/presentation/components/VinculoDialog';
import {
  CancelamentoDialog, RetificacaoDialog,
} from '@/contextos/sacramento/presentation/components/MotivoDialog';

const formularioVazio = (tipo: TipoSacramento): AssentoFormInput => ({
  tipo,
  visibilidade: 'COMUM',
  livro: '',
  folha: '',
  termo: '',
  paroquiaCustodiante: '',
  localCelebracao: '',
  dataCelebracao: '',
  dadoSensivel: false,
  dadosEspecificos: {},
  participantes: [participanteVazio(PAPEIS_POR_TIPO[tipo][0])],
});

/**
 * Livro de assentos sacramentais (Cân. 535). Uma aba por sacramento; penitência jamais é registrada
 * (Cân. 983). Secretaria prepara o termo, Pároco homologa; nada é excluído, só cancelado com motivo.
 */
const Sacramentos = () => {
  const { user } = useAuth();
  const { showError } = useNotification();
  const podeGerir = canManageSacramentos(user);
  const podeHomologar = canHomologarSacramento(user);

  const [tipo, setTipo] = useState<TipoSacramento>('BATISMO');
  const [busca, setBusca] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [visualizarId, setVisualizarId] = useState<string | null>(null);
  const [form, setForm] = useState<{ edicao: Assento | null; aberto: boolean }>({ edicao: null, aberto: false });
  const [vinculo, setVinculo] = useState<{ assento: Assento; participante: Participante } | null>(null);
  const [averbarAssento, setAverbarAssento] = useState<Assento | null>(null);
  const [retificarAssento, setRetificarAssento] = useState<Assento | null>(null);
  const [cancelarAssento, setCancelarAssento] = useState<Assento | null>(null);
  const [certidao, setCertidao] = useState<Certidao | null>(null);

  const { data: assentos = [], isLoading } = useAssentos(tipo);
  const m = useAssentoMutations();

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    return assentos.filter((a) => !termo || (a.sujeito ?? '').toLowerCase().includes(termo)
      || [a.livro, a.folha, a.termo].some((v) => (v ?? '').toLowerCase().includes(termo)));
  }, [assentos, busca]);

  const abrirEdicao = async (id: string) => {
    try {
      setForm({ edicao: await obterAssento(id), aberto: true });
    } catch (err) {
      showError(extractErrorMessage(err, 'Erro ao carregar o rascunho.'));
    }
  };

  const abrirCertidao = async (id: string) => {
    try {
      setCertidao(await emitirCertidao(id));
    } catch (err) {
      showError(extractErrorMessage(err, 'Erro ao emitir a certidão.'));
    }
  };

  const salvarForm = (dados: AssentoFormInput) => {
    const fechar = { onSuccess: () => setForm({ edicao: null, aberto: false }) };
    if (form.edicao) m.atualizar.mutate({ id: form.edicao.id, teor: paraTeorPayload(dados) }, fechar);
    else m.lavrar.mutate(paraLavrarPayload(dados), fechar);
  };

  const acoesDaLinha = (a: AssentoResumo): TableAction[] => {
    const acoes: TableAction[] = [
      { label: 'Visualizar', icon: <VisibilityIcon fontSize="small" />, onClick: () => setVisualizarId(a.id) },
    ];
    if (podeGerir && a.status === 'DRAFT') {
      acoes.push({ label: 'Editar rascunho', icon: <EditIcon fontSize="small" />, onClick: () => void abrirEdicao(a.id) });
    }
    if (podeHomologar && a.status === 'DRAFT') {
      acoes.push({ label: 'Homologar', icon: <VerifiedIcon fontSize="small" color="success" />, onClick: () => m.homologar.mutate(a.id) });
    }
    if (podeGerir && a.status === 'SEALED') {
      acoes.push({ label: 'Emitir certidão', icon: <DescriptionIcon fontSize="small" />, onClick: () => void abrirCertidao(a.id) });
    }
    return acoes;
  };

  return (
    <Stack spacing={3}>
      <PageHeader
        icon={<AutoStoriesIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Sacramentos"
        subtitle="Livro de assentos de Batismo, Confirmação, Matrimônio e Ordem Sacra."
        actions={podeGerir && (
          <ActionButton
            title="Novo Assento"
            icon={<AddIcon />}
            color="primary"
            onClick={() => setForm({ edicao: null, aberto: true })}
            sx={{ border: '1px solid', borderColor: 'primary.main' }}
          />
        )}
      />
      <Tabs value={tipo} onChange={(_, v: TipoSacramento) => { setTipo(v); setPage(0); }} variant="scrollable" aria-label="Sacramentos">
        {TIPOS_SACRAMENTO.map((t) => <Tab key={t} value={t} label={TIPO_SACRAMENTO_LABEL[t]} />)}
      </Tabs>
      <SearchField value={busca} onChange={(v) => { setBusca(v); setPage(0); }} placeholder="Pesquisar por nome, livro, folha ou termo..." />
      <AssentosTable
        assentos={filtrados}
        temAssentos={assentos.length > 0}
        isLoading={isLoading}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={setPage}
        onRowsPerPageChange={setRowsPerPage}
        onAbrir={(a) => setVisualizarId(a.id)}
        rowActions={acoesDaLinha}
      />

      <AssentoViewModal
        assentoId={visualizarId}
        onClose={() => setVisualizarId(null)}
        podeGerir={podeGerir}
        podeHomologar={podeHomologar}
        onEditar={(a) => { setVisualizarId(null); setForm({ edicao: a, aberto: true }); }}
        onHomologar={(a) => m.homologar.mutate(a.id)}
        onRetificar={setRetificarAssento}
        onCancelar={setCancelarAssento}
        onCertidao={(a) => void abrirCertidao(a.id)}
        onVincular={(assento, participante) => setVinculo({ assento, participante })}
        onDesvincular={(a, p) => m.desvincular.mutate({ id: a.id, participanteId: p.id })}
        onAprovarVinculo={(a, p) => m.aprovarVinculo.mutate({ id: a.id, participanteId: p.id })}
        onRejeitarVinculo={(a, p) => m.rejeitarVinculo.mutate({ id: a.id, participanteId: p.id })}
        onAverbar={setAverbarAssento}
        onHomologarAverbacao={(a: Assento, av: Averbacao) => m.homologarAverbacao.mutate({ id: a.id, averbacaoId: av.id })}
      />

      <AssentoFormModal
        open={form.aberto}
        tipo={form.edicao?.tipo ?? tipo}
        inicial={form.edicao ? assentoParaForm(form.edicao) : formularioVazio(tipo)}
        edicao={form.edicao !== null}
        podeLivroReservado={canAcessarLivroReservado(user)}
        isPending={m.lavrar.isPending || m.atualizar.isPending}
        onClose={() => setForm({ edicao: null, aberto: false })}
        onSubmit={salvarForm}
      />

      <VinculoDialog
        open={vinculo !== null}
        participante={vinculo?.participante ?? null}
        isPending={m.vincular.isPending}
        onClose={() => setVinculo(null)}
        onConfirm={(v) => {
          if (vinculo) m.vincular.mutate({ id: vinculo.assento.id, participanteId: vinculo.participante.id, ...v }, { onSuccess: () => setVinculo(null) });
        }}
      />

      <AverbacaoDialog
        open={averbarAssento !== null}
        isPending={m.averbar.isPending}
        onClose={() => setAverbarAssento(null)}
        onConfirm={(payload) => {
          if (averbarAssento) m.averbar.mutate({ id: averbarAssento.id, payload }, { onSuccess: () => setAverbarAssento(null) });
        }}
      />

      <RetificacaoDialog
        open={retificarAssento !== null}
        isPending={m.retificar.isPending}
        onClose={() => setRetificarAssento(null)}
        onConfirm={(payload) => {
          if (retificarAssento) m.retificar.mutate({ id: retificarAssento.id, payload }, { onSuccess: () => setRetificarAssento(null) });
        }}
      />

      <CancelamentoDialog
        open={cancelarAssento !== null}
        isPending={m.cancelar.isPending}
        onClose={() => setCancelarAssento(null)}
        onConfirm={(motivo) => {
          if (cancelarAssento) m.cancelar.mutate({ id: cancelarAssento.id, motivo }, { onSuccess: () => setCancelarAssento(null) });
        }}
      />

      <CertidaoDialog certidao={certidao} onClose={() => setCertidao(null)} />
    </Stack>
  );
};

export default Sacramentos;
