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
  Button,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Tabs,
  Tab,
} from '@mui/material';
import { getAnosParoquiais, updateAnoParoquialStatus } from '../api/anosParoquiais';
import { AnoParoquialStatus } from '../api/types';
import { useNotification } from '../context/NotificationContext';
import { useAuth } from '../auth/AuthContext';
import { canManageAnoParoquial, canManageLocais } from '../auth/permissions';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const CustomTabPanel = (props: TabPanelProps) => {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`settings-tabpanel-${index}`}
      aria-labelledby={`settings-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ py: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
};

const a11yProps = (index: number) => {
  return {
    id: `settings-tab-${index}`,
    'aria-controls': `settings-tabpanel-${index}`,
  };
};

const Settings = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();
  const isAuthorizedAno = canManageAnoParoquial(user);
  const isAuthorizedLocais = canManageLocais(user);

  const [tabValue, setTabValue] = useState(0);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [selectedYear, setSelectedYear] = useState<number | null>(null);

  const { data: years = [], isLoading, isError } = useQuery({
    queryKey: ['anosParoquiais'],
    queryFn: getAnosParoquiais,
    enabled: tabValue === 0,
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

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleClosePlanejamento = (ano: number) => {
    if (!isAuthorizedAno) return;
    updateMutation.mutate({ ano, status: 'FECHADO' });
  };

  const handleReopenPlanejamento = (ano: number) => {
    if (!isAuthorizedAno) return;
    updateMutation.mutate({ ano, status: 'EM_PLANEJAMENTO' });
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

  return (
    <Container maxWidth="lg">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 700, color: '#37474f' }}>
          Configurações do Sistema
        </Typography>
        <Typography variant="body1" color="textSecondary" sx={{ mb: 4 }}>
          Gerencie os parâmetros globais do sistema, como o calendário paroquial e os locais físicos.
        </Typography>

        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
          <Tabs value={tabValue} onChange={handleTabChange} aria-label="Abas de Configurações">
            <Tab label="Ano Paroquial" {...a11yProps(0)} sx={{ fontWeight: 600 }} />
            <Tab label="Locais" {...a11yProps(1)} sx={{ fontWeight: 600 }} />
          </Tabs>
        </Box>

        {/* Tab 1: Ano Paroquial */}
        <CustomTabPanel value={tabValue} index={0}>
          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 8 }}>
              <CircularProgress />
            </Box>
          ) : isError ? (
            <Alert severity="error">
              Erro ao carregar as informações dos anos paroquiais.
            </Alert>
          ) : (
            <Box>
              <Typography variant="h5" sx={{ mb: 2, fontWeight: 600, color: 'text.primary' }}>
                Gestão do Ano Paroquial
              </Typography>
              <Typography variant="body2" color="textSecondary" sx={{ mb: 3 }}>
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
                      {isAuthorizedAno && <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {years.map((y) => {
                      const isConcluido = y.status === 'CONCLUIDO';
                      const hasUnconcludedPriorYear = years.some(
                        (other) => other.ano < y.ano && other.status !== 'CONCLUIDO'
                      );

                      return (
                        <TableRow key={y.ano}>
                          <TableCell>{y.ano}</TableCell>
                          <TableCell>{getStatusLabel(y.status)}</TableCell>
                          <TableCell>{formatDate(y.dataFechamentoUtc)}</TableCell>
                          <TableCell>{formatDate(y.updatedAt)}</TableCell>
                          {isAuthorizedAno && (
                            <TableCell align="right">
                              {!isConcluido && (
                                <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end', alignItems: 'center' }}>
                                  {y.status === 'EM_PLANEJAMENTO' && (
                                    <Button
                                      variant="outlined"
                                      color="primary"
                                      size="small"
                                      onClick={() => handleClosePlanejamento(y.ano)}
                                      disabled={updateMutation.isPending || hasUnconcludedPriorYear}
                                      title={hasUnconcludedPriorYear ? "O ano anterior precisa estar concluído." : undefined}
                                    >
                                      Fechar
                                    </Button>
                                  )}
                                  {y.status === 'FECHADO' && (
                                    <Button
                                      variant="outlined"
                                      color="primary"
                                      size="small"
                                      onClick={() => handleReopenPlanejamento(y.ano)}
                                      disabled={updateMutation.isPending}
                                    >
                                      Reabrir
                                    </Button>
                                  )}
                                  <Button
                                    variant="contained"
                                    color="error"
                                    size="small"
                                    onClick={() => handleConcludeClick(y.ano)}
                                    disabled={updateMutation.isPending || hasUnconcludedPriorYear}
                                    title={hasUnconcludedPriorYear ? "O ano anterior precisa estar concluído." : undefined}
                                  >
                                    Concluir
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
          )}
        </CustomTabPanel>

        {/* Tab 2: Locais */}
        <CustomTabPanel value={tabValue} index={1}>
          <Box>
            <Typography variant="h5" sx={{ mb: 2, fontWeight: 600, color: 'text.primary' }}>
              Gestão de Locais (Espaços Físicos)
            </Typography>
            <Typography variant="body2" color="textSecondary" sx={{ mb: 3 }}>
              Esqueleto da funcionalidade de CRUD de locais. (Em breve)
            </Typography>
          </Box>
        </CustomTabPanel>
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

export default Settings;
