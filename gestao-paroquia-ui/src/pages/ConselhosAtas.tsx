import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Card,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
  Stack,
  useTheme,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import DescriptionIcon from '@mui/icons-material/Description';
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff';
import { useAuth } from '../auth/AuthContext';
import { useNotification } from '../context/NotificationContext';
import { getCpcDecisoes, createCpcDecisao, homologarCpcDecisao } from '../api/conselhos';
import { getOrganizacoes } from '../api/organizacoes';
import { CpcDecisao, StatusHomologacao } from '../api/types';
import { CpcDecisaoModal } from '../components/CpcDecisaoModal';
import ActionButton from '../components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '../theme/dialogStyles';
import TableActionMenu from '../components/TableActionMenu';
import StatusIcon from '../components/StatusIcon';
import { getHomologacaoStatusMeta } from '../utils/statusMeta/homologacaoStatus';
import PageHeader from '../components/PageHeader';
import { formatDateBr } from '../utils/masks';

export const ConselhosAtas: React.FC = () => {
  const theme = useTheme();
  const { user } = useAuth();
  const { showSuccess, showError } = useNotification();
  const queryClient = useQueryClient();

  const [selectedComunidade, setSelectedComunidade] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');

  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);

  const [isModalOpen, setIsModalOpen] = useState(false);

  const [vetoDialogItem, setVetoDialogItem] = useState<CpcDecisao | null>(null);
  const [motivoVeto, setMotivoVeto] = useState('');

  const canHomologate =
    user?.roles?.some((r) =>
      ['ADMIN', 'ROLE_ADMIN', 'PAROCO', 'ROLE_PAROCO', 'MESA_CPP', 'ROLE_MESA_CPP'].includes(r)
    ) ?? false;

  const { data: decisoes = [], isLoading } = useQuery({
    queryKey: ['cpcDecisoes', selectedComunidade, selectedStatus],
    queryFn: () =>
      getCpcDecisoes({
        comunidadeId: selectedComunidade || undefined,
        status: (selectedStatus as StatusHomologacao) || undefined,
      }),
  });

  const { data: organizacoes = [] } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
  });

  const communities = organizacoes.filter((o) => o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE');

  const createMutation = useMutation({
    mutationFn: createCpcDecisao,
    onSuccess: () => {
      showSuccess('Ata/Decisão submetida com sucesso para homologação!');
      setIsModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['cpcDecisoes'] });
    },
    onError: () => showError('Erro ao submeter ata de conselho.'),
  });

  const homologarMutation = useMutation({
    mutationFn: (params: { id: string; status: StatusHomologacao; motivoVeto?: string }) =>
      homologarCpcDecisao(params.id, { status: params.status, motivoVeto: params.motivoVeto }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['cpcDecisoes'] }),
  });

  const handleHomologar = (id: string) => {
    homologarMutation.mutate(
      { id, status: 'HOMOLOGADO' },
      {
        onSuccess: () => showSuccess('Ata/Decisão homologada com sucesso pelo Pároco/Mesa CPP!'),
        onError: () => showError('Erro ao homologar ata.'),
      }
    );
  };

  const handleConfirmVeto = () => {
    if (!vetoDialogItem) return;
    if (!motivoVeto.trim()) {
      showError('O motivo do veto é obrigatório.');
      return;
    }
    homologarMutation.mutate(
      { id: vetoDialogItem.id, status: 'VETADO', motivoVeto: motivoVeto.trim() },
      {
        onSuccess: () => {
          showSuccess('Ata/Decisão vetada com sucesso.');
          setVetoDialogItem(null);
          setMotivoVeto('');
        },
        onError: () => showError('Erro ao vetar ata.'),
      }
    );
  };

  const handleClearFilters = () => {
    setSelectedComunidade('');
    setSelectedStatus('');
  };

  const getCommunityName = (id: string) => {
    return communities.find((c) => c.id === id)?.nome || 'Comunidade';
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <PageHeader
        icon={<DescriptionIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Governança dos Conselhos (CPP & CPC)"
        subtitle="Gerencie o registro de atas das reuniões dos conselhos pastorais de comunidade (CPC) e do conselho pastoral paroquial (CPP), acompanhando a chancela e homologação."
        actions={
          <ActionButton
            title="Nova Ata / Decisão"
            icon={<AddIcon />}
            color="primary"
            onClick={() => setIsModalOpen(true)}
          />
        }
      />

      <Card
        elevation={0}
        sx={{
          p: 2,
          mb: 3,
          
          border: '1px solid',
          borderColor: 'divider',
          backgroundColor: theme.custom?.subtlePanelBg ?? '#F8F9FA',
        }}
      >
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
          <FormControl size="small" sx={{ minWidth: 240, flex: { xs: 1, sm: 'initial' } }}>
            <InputLabel id="filter-comunidade-label">Comunidade de Origem</InputLabel>
            <Select
              labelId="filter-comunidade-label"
              value={selectedComunidade}
              label="Comunidade de Origem"
              onChange={(e) => setSelectedComunidade(e.target.value)}
             
            >
              <MenuItem value="">Todas as Comunidades</MenuItem>
              {communities.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  {c.nome}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 220, flex: { xs: 1, sm: 'initial' } }}>
            <InputLabel id="filter-status-label">Status de Homologação</InputLabel>
            <Select
              labelId="filter-status-label"
              value={selectedStatus}
              label="Status de Homologação"
              onChange={(e) => setSelectedStatus(e.target.value)}
             
            >
              <MenuItem value="">Todos os Status</MenuItem>
              <MenuItem value="PENDENTE_HOMOLOGACAO">Pendente de Homologação</MenuItem>
              <MenuItem value="HOMOLOGADO">Homologado</MenuItem>
              <MenuItem value="VETADO">Vetado</MenuItem>
            </Select>
          </FormControl>

          {(selectedComunidade || selectedStatus) && (
            <ActionButton
              title="Limpar Filtros"
              icon={<FilterAltOffIcon fontSize="small" />}
              size="small"
              onClick={handleClearFilters}
              color="secondary"
            />
          )}
        </Stack>
      </Card>

      {isLoading ? (
        <Stack spacing={1}>
          <Skeleton variant="rectangular" height={48} />
          <Skeleton variant="rectangular" height={72} />
          <Skeleton variant="rectangular" height={72} />
          <Skeleton variant="rectangular" height={72} />
        </Stack>
      ) : (
        <TableContainer component={Paper} sx={{ overflow: 'hidden' }}>
          <Table>
            <TableHead sx={{ backgroundColor: 'action.hover' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Título / Pauta</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Comunidade</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Data da Reunião</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Status Homologação</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="center">
                  Ações de Homologação
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {decisoes.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 6 }}>
                    <Typography variant="body1" color="text.secondary">
                      Nenhuma ata ou decisão registrada para os filtros selecionados.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                decisoes
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((item) => (
                    <TableRow key={item.id} hover>
                      <TableCell sx={{ maxW: 400 }}>
                        <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                          {item.titulo}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'pre-line' }}>
                          {item.pautaDescricao}
                        </Typography>
                        {item.motivoVeto && (
                          <Typography variant="caption" sx={{ display: 'block', color: 'error.main', mt: 0.5, fontWeight: 500 }}>
                            Motivo do Veto: {item.motivoVeto}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>{getCommunityName(item.comunidadeId)}</TableCell>
                      <TableCell>{formatDateBr(item.dataReuniao)}</TableCell>
                      <TableCell><StatusIcon {...getHomologacaoStatusMeta(item.statusHomologacao)} sx={{ fontWeight: 600 }} /></TableCell>
                      <TableCell align="center">
                        {canHomologate && item.statusHomologacao === 'PENDENTE_HOMOLOGACAO' ? (
                          <TableActionMenu
                            actions={[
                              { label: 'Homologar ata', icon: <CheckCircleIcon fontSize="small" />, onClick: () => handleHomologar(item.id) },
                              { label: 'Vetar ata', icon: <CancelIcon fontSize="small" />, onClick: () => setVetoDialogItem(item) },
                            ]}
                          />
                        ) : (
                          <Typography variant="caption" color="text.secondary">
                            —
                          </Typography>
                        )}
                      </TableCell>
                    </TableRow>
                  ))
              )}
            </TableBody>
          </Table>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={decisoes.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
            }}
            labelRowsPerPage="Linhas por página:"
            labelDisplayedRows={({ from, to, count }) =>
              `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
            }
          />
        </TableContainer>
      )}

      <CpcDecisaoModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={(data) => createMutation.mutate(data)}
        isSubmitting={createMutation.isPending}
        communities={communities}
      />

      <Dialog open={!!vetoDialogItem} onClose={() => setVetoDialogItem(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Vetar Ata / Decisão de Conselho</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5} sx={{ mt: 1 }}>
            <Typography variant="body2" color="textSecondary">
              Informe o motivo do veto para a ata "{vetoDialogItem?.titulo}". Esta justificativa será gravada de forma auditável.
            </Typography>
            <TextField
              label="Motivo do Veto"
              fullWidth
              multiline
              rows={3}
              size="small"
              value={motivoVeto}
              onChange={(e) => setMotivoVeto(e.target.value)}
              required
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar veto" icon={<CloseIcon />} onClick={() => setVetoDialogItem(null)} color="secondary" />
          <ActionButton title="Confirmar veto da ata / decisão" icon={<CancelIcon />} onClick={handleConfirmVeto} color="error" />
        </DialogActions>
      </Dialog>
    </Container>
  );
};
export default ConselhosAtas;
