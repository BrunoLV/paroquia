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
import { getLocais, createLocal, updateLocal, deleteLocal } from '../api/locais';
import { AnoParoquialStatus, Local } from '../api/types';
import { useNotification } from '../context/NotificationContext';
import { useAuth } from '../auth/AuthContext';
import { canManageAnoParoquial, canManageLocais } from '../auth/permissions';
import LocalFormModal from './LocalFormModal';

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
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedLocal, setSelectedLocal] = useState<Local | null>(null);

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

  const { data: locais = [], isLoading: isLoadingLocais, isError: isErrorLocais } = useQuery({
    queryKey: ['locais'],
    queryFn: getLocais,
    enabled: tabValue === 1,
  });

  const createLocalMutation = useMutation({
    mutationFn: (data: Omit<Local, 'id'>) => createLocal(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['locais'] });
      showSuccess('Local cadastrado com sucesso!');
      setModalOpen(false);
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao cadastrar local.';
      showError(errMsg);
    },
  });

  const updateLocalMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Omit<Local, 'id'> }) => updateLocal(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['locais'] });
      showSuccess('Local atualizado com sucesso!');
      setModalOpen(false);
      setSelectedLocal(null);
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao atualizar local.';
      showError(errMsg);
    },
  });

  const toggleLocalStatusMutation = useMutation({
    mutationFn: async (local: Local) => {
      if (local.ativo) {
        return deleteLocal(local.id);
      } else {
        const { id, ...data } = local;
        return updateLocal(id, { ...data, ativo: true });
      }
    },
    onSuccess: (_, local) => {
      queryClient.invalidateQueries({ queryKey: ['locais'] });
      showSuccess(`Local ${local.ativo ? 'inativado' : 'ativado'} com sucesso!`);
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao alterar status do local.';
      showError(errMsg);
    },
  });

  const getTipoLabel = (tipo: string) => {
    switch (tipo) {
      case 'IGREJA':
        return 'Igreja';
      case 'SALAO':
        return 'Salão';
      case 'SALA':
        return 'Sala';
      case 'OUTRO':
        return 'Outro';
      default:
        return tipo;
    }
  };

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
          {isLoadingLocais ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 8 }}>
              <CircularProgress />
            </Box>
          ) : isErrorLocais ? (
            <Alert severity="error">
              Erro ao carregar as informações dos locais.
            </Alert>
          ) : (
            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Box>
                  <Typography variant="h5" sx={{ fontWeight: 600, color: 'text.primary' }}>
                    Gestão de Locais (Espaços Físicos)
                  </Typography>
                  <Typography variant="body2" color="textSecondary" sx={{ mt: 0.5 }}>
                    Cadastre e gerencie os espaços disponíveis na paróquia para a realização de eventos.
                  </Typography>
                </Box>
                {isAuthorizedLocais && (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => {
                      setSelectedLocal(null);
                      setModalOpen(true);
                    }}
                    sx={{ borderRadius: 0, fontWeight: 600 }}
                  >
                    Novo Local
                  </Button>
                )}
              </Box>

              <TableContainer component={Paper} sx={{ borderRadius: 0, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                <Table>
                  <TableHead sx={{ bgcolor: 'grey.100' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Capacidade</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Endereço</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      {isAuthorizedLocais && <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {locais.map((local) => (
                      <TableRow key={local.id}>
                        <TableCell sx={{ fontWeight: 500 }}>{local.nome}</TableCell>
                        <TableCell>{getTipoLabel(local.tipo)}</TableCell>
                        <TableCell>{local.capacidade !== null ? local.capacidade : '-'}</TableCell>
                        <TableCell>{local.endereco || '-'}</TableCell>
                        <TableCell>
                          <Box
                            sx={{
                              display: 'inline-block',
                              px: 1,
                              py: 0.5,
                              fontSize: '0.75rem',
                              fontWeight: 700,
                              color: local.ativo ? '#2e7d32' : '#c62828',
                              bgcolor: local.ativo ? '#e8f5e9' : '#ffebee',
                            }}
                          >
                            {local.ativo ? 'Ativo' : 'Inativo'}
                          </Box>
                        </TableCell>
                        {isAuthorizedLocais && (
                          <TableCell align="right">
                            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                              <Button
                                variant="outlined"
                                color="primary"
                                size="small"
                                onClick={() => {
                                  setSelectedLocal(local);
                                  setModalOpen(true);
                                }}
                                disabled={toggleLocalStatusMutation.isPending}
                                sx={{ borderRadius: 0 }}
                              >
                                Editar
                              </Button>
                              <Button
                                variant="outlined"
                                color={local.ativo ? 'error' : 'primary'}
                                size="small"
                                onClick={() => toggleLocalStatusMutation.mutate(local)}
                                disabled={toggleLocalStatusMutation.isPending}
                                sx={{ borderRadius: 0 }}
                              >
                                {local.ativo ? 'Inativar' : 'Ativar'}
                              </Button>
                            </Box>
                          </TableCell>
                        )}
                      </TableRow>
                    ))}
                    {locais.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={isAuthorizedLocais ? 6 : 5} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                          Nenhum local cadastrado.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
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

      <LocalFormModal
        open={modalOpen}
        onClose={() => {
          setModalOpen(false);
          setSelectedLocal(null);
        }}
        onSave={(data) => {
          if (selectedLocal) {
            updateLocalMutation.mutate({ id: selectedLocal.id, data });
          } else {
            createLocalMutation.mutate(data);
          }
        }}
        local={selectedLocal}
        isPending={createLocalMutation.isPending || updateLocalMutation.isPending}
      />
    </Container>
  );
};

export default Settings;
