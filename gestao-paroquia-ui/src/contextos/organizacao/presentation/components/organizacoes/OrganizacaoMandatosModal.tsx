import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Alert, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, Grid, MenuItem,
  Paper, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CloseIcon from '@mui/icons-material/Close';
import EventBusyIcon from '@mui/icons-material/EventBusy';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { getOrganizacaoMembros } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import {
  encerrarMandato, getMandatos, homologarMandato, proporMandato, vetarMandato,
} from '@/contextos/organizacao/infrastructure/http/mandatos';
import {
  FUNCAO_COORDENACAO_LABELS, FuncaoCoordenacao, MandatoCoordenacao, Organizacao,
} from '@/shared/domain/types/types';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import { formatDateBr } from '@/shared/utils/masks';
import { getMandatoStatusMeta } from '@/shared/utils/statusMeta/mandatoStatus';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';

interface OrganizacaoMandatosModalProps {
  open: boolean;
  onClose: () => void;
  organizacao: Organizacao | null;
  canPropor: boolean;
  canHomologar: boolean;
}

const hoje = () => new Date().toISOString().split('T')[0];

/**
 * Coordenação de uma organização como encargo eclesial temporário: o grupo ou o conselho propõe um fiel
 * (membro ativo) e somente o Pároco homologa, veta ou encerra o mandato (Cân. 519 e 317).
 */
export const OrganizacaoMandatosModal: React.FC<OrganizacaoMandatosModalProps> = ({
  open,
  onClose,
  organizacao,
  canPropor,
  canHomologar,
}) => {
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();

  const [fielId, setFielId] = useState('');
  const [funcao, setFuncao] = useState<FuncaoCoordenacao>('COORDENADOR');
  const [inicio, setInicio] = useState(hoje());
  const [fim, setFim] = useState('');
  const [vetando, setVetando] = useState<MandatoCoordenacao | null>(null);
  const [motivo, setMotivo] = useState('');

  const orgId = organizacao?.id;
  const { data: mandatos = [], isLoading } = useQuery({
    queryKey: ['mandatos', orgId],
    queryFn: () => getMandatos(orgId!),
    enabled: open && !!orgId,
  });
  const { data: membros = [] } = useQuery({
    queryKey: ['organizacaoMembros', orgId],
    queryFn: () => getOrganizacaoMembros(orgId!),
    enabled: open && !!orgId && canPropor,
  });
  const candidatos = membros.filter((m) => m.fielId && m.ativo);

  const aposMutacao = (mensagem: string) => {
    queryClient.invalidateQueries({ queryKey: ['mandatos', orgId] });
    queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
    queryClient.invalidateQueries({ queryKey: ['organizacaoMembros', orgId] });
    showSuccess(mensagem);
  };

  const proporMutation = useMutation({
    mutationFn: () => proporMandato(orgId!, { fielId, funcao, inicio, fim }),
    onSuccess: () => {
      aposMutacao('Mandato proposto; aguarda a homologação do Pároco.');
      setFielId('');
      setFim('');
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao propor mandato.')),
  });
  const homologarMutation = useMutation({
    mutationFn: (m: MandatoCoordenacao) => homologarMandato(orgId!, m.id),
    onSuccess: () => aposMutacao('Mandato homologado.'),
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao homologar mandato.')),
  });
  const encerrarMutation = useMutation({
    mutationFn: (m: MandatoCoordenacao) => encerrarMandato(orgId!, m.id),
    onSuccess: () => aposMutacao('Mandato encerrado.'),
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao encerrar mandato.')),
  });
  const vetarMutation = useMutation({
    mutationFn: () => vetarMandato(orgId!, vetando!.id, motivo.trim()),
    onSuccess: () => {
      aposMutacao('Mandato vetado.');
      setVetando(null);
      setMotivo('');
    },
    onError: (err) => showError(extractErrorMessage(err, 'Erro ao vetar mandato.')),
  });

  const podeEnviar = !!fielId && !!inicio && !!fim && !proporMutation.isPending;

  return (
    <>
      <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
        <DialogTitle>{`Coordenação — ${organizacao?.nome ?? ''}`}</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Alert severity="info">
              A coordenação é um encargo temporário. O grupo propõe e somente o Pároco confirma, veta ou encerra o mandato.
            </Alert>

            {canPropor && (
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main', mb: 2 }}>
                    Propor mandato
                  </Typography>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <FormControl fullWidth size="small">
                        <SearchableSelect
                          labelId="mandato-fiel-label"
                          label="Fiel (membro ativo)"
                          value={fielId}
                          onChange={(e) => setFielId(e.target.value as string)}
                        >
                          <MenuItem value=""><em>Selecione...</em></MenuItem>
                          {candidatos.map((m) => (
                            <MenuItem key={m.vinculoId} value={m.fielId}>{m.fielNome || m.nomeCompleto || m.usuarioNome}</MenuItem>
                          ))}
                        </SearchableSelect>
                      </FormControl>
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <FormControl fullWidth size="small">
                        <SearchableSelect
                          labelId="mandato-funcao-label"
                          label="Função"
                          value={funcao}
                          onChange={(e) => setFuncao(e.target.value as FuncaoCoordenacao)}
                        >
                          {Object.entries(FUNCAO_COORDENACAO_LABELS).map(([valor, rotulo]) => (
                            <MenuItem key={valor} value={valor}>{rotulo}</MenuItem>
                          ))}
                        </SearchableSelect>
                      </FormControl>
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <TextField
                        label="Início" type="date" size="small" fullWidth value={inicio}
                        onChange={(e) => setInicio(e.target.value)}
                        slotProps={{ inputLabel: { shrink: true } }}
                      />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <TextField
                        label="Término (máx. 3 anos)" type="date" size="small" fullWidth value={fim}
                        onChange={(e) => setFim(e.target.value)}
                        slotProps={{ inputLabel: { shrink: true } }}
                      />
                    </Grid>
                  </Grid>
                  <Stack direction="row" sx={{ justifyContent: 'flex-end', mt: 2 }}>
                    <ActionButton
                      title="Propor" icon={<AddIcon />} color="primary" disabled={!podeEnviar}
                      onClick={() => proporMutation.mutate()}
                    />
                  </Stack>
                </CardContent>
              </Card>
            )}

            {isLoading ? (
              <Skeleton variant="rectangular" height={96} />
            ) : (
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead sx={{ backgroundColor: 'action.hover' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Fiel</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Função</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Período</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Situação</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {mandatos.map((m) => (
                      <TableRow key={m.id} hover>
                        <TableCell sx={{ fontWeight: 600 }}>{m.fielNome || '-'}</TableCell>
                        <TableCell>{FUNCAO_COORDENACAO_LABELS[m.funcao]}</TableCell>
                        <TableCell>{`${formatDateBr(m.inicio)} a ${formatDateBr(m.fim)}`}</TableCell>
                        <TableCell>
                          <StatusIcon {...getMandatoStatusMeta(m.status)} />
                          {m.status === 'VETADO' && m.motivoVeto && (
                            <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                              {m.motivoVeto}
                            </Typography>
                          )}
                        </TableCell>
                        <TableCell align="right">
                          {canHomologar && (m.status === 'PROPOSTO' || m.status === 'HOMOLOGADO') && (
                            <TableActionMenu
                              actions={m.status === 'PROPOSTO' ? [
                                { label: 'Homologar', icon: <CheckCircleIcon fontSize="small" color="success" />, onClick: () => homologarMutation.mutate(m), disabled: homologarMutation.isPending },
                                { label: 'Vetar', icon: <BlockIcon fontSize="small" color="error" />, onClick: () => setVetando(m) },
                              ] : [
                                { label: 'Encerrar mandato', icon: <EventBusyIcon fontSize="small" color="error" />, onClick: () => encerrarMutation.mutate(m), disabled: encerrarMutation.isPending },
                              ]}
                            />
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                    {mandatos.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                          <Typography variant="body2" color="text.secondary">Nenhum mandato registrado.</Typography>
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
        </DialogActions>
      </Dialog>

      <Dialog open={!!vetando} onClose={() => setVetando(null)} fullWidth maxWidth="xs">
        <DialogTitle>Vetar mandato</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <TextField
            label="Motivo do veto" size="small" fullWidth required multiline minRows={2} value={motivo}
            onChange={(e) => setMotivo(e.target.value)} sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={() => setVetando(null)} />
          <ActionButton
            title="Vetar" icon={<BlockIcon />} color="error"
            disabled={!motivo.trim() || vetarMutation.isPending} onClick={() => vetarMutation.mutate()}
          />
        </DialogActions>
      </Dialog>
    </>
  );
};
