import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Alert, Box, Card, CircularProgress, Container, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, FormControl, MenuItem, Paper, Skeleton, Stack, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, TextField, Typography } from '@mui/material';
import Autocomplete from '@/shared/presentation/components/SearchableAutocomplete';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CloseIcon from '@mui/icons-material/Close';
import UploadIcon from '@mui/icons-material/CloudUpload';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import KeyIcon from '@mui/icons-material/Key';
import LinkIcon from '@mui/icons-material/Link';
import LockIcon from '@mui/icons-material/Lock';
import SaveIcon from '@mui/icons-material/Save';
import SettingsIcon from '@mui/icons-material/Settings';

import { getAnosParoquiais, updateAnoParoquialStatus } from '@/contextos/governanca/infrastructure/http/anosParoquiais';
import { getOrganizacoes } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import {
  adicionarVinculo,
  AdicionarVinculoRequest,
  alternarStatusUsuario,
  approveUsuario,
  createUsuario,
  CreateUsuarioRequest,
  getUsuarios,
  getVinculos,
  removerVinculo,
  updateUsuario,
  Usuario,
} from '@/contextos/iam/infrastructure/http/usuarios';
import { alterarMinhaSenha } from '@/contextos/iam/infrastructure/http/auth';
import { getFieis } from '@/contextos/fiel/infrastructure/http/fieis';
import {
  createPastoralOficial,
  deletePastoralOficial,
  getPastoraisOficiais,
  updatePastoralOficial,
} from '@/contextos/organizacao/infrastructure/http/pastoraisOficiais';
import { AnoParoquialStatus, PAPEL_ECLESIAL_LABELS, PastoralMovimentoOficial } from '@/shared/domain/types/types';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import {
  canApproveUsuario,
  canCreateUsuario,
  canEditOrInactivateUser,
  canManageAnoParoquial,
  canManageIdentidadeVisual,
  canManageUsuarios,
  isAdmin,
} from '@/contextos/iam/presentation/auth/permissions';
import { IdentidadeVisualTab } from '@/contextos/governanca/presentation/components/configuracoes/IdentidadeVisualTab';
import ImportModal from '@/contextos/importacao/presentation/components/ImportModal';
import PageHeader from '@/shared/presentation/components/PageHeader';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import { getAnoParoquialStatusMeta } from '@/shared/utils/statusMeta/anoParoquialStatus';
import { getUsuarioSituacaoMeta } from '@/shared/utils/statusMeta/situacaoStatus';
import { FormattedEmail } from '@/shared/presentation/components/FormattedCell';

interface TabPanelProps {
  children?: React.ReactNode;
  value: string;
  currentValue: string;
}

const CustomTabPanel = ({ children, value, currentValue }: TabPanelProps) => {
  if (value !== currentValue) return null;
  return (
    <div role="tabpanel" id={`settings-tabpanel-${value}`} aria-labelledby={`settings-tab-${value}`}>
      <Box sx={{ py: 3 }}>{children}</Box>
    </div>
  );
};

const a11yProps = (value: string) => ({
  id: `settings-tab-${value}`,
  'aria-controls': `settings-tabpanel-${value}`,
});

export const Configuracoes = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  const queryClient = useQueryClient();
  const isAuthorizedAno = canManageAnoParoquial(user);
  const isAuthorizedUsers = canManageUsuarios(user);

  const [tabValue, setTabValue] = useState<string>('ano');
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

  // Edit User states
  const [isEditUserModalOpen, setIsEditUserModalOpen] = useState(false);
  const [selectedUserForEdit, setSelectedUserForEdit] = useState<Usuario | null>(null);
  const [editUsername, setEditUsername] = useState('');
  const [editUserEnabled, setEditUserEnabled] = useState(true);

  // Inactivate/Activate User confirmation states
  const [isConfirmStatusModalOpen, setIsConfirmStatusModalOpen] = useState(false);
  const [userForStatusToggle, setUserForStatusToggle] = useState<Usuario | null>(null);

  // Change Password states (Self user)
  const [senhaAtual, setSenhaAtual] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmacaoNovaSenha, setConfirmacaoNovaSenha] = useState('');

  // Catalog states
  const [poModalOpen, setPoModalOpen] = useState(false);
  const [selectedPO, setSelectedPO] = useState<PastoralMovimentoOficial | null>(null);
  const [poNome, setPoNome] = useState('');
  const [poTipo, setPoTipo] = useState<'PASTORAL' | 'MOVIMENTO'>('PASTORAL');
  const [poErrors, setPoErrors] = useState<{ nome?: string }>({});

  const { data: years = [], isLoading: isLoadingYears, isError: isErrorYears } = useQuery({
    queryKey: ['anosParoquiais'],
    queryFn: getAnosParoquiais,
    enabled: tabValue === 'ano',
  });

  const updateAnoMutation = useMutation({
    mutationFn: ({ ano, status }: { ano: number; status: AnoParoquialStatus }) =>
      updateAnoParoquialStatus(ano, status),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['anosParoquiais'] });
      setConfirmOpen(false);
      showSuccess(`Ano paroquial ${updated.ano} atualizado com sucesso!`);
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao atualizar o status do ano paroquial.'));
    },
  });

  const { data: users = [], isLoading: isLoadingUsers, isError: isErrorUsers } = useQuery({
    queryKey: ['users'],
    queryFn: getUsuarios,
    enabled: isAuthorizedUsers && tabValue === 'usuarios',
  });

  const { data: vinculos = [], isLoading: isLoadingVinculos } = useQuery({
    queryKey: ['userVinculos', selectedUserForVinculos?.id],
    queryFn: () => getVinculos(selectedUserForVinculos!.id),
    enabled: !!selectedUserForVinculos && isVinculosModalOpen,
  });

  const { data: orgs = [] } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
    enabled: isVinculosModalOpen,
  });

  const { data: fieisList = [], isLoading: isLoadingFieis } = useQuery({
    queryKey: ['fieis', 'list'],
    queryFn: () => getFieis(),
    enabled: isAdminRegisterModalOpen,
  });

  const addVinculoMutation = useMutation({
    mutationFn: (request: AdicionarVinculoRequest) => adicionarVinculo(selectedUserForVinculos!.id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userVinculos', selectedUserForVinculos?.id] });
      setNewVinculoOrgId('');
      setNewVinculoPapel('');
      setNewVinculoChurchId('');
      showSuccess('Vínculo adicionado com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao adicionar vínculo.'));
    },
  });

  const removeVinculoMutation = useMutation({
    mutationFn: (vinculoId: string) => removerVinculo(selectedUserForVinculos!.id, vinculoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userVinculos', selectedUserForVinculos?.id] });
      showSuccess('Vínculo removido com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao remover vínculo.'));
    },
  });

  const registerUserMutation = useMutation({
    mutationFn: (request: CreateUsuarioRequest) => createUsuario(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsAdminRegisterModalOpen(false);
      setAdminRegisterFielId('');
      setAdminRegisterUsername('');
      setAdminRegisterPassword('');
      showSuccess('Usuário cadastrado com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao cadastrar usuário.'));
    },
  });

  const updateUserMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: { username?: string; enabled?: boolean } }) =>
      updateUsuario(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsEditUserModalOpen(false);
      setSelectedUserForEdit(null);
      showSuccess('Usuário atualizado com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao atualizar dados do usuário.'));
    },
  });

  const toggleStatusMutation = useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      alternarStatusUsuario(id, enabled),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsConfirmStatusModalOpen(false);
      setUserForStatusToggle(null);
      showSuccess('Status do usuário alterado com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao alterar status do usuário.'));
    },
  });

  const approveUserMutation = useMutation({
    mutationFn: (userId: string) => approveUsuario(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['notificacoes'] });
      queryClient.invalidateQueries({ queryKey: ['aprovacoesPendentesCount'] });
      showSuccess('Usuário e vínculos aprovados com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao aprovar usuário.'));
    },
  });

  const alterarSenhaMutation = useMutation({
    mutationFn: (data: { senhaAtual: string; novaSenha: string }) => alterarMinhaSenha(data),
    onSuccess: () => {
      setSenhaAtual('');
      setNovaSenha('');
      setConfirmacaoNovaSenha('');
      showSuccess('Sua senha foi alterada com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Falha ao alterar senha. Verifique a senha atual.'));
    },
  });

  const handleOpenEditUser = (u: Usuario) => {
    setSelectedUserForEdit(u);
    setEditUsername(u.username);
    setEditUserEnabled(u.enabled);
    setIsEditUserModalOpen(true);
  };

  const handleOpenConfirmStatus = (u: Usuario) => {
    setUserForStatusToggle(u);
    setIsConfirmStatusModalOpen(true);
  };

  const handleSalvarSenha = (e: React.FormEvent) => {
    e.preventDefault();
    if (!senhaAtual) {
      showError('Informe a senha atual.');
      return;
    }
    if (!novaSenha || novaSenha.length < 6) {
      showError('A nova senha deve ter no mínimo 6 caracteres.');
      return;
    }
    if (novaSenha !== confirmacaoNovaSenha) {
      showError('A nova senha e a confirmação não conferem.');
      return;
    }
    alterarSenhaMutation.mutate({ senhaAtual, novaSenha });
  };

  // Official Pastorals and Movements
  const { data: pastoraisOficiais = [], isLoading: isLoadingPO, isError: isErrorPO } = useQuery({
    queryKey: ['pastoraisOficiais'],
    queryFn: getPastoraisOficiais,
    enabled: isAdmin(user) && tabValue === 'catalogo',
  });

  const createPOMutation = useMutation({
    mutationFn: createPastoralOficial,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pastoraisOficiais'] });
      setPoModalOpen(false);
      showSuccess('Tipo oficial cadastrado com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao cadastrar tipo oficial.'));
    },
  });

  const updatePOMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Omit<PastoralMovimentoOficial, 'id'> }) =>
      updatePastoralOficial(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pastoraisOficiais'] });
      setPoModalOpen(false);
      showSuccess('Tipo oficial atualizado com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao atualizar tipo oficial.'));
    },
  });

  const deletePOMutation = useMutation({
    mutationFn: deletePastoralOficial,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pastoraisOficiais'] });
      showSuccess('Tipo oficial excluído com sucesso!');
    },
    onError: (err) => {
      showError(extractErrorMessage(err, 'Erro ao excluir tipo oficial.'));
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
    if (!poNome.trim()) errors.nome = 'Nome é obrigatório.';
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
          <Tab value="ano" label="Ano Paroquial" {...a11yProps('ano')} sx={{ fontWeight: 600 }} />
          {canManageIdentidadeVisual(user) && (
            <Tab value="identidade-visual" label="Identidade Visual" {...a11yProps('identidade-visual')} sx={{ fontWeight: 600 }} />
          )}
          {isAuthorizedUsers && (
            <Tab value="usuarios" label="Membros / Usuários" {...a11yProps('usuarios')} sx={{ fontWeight: 600 }} />
          )}
          {isAdmin(user) && (
            <Tab value="catalogo" label="Catálogo Oficial" {...a11yProps('catalogo')} sx={{ fontWeight: 600 }} />
          )}
          <Tab value="minha-senha" label="Minha Senha" {...a11yProps('minha-senha')} sx={{ fontWeight: 600 }} />
        </Tabs>
      </Box>

      {/* Tab: Ano Paroquial */}
      <CustomTabPanel value="ano" currentValue={tabValue}>
        {isLoadingYears ? (
          <Box sx={{ my: 4 }}>
            <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
            {[1, 2, 3, 4].map((i) => (
              <Skeleton key={i} variant="rectangular" height={48} sx={{ mb: 1.5 }} />
            ))}
          </Box>
        ) : isErrorYears ? (
          <Alert severity="error">Erro ao carregar os anos paroquiais.</Alert>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
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
                              <TableActionMenu actions={[{
                                label: "Fechar Ano", icon: <LockIcon color="warning" />,
                                onClick: () => updateAnoMutation.mutate({ ano: ano.ano, status: 'FECHADO' }), disabled: updateAnoMutation.isPending,
                              }]} />
                            )}
                            {ano.status === 'FECHADO' && (
                              <TableActionMenu actions={[{
                                label: "Concluir Ano", icon: <CheckCircleIcon color="secondary" />,
                                onClick: () => {
                                  setSelectedYear(ano.ano);
                                  setConfirmOpen(true);
                                }, disabled: updateAnoMutation.isPending,
                              }]} />
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
                <SearchableTablePagination colSpan={isAuthorizedAno ? 5 : 4}
                  rowsPerPageOptions={[5, 10, 25]}
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
              </Table>
            </TableContainer>
          </Box>
        )}
      </CustomTabPanel>
      
      {/* Tab: Identidade Visual */}
      {canManageIdentidadeVisual(user) && (
        <CustomTabPanel value="identidade-visual" currentValue={tabValue}>
          <IdentidadeVisualTab />
        </CustomTabPanel>
      )}

      {/* Tab: Membros / Usuários */}
      {isAuthorizedUsers && (
        <CustomTabPanel value="usuarios" currentValue={tabValue}>
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
                {user && canCreateUsuario(user) && (
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
                      <TableCell sx={{ fontWeight: 600 }}>E-mail / Username</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {users
                      .slice(usersPage * usersRowsPerPage, usersPage * usersRowsPerPage + usersRowsPerPage)
                      .map((u) => (
                        <TableRow key={u.id} hover>
                          <TableCell sx={{ fontWeight: 500 }}>{u.nome || '-'}</TableCell>
                          <TableCell><FormattedEmail value={u.username} /></TableCell>
                          <TableCell><StatusIcon {...getUsuarioSituacaoMeta(u.enabled)} /></TableCell>
                          <TableCell align="right">
                            <TableActionMenu
                              actions={[
                                ...(user && canEditOrInactivateUser(user) ? [{
                                  label: 'Editar',
                                  icon: <EditIcon fontSize="small" />,
                                  onClick: () => handleOpenEditUser(u),
                                }] : []),
                                {
                                  label: 'Gerenciar vínculos',
                                  icon: <LinkIcon fontSize="small" />,
                                  onClick: () => {
                                    setSelectedUserForVinculos(u);
                                    setIsVinculosModalOpen(true);
                                  },
                                },
                                ...(user && canEditOrInactivateUser(user) ? [{
                                  label: u.enabled ? 'Inativar Usuário' : 'Ativar Usuário',
                                  icon: u.enabled ? <BlockIcon fontSize="small" color="error" /> : <CheckCircleIcon fontSize="small" color="success" />,
                                  onClick: () => handleOpenConfirmStatus(u),
                                }] : []),
                                ...(!u.enabled && canApproveUsuario(user) ? [{
                                  label: 'Aprovar',
                                  icon: <CheckCircleIcon fontSize="small" />,
                                  onClick: () => approveUserMutation.mutate(u.id),
                                  disabled: approveUserMutation.isPending,
                                }] : []),
                              ]}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    {users.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={4} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                          Nenhum usuário encontrado.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                  <SearchableTablePagination colSpan={4}
                    rowsPerPageOptions={[5, 10, 25]}
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
                </Table>
              </TableContainer>
            </Box>
          )}
        </CustomTabPanel>
      )}

      {/* Tab: Catálogo Oficial */}
      {isAdmin(user) && (
        <CustomTabPanel value="catalogo" currentValue={tabValue}>
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
                  <SearchableTablePagination colSpan={3}
                    rowsPerPageOptions={[5, 10, 25]}
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
                </Table>
              </TableContainer>
            </Box>
          )}
        </CustomTabPanel>
      )}

      {/* Tab: Minha Senha */}
      <CustomTabPanel value="minha-senha" currentValue={tabValue}>
        <Box sx={{ maxWidth: 500 }}>
          <Card variant="outlined" sx={{ borderRadius: 2, p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
              <KeyIcon color="primary" />
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Alterar Minha Senha
              </Typography>
            </Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Mantenha sua conta protegida atualizando sua senha periodicamente.
            </Typography>

            <Box component="form" onSubmit={handleSalvarSenha} noValidate>
              <Stack spacing={2.5}>
                <TextField
                  label="Senha Atual"
                  type="password"
                  fullWidth
                  size="small"
                  required
                  value={senhaAtual}
                  onChange={(e) => setSenhaAtual(e.target.value)}
                  autoComplete="current-password"
                />

                <TextField
                  label="Nova Senha"
                  type="password"
                  fullWidth
                  size="small"
                  required
                  value={novaSenha}
                  onChange={(e) => setNovaSenha(e.target.value)}
                  autoComplete="new-password"
                  helperText="Mínimo de 6 caracteres"
                />

                <TextField
                  label="Confirmar Nova Senha"
                  type="password"
                  fullWidth
                  size="small"
                  required
                  value={confirmacaoNovaSenha}
                  onChange={(e) => setConfirmacaoNovaSenha(e.target.value)}
                  autoComplete="new-password"
                />

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', pt: 1 }}>
                  <ActionButton
                    title="Alterar Senha"
                    type="submit"
                    icon={alterarSenhaMutation.isPending ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
                    color="primary"
                    disabled={!senhaAtual || !novaSenha || !confirmacaoNovaSenha || alterarSenhaMutation.isPending}
                  />
                </Box>
              </Stack>
            </Box>
          </Card>
        </Box>
      </CustomTabPanel>

      {/* Modal de Edição de Usuário */}
      <Dialog
        open={isEditUserModalOpen}
        onClose={() => setIsEditUserModalOpen(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>Editar Usuário</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Card variant="outlined" sx={{ borderRadius: 2, p: 2, mt: 1 }}>
            <Stack spacing={2.5}>
              <Box>
                <Typography variant="caption" color="text.secondary">
                  Fiel Vinculado
                </Typography>
                <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                  {selectedUserForEdit?.nome || 'Não associado'}
                </Typography>
              </Box>

              <TextField
                label="E-mail / Username"
                size="small"
                fullWidth
                value={editUsername}
                onChange={(e) => setEditUsername(e.target.value)}
                required
              />

              <FormControl fullWidth size="small">

                <SearchableSelect
                  labelId="edit-user-status-label"
                  label="Status da Conta"
                  value={editUserEnabled ? 'true' : 'false'}
                  onChange={(e) => setEditUserEnabled(e.target.value === 'true')}
                >
                  <MenuItem value="true">Ativo</MenuItem>
                  <MenuItem value="false">Inativo</MenuItem>
                </SearchableSelect>
              </FormControl>
            </Stack>
          </Card>
        </DialogContent>
        <DialogActions>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            color="secondary"
            onClick={() => setIsEditUserModalOpen(false)}
          />
          <ActionButton
            title="Salvar Alterações"
            icon={updateUserMutation.isPending ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
            color="primary"
            onClick={() => {
              if (!selectedUserForEdit) return;
              updateUserMutation.mutate({
                id: selectedUserForEdit.id,
                data: {
                  username: editUsername.trim(),
                  enabled: editUserEnabled,
                },
              });
            }}
            disabled={!editUsername.trim() || updateUserMutation.isPending}
          />
        </DialogActions>
      </Dialog>

      {/* Diálogo de Confirmação de Inativação / Reativação */}
      <Dialog
        open={isConfirmStatusModalOpen}
        onClose={() => setIsConfirmStatusModalOpen(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>
          {userForStatusToggle?.enabled ? 'Inativar Usuário' : 'Ativar Usuário'}
        </DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Typography variant="body2">
            Tem certeza de que deseja {userForStatusToggle?.enabled ? 'inativar' : 'ativar'} o acesso do usuário{' '}
            <strong>{userForStatusToggle?.nome || userForStatusToggle?.username}</strong>?
          </Typography>
        </DialogContent>
        <DialogActions>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            color="secondary"
            onClick={() => setIsConfirmStatusModalOpen(false)}
          />
          <ActionButton
            title={userForStatusToggle?.enabled ? 'Inativar' : 'Ativar'}
            icon={toggleStatusMutation.isPending ? <CircularProgress size={20} color="inherit" /> : (userForStatusToggle?.enabled ? <BlockIcon /> : <CheckCircleIcon />)}
            color={userForStatusToggle?.enabled ? 'error' : 'success'}
            onClick={() => {
              if (!userForStatusToggle) return;
              toggleStatusMutation.mutate({
                id: userForStatusToggle.id,
                enabled: !userForStatusToggle.enabled,
              });
            }}
            disabled={toggleStatusMutation.isPending}
          />
        </DialogActions>
      </Dialog>

      {/* Vinculos Management Modal */}
      <Dialog open={isVinculosModalOpen} onClose={() => setIsVinculosModalOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Vínculos com Pastorais e Movimentos: {selectedUserForVinculos?.nome || selectedUserForVinculos?.username}
        </DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          {isLoadingVinculos ? (
            <Box sx={{ py: 3, display: 'flex', justifyContent: 'center' }}>
              <CircularProgress size={24} />
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, pt: 1 }}>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead sx={{ backgroundColor: 'action.hover' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Organização</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Papel</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Igreja de Atuação</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {vinculos.map((v) => (
                      <TableRow key={v.id}>
                        <TableCell>{v.organizacaoNome || 'Organização'}</TableCell>
                        <TableCell>{v.tipoOrganizacao}</TableCell>
                        <TableCell>{PAPEL_ECLESIAL_LABELS[v.papel] || v.papel}</TableCell>
                        <TableCell>{v.igrejaVinculoNome || '-'}</TableCell>
                        <TableCell>
                          <Typography variant="caption" color={v.ativo ? 'success.main' : 'warning.main'} sx={{ fontWeight: 600 }}>
                            {v.ativo ? 'Ativo' : 'Pendente de Aprovação'}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <TableActionMenu actions={[{
                            label: "Remover Vínculo", icon: <DeleteIcon fontSize="small" color="error" />,
                            onClick: () => removeVinculoMutation.mutate(v.id), disabled: removeVinculoMutation.isPending,
                          }]} />
                        </TableCell>
                      </TableRow>
                    ))}
                    {vinculos.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={6} align="center" sx={{ py: 2, color: 'text.secondary' }}>
                          Nenhum vínculo organizacional associado.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>

              <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                Adicionar Novo Vínculo
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                <FormControl sx={{ minWidth: 200 }} size="small">

                  <SearchableSelect
                    labelId="add-vinculo-org-label"
                    value={newVinculoOrgId}
                    label="Organização"
                    onChange={(e) => {
                      setNewVinculoOrgId(e.target.value);
                      setNewVinculoChurchId('');
                    }}
                  >
                    {orgs
                      .filter((o) => o.ativo && o.tipo !== 'MATRIZ' && o.tipo !== 'COMUNIDADE')
                      .map((o) => (
                        <MenuItem key={o.id} value={o.id}>
                          {o.nome}
                        </MenuItem>
                      ))}
                  </SearchableSelect>
                </FormControl>

                <FormControl sx={{ minWidth: 200 }} size="small">

                  <SearchableSelect
                    labelId="add-vinculo-role-label"
                    value={newVinculoPapel}
                    label="Papel"
                    onChange={(e) => setNewVinculoPapel(e.target.value)}
                  >
                    <MenuItem value="COORDENADOR">Coordenador de Grupo/Pastoral</MenuItem>
                    <MenuItem value="SECRETARIO_GRUPO">Secretário de Grupo/Pastoral</MenuItem>
                    <MenuItem value="MEMBRO_ATIVO">Membro Ativo</MenuItem>
                    <MenuItem value="VOLUNTARIO">Voluntário</MenuItem>
                    <MenuItem value="SECRETARIO_CONSELHO">Secretário de Conselho</MenuItem>
                    <MenuItem value="MEMBRO_CONSELHO_PASTORAL">Membro do Conselho Pastoral (CPP/CPC)</MenuItem>
                    <MenuItem value="MEMBRO_CONSELHO_ECONOMICO">Membro do Conselho Econômico (CAE)</MenuItem>
                  </SearchableSelect>
                </FormControl>

                {(() => {
                  const selectedOrgObj = orgs.find((o) => o.id === newVinculoOrgId);
                  return selectedOrgObj && (selectedOrgObj.tipo === 'PASTORAL' || selectedOrgObj.tipo === 'MOVIMENTO') ? (
                    <FormControl sx={{ minWidth: 200 }} size="small">

                      <SearchableSelect
                        labelId="add-vinculo-church-label"
                        value={newVinculoChurchId}
                        label="Igreja de Atuação"
                        onChange={(e) => setNewVinculoChurchId(e.target.value)}
                        inputProps={{ 'data-testid': 'add-vinculo-church-select' }}
                      >
                        {orgs
                          .filter((o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'))
                          .map((o) => (
                            <MenuItem key={o.id} value={o.id}>
                              {o.nome}
                            </MenuItem>
                          ))}
                      </SearchableSelect>
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
                      grupoParoquialId: newVinculoOrgId,
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
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Card variant="outlined" sx={{ borderRadius: 2, p: 2, mt: 1 }}>
            <Stack spacing={2.5}>
              <Autocomplete
                size="small"
                fullWidth
                options={fieisList}
                loading={isLoadingFieis}
                getOptionLabel={(option) => {
                  const cpf = option.dadosIdentificacao?.cpfFormatado ?? option.cpf;
                  return cpf ? `${option.nome} (${cpf})` : (option.nome || '');
                }}
                isOptionEqualToValue={(option, val) => {
                  const optId = option.fielId ?? option.id;
                  const valId = val.fielId ?? val.id;
                  return optId === valId;
                }}
                value={fieisList.find((f) => (f.fielId ?? f.id) === adminRegisterFielId) || null}
                onChange={(_, newValue) => {
                  const fid = newValue ? (newValue.fielId ?? newValue.id) : '';
                  setAdminRegisterFielId(fid);
                  const email = newValue?.contato?.email ?? newValue?.email;
                  if (email) {
                    setAdminRegisterUsername(email);
                  }
                }}
                noOptionsText="Nenhum fiel encontrado"
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Vincular a Fiel Cadastrado"
                    size="small"
                    required
                    placeholder="Pesquise o fiel por nome ou CPF..."
                    slotProps={{
                      ...params.slotProps,
                      htmlInput: {
                        ...params.slotProps.htmlInput,
                        'data-testid': 'admin-reg-fiel-autocomplete',
                      },
                    }}
                  />
                )}
              />

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
            </Stack>
          </Card>
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
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Card variant="outlined" sx={{ borderRadius: 2, p: 2, mt: 1 }}>
            <Stack spacing={2.5}>
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

                <SearchableSelect
                  labelId="po-tipo-label"
                  value={poTipo}
                  label="Tipo"
                  onChange={(e) => setPoTipo(e.target.value as 'PASTORAL' | 'MOVIMENTO')}
                >
                  <MenuItem value="PASTORAL">Pastoral</MenuItem>
                  <MenuItem value="MOVIMENTO">Movimento</MenuItem>
                </SearchableSelect>
              </FormControl>
            </Stack>
          </Card>
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

export default Configuracoes;
