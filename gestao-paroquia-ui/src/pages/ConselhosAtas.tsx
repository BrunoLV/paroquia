import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Button,
  Card,
  Chip,
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
  TableRow,
  TextField,
  Typography,
  IconButton,
  Tooltip,
  Stack,
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
import PageHeader from '../components/PageHeader';
import theme from '../theme';

export const ConselhosAtas: React.FC = () => {
  const { user } = useAuth();
  const { showSuccess, showError } = useNotification();
  const queryClient = useQueryClient();

  const [selectedComunidade, setSelectedComunidade] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');

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

  const getStatusChip = (status: StatusHomologacao) => {
    switch (status) {
      case 'HOMOLOGADO':
        return <Chip label="Homologado" color="success" size="small" sx={{ fontWeight: 600 }} />;
      case 'VETADO':
        return <Chip label="Vetado" color="error" size="small" sx={{ fontWeight: 600 }} />;
      default:
        return <Chip label="Pendente de Homologação" color="warning" size="small" sx={{ fontWeight: 600 }} />;
    }
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
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => setIsModalOpen(true)}
            sx={{ textTransform: 'none', fontWeight: 600, px: 3, py: 1 }}
          >
            Nova Ata / Decisão
          </Button>
        }
      />

      <Card
        elevation={0}
        sx={{
          p: 2,
          mb: 3,
          
          border: '1px solid',
          borderColor: 'divider',
          backgroundColor: theme.custom.subtlePanelBg,
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
            <Tooltip title="Limpar Filtros">
              <IconButton size="small" onClick={handleClearFilters} color="default">
                <FilterAltOffIcon fontSize="small" />
              </IconButton>
            </Tooltip>
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
        <TableContainer component={Paper} sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.05)', overflow: 'hidden' }}>
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
                decisoes.map((item) => (
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
                    <TableCell>{item.dataReuniao}</TableCell>
                    <TableCell>{getStatusChip(item.statusHomologacao)}</TableCell>
                    <TableCell align="center">
                      {canHomologate && item.statusHomologacao === 'PENDENTE_HOMOLOGACAO' ? (
                        <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1 }}>
                          <Tooltip title="Homologar Ata (Aprovar)">
                            <IconButton
                              color="success"
                              size="small"
                              onClick={() => handleHomologar(item.id)}
                              data-testid={`homologate-btn-${item.id}`}
                            >
                              <CheckCircleIcon />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Vetar Ata (Reprovar)">
                            <IconButton
                              color="error"
                              size="small"
                              onClick={() => setVetoDialogItem(item)}
                              data-testid={`veto-btn-${item.id}`}
                            >
                              <CancelIcon />
                            </IconButton>
                          </Tooltip>
                        </Box>
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
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
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
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVetoDialogItem(null)} color="secondary" startIcon={<CloseIcon />}>
            Cancelar
          </Button>
          <Button onClick={handleConfirmVeto} variant="contained" color="error" startIcon={<CancelIcon />}>
            Confirmar Veto
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};
export default ConselhosAtas;
