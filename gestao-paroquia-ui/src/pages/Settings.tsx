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
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  OutlinedInput,
} from '@mui/material';
import { getAnosParoquiais, updateAnoParoquialStatus } from '../api/anosParoquiais';
import { getLocais, createLocal, updateLocal, deleteLocal } from '../api/locais';
import { getOrganizacoes, createOrganizacao, updateOrganizacao, getOrganizacaoMembros } from '../api/organizacoes';
import { getUsuarios, approveUsuario, getMemberships, addMembership, removeMembership, Usuario, AddMembershipRequest, registrarUsuario, RegistrarUsuarioRequest } from '../api/usuarios';
import { getPastoraisOficiais, createPastoralOficial, updatePastoralOficial, deletePastoralOficial } from '../api/pastoraisOficiais';
import { AnoParoquialStatus, Local, Organizacao, PastoralMovimentoOficial } from '../api/types';
import { useNotification } from '../context/NotificationContext';
import { useAuth } from '../auth/AuthContext';
import { canManageAnoParoquial, canManageLocais, canManageOrganizacoes, isAdmin } from '../auth/permissions';
import LocalFormModal from './LocalFormModal';
import OrganizacaoFormModal from './OrganizacaoFormModal';

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
  const isAuthorizedOrgs = canManageOrganizacoes(user);

  const [tabValue, setTabValue] = useState(0);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [selectedYear, setSelectedYear] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedLocal, setSelectedLocal] = useState<Local | null>(null);
  const [orgModalOpen, setOrgModalOpen] = useState(false);
  const [selectedOrg, setSelectedOrg] = useState<Organizacao | null>(null);
  const [orgSearch, setOrgSearch] = useState('');

  // User and Membership states
  const [selectedUserForMemberships, setSelectedUserForMemberships] = useState<Usuario | null>(null);
  const [isMembershipsModalOpen, setIsMembershipsModalOpen] = useState(false);
  const [newMembershipOrgId, setNewMembershipOrgId] = useState('');
  const [newMembershipPapel, setNewMembershipPapel] = useState('');
  const [newMembershipChurchId, setNewMembershipChurchId] = useState('');

  // Organization members states
  const [selectedOrgForMembers, setSelectedOrgForMembers] = useState<Organizacao | null>(null);
  const [isOrgMembersModalOpen, setIsOrgMembersModalOpen] = useState(false);
  const [newOrgMemberUserId, setNewOrgMemberUserId] = useState('');
  const [newOrgMemberPapel, setNewOrgMemberPapel] = useState('');
  const [newOrgMemberChurchId, setNewOrgMemberChurchId] = useState('');

  // Admin user registration states
  const [isAdminRegisterModalOpen, setIsAdminRegisterModalOpen] = useState(false);
  const [adminRegisterNome, setAdminRegisterNome] = useState('');
  const [adminRegisterUsername, setAdminRegisterUsername] = useState('');
  const [adminRegisterPassword, setAdminRegisterPassword] = useState('');
  const [adminRegisterTelefone, setAdminRegisterTelefone] = useState('');
  const [adminRegisterDataNascimento, setAdminRegisterDataNascimento] = useState('');
  const [adminRegisterSelectedOrgs, setAdminRegisterSelectedOrgs] = useState<string[]>([]);
  const [adminRegisterChurchSelections, setAdminRegisterChurchSelections] = useState<Record<string, string>>({});

  // Catalog states
  const [poModalOpen, setPoModalOpen] = useState(false);
  const [selectedPO, setSelectedPO] = useState<PastoralMovimentoOficial | null>(null);
  const [poNome, setPoNome] = useState('');
  const [poTipo, setPoTipo] = useState<'PASTORAL' | 'MOVIMENTO'>('PASTORAL');
  const [poErrors, setPoErrors] = useState<{ nome?: string }>({});

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

  const { data: orgs = [], isLoading: isLoadingOrgs, isError: isErrorOrgs } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
    enabled: tabValue === 2 || tabValue === 3,
  });

  const isAuthorizedUsers = canManageOrganizacoes(user);

  const { data: users = [], isLoading: isLoadingUsers, isError: isErrorUsers } = useQuery({
    queryKey: ['usuarios'],
    queryFn: getUsuarios,
    enabled: (tabValue === 3 || isOrgMembersModalOpen || orgModalOpen) && isAuthorizedUsers,
  });

  const { data: editingOrgMembros = [] } = useQuery({
    queryKey: ['organizacaoMembros', selectedOrg?.id],
    queryFn: () => getOrganizacaoMembros(selectedOrg!.id),
    enabled: !!selectedOrg && orgModalOpen && (selectedOrg.tipo === 'PASTORAL' || selectedOrg.tipo === 'MOVIMENTO'),
  });

  const approveUserMutation = useMutation({
    mutationFn: (id: string) => approveUsuario(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      showSuccess('Usuário aprovado com sucesso!');
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao aprovar usuário.';
      showError(errMsg);
    },
  });

  const { data: memberships = [], isLoading: isLoadingMemberships } = useQuery({
    queryKey: ['memberships', selectedUserForMemberships?.id],
    queryFn: () => getMemberships(selectedUserForMemberships!.id),
    enabled: !!selectedUserForMemberships && isMembershipsModalOpen,
  });

  const addMembershipMutation = useMutation({
    mutationFn: (request: AddMembershipRequest) => addMembership(selectedUserForMemberships!.id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['memberships', selectedUserForMemberships?.id] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Vínculo adicionado com sucesso!');
      setNewMembershipOrgId('');
      setNewMembershipPapel('');
      setNewMembershipChurchId('');
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao adicionar vínculo.';
      showError(errMsg);
    },
  });

  const removeMembershipMutation = useMutation({
    mutationFn: (membershipId: string) => removeMembership(selectedUserForMemberships!.id, membershipId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['memberships', selectedUserForMemberships?.id] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Vínculo removido com sucesso!');
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao remover vínculo.';
      showError(errMsg);
    },
  });

  const { data: orgMembers = [], isLoading: isLoadingOrgMembers } = useQuery({
    queryKey: ['organizacaoMembros', selectedOrgForMembers?.id],
    queryFn: () => getOrganizacaoMembros(selectedOrgForMembers!.id),
    enabled: !!selectedOrgForMembers && isOrgMembersModalOpen,
  });

  const addOrgMemberMutation = useMutation({
    mutationFn: ({ userId, request }: { userId: string; request: AddMembershipRequest }) =>
      addMembership(userId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizacaoMembros', selectedOrgForMembers?.id] });
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Membro vinculado com sucesso!');
      setNewOrgMemberUserId('');
      setNewOrgMemberPapel('');
      setNewOrgMemberChurchId('');
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao vincular membro.';
      showError(errMsg);
    },
  });

  const removeOrgMemberMutation = useMutation({
    mutationFn: ({ userId, membershipId }: { userId: string; membershipId: string }) =>
      removeMembership(userId, membershipId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizacaoMembros', selectedOrgForMembers?.id] });
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Membro removido com sucesso!');
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao remover membro.';
      showError(errMsg);
    },
  });

  const registerUserMutation = useMutation({
    mutationFn: (request: RegistrarUsuarioRequest) => registrarUsuario(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess('Membro cadastrado com sucesso!');
      setIsAdminRegisterModalOpen(false);
      // Reset fields
      setAdminRegisterNome('');
      setAdminRegisterUsername('');
      setAdminRegisterPassword('');
      setAdminRegisterTelefone('');
      setAdminRegisterDataNascimento('');
      setAdminRegisterSelectedOrgs([]);
      setAdminRegisterChurchSelections({});
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao cadastrar membro.';
      showError(errMsg);
    },
  });

  const filteredOrgs = orgs.filter((org) => {
    const query = orgSearch.toLowerCase().trim();
    if (!query) return true;
    return (
      org.nome.toLowerCase().includes(query) ||
      (org.coordenador && org.coordenador.toLowerCase().includes(query)) ||
      (org.viceCoordenador && org.viceCoordenador.toLowerCase().includes(query)) ||
      getOrgTipoLabel(org.tipo).toLowerCase().includes(query)
    );
  });

  const { data: pastoraisOficiais = [], isLoading: isLoadingPO, isError: isErrorPO } = useQuery({
    queryKey: ['pastoraisOficiais'],
    queryFn: getPastoraisOficiais,
    enabled: tabValue === 4 || orgModalOpen,
  });

  const createPOMutation = useMutation({
    mutationFn: (data: Omit<PastoralMovimentoOficial, 'id'>) => createPastoralOficial(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pastoraisOficiais'] });
      showSuccess('Tipo oficial cadastrado com sucesso!');
      setPoModalOpen(false);
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao cadastrar tipo oficial.';
      showError(errMsg);
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
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao atualizar tipo oficial.';
      showError(errMsg);
    },
  });

  const deletePOMutation = useMutation({
    mutationFn: (id: string) => deletePastoralOficial(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pastoraisOficiais'] });
      showSuccess('Tipo oficial excluído com sucesso!');
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao excluir tipo oficial.';
      showError(errMsg);
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

  const handlePOSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!poNome.trim()) {
      setPoErrors({ nome: 'O nome é obrigatório' });
      return;
    }
    if (selectedPO) {
      updatePOMutation.mutate({ id: selectedPO.id, data: { nome: poNome.trim(), tipo: poTipo } });
    } else {
      createPOMutation.mutate({ nome: poNome.trim(), tipo: poTipo });
    }
  };

  const createOrgMutation = useMutation({
    mutationFn: async ({ data }: { data: Omit<Organizacao, 'id'> }) => {
      return createOrganizacao(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      showSuccess('Pastoral / Movimento cadastrado com sucesso!');
      setOrgModalOpen(false);
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao cadastrar pastoral/movimento.';
      showError(errMsg);
    },
  });

  const updateOrgMutation = useMutation({
    mutationFn: async ({ id, data }: { id: string; data: Omit<Organizacao, 'id'> }) => {
      return updateOrganizacao(id, data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['organizacaoMembros'] });
      showSuccess('Pastoral / Movimento atualizado com sucesso!');
      setOrgModalOpen(false);
      setSelectedOrg(null);
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao atualizar pastoral/movimento.';
      showError(errMsg);
    },
  });

  const toggleOrgStatusMutation = useMutation({
    mutationFn: async (org: Organizacao) => {
      const { id, ...data } = org;
      return updateOrganizacao(id, { ...data, ativo: !org.ativo });
    },
    onSuccess: (_, org) => {
      queryClient.invalidateQueries({ queryKey: ['organizacoes'] });
      showSuccess(`Pastoral / Movimento ${org.ativo ? 'inativado' : 'ativado'} com sucesso!`);
    },
    onError: (error: any) => {
      const errMsg = error.response?.data?.message || 'Erro ao alterar status da pastoral/movimento.';
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

  const getOrgTipoLabel = (tipo: string) => {
    switch (tipo) {
      case 'PASTORAL':
        return 'Pastoral';
      case 'MOVIMENTO':
        return 'Movimento';
      case 'CLERO':
        return 'Clero';
      case 'CONSELHO_PASTORAL':
        return 'Conselho Pastoral';
      case 'CONSELHO_ECONOMICO':
        return 'Conselho Econômico';
      case 'MATRIZ':
        return 'Matriz';
      case 'COMUNIDADE':
        return 'Comunidade';
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
            <Tab label="Pastorais e Movimentos" {...a11yProps(2)} sx={{ fontWeight: 600 }} />
            {isAuthorizedUsers && (
              <Tab label="Membros / Usuários" {...a11yProps(3)} sx={{ fontWeight: 600 }} />
            )}
            {isAdmin(user) && (
              <Tab label="Catálogo Oficial" {...a11yProps(4)} sx={{ fontWeight: 600 }} />
            )}
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

        <CustomTabPanel value={tabValue} index={2}>
          {isLoadingOrgs ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
              <CircularProgress />
            </Box>
          ) : isErrorOrgs ? (
            <Alert severity="error" sx={{ borderRadius: 0 }}>
              Erro ao carregar pastorais e movimentos.
            </Alert>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
                <TextField
                  placeholder="Pesquisar por nome, tipo ou coordenador..."
                  variant="outlined"
                  size="small"
                  value={orgSearch}
                  onChange={(e) => setOrgSearch(e.target.value)}
                  sx={{ minWidth: 300, '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
                />
                {isAuthorizedOrgs && (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => {
                      setSelectedOrg(null);
                      setOrgModalOpen(true);
                    }}
                    sx={{ borderRadius: 0, fontWeight: 600 }}
                  >
                    Nova Pastoral / Movimento
                  </Button>
                )}
              </Box>

              <TableContainer component={Paper} sx={{ borderRadius: 0, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                <Table>
                  <TableHead sx={{ bgcolor: 'grey.100' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Coordenador</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Vice-Coordenador</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Contato</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredOrgs.map((org) => (
                      <TableRow key={org.id}>
                        <TableCell sx={{ fontWeight: 500 }}>{org.nome}</TableCell>
                        <TableCell>{getOrgTipoLabel(org.tipo)}</TableCell>
                        <TableCell>{org.coordenador || '-'}</TableCell>
                        <TableCell>{org.viceCoordenador || '-'}</TableCell>
                        <TableCell>{org.contato || '-'}</TableCell>
                        <TableCell>
                          <Box
                            sx={{
                              display: 'inline-block',
                              px: 1,
                              py: 0.5,
                              fontSize: '0.75rem',
                              fontWeight: 700,
                              color: org.ativo ? '#2e7d32' : '#c62828',
                              bgcolor: org.ativo ? '#e8f5e9' : '#ffebee',
                            }}
                          >
                            {org.ativo ? 'Ativo' : 'Inativo'}
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                            <Button
                              variant="outlined"
                              color="primary"
                              size="small"
                              onClick={() => {
                                setSelectedOrgForMembers(org);
                                setIsOrgMembersModalOpen(true);
                              }}
                              sx={{ borderRadius: 0 }}
                            >
                              Membros
                            </Button>
                            {isAuthorizedOrgs && (
                              <>
                                <Button
                                  variant="outlined"
                                  color="primary"
                                  size="small"
                                  onClick={() => {
                                    setSelectedOrg(org);
                                    setOrgModalOpen(true);
                                  }}
                                  disabled={toggleOrgStatusMutation.isPending}
                                  sx={{ borderRadius: 0 }}
                                >
                                  Editar
                                </Button>
                                <Button
                                  variant="outlined"
                                  color={org.ativo ? 'error' : 'primary'}
                                  size="small"
                                  onClick={() => toggleOrgStatusMutation.mutate(org)}
                                  disabled={toggleOrgStatusMutation.isPending}
                                  sx={{ borderRadius: 0 }}
                                >
                                  {org.ativo ? 'Inativar' : 'Ativar'}
                                </Button>
                              </>
                            )}
                          </Box>
                        </TableCell>
                      </TableRow>
                    ))}
                    {filteredOrgs.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={7} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                          Nenhuma pastoral ou movimento encontrado.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </CustomTabPanel>

        {isAuthorizedUsers && (
          <CustomTabPanel value={tabValue} index={3}>
            {isLoadingUsers ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                <CircularProgress />
              </Box>
            ) : isErrorUsers ? (
              <Alert severity="error" sx={{ borderRadius: 0 }}>
                Erro ao carregar os usuários.
              </Alert>
            ) : (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {user && user.roles.some(r => r === 'ADMIN' || r === 'ROLE_ADMIN' || r === 'PAROCO' || r === 'ROLE_CLERO_PAROCO') && (
                  <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 1 }}>
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={() => setIsAdminRegisterModalOpen(true)}
                      sx={{ borderRadius: 0, fontWeight: 600 }}
                    >
                      Cadastrar Usuário
                    </Button>
                  </Box>
                )}
                <TableContainer component={Paper} sx={{ borderRadius: 0, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                  <Table>
                    <TableHead sx={{ bgcolor: 'grey.100' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>E-mail</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Telefone</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Data de Nascimento</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {users.map((u) => (
                        <TableRow key={u.id}>
                          <TableCell sx={{ fontWeight: 500 }}>{u.nome}</TableCell>
                          <TableCell>{u.username}</TableCell>
                          <TableCell>{u.telefone || '-'}</TableCell>
                          <TableCell>{u.dataNascimento || '-'}</TableCell>
                          <TableCell>
                            <Box
                              sx={{
                                display: 'inline-block',
                                px: 1,
                                py: 0.5,
                                fontSize: '0.75rem',
                                fontWeight: 700,
                                color: u.enabled ? '#2e7d32' : '#ed6c02',
                                bgcolor: u.enabled ? '#e8f5e9' : '#fff3e0',
                              }}
                            >
                              {u.enabled ? 'Ativo' : 'Pendente'}
                            </Box>
                          </TableCell>
                          <TableCell align="right">
                            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                              {!u.enabled && (
                                <Button
                                  variant="contained"
                                  color="primary"
                                  size="small"
                                  onClick={() => approveUserMutation.mutate(u.id)}
                                  disabled={approveUserMutation.isPending}
                                  sx={{ borderRadius: 0, fontWeight: 600 }}
                                >
                                  Aprovar
                                </Button>
                              )}
                              <Button
                                variant="outlined"
                                color="primary"
                                size="small"
                                onClick={() => {
                                  setSelectedUserForMemberships(u);
                                  setIsMembershipsModalOpen(true);
                                }}
                                sx={{ borderRadius: 0 }}
                              >
                                Vínculos
                              </Button>
                            </Box>
                          </TableCell>
                        </TableRow>
                      ))}
                      {users.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={6} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                            Nenhum usuário encontrado.
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}
          </CustomTabPanel>
        )}

        {isAdmin(user) && (
          <CustomTabPanel value={tabValue} index={4}>
            {isLoadingPO ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                <CircularProgress />
              </Box>
            ) : isErrorPO ? (
              <Alert severity="error" sx={{ borderRadius: 0 }}>
                Erro ao carregar o catálogo de pastorais e movimentos oficiais.
              </Alert>
            ) : (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="h5" sx={{ fontWeight: 600, color: 'text.primary' }}>
                    Catálogo Oficial de Pastorais e Movimentos
                  </Typography>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={handleCreatePO}
                    sx={{ borderRadius: 0, fontWeight: 600 }}
                  >
                    Novo Tipo
                  </Button>
                </Box>

                <TableContainer component={Paper} sx={{ borderRadius: 0, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                  <Table>
                    <TableHead sx={{ bgcolor: 'grey.100' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {pastoraisOficiais.map((po) => (
                        <TableRow key={po.id}>
                          <TableCell sx={{ fontWeight: 500 }}>{po.nome}</TableCell>
                          <TableCell>{po.tipo === 'PASTORAL' ? 'Pastoral' : 'Movimento'}</TableCell>
                          <TableCell align="right">
                            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                              <Button
                                variant="outlined"
                                color="primary"
                                size="small"
                                onClick={() => handleEditPO(po)}
                                sx={{ borderRadius: 0 }}
                              >
                                Editar
                              </Button>
                              <Button
                                variant="outlined"
                                color="error"
                                size="small"
                                onClick={() => {
                                  if (window.confirm('Tem certeza de que deseja excluir este tipo oficial?')) {
                                    deletePOMutation.mutate(po.id);
                                  }
                                }}
                                disabled={deletePOMutation.isPending}
                                sx={{ borderRadius: 0 }}
                              >
                                Excluir
                              </Button>
                            </Box>
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
              </Box>
            )}
          </CustomTabPanel>
        )}
      </Box>

      {/* User Memberships Dialog */}
      <Dialog
        open={isMembershipsModalOpen}
        onClose={() => {
          setIsMembershipsModalOpen(false);
          setSelectedUserForMemberships(null);
          setNewMembershipOrgId('');
          setNewMembershipPapel('');
        }}
        maxWidth="md"
        fullWidth
        sx={{ '& .MuiPaper-root': { borderRadius: 0 } }}
      >
        <DialogTitle sx={{ fontWeight: 700 }}>
          Gerenciar Vínculos - {selectedUserForMemberships?.nome}
        </DialogTitle>
        <DialogContent dividers>
          {isLoadingMemberships ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                Vínculos Atuais:
              </Typography>
              <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 0 }}>
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'grey.50' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Organização</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Papel</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {(memberships || []).map((m) => (
                      <TableRow key={m.id}>
                        <TableCell sx={{ fontWeight: 500 }}>{m.organizacaoNome}</TableCell>
                        <TableCell>{getOrgTipoLabel(m.tipoOrganizacao)}</TableCell>
                        <TableCell>{m.papel}</TableCell>
                        <TableCell>{m.ativo ? 'Ativo' : 'Inativo'}</TableCell>
                        <TableCell align="right">
                          <Button
                            variant="text"
                            color="error"
                            size="small"
                            onClick={() => removeMembershipMutation.mutate(m.id)}
                            disabled={removeMembershipMutation.isPending}
                            sx={{ textTransform: 'none' }}
                          >
                            Remover
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                    {(memberships || []).length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} align="center" sx={{ py: 2, color: 'text.secondary' }}>
                          Nenhum vínculo ativo encontrado para este usuário.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>

              <Box sx={{ borderBottom: 1, borderColor: 'divider', my: 1 }} />

              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                Adicionar Novo Vínculo:
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                <FormControl sx={{ minWidth: 250 }} size="small">
                  <InputLabel id="add-membership-org-label">Organização</InputLabel>
                  <Select
                    labelId="add-membership-org-label"
                    id="add-membership-org"
                    value={newMembershipOrgId}
                    label="Organização"
                    onChange={(e) => setNewMembershipOrgId(e.target.value)}
                  >
                    {orgs
                      .filter((o) => o.ativo)
                      .map((o) => (
                        <MenuItem key={o.id} value={o.id}>
                          {o.nome} ({getOrgTipoLabel(o.tipo)})
                        </MenuItem>
                      ))}
                  </Select>
                </FormControl>

                <FormControl sx={{ minWidth: 200 }} size="small">
                  <InputLabel id="add-membership-role-label">Papel</InputLabel>
                  <Select
                    labelId="add-membership-role-label"
                    id="add-membership-role"
                    value={newMembershipPapel}
                    label="Papel"
                    onChange={(e) => setNewMembershipPapel(e.target.value)}
                  >
                    <MenuItem value="COORDENADOR">Coordenador</MenuItem>
                    <MenuItem value="VICE_COORDENADOR">Vice-Coordenador</MenuItem>
                    <MenuItem value="MEMBRO">Membro</MenuItem>
                    <MenuItem value="PAROCO">Pároco</MenuItem>
                    <MenuItem value="VIGARIO">Vigário</MenuItem>
                    <MenuItem value="PADRE">Padre</MenuItem>
                    <MenuItem value="SECRETARIO">Secretário</MenuItem>
                  </Select>
                </FormControl>

                {(() => {
                  const selectedOrgObj = orgs.find((o) => o.id === newMembershipOrgId);
                  return selectedOrgObj && (selectedOrgObj.tipo === 'PASTORAL' || selectedOrgObj.tipo === 'MOVIMENTO') ? (
                    <FormControl sx={{ minWidth: 200 }} size="small">
                      <InputLabel id="add-membership-church-label">Igreja de Atuação</InputLabel>
                      <Select
                        labelId="add-membership-church-label"
                        id="add-membership-church"
                        value={newMembershipChurchId}
                        label="Igreja de Atuação"
                        onChange={(e) => setNewMembershipChurchId(e.target.value)}
                        inputProps={{ 'data-testid': 'add-membership-church-select' }}
                      >
                        {orgs
                          .filter((o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'))
                          .map((o) => (
                            <MenuItem key={o.id} value={o.id}>
                              {o.nome}
                            </MenuItem>
                          ))}
                      </Select>
                    </FormControl>
                  ) : null;
                })()}

                <Button
                  variant="contained"
                  onClick={() => {
                    const selectedOrgObj = orgs.find((o) => o.id === newMembershipOrgId);
                    if (!selectedOrgObj) return;
                    if (selectedOrgObj.tipo === 'PASTORAL' || selectedOrgObj.tipo === 'MOVIMENTO') {
                      if (!newMembershipChurchId) {
                        showError('A igreja de atuação é obrigatória para pastorais e movimentos');
                        return;
                      }
                    }
                    addMembershipMutation.mutate({
                      organizacaoId: newMembershipOrgId,
                      tipo: selectedOrgObj.tipo,
                      papel: newMembershipPapel,
                      igrejaVinculoId: (selectedOrgObj.tipo === 'PASTORAL' || selectedOrgObj.tipo === 'MOVIMENTO') ? newMembershipChurchId : undefined
                    });
                  }}
                  disabled={!newMembershipOrgId || !newMembershipPapel || addMembershipMutation.isPending}
                  sx={{ borderRadius: 0, fontWeight: 600, height: 40 }}
                >
                  {addMembershipMutation.isPending ? 'Adicionando...' : 'Adicionar Vínculo'}
                </Button>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              setIsMembershipsModalOpen(false);
              setSelectedUserForMemberships(null);
              setNewMembershipOrgId('');
              setNewMembershipPapel('');
              setNewMembershipChurchId('');
            }}
            color="inherit"
          >
            Fechar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Organization Members Dialog */}
      <Dialog
        open={isOrgMembersModalOpen}
        onClose={() => {
          setIsOrgMembersModalOpen(false);
          setSelectedOrgForMembers(null);
          setNewOrgMemberUserId('');
          setNewOrgMemberPapel('');
        }}
        maxWidth="md"
        fullWidth
        sx={{ '& .MuiPaper-root': { borderRadius: 0 } }}
      >
        <DialogTitle sx={{ fontWeight: 700 }}>
          Membros - {selectedOrgForMembers?.nome}
        </DialogTitle>
        <DialogContent dividers>
          {isLoadingOrgMembers ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                Membros Vinculados:
              </Typography>
              <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 0 }}>
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'grey.50' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Nome do Membro</TableCell>
                      {selectedOrgForMembers && (selectedOrgForMembers.tipo === 'PASTORAL' || selectedOrgForMembers.tipo === 'MOVIMENTO') && (
                        <TableCell sx={{ fontWeight: 600 }}>Igreja de Atuação</TableCell>
                      )}
                      <TableCell sx={{ fontWeight: 600 }}>Papel</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600 }}>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {(orgMembers || []).map((m) => (
                      <TableRow key={m.membershipId}>
                        <TableCell sx={{ fontWeight: 500 }}>{m.usuarioNome}</TableCell>
                        {selectedOrgForMembers && (selectedOrgForMembers.tipo === 'PASTORAL' || selectedOrgForMembers.tipo === 'MOVIMENTO') && (
                          <TableCell>{orgs.find((o) => o.id === m.igrejaVinculoId)?.nome || '-'}</TableCell>
                        )}
                        <TableCell>{m.papel}</TableCell>
                        <TableCell>{m.ativo ? 'Ativo' : 'Inativo'}</TableCell>
                        <TableCell align="right">
                          <Button
                            variant="text"
                            color="error"
                            size="small"
                            onClick={() => removeOrgMemberMutation.mutate({ userId: m.usuarioId, membershipId: m.membershipId })}
                            disabled={removeOrgMemberMutation.isPending}
                            sx={{ textTransform: 'none' }}
                          >
                            Remover
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                    {(orgMembers || []).length === 0 && (
                      <TableRow>
                        <TableCell
                          colSpan={selectedOrgForMembers && (selectedOrgForMembers.tipo === 'PASTORAL' || selectedOrgForMembers.tipo === 'MOVIMENTO') ? 5 : 4}
                          align="center"
                          sx={{ py: 2, color: 'text.secondary' }}
                        >
                          Nenhum membro vinculado a esta pastoral/movimento.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>

              <Box sx={{ borderBottom: 1, borderColor: 'divider', my: 1 }} />

              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                Vincular Novo Membro:
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                <FormControl sx={{ minWidth: 250 }} size="small">
                  <InputLabel id="add-member-user-label">Usuário</InputLabel>
                  <Select
                    labelId="add-member-user-label"
                    id="add-member-user"
                    value={newOrgMemberUserId}
                    label="Usuário"
                    onChange={(e) => setNewOrgMemberUserId(e.target.value)}
                  >
                    {users
                      .filter((u) => u.enabled)
                      .map((u) => (
                        <MenuItem key={u.id} value={u.id}>
                          {u.nome} ({u.username})
                        </MenuItem>
                      ))}
                  </Select>
                </FormControl>

                <FormControl sx={{ minWidth: 200 }} size="small">
                  <InputLabel id="add-member-role-label">Papel</InputLabel>
                  <Select
                    labelId="add-member-role-label"
                    id="add-member-role"
                    value={newOrgMemberPapel}
                    label="Papel"
                    onChange={(e) => setNewOrgMemberPapel(e.target.value)}
                  >
                    <MenuItem value="COORDENADOR">Coordenador</MenuItem>
                    <MenuItem value="VICE_COORDENADOR">Vice-Coordenador</MenuItem>
                    <MenuItem value="MEMBRO">Membro</MenuItem>
                    <MenuItem value="TESOUREIRO">Tesoureiro</MenuItem>
                    <MenuItem value="SECRETARIO">Secretário</MenuItem>
                  </Select>
                </FormControl>

                {selectedOrgForMembers && (selectedOrgForMembers.tipo === 'PASTORAL' || selectedOrgForMembers.tipo === 'MOVIMENTO') && (
                  <FormControl sx={{ minWidth: 200 }} size="small">
                    <InputLabel id="add-member-church-label">Igreja de Atuação</InputLabel>
                    <Select
                      labelId="add-member-church-label"
                      id="add-member-church"
                      value={newOrgMemberChurchId}
                      label="Igreja de Atuação"
                      onChange={(e) => setNewOrgMemberChurchId(e.target.value)}
                      inputProps={{ 'data-testid': 'add-member-church-select' }}
                    >
                      {orgs
                        .filter((o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'))
                        .map((o) => (
                          <MenuItem key={o.id} value={o.id}>
                            {o.nome}
                          </MenuItem>
                        ))}
                    </Select>
                  </FormControl>
                )}

                <Button
                  variant="contained"
                  onClick={() => {
                    if (!selectedOrgForMembers) return;
                    if (selectedOrgForMembers.tipo === 'PASTORAL' || selectedOrgForMembers.tipo === 'MOVIMENTO') {
                      if (!newOrgMemberChurchId) {
                        showError('A igreja de atuação é obrigatória para pastorais e movimentos');
                        return;
                      }
                    }
                    addOrgMemberMutation.mutate({
                      userId: newOrgMemberUserId,
                      request: {
                        organizacaoId: selectedOrgForMembers.id,
                        tipo: selectedOrgForMembers.tipo,
                        papel: newOrgMemberPapel,
                        igrejaVinculoId: (selectedOrgForMembers.tipo === 'PASTORAL' || selectedOrgForMembers.tipo === 'MOVIMENTO') ? newOrgMemberChurchId : undefined
                      }
                    });
                  }}
                  disabled={!newOrgMemberUserId || !newOrgMemberPapel || addOrgMemberMutation.isPending}
                  sx={{ borderRadius: 0, fontWeight: 600, height: 40 }}
                >
                  {addOrgMemberMutation.isPending ? 'Vinculando...' : 'Vincular Membro'}
                </Button>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              setIsOrgMembersModalOpen(false);
              setSelectedOrgForMembers(null);
              setNewOrgMemberUserId('');
              setNewOrgMemberPapel('');
              setNewOrgMemberChurchId('');
            }}
            color="inherit"
          >
            Fechar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Admin User Registration Dialog */}
      <Dialog
        open={isAdminRegisterModalOpen}
        onClose={() => {
          setIsAdminRegisterModalOpen(false);
          setAdminRegisterNome('');
          setAdminRegisterUsername('');
          setAdminRegisterPassword('');
          setAdminRegisterTelefone('');
          setAdminRegisterDataNascimento('');
          setAdminRegisterSelectedOrgs([]);
          setAdminRegisterChurchSelections({});
        }}
        maxWidth="sm"
        fullWidth
        sx={{ '& .MuiPaper-root': { borderRadius: 0 } }}
      >
        <DialogTitle sx={{ fontWeight: 700 }}>
          Cadastrar Novo Usuário / Membro
        </DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="adminRegisterNome"
              label="Nome Completo"
              name="adminRegisterNome"
              value={adminRegisterNome}
              onChange={(e) => setAdminRegisterNome(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="adminRegisterUsername"
              label="E-mail"
              name="adminRegisterUsername"
              type="email"
              value={adminRegisterUsername}
              onChange={(e) => setAdminRegisterUsername(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="adminRegisterPassword"
              label="Senha"
              type="password"
              id="adminRegisterPassword"
              value={adminRegisterPassword}
              onChange={(e) => setAdminRegisterPassword(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="adminRegisterTelefone"
              label="Telefone"
              name="adminRegisterTelefone"
              value={adminRegisterTelefone}
              onChange={(e) => setAdminRegisterTelefone(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="adminRegisterDataNascimento"
              label="Data de Nascimento"
              name="adminRegisterDataNascimento"
              type="date"
              slotProps={{ inputLabel: { shrink: true } }}
              value={adminRegisterDataNascimento}
              onChange={(e) => setAdminRegisterDataNascimento(e.target.value)}
            />
            <FormControl fullWidth margin="normal">
              <InputLabel id="admin-reg-orgs-label">Pastorais / Movimentos Iniciais</InputLabel>
              <Select
                labelId="admin-reg-orgs-label"
                id="admin-reg-orgs"
                multiple
                value={adminRegisterSelectedOrgs}
                onChange={(e) => {
                  const val = typeof e.target.value === 'string' ? e.target.value.split(',') : e.target.value;
                  setAdminRegisterSelectedOrgs(val);
                  setAdminRegisterChurchSelections((prev) => {
                    const next = { ...prev };
                    Object.keys(next).forEach((id) => {
                      if (!val.includes(id)) {
                        delete next[id];
                      }
                    });
                    return next;
                  });
                }}
                input={<OutlinedInput label="Pastorais / Movimentos Iniciais" />}
              >
                {orgs
                  .filter((o) => o.ativo && o.tipo !== 'MATRIZ' && o.tipo !== 'COMUNIDADE')
                  .map((o) => (
                    <MenuItem key={o.id} value={o.id}>
                      {o.nome} ({getOrgTipoLabel(o.tipo)})
                    </MenuItem>
                  ))}
              </Select>
            </FormControl>

            {adminRegisterSelectedOrgs
              .map((id) => orgs.find((o) => o.id === id))
              .filter((org): org is Organizacao => !!org && (org.tipo === 'PASTORAL' || org.tipo === 'MOVIMENTO'))
              .map((org) => {
                const activeChurches = orgs.filter(
                  (o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE')
                );
                return (
                  <FormControl fullWidth margin="normal" key={org.id}>
                    <InputLabel id={`admin-reg-church-label-${org.id}`}>Igreja de atuação para {org.nome}</InputLabel>
                    <Select
                      labelId={`admin-reg-church-label-${org.id}`}
                      id={`admin-reg-church-${org.id}`}
                      value={adminRegisterChurchSelections[org.id] || ''}
                      label={`Igreja de atuação para ${org.nome}`}
                      onChange={(e) => {
                        const val = e.target.value;
                        setAdminRegisterChurchSelections((prev) => ({ ...prev, [org.id]: val }));
                      }}
                    >
                      {activeChurches.map((church) => (
                        <MenuItem key={church.id} value={church.id}>
                          {church.nome}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                );
              })}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              setIsAdminRegisterModalOpen(false);
              setAdminRegisterNome('');
              setAdminRegisterUsername('');
              setAdminRegisterPassword('');
              setAdminRegisterTelefone('');
              setAdminRegisterDataNascimento('');
              setAdminRegisterSelectedOrgs([]);
              setAdminRegisterChurchSelections({});
            }}
            color="inherit"
          >
            Cancelar
          </Button>
          <Button
            variant="contained"
            color="primary"
            disabled={
              !adminRegisterNome.trim() ||
              !adminRegisterUsername.trim() ||
              !adminRegisterPassword ||
              !adminRegisterTelefone.trim() ||
              !adminRegisterDataNascimento ||
              registerUserMutation.isPending
            }
            onClick={() => {
              const solicitacoes = [];
              for (const orgId of adminRegisterSelectedOrgs) {
                const org = orgs.find((o) => o.id === orgId);
                if (org && (org.tipo === 'PASTORAL' || org.tipo === 'MOVIMENTO')) {
                  const churchId = adminRegisterChurchSelections[orgId];
                  if (!churchId) {
                    showError(`Selecione a igreja de atuação para ${org.nome}`);
                    return;
                  }
                  solicitacoes.push({ organizacaoId: orgId, igrejaVinculoId: churchId });
                } else {
                  solicitacoes.push({ organizacaoId: orgId, igrejaVinculoId: null });
                }
              }

              registerUserMutation.mutate({
                nome: adminRegisterNome,
                username: adminRegisterUsername,
                password: adminRegisterPassword,
                telefone: adminRegisterTelefone,
                dataNascimento: adminRegisterDataNascimento,
                organizacoesSolicitadas: solicitacoes
              });
            }}
            sx={{ borderRadius: 0, fontWeight: 600 }}
          >
            {registerUserMutation.isPending ? 'Cadastrando...' : 'Cadastrar'}
          </Button>
        </DialogActions>
      </Dialog>

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

      <OrganizacaoFormModal
        open={orgModalOpen}
        onClose={() => {
          setOrgModalOpen(false);
          setSelectedOrg(null);
        }}
        onSave={(data) => {
          if (selectedOrg) {
            updateOrgMutation.mutate({ id: selectedOrg.id, data });
          } else {
            createOrgMutation.mutate({ data });
          }
        }}
        organizacao={selectedOrg}
        isPending={createOrgMutation.isPending || updateOrgMutation.isPending}
        usuarios={users}
        igrejas={orgs.filter(o => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'))}
        membros={editingOrgMembros}
        organizacoes={orgs}
        pastoraisOficiais={pastoraisOficiais}
      />

      {/* Official Catalog Form Modal */}
      <Dialog
        open={poModalOpen}
        onClose={() => setPoModalOpen(false)}
        fullWidth
        maxWidth="sm"
        sx={{ '& .MuiPaper-root': { borderRadius: 0 } }}
      >
        <form onSubmit={handlePOSubmit}>
          <DialogTitle sx={{ fontWeight: 600, color: 'primary.main' }}>
            {selectedPO ? 'Editar Tipo Oficial' : 'Novo Tipo Oficial'}
          </DialogTitle>
          <DialogContent dividers>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
              <TextField
                label="Nome"
                variant="outlined"
                fullWidth
                required
                value={poNome}
                onChange={(e) => {
                  setPoNome(e.target.value);
                  if (e.target.value.trim()) setPoErrors({});
                }}
                error={!!poErrors.nome}
                helperText={poErrors.nome}
                sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
              />
              <FormControl fullWidth required>
                <InputLabel id="po-tipo-label">Tipo</InputLabel>
                <Select
                  labelId="po-tipo-label"
                  label="Tipo"
                  value={poTipo}
                  onChange={(e) => setPoTipo(e.target.value as 'PASTORAL' | 'MOVIMENTO')}
                  sx={{ borderRadius: 0 }}
                >
                  <MenuItem value="PASTORAL">Pastoral</MenuItem>
                  <MenuItem value="MOVIMENTO">Movimento</MenuItem>
                </Select>
              </FormControl>
            </Box>
          </DialogContent>
          <DialogActions sx={{ p: 2 }}>
            <Button onClick={() => setPoModalOpen(false)} color="inherit" sx={{ borderRadius: 0 }}>
              Cancelar
            </Button>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={createPOMutation.isPending || updatePOMutation.isPending}
              sx={{ borderRadius: 0, fontWeight: 600 }}
            >
              Salvar
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Container>
  );
};

export default Settings;
