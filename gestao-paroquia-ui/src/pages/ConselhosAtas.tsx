import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Chip,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Paper,
  Select,
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
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import DescriptionIcon from '@mui/icons-material/Description';
import { useAuth } from '../auth/AuthContext';
import { useNotification } from '../context/NotificationContext';
import { getCpcDecisoes, createCpcDecisao, homologarCpcDecisao } from '../api/conselhos';
import { getOrganizacoes } from '../api/organizacoes';
import { CpcDecisao, Organizacao, StatusHomologacao } from '../api/types';
import { CpcDecisaoModal } from '../components/CpcDecisaoModal';

export const ConselhosAtas: React.FC = () => {
  const { user } = useAuth();
  const { showSuccess, showError } = useNotification();

  const [decisoes, setDecisoes] = useState<CpcDecisao[]>([]);
  const [communities, setCommunities] = useState<Organizacao[]>([]);
  const [loading, setLoading] = useState(false);

  const [selectedComunidade, setSelectedComunidade] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [vetoDialogItem, setVetoDialogItem] = useState<CpcDecisao | null>(null);
  const [motivoVeto, setMotivoVeto] = useState('');

  const canHomologate =
    user?.roles?.some((r) =>
      ['ADMIN', 'ROLE_ADMIN', 'PAROCO', 'ROLE_PAROCO', 'MESA_CPP', 'ROLE_MESA_CPP'].includes(r)
    ) ?? false;

  const loadData = async () => {
    setLoading(true);
    try {
      const [decisoesData, orgsData] = await Promise.all([
        getCpcDecisoes({
          comunidadeId: selectedComunidade || undefined,
          status: (selectedStatus as StatusHomologacao) || undefined,
        }),
        getOrganizacoes(),
      ]);

      setDecisoes(decisoesData);
      setCommunities(orgsData.filter((o) => o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'));
    } catch (err: any) {
      showError('Erro ao carregar atas e decisões dos conselhos.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedComunidade, selectedStatus]);

  const handleCreateDecisao = async (data: {
    titulo: string;
    pautaDescricao: string;
    dataReuniao: string;
    comunidadeId: string;
  }) => {
    setIsSubmitting(true);
    try {
      await createCpcDecisao(data);
      showSuccess('Ata/Decisão submetida com sucesso para homologação!');
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      showError('Erro ao submeter ata de conselho.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleHomologar = async (id: string) => {
    try {
      await homologarCpcDecisao(id, { status: 'HOMOLOGADO' });
      showSuccess('Ata/Decisão homologada com sucesso pelo Pároco/Mesa CPP!');
      loadData();
    } catch (err: any) {
      showError('Erro ao homologar ata.');
    }
  };

  const handleConfirmVeto = async () => {
    if (!vetoDialogItem) return;
    if (!motivoVeto.trim()) {
      showError('O motivo do veto é obrigatório.');
      return;
    }
    try {
      await homologarCpcDecisao(vetoDialogItem.id, {
        status: 'VETADO',
        motivoVeto: motivoVeto.trim(),
      });
      showSuccess('Ata/Decisão vetada com sucesso.');
      setVetoDialogItem(null);
      setMotivoVeto('');
      loadData();
    } catch (err: any) {
      showError('Erro ao vetar ata.');
    }
  };

  const getStatusChip = (status: StatusHomologacao) => {
    switch (status) {
      case 'HOMOLOGADO':
        return <Chip label="Homologado" color="success" size="small" sx={{ fontWeight: 'bold' }} />;
      case 'VETADO':
        return <Chip label="Vetado" color="error" size="small" sx={{ fontWeight: 'bold' }} />;
      default:
        return <Chip label="Pendente de Homologação" color="warning" size="small" sx={{ fontWeight: 'bold' }} />;
    }
  };

  const getCommunityName = (id: string) => {
    return communities.find((c) => c.id === id)?.nome || 'Comunidade';
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box display="flex" alignItems="center" gap={1.5}>
          <DescriptionIcon color="primary" sx={{ fontSize: 32 }} />
          <Typography variant="h5" fontWeight="bold">
            Governança dos Conselhos (CPP & CPC)
          </Typography>
        </Box>

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setIsModalOpen(true)}
          sx={{ borderRadius: 0 }}
        >
          Nova Ata / Decisão
        </Button>
      </Box>

      {/* Filtros */}
      <Paper sx={{ p: 2, mb: 3, borderRadius: 0 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth size="small">
              <InputLabel>Filtrar por Comunidade</InputLabel>
              <Select
                value={selectedComunidade}
                label="Filtrar por Comunidade"
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
          </Grid>
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth size="small">
              <InputLabel>Filtrar por Status</InputLabel>
              <Select
                value={selectedStatus}
                label="Filtrar por Status"
                onChange={(e) => setSelectedStatus(e.target.value)}
              >
                <MenuItem value="">Todos os Status</MenuItem>
                <MenuItem value="PENDENTE_HOMOLOGACAO">Pendente de Homologação</MenuItem>
                <MenuItem value="HOMOLOGADO">Homologado</MenuItem>
                <MenuItem value="VETADO">Vetado</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Tabela de Atas/Decisões */}
      <TableContainer component={Paper} sx={{ borderRadius: 0 }}>
        <Table>
          <TableHead sx={{ backgroundColor: 'action.hover' }}>
            <TableRow>
              <TableCell fontWeight="bold">Título</TableCell>
              <TableCell fontWeight="bold">Comunidade</TableCell>
              <TableCell fontWeight="bold">Data Reunião</TableCell>
              <TableCell fontWeight="bold">Status Homologação</TableCell>
              <TableCell fontWeight="bold" align="center">
                Ações
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {decisoes.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                  <Typography variant="body2" color="text.secondary">
                    Nenhuma ata ou decisão registrada para os filtros selecionados.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              decisoes.map((item) => (
                <TableRow key={item.id} hover>
                  <TableCell>
                    <Typography variant="subtitle2" fontWeight="bold">
                      {item.titulo}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {item.pautaDescricao}
                    </Typography>
                    {item.motivoVeto && (
                      <Typography variant="caption" display="block" color="error.main" sx={{ mt: 0.5 }}>
                        Motivo Veto: {item.motivoVeto}
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>{getCommunityName(item.comunidadeId)}</TableCell>
                  <TableCell>{item.dataReuniao}</TableCell>
                  <TableCell>{getStatusChip(item.statusHomologacao)}</TableCell>
                  <TableCell align="center">
                    {canHomologate && item.statusHomologacao === 'PENDENTE_HOMOLOGACAO' ? (
                      <Box display="flex" justifyContent="center" gap={1}>
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

      {/* Modal de Criação */}
      <CpcDecisaoModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCreateDecisao}
        isSubmitting={isSubmitting}
        communities={communities}
      />

      {/* Dialog de Veto */}
      <Dialog open={!!vetoDialogItem} onClose={() => setVetoDialogItem(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Vetar Ata / Decisão de Conselho</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>
            Informe o motivo do veto para a ata "{vetoDialogItem?.titulo}". Esta justificativa será gravada de forma auditável.
          </Typography>
          <TextField
            label="Motivo do Veto *"
            fullWidth
            multiline
            rows={3}
            value={motivoVeto}
            onChange={(e) => setMotivoVeto(e.target.value)}
            required
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setVetoDialogItem(null)} color="inherit">
            Cancelar
          </Button>
          <Button onClick={handleConfirmVeto} variant="contained" color="error">
            Confirmar Veto
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};
export default ConselhosAtas;
