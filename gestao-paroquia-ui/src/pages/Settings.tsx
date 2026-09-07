import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Alert,
  Box,
  CircularProgress,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Skeleton,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import {
  Add as AddIcon,
  Category as CategoryIcon,
  CheckCircle as CheckCircleIcon,
  Close as CloseIcon,
  CloudUpload as UploadIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  Link as LinkIcon,
  Lock as LockIcon,
  Settings as SettingsIcon,
} from '@mui/icons-material';

import { getAnosParoquiais, updateAnoParoquialStatus } from '@/api/anosParoquiais';
import { getOrganizacoes } from '@/api/organizacoes';
import {
  adicionarVinculo,
  AdicionarVinculoRequest,
  approveUsuario,
  createUsuario,
  CreateUsuarioRequest,
  getUsuarios,
  getVinculos,
  removerVinculo,
  Usuario,
} from '@/api/usuarios';
import { getFieis } from '@/api/fieis';
import {
  createPastoralOficial,
  deletePastoralOficial,
  getPastoraisOficiais,
  updatePastoralOficial,
} from '@/api/pastoraisOficiais';
import { AnoParoquialStatus, PAPEL_ECLESIAL_LABELS, PastoralMovimentoOficial } from '@/api/types';
import { useNotification } from '@/context/NotificationContext';
import { extractErrorMessage } from '@/utils/errorUtils';
import { formatDateBr } from '@/utils/masks';
import { useAuth } from '@/auth/AuthContext';
import { canManageAnoParoquial, canManageOrganizacoes, isAdmin } from '@/auth/permissions';
import ImportModal from '@/components/ImportModal';
import PageHeader from '@/components/PageHeader';
import StatusIcon from '@/components/StatusIcon';
import SacramentoIcon from '@/components/SacramentoIcon';
import ActionButton from '@/components/ActionButton';
import TableActionMenu from '@/components/TableActionMenu';
import { getAnoParoquialStatusMeta } from '@/utils/statusMeta/anoParoquialStatus';
import { SACRAMENTO_META } from '@/utils/statusMeta/sacramentos';
import { getOrganismoTipoMeta } from '@/utils/statusMeta/organismoTipoStatus';
import { getSituacaoAtivoMeta, getUsuarioSituacaoMeta } from '@/utils/statusMeta/situacaoStatus';
import { FormattedEmail, FormattedPhone } from '@/components/FormattedCell';

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
      {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
    </div>
  );
};

const a11yProps = (index: number) => ({
  id: `settings-tab-${index}`,
  'aria-controls': `settings-tabpanel-${index}`,
});

export const Settings = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();
  const isAuthorizedAno = canManageAnoParoquial(user);
  const isAuthorizedUsers = canManageOrganizacoes(user);

  const [tabValue, setTabValue] = useState<number>(0);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [selectedYear, setSelectedYear] = useState<number | null>(null);

  // Pagination states
  const [anoPage, setAnoPage] = useState(0);
  const [anoRowsPerPage, setAnoRowsPerPage] = useState(10);
  const [usersPage, setUsersPage] = useState(0);
  const [usersRowsPerPage, setUsersRowsPerPage] = useState(10);
  const [poPage, setPoPage] = useState(0);
  const [poRowsPerPage, setPoRowsPerPage] = useState(10);

  // User and Vinculo states
  const [selectedUserForVinculos, setSelectedUserForVinculos] = useState<Usuario | null>(null);
  const [isVinculosModalOpen, setIsVinculosModalOpen] = useState(false);
  const [newVinculoOrgId, setNewVinculoOrgId] = useState('');
  const [newVinculoPapel, setNewVinculoPapel] = useState('');
  const [newVinculoChurchId, setNewVinculoChurchId] = useState('');

  // Admin user registration states
  const [isAdminRegisterModalOpen, setIsAdminRegisterModalOpen] = useState(false);
  const [isImportModalOpen, setIsImportModalOpen] = useState(false);
  const [adminRegisterFielId, setAdminRegisterFielId] = useState('');
  const [adminRegisterUsername, setAdminRegisterUsername] = useState('');
  const [adminRegisterPassword, setAdminRegisterPassword] = useState('');

  // Catalog states
  const [poModalOpen, setPoModalOpen] = useState(false);
  const [selectedPO, setSelectedPO] = useState<PastoralMovimentoOficial | null>(null);
  const [poNome, setPoNome] = useState('');
  const [poTipo, setPoTipo] = useState<'PASTORAL' | 'MOVIMENTO'>('PASTORAL');
  const [poErrors, setPoErrors] = useState<{ nome?: string }>({});

  const { data: years = [], isLoading: isLoadingYears, isError: isErrorYears } = useQuery({
    queryKey: ['anosParoquiais'],
    queryFn: getAnosParoquiais,
    enabled: tabValue === 0,
  });

  const updateAnoMutation = useMutation({
    mutationFn: ({ ano, status }: { ano: number; status: AnoParoquialStatus }) =>
      updateAnoParoquialStatus(ano, status),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['anosParoquiais'] });
      showSuccess(`Ano paroquial ${updated.ano} atualizado com sucesso!`);
      setConfirmOpen(false);
      setSelectedYear(null);
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao atualizar o ano paroquial.'));
      setConfirmOpen(false);
      setSelectedYear(null);
    },
  });

  const { data: orgs = [] } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
    enabled: isVinculosModalOpen,
  });

  const { data: users = [], isLoading: isLoadingUsers, isError: isErrorUsers } = useQuery({
    queryKey: ['usuarios'],
    queryFn: getUsuarios,
    enabled: tabValue === 1 && isAuthorizedUsers,
  });

  const approveUserMutation = useMutation({
    mutationFn: (id: string) => approveUsuario(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      showSuccess('Usuário aprovado com sucesso!');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao aprovar usuário.'));
    },
  });

  const { data: vinculos = [], isLoading: isLoadingVinculos } = useQuery({
    queryKey: ['vinculos', selectedUserForVinculos?.id],
    queryFn: () => getVinculos(selectedUserForVinculos!.id),
    enabled: !!selectedUserForVinculos && isVinculosModalOpen,
  });

  const addVinculoMutation = useMutation({
    mutationFn: (request: AdicionarVinculoRequest) => adicionarVinculo(selectedUserForVinculos!.id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vinculos', selectedUserForVinculos?.id] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Vínculo adicionado com sucesso!');
      setNewVinculoOrgId('');
      setNewVinculoPapel('');
      setNewVinculoChurchId('');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao adicionar vínculo.'));
    },
  });

  const removeVinculoMutation = useMutation({
    mutationFn: (vinculoId: string) => removerVinculo(selectedUserForVinculos!.id, vinculoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vinculos', selectedUserForVinculos?.id] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Vínculo removido com sucesso!');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao remover vínculo.'));
    },
  });

  const { data: fieisList = [] } = useQuery({
    queryKey: ['fieis'],
    queryFn: getFieis,
    enabled: isAdminRegisterModalOpen,
  });

  const registerUserMutation = useMutation({
    mutationFn: (request: CreateUsuarioRequest) => createUsuario(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      showSuccess('Usuário cadastrado com sucesso!');
      setIsAdminRegisterModalOpen(false);
      setAdminRegisterFielId('');
      setAdminRegisterUsername('');
      setAdminRegisterPassword('');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao cadastrar usuário.'));
    },
  });

  const { data: pastoraisOficiais = [], isLoading: isLoadingPO, isError: isErrorPO } = useQuery({
    queryKey: ['pastoraisOficiais'],
    queryFn: getPastoraisOficiais,
    enabled: tabValue === 2 && isAdmin(user),
  });

  const createPOMutation = useMutation({
    mutationFn: (data: Omit<PastoralMovimentoOficial, 'id'>) => createPastoralOficial(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pastoraisOficiais'] });
      showSuccess('Tipo oficial criado com sucesso!');
      setPoModalOpen(false);
      setSelectedPO(null);
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao cadastrar tipo oficial.'));
    },
  });

  const updatePOMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Omit<PastoralMovimentoOficial, 'id'> }) =>
      updatePastoralOficial(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pastoraisOficiais'] });
      showSuccess('Tipo oficial atualizado com sucesso!');
      setPoModalOpen(false);
      setSelectedPO(null);
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao atualizar tipo oficial.'));
    },
  });

  const deletePOMutation = useMutation({
    mutationFn: (id: string) => deletePastoralOficial(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pastoraisOficiais'] });
      showSuccess('Tipo oficial removido com sucesso!');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao remover tipo oficial.'));
    },
  });

  const handleCreatePO = () => {
    setSelectedPO(null);
    setPoNome('');
    setPoTipo('PASTORAL');
    setPoErrors({});
    setPoModalOpen(true);
  };

  const handleEditPO = (po: PastoralMovimentoOficial) => {
    setSelectedPO(po);
    setPoNome(po.nome);
    setPoTipo(po.tipo);
    setPoErrors({});
    setPoModalOpen(true);
  };

  const handleSavePO = () => {
    const errors: { nome?: string } = {};
    if (!poNome.trim()) {
      errors.nome = 'O nome é obrigatório';
    }
    if (Object.keys(errors).length > 0) {
      setPoErrors(errors);
      return;
    }
    if (selectedPO) {
      updatePOMutation.mutate({ id: selectedPO.id, data: { nome: poNome.trim(), tipo: poTipo } });
    } else {
      createPOMutation.mutate({ nome: poNome.trim(), tipo: poTipo });
    }
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('pt-BR');
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <PageHeader
        icon={<SettingsIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Configurações do Sistema"
        subtitle="Gerencie os parâmetros administrativos e técnicos do sistema."
        actions={
          isAdmin(user) && (
            <ActionButton
              title="Importar em Lote (CSV/Excel)"
              icon={<UploadIcon />}
              color="primary"
              onClick={() => setIsImportModalOpen(true)}
            />
          )
        }
      />

      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        <Tabs value={tabValue} onChange={(_, val) => setTabValue(val)} aria-label="Abas de Configurações">
          <Tab label="Ano Paroquial" {...a11yProps(0)} sx={{ fontWeight: 600 }} />
          {isAuthorizedUsers && (
            <Tab label="Membros / Usuários" {...a11yProps(1)} sx={{ fontWeight: 600 }} />
          )}
          {isAdmin(user) && (
            <Tab label="Catálogo Oficial" {...a11yProps(2)} sx={{ fontWeight: 600 }} />
          )}
        </Tabs>
      </Box>

      {/* Tab 0: Ano Paroquial */}
      <CustomTabPanel value={tabValue} index={0}>
        {isLoadingYears ? (
          <Box sx={{ my: 4 }}>
            <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
            {[1, 2, 3, 4].map((i) => (
              <Skeleton key={i} variant="rectangular" height={40} sx={{ mb: 1 }} />
            ))}
          </Box>
        ) : isErrorYears ? (
          <Alert severity="error">Erro ao carregar as informações dos anos paroquiais.</Alert>
        ) : (
          <Box>
            <TableContainer component={Paper}>
              <Table>
                <TableHead sx={{ backgroundColor: 'action.hover' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 600 }}>Ano</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Data de Fechamento</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Última Atualização</TableCell>
                    {isAuthorizedAno && <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {years
                    .slice(anoPage * anoRowsPerPage, anoPage * anoRowsPerPage + anoRowsPerPage)
                    .map((ano) => (
                      <TableRow key={ano.ano} hover>
                        <TableCell sx={{ fontWeight: 500 }}>{ano.ano}</TableCell>
                        <TableCell><StatusIcon {...getAnoParoquialStatusMeta(ano.status)} /></TableCell>
                        <TableCell>{formatDate(ano.dataFechamentoUtc)}</TableCell>
                        <TableCell>{formatDate(ano.updatedAt)}</TableCell>
                        {isAuthorizedAno && (
                          <TableCell align="right">
                            {ano.status === 'EM_PLANEJAMENTO' && (
                              <ActionButton
                                title="Fechar Ano"
                                label="Fechar Ano"
                                icon={<LockIcon />}
                                color="warning"
                                onClick={() => updateAnoMutation.mutate({ ano: ano.ano, status: 'FECHADO' })}
                                disabled={updateAnoMutation.isPending}
                              />
                            )}
                            {ano.status === 'FECHADO' && (
                              <ActionButton
                                title="Concluir Ano"
                                label="Concluir Ano"
                                icon={<CheckCircleIcon />}
                                color="secondary"
                                onClick={() => {
                                  setSelectedYear(ano.ano);
                                  setConfirmOpen(true);
                                }}
                                disabled={updateAnoMutation.isPending}
                              />
                            )}
                            {ano.status === 'CONCLUIDO' && (
                              <Typography variant="body2" color="text.secondary">
                                Concluído
                              </Typography>
                            )}
                          </TableCell>
                        )}
                      </TableRow>
                    ))}
                  {years.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={isAuthorizedAno ? 5 : 4} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                        Nenhum ano paroquial registrado.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              rowsPerPageOptions={[5, 10, 25]}
              component="div"
              count={years.length}
              rowsPerPage={anoRowsPerPage}
              page={anoPage}
              onPageChange={(_, newPage) => setAnoPage(newPage)}
              onRowsPerPageChange={(e) => {
                setAnoRowsPerPage(parseInt(e.target.value, 10));
                setAnoPage(0);
              }}
              labelRowsPerPage="Linhas por página:"
              labelDisplayedRows={({ from, to, count }) =>
                `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
              }
            />
          </Box>
        )}
      </CustomTabPanel>

      {/* Tab 1: Membros / Usuários */}
      {isAuthorizedUsers && (
        <CustomTabPanel value={tabValue} index={1}>
          {isLoadingUsers ? (
            <Box sx={{ py: 4 }}>
              <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
              {[1, 2, 3, 4].map((i) => (
                <Skeleton key={i} variant="rectangular" height={40} sx={{ mb: 1 }} />
              ))}
            </Box>
          ) : isErrorUsers ? (
            <Alert severity="error">Erro ao carregar os usuários.</Alert>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', mb: 1 }}>
                {user && user.roles.some((r) => r === 'ADMIN' || r === 'ROLE_ADMIN' || r === 'PAROCO' || r === 'ROLE_CLERO_PAROCO') && (
                  <ActionButton
                    title="Cadastrar Usuário"
                    icon={<AddIcon />}
                    color="primary"
                    onClick={() => setIsAdminRegisterModalOpen(true)}
                  />
                )}
              </Box>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead sx={{ backgroundColor: 'action.hover' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>E-mail</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Igreja / Comunidade</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Telefone</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Data de Nascimento</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Sacramentos</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {users
                      .slice(usersPage * usersRowsPerPage, usersPage * usersRowsPerPage + usersRowsPerPage)
                      .map((u) => (
                        <TableRow key={u.id} hover>
                          <TableCell sx={{ fontWeight: 500 }}>{u.nome}</TableCell>
                          <TableCell><FormattedEmail value={u.username} /></TableCell>
                          <TableCell>{u.igrejaNome || '-'}</TableCell>
                          <TableCell><FormattedPhone value={u.telefone} /></TableCell>
                          <TableCell>{formatDateBr(u.dataNascimento) || '-'}</TableCell>
                          <TableCell>
                            <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap' }}>
                              {u.batizado && <SacramentoIcon {...SACRAMENTO_META.batizado} color="info" />}
                              {u.primeiraComunhao && <SacramentoIcon {...SACRAMENTO_META.primeiraComunhao} color="success" />}
                              {u.crismado && <SacramentoIcon {...SACRAMENTO_META.crismado} color="secondary" />}
                              {u.casadoIgrejaCatolica && <SacramentoIcon {...SACRAMENTO_META.casadoIgrejaCatolica} color="warning" />}
                              {!u.batizado && !u.primeiraComunhao && !u.crismado && !u.casadoIgrejaCatolica && '-'}
                            </Box>
                          </TableCell>
                          <TableCell><StatusIcon {...getUsuarioSituacaoMeta(u.enabled)} /></TableCell>
                          <TableCell align="right">
                            <TableActionMenu
                              actions={[
                                ...(!u.enabled ? [{ label: 'Aprovar', icon: <CheckCircleIcon fontSize="small" />, onClick: () => approveUserMutation.mutate(u.id), disabled: approveUserMutation.isPending }] : []),
                                { label: 'Gerenciar vínculos', icon: <LinkIcon fontSize="small" />, onClick: () => { setSelectedUserForVinculos(u); setIsVinculosModalOpen(true); } },
                              ]}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    {users.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={8} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                          Nenhum usuário encontrado.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
              <TablePagination
                rowsPerPageOptions={[5, 10, 25]}
                component="div"
                count={users.length}
                rowsPerPage={usersRowsPerPage}
                page={usersPage}
                onPageChange={(_, newPage) => setUsersPage(newPage)}
                onRowsPerPageChange={(e) => {
                  setUsersRowsPerPage(parseInt(e.target.value, 10));
                  setUsersPage(0);
                }}
                labelRowsPerPage="Linhas por página:"
                labelDisplayedRows={({ from, to, count }) =>
                  `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
                }
              />
            </Box>
          )}
        </CustomTabPanel>
      )}

      {/* Tab 2: Catálogo Oficial */}
      {isAdmin(user) && (
        <CustomTabPanel value={tabValue} index={2}>
          {isLoadingPO ? (
            <Box sx={{ py: 4 }}>
              <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
              {[1, 2, 3].map((i) => (
                <Skeleton key={i} variant="rectangular" height={40} sx={{ mb: 1 }} />
              ))}
            </Box>
          ) : isErrorPO ? (
            <Alert severity="error">Erro ao carregar o catálogo de pastorais e movimentos oficiais.</Alert>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', mb: 1 }}>
                <ActionButton title="Novo Tipo" icon={<AddIcon />} color="primary" onClick={handleCreatePO} />
              </Box>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead sx={{ backgroundColor: 'action.hover' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pastoraisOficiais
                      .slice(poPage * poRowsPerPage, poPage * poRowsPerPage + poRowsPerPage)
                      .map((po) => (
                        <TableRow key={po.id} hover>
                          <TableCell sx={{ fontWeight: 500 }}>{po.nome}</TableCell>
                          <TableCell>{po.tipo === 'PASTORAL' ? 'Pastoral' : 'Movimento'}</TableCell>
                          <TableCell align="right">
                            <TableActionMenu
                              actions={[
                                { label: 'Editar', icon: <EditIcon fontSize="small" />, onClick: () => handleEditPO(po) },
                                {
                                  label: 'Excluir',
                                  icon: <DeleteIcon fontSize="small" />,
                                  onClick: () => {
                                    if (window.confirm('Tem certeza de que deseja excluir este tipo oficial?')) {
                                      deletePOMutation.mutate(po.id);
                                    }
                                  },
                                  disabled: deletePOMutation.isPending,
                                },
                              ]}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    {pastoraisOficiais.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={3} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                          Nenhum tipo cadastrado.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
              <TablePagination
                rowsPerPageOptions={[5, 10, 25]}
                component="div"
                count={pastoraisOficiais.length}
                rowsPerPage={poRowsPerPage}
                page={poPage}
                onPageChange={(_, newPage) => setPoPage(newPage)}
                onRowsPerPageChange={(e) => {
                  setPoRowsPerPage(parseInt(e.target.value, 10));
                  setPoPage(0);
                }}
                labelRowsPerPage="Linhas por página:"
                labelDisplayedRows={({ from, to, count }) =>
                  `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
                }
              />
            </Box>
          )}
        </CustomTabPanel>
      )}

      {/* User Vinculos Dialog */}
      <Dialog
        open={isVinculosModalOpen}
        onClose={() => {
          setIsVinculosModalOpen(false);
          setSelectedUserForVinculos(null);
          setNewVinculoOrgId('');
          setNewVinculoPapel('');
          setNewVinculoChurchId('');
        }}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Gerenciar Vínculos - {selectedUserForVinculos?.nome}</DialogTitle>
        <DialogContent dividers>
          {isLoadingVinculos ? (
            <Box sx={{ py: 2 }}>
              <Skeleton variant="text" width={200} height={28} sx={{ mb: 1.5 }} />
              {[1, 2, 3].map((i) => (
                <Skeleton key={i} variant="rectangular" height={36} sx={{ mb: 1 }} />
              ))}
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Vínculos Atuais:</Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'action.hover' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Organização</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Igreja / Comunidade</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Papel</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {(vinculos || []).map((m) => (
                      <TableRow key={m.id}>
                        <TableCell sx={{ fontWeight: 500 }}>{m.organizacaoNome || orgs.find((o) => o.id === m.organizacaoId)?.nome || '-'}</TableCell>
                        <TableCell>{m.igrejaVinculoNome || orgs.find((o) => o.id === m.igrejaVinculoId)?.nome || '-'}</TableCell>
                        <TableCell><StatusIcon {...getOrganismoTipoMeta(m.tipoOrganizacao)} /></TableCell>
                        <TableCell>{PAPEL_ECLESIAL_LABELS[m.papel] || m.papel}</TableCell>
                        <TableCell><StatusIcon {...getSituacaoAtivoMeta(m.ativo)} /></TableCell>
                        <TableCell align="right">
                          <TableActionMenu actions={[{ label: 'Remover', icon: <DeleteIcon fontSize="small" />, onClick: () => removeVinculoMutation.mutate(m.id), disabled: removeVinculoMutation.isPending }]} />
                        </TableCell>
                      </TableRow>
                    ))}
                    {(vinculos || []).length === 0 && (
                      <TableRow>
                        <TableCell colSpan={6} align="center" sx={{ py: 2, color: 'text.secondary' }}>
                          Nenhum vínculo ativo encontrado para este usuário.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>

              <Box sx={{ borderBottom: 1, borderColor: 'divider', my: 1 }} />

              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Adicionar Novo Vínculo:</Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                <FormControl sx={{ minWidth: 250 }} size="small">
                  <InputLabel id="add-vinculo-org-label">Organização</InputLabel>
                  <Select
                    labelId="add-vinculo-org-label"
                    value={newVinculoOrgId}
                    label="Organização"
                    onChange={(e) => setNewVinculoOrgId(e.target.value)}
                  >
                    {orgs
                      .filter((o) => {
                        if (!o.ativo) return false;
                        if (!selectedUserForVinculos?.igrejaId) return true;
                        const userChurch = selectedUserForVinculos.igrejaId;
                        if (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE') {
                          return o.id === userChurch;
                        }
                        return o.parentId === userChurch || !o.parentId;
                      })
                      .map((o) => (
                        <MenuItem key={o.id} value={o.id}>
                          {o.nome}
                        </MenuItem>
                      ))}
                  </Select>
                </FormControl>

                <FormControl sx={{ minWidth: 200 }} size="small">
                  <InputLabel id="add-vinculo-role-label">Papel</InputLabel>
                  <Select
                    labelId="add-vinculo-role-label"
                    value={newVinculoPapel}
                    label="Papel"
                    onChange={(e) => setNewVinculoPapel(e.target.value)}
                  >
                    <MenuItem value="COORD_PASTORAL_GLOBAL">Coordenador Pastoral Global</MenuItem>
                    <MenuItem value="COORD_PASTORAL_LOCAL">Coordenador Pastoral Local</MenuItem>
                    <MenuItem value="COORD_MOVIMENTO">Coordenador de Movimento</MenuItem>
                    <MenuItem value="VOLUNTARIO_PASTORAL_LOCAL">Voluntário Pastoral Local</MenuItem>
                    <MenuItem value="MEMBRO_MOVIMENTO">Membro de Movimento</MenuItem>
                    <MenuItem value="PRESIDENTE_CPC">Presidente CPC</MenuItem>
                    <MenuItem value="MEMBRO_CPC">Membro CPC</MenuItem>
                    <MenuItem value="MESA_CPP">Mesa Diretora CPP</MenuItem>
                    <MenuItem value="MEMBRO_CPP">Membro CPP</MenuItem>
                    <MenuItem value="MEMBRO_CAE">Membro CAE</MenuItem>
                    <MenuItem value="PAROCO">Pároco</MenuItem>
                    <MenuItem value="VIGARIO">Vigário</MenuItem>
                    <MenuItem value="PADRE">Padre</MenuItem>
                    <MenuItem value="SECRETARIO">Secretário</MenuItem>
                  </Select>
                </FormControl>

                {(() => {
                  const selectedOrgObj = orgs.find((o) => o.id === newVinculoOrgId);
                  return selectedOrgObj && (selectedOrgObj.tipo === 'PASTORAL' || selectedOrgObj.tipo === 'MOVIMENTO') ? (
                    <FormControl sx={{ minWidth: 200 }} size="small">
                      <InputLabel id="add-vinculo-church-label">Igreja de Atuação</InputLabel>
                      <Select
                        labelId="add-vinculo-church-label"
                        value={newVinculoChurchId}
                        label="Igreja de Atuação"
                        onChange={(e) => setNewVinculoChurchId(e.target.value)}
                        inputProps={{ 'data-testid': 'add-vinculo-church-select' }}
                      >
                        {orgs
                          .filter((o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE') && (!selectedUserForVinculos?.igrejaId || o.id === selectedUserForVinculos.igrejaId))
                          .map((o) => (
                            <MenuItem key={o.id} value={o.id}>
                              {o.nome}
                            </MenuItem>
                          ))}
                      </Select>
                    </FormControl>
                  ) : null;
                })()}

                <ActionButton
                  title="Adicionar Vínculo"
                  icon={addVinculoMutation.isPending ? <CircularProgress size={20} /> : <AddIcon />}
                  color="primary"
                  onClick={() => {
                    const selectedOrgObj = orgs.find((o) => o.id === newVinculoOrgId);
                    if (!selectedOrgObj) return;
                    if (selectedOrgObj.tipo === 'PASTORAL' || selectedOrgObj.tipo === 'MOVIMENTO') {
                      if (!newVinculoChurchId) {
                        showError('A igreja de atuação é obrigatória para pastorais e movimentos');
                        return;
                      }
                    }
                    addVinculoMutation.mutate({
                      organizacaoId: newVinculoOrgId,
                      tipo: selectedOrgObj.tipo,
                      papel: newVinculoPapel,
                      igrejaVinculoId: (selectedOrgObj.tipo === 'PASTORAL' || selectedOrgObj.tipo === 'MOVIMENTO') ? newVinculoChurchId : undefined,
                    });
                  }}
                  disabled={!newVinculoOrgId || !newVinculoPapel || addVinculoMutation.isPending}
                />
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <ActionButton
            title="Fechar"
            icon={<CloseIcon />}
            color="secondary"
            onClick={() => {
              setIsVinculosModalOpen(false);
              setSelectedUserForVinculos(null);
              setNewVinculoOrgId('');
              setNewVinculoPapel('');
              setNewVinculoChurchId('');
            }}
          />
        </DialogActions>
      </Dialog>

      {/* Admin User Register Modal */}
      <Dialog open={isAdminRegisterModalOpen} onClose={() => setIsAdminRegisterModalOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Cadastrar Novo Usuário do Sistema</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <FormControl fullWidth size="small">
              <InputLabel id="admin-reg-fiel-label">Vincular a Fiel Cadastrado</InputLabel>
              <Select
                labelId="admin-reg-fiel-label"
                value={adminRegisterFielId}
                label="Vincular a Fiel Cadastrado"
                onChange={(e) => {
                  setAdminRegisterFielId(e.target.value);
                  const fielSel = fieisList.find((f) => f.id === e.target.value);
                  if (fielSel && fielSel.email) {
                    setAdminRegisterUsername(fielSel.email);
                  }
                }}
              >
                {fieisList.map((f) => (
                  <MenuItem key={f.id} value={f.id}>
                    {f.nome} ({f.cpf})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              label="E-mail / Username"
              type="email"
              fullWidth
              size="small"
              required
              value={adminRegisterUsername}
              onChange={(e) => setAdminRegisterUsername(e.target.value)}
            />

            <TextField
              label="Senha de Acesso"
              type="password"
              fullWidth
              size="small"
              required
              value={adminRegisterPassword}
              onChange={(e) => setAdminRegisterPassword(e.target.value)}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={() => setIsAdminRegisterModalOpen(false)} />
          <ActionButton
            title="Cadastrar"
            icon={registerUserMutation.isPending ? <CircularProgress size={20} /> : <AddIcon />}
            color="primary"
            onClick={() => {
              if (!adminRegisterFielId || !adminRegisterUsername.trim() || !adminRegisterPassword) {
                showError('Selecione o fiel e preencha o e-mail e a senha.');
                return;
              }
              registerUserMutation.mutate({
                username: adminRegisterUsername.trim(),
                password: adminRegisterPassword,
                fielId: adminRegisterFielId,
              });
            }}
            disabled={!adminRegisterFielId || !adminRegisterUsername.trim() || !adminRegisterPassword || registerUserMutation.isPending}
          />
        </DialogActions>
      </Dialog>

      {/* PO Form Modal */}
      <Dialog open={poModalOpen} onClose={() => setPoModalOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedPO ? 'Editar Tipo Oficial' : 'Novo Tipo Oficial'}</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Nome do Tipo Oficial"
              fullWidth
              size="small"
              required
              value={poNome}
              onChange={(e) => setPoNome(e.target.value)}
              error={!!poErrors.nome}
              helperText={poErrors.nome}
            />
            <FormControl fullWidth size="small">
              <InputLabel id="po-tipo-label">Tipo</InputLabel>
              <Select
                labelId="po-tipo-label"
                value={poTipo}
                label="Tipo"
                onChange={(e) => setPoTipo(e.target.value as 'PASTORAL' | 'MOVIMENTO')}
              >
                <MenuItem value="PASTORAL">Pastoral</MenuItem>
                <MenuItem value="MOVIMENTO">Movimento</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={() => setPoModalOpen(false)} />
          <ActionButton
            title="Salvar"
            icon={createPOMutation.isPending || updatePOMutation.isPending ? <CircularProgress size={20} /> : <AddIcon />}
            color="primary"
            onClick={handleSavePO}
            disabled={createPOMutation.isPending || updatePOMutation.isPending}
          />
        </DialogActions>
      </Dialog>

      {/* Conclude Confirmation Dialog */}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Confirmar Conclusão do Ano Paroquial</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Tem certeza de que deseja concluir o ano paroquial {selectedYear}? Esta ação fechará definitivamente o período.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={() => setConfirmOpen(false)} />
          <ActionButton
            title="Confirmar"
            icon={updateAnoMutation.isPending ? <CircularProgress size={20} /> : <CheckCircleIcon />}
            color="primary"
            onClick={() => {
              if (selectedYear !== null) {
                updateAnoMutation.mutate({ ano: selectedYear, status: 'CONCLUIDO' });
              }
            }}
            disabled={updateAnoMutation.isPending}
          />
        </DialogActions>
      </Dialog>

      <ImportModal open={isImportModalOpen} onClose={() => setIsImportModalOpen(false)} />
    </Container>
  );
};

export default Settings;
