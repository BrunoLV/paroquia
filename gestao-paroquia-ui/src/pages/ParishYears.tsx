import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Container,
  Typography,
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Switch,
  Button,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import { getAnosParoquiais, updateAnoParoquialStatus } from '../api/anosParoquiais';
import { AnoParoquial, AnoParoquialStatus } from '../api/types';
import { useNotification } from '../context/NotificationContext';
import { useAuth } from '../auth/AuthContext';
import { canManageAnoParoquial } from '../auth/permissions';

const ParishYears = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();
  const isAuthorized = canManageAnoParoquial(user);

  const [confirmOpen, setConfirmOpen] = useState(false);
  const [selectedYear, setSelectedYear] = useState<number | null>(null);

  const { data: years = [], isLoading, isError } = useQuery({
    queryKey: ['anosParoquiais'],
    queryFn: getAnosParoquiais,
  });

  const updateMutation = useMutation({
    mutationFn: ({ ano, status }: { ano: number; status: AnoParoquialStatus }) =>
      updateAnoParoquialStatus(ano, status),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['anosParoquiais'] });
      showSuccess(`Ano paroquial ${updated.ano} atualizado com sucesso!`);
      setConfirmOpen(false);
      setSelectedYear(null);
    },
    onError: (error: any) => {
      console.error('Error updating parish year:', error);
      const errMsg = error.response?.data?.message || 'Erro ao atualizar o ano paroquial.';
      showError(errMsg);
      setConfirmOpen(false);
      setSelectedYear(null);
    },
  });

  const handleStatusToggle = (ano: number, currentStatus: AnoParoquialStatus) => {
    if (!isAuthorized) return;
    const targetStatus: AnoParoquialStatus =
      currentStatus === 'EM_PLANEJAMENTO' ? 'FECHADO' : 'EM_PLANEJAMENTO';
    updateMutation.mutate({ ano, status: targetStatus });
  };

  const handleConcludeClick = (ano: number) => {
    setSelectedYear(ano);
    setConfirmOpen(true);
  };

  const handleConfirmConclude = () => {
    if (selectedYear !== null) {
      updateMutation.mutate({ ano: selectedYear, status: 'CONCLUIDO' });
    }
  };

  const getStatusLabel = (status: AnoParoquialStatus) => {
    switch (status) {
      case 'EM_PLANEJAMENTO':
        return 'Planejamento Aberto';
      case 'FECHADO':
        return 'Planejamento Fechado (Trava)';
      case 'CONCLUIDO':
        return 'Concluído';
      default:
        return status;
    }
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('pt-BR');
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', my: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isError) {
    return (
      <Container>
        <Alert severity="error" sx={{ mt: 4 }}>
          Erro ao carregar as informações dos anos paroquiais.
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 600, color: 'primary.main' }}>
          Gestão do Ano Paroquial
        </Typography>
        <Typography variant="body1" color="textSecondary" sx={{ mb: 4 }}>
          Controle as fases de planejamento e fechamento do calendário paroquial.
        </Typography>

        <TableContainer component={Paper} sx={{ borderRadius: 0, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
          <Table>
            <TableHead sx={{ bgcolor: 'grey.100' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Ano</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Data de Fechamento</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Última Atualização</TableCell>
                {isAuthorized && <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {years.map((y) => {
                const isConcluido = y.status === 'CONCLUIDO';
                const isPlanejamento = y.status === 'EM_PLANEJAMENTO';

                return (
                  <TableRow key={y.ano}>
                    <TableCell>{y.ano}</TableCell>
                    <TableCell>{getStatusLabel(y.status)}</TableCell>
                    <TableCell>{formatDate(y.dataFechamentoUtc)}</TableCell>
                    <TableCell>{formatDate(y.updatedAt)}</TableCell>
                    {isAuthorized && (
                      <TableCell align="right">
                        {!isConcluido && (
                          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', alignItems: 'center' }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Typography variant="body2">Aberto/Fechado</Typography>
                              <Switch
                                checked={y.status === 'FECHADO'}
                                onChange={() => handleStatusToggle(y.ano, y.status)}
                                disabled={updateMutation.isPending}
                              />
                            </Box>
                            <Button
                              variant="outlined"
                              color="error"
                              size="small"
                              onClick={() => handleConcludeClick(y.ano)}
                              disabled={updateMutation.isPending}
                            >
                              Concluir Ano
                            </Button>
                          </Box>
                        )}
                        {isConcluido && <Typography color="textSecondary" variant="body2">-</Typography>}
                      </TableCell>
                    )}
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>

      {/* Confirmation Modal */}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Confirmar Conclusão</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Tem certeza que deseja concluir o ano paroquial de {selectedYear}?
            Esta ação é irreversível e o ano ficará como histórico de leitura.
            O próximo ano será inicializado em modo de planejamento automaticamente.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)} color="inherit">
            Cancelar
          </Button>
          <Button
            onClick={handleConfirmConclude}
            color="error"
            variant="contained"
            disabled={updateMutation.isPending}
          >
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ParishYears;
