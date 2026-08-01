import React, {useState} from 'react';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {
    Alert,
    Box,
    Button,
    Card,
    Checkbox,
    Chip,
    Container,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    FormControl,
    FormControlLabel,
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
    TableRow,
    Tabs,
    TextField,
    Typography,
} from '@mui/material';
import {
  CalendarMonth as CalendarIcon,
  Category as CategoryIcon,
  Groups as GroupsIcon,
  LocationOn as LocationIcon,
  People as PeopleIcon,
  Settings as SettingsIcon,
} from '@mui/icons-material';

import {getAnosParoquiais, updateAnoParoquialStatus} from '../api/anosParoquiais';
import {createLocal, deleteLocal, getLocais, updateLocal} from '../api/locais';
import {createOrganizacao, getOrganizacaoMembros, getOrganizacoes, updateOrganizacao} from '../api/organizacoes';
import {
    addMembership,
    AddMembershipRequest,
    approveUsuario,
    getMemberships,
    getUsuarios,
    registrarUsuario,
    RegistrarUsuarioRequest,
    removeMembership,
    Usuario
} from '../api/usuarios';
import {
    createPastoralOficial,
    deletePastoralOficial,
    getPastoraisOficiais,
    updatePastoralOficial
} from '../api/pastoraisOficiais';
import {AnoParoquialStatus, Local, Organizacao, PAPEL_ECLESIAL_LABELS, PastoralMovimentoOficial} from '../api/types';
import {useNotification} from '../context/NotificationContext';
import {useAuth} from '../auth/AuthContext';
import {canManageAnoParoquial, canManageLocais, canManageOrganizacoes, isAdmin} from '../auth/permissions';
import LocalFormModal from './LocalFormModal';
import OrganizacaoFormModal from './OrganizacaoFormModal';
import ImportModal from '../components/ImportModal';
import PageHeader from '../components/PageHeader';

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
  const [isImportModalOpen, setIsImportModalOpen] = useState(false);
  const [adminRegisterNome, setAdminRegisterNome] = useState('');
  const [adminRegisterUsername, setAdminRegisterUsername] = useState('');
  const [adminRegisterPassword, setAdminRegisterPassword] = useState('');
  const [adminRegisterTelefone, setAdminRegisterTelefone] = useState('');
  const [adminRegisterDataNascimento, setAdminRegisterDataNascimento] = useState('');
  const [adminRegisterChurchId, setAdminRegisterChurchId] = useState('');
  const [adminRegisterBatizado, setAdminRegisterBatizado] = useState(false);
  const [adminRegisterPrimeiraComunhao, setAdminRegisterPrimeiraComunhao] = useState(false);
  const [adminRegisterCrismado, setAdminRegisterCrismado] = useState(false);
  const [adminRegisterCasadoIgrejaCatolica, setAdminRegisterCasadoIgrejaCatolica] = useState(false);


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
      setAdminRegisterChurchId('');
      setAdminRegisterBatizado(false);
      setAdminRegisterPrimeiraComunhao(false);
      setAdminRegisterCrismado(false);
      setAdminRegisterCasadoIgrejaCatolica(false);
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
        return 'Conselho Pastoral Paroquial (CPP)';
      case 'CONSELHO_ECONOMICO':
        return 'Conselho Econômico Paroquial (CAE)';
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
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <PageHeader
        icon={<SettingsIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Configurações do Sistema"
        subtitle="Gerencie os parâmetros globais do sistema, como o calendário paroquial, os locais físicos e pastorais."
        actions={isAdmin(user) && (
          <Button
            variant="outlined"
            color="primary"
            onClick={() => setIsImportModalOpen(true)}
            sx={{ textTransform: 'none', fontWeight: 600, px: 2.5, py: 1 }}
          >
            Importar em Lote (CSV/Excel)
          </Button>
        )}
      />

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
            <Box sx={{ my: 4 }}>
              <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
              {[1, 2, 3, 4].map((i) => (
                <Skeleton key={i} variant="rectangular" height={40} sx={{ mb: 1 }} />
              ))}
            </Box>
          ) : isError ? (
            <Alert severity="error">
              Erro ao carregar as informações dos anos paroquiais.
            </Alert>
          ) : (
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
                <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', display: 'flex' }}>
                  <CalendarIcon sx={{ fontSize: 26, color: 'primary.main' }} />
                </Box>
                <Box>
                  <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                    Gestão do Ano Paroquial
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.2 }}>
                    Controle as fases de planejamento e fechamento do calendário paroquial.
                  </Typography>
                </Box>
              </Box>

              <TableContainer component={Paper} sx={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
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
            <Box sx={{ my: 4 }}>
              <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
              {[1, 2, 3, 4].map((i) => (
                <Skeleton key={i} variant="rectangular" height={40} sx={{ mb: 1 }} />
              ))}
            </Box>
          ) : isErrorLocais ? (
            <Alert severity="error">
              Erro ao carregar as informações dos locais.
            </Alert>
          ) : (
            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', display: 'flex' }}>
                    <LocationIcon sx={{ fontSize: 26, color: 'primary.main' }} />
                  </Box>
                  <Box>
                    <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                      Gestão de Locais (Espaços Físicos)
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.2 }}>
                      Cadastre e gerencie os espaços disponíveis na paróquia para a realização de eventos.
                    </Typography>
                  </Box>
                </Box>
                {isAuthorizedLocais && (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => {
                      setSelectedLocal(null);
                      setModalOpen(true);
                    }}
                    sx={{ fontWeight: 600 }}
                  >
                    Novo Local
                  </Button>
                )}
              </Box>

              <TableContainer component={Paper} sx={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                <Table>
                  <TableHead sx={{ bgcolor: 'grey.100' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Nome</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Capacidade</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Matriz / Comunidade</TableCell>
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
                        <TableCell>{local.organizacaoNome || '-'}</TableCell>
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
                               
                              >
                                Editar
                              </Button>
                              <Button
                                variant="outlined"
                                color={local.ativo ? 'error' : 'primary'}
                                size="small"
                                onClick={() => toggleLocalStatusMutation.mutate(local)}
                                disabled={toggleLocalStatusMutation.isPending}
                               
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
            <Box sx={{ py: 4 }}>
              <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
              {[1, 2, 3, 4].map((i) => (
                <Skeleton key={i} variant="rectangular" height={40} sx={{ mb: 1 }} />
              ))}
            </Box>
          ) : isErrorOrgs ? (
            <Alert severity="error">
              Erro ao carregar pastorais e movimentos.
            </Alert>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, flexWrap: 'wrap', gap: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', display: 'flex' }}>
                    <GroupsIcon sx={{ fontSize: 26, color: 'primary.main' }} />
                  </Box>
                  <Box>
                    <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                      Gestão de Pastorais e Movimentos
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.2 }}>
                      Cadastre e organize as pastorais, movimentos e organismos da paróquia.
                    </Typography>
                  </Box>
                </Box>
              </Box>

              <Card
                elevation={0}
                sx={{
                  p: 2,
                  mb: 1,
                  
                  border: '1px solid',
                  borderColor: 'divider',
                  backgroundColor: '#fafafa',
                }}
              >
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
                  <TextField
                    placeholder="Pesquisar por nome, tipo ou coordenador..."
                    variant="outlined"
                    size="small"
                    value={orgSearch}
                    onChange={(e) => setOrgSearch(e.target.value)}
                    sx={{ minWidth: 320, '& .MuiOutlinedInput-root': { bgcolor: '#fff' } }}
                  />
                  {isAuthorizedOrgs && (
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={() => {
                        setSelectedOrg(null);
                        setOrgModalOpen(true);
                      }}
                      sx={{ fontWeight: 600 }}
                    >
                      Nova Pastoral / Movimento
                    </Button>
                  )}
                </Box>
              </Card>

              <TableContainer component={Paper} sx={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
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
                                 
                                >
                                  Editar
                                </Button>
                                <Button
                                  variant="outlined"
                                  color={org.ativo ? 'error' : 'primary'}
                                  size="small"
                                  onClick={() => toggleOrgStatusMutation.mutate(org)}
                                  disabled={toggleOrgStatusMutation.isPending}
                                 
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
              <Box sx={{ py: 4 }}>
                <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
                {[1, 2, 3, 4].map((i) => (
                  <Skeleton key={i} variant="rectangular" height={40} sx={{ mb: 1 }} />
                ))}
              </Box>
            ) : isErrorUsers ? (
              <Alert severity="error">
                Erro ao carregar os usuários.
              </Alert>
            ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, flexWrap: 'wrap', gap: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', display: 'flex' }}>
                    <PeopleIcon sx={{ fontSize: 26, color: 'primary.main' }} />
                  </Box>
                  <Box>
                    <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                      Gestão de Membros e Usuários
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.2 }}>
                      Gerencie o acesso, permissões e papéis dos usuários e membros cadastrados no sistema.
                    </Typography>
                  </Box>
                </Box>
                {user && user.roles.some(r => r === 'ADMIN' || r === 'ROLE_ADMIN' || r === 'PAROCO' || r === 'ROLE_CLERO_PAROCO') && (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => setIsAdminRegisterModalOpen(true)}
                    sx={{ fontWeight: 600 }}
                  >
                    Cadastrar Usuário
                  </Button>
                )}
              </Box>
                <TableContainer component={Paper} sx={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
                  <Table>
                    <TableHead sx={{ bgcolor: 'grey.100' }}>
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
                      {users.map((u) => (
                        <TableRow key={u.id}>
                          <TableCell sx={{ fontWeight: 500 }}>{u.nome}</TableCell>
                          <TableCell>{u.username}</TableCell>
                          <TableCell>{u.igrejaNome || '-'}</TableCell>
                          <TableCell>{u.telefone || '-'}</TableCell>
                          <TableCell>{u.dataNascimento || '-'}</TableCell>
                          <TableCell>
                            <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                              {u.batizado && <Chip label="Batismo" size="small" color="info" variant="outlined" />}
                              {u.primeiraComunhao && <Chip label="1ª Comunhão" size="small" color="success" variant="outlined" />}
                              {u.crismado && <Chip label="Crisma" size="small" color="secondary" variant="outlined" />}
                              {u.casadoIgrejaCatolica && <Chip label="Matrimônio" size="small" color="warning" variant="outlined" />}
                              {!u.batizado && !u.primeiraComunhao && !u.crismado && !u.casadoIgrejaCatolica && '-'}
                            </Box>
                          </TableCell>

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
                                  sx={{ fontWeight: 600 }}
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
                               
                              >
                                Gerenciar Vínculos
                              </Button>
                            </Box>
                          </TableCell>
                        </TableRow>
                      ))}
                      {users.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={7} align="center" sx={{ py: 3, color: 'text.secondary' }}>
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
              <Box sx={{ py: 4 }}>
                <Skeleton variant="text" width={260} height={32} sx={{ mb: 2 }} />
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} variant="rectangular" height={40} sx={{ mb: 1 }} />
                ))}
              </Box>
            ) : isErrorPO ? (
              <Alert severity="error">
                Erro ao carregar o catálogo de pastorais e movimentos oficiais.
              </Alert>
            ) : (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, flexWrap: 'wrap', gap: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', display: 'flex' }}>
                      <CategoryIcon sx={{ fontSize: 26, color: 'primary.main' }} />
                    </Box>
                    <Box>
                      <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                        Catálogo Oficial de Pastorais e Movimentos
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 0.2 }}>
                        Mantenha o catálogo oficial padronizado de pastorais e movimentos da diocese.
                      </Typography>
                    </Box>
                  </Box>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={handleCreatePO}
                    sx={{ fontWeight: 600 }}
                  >
                    Novo Tipo
                  </Button>
                </Box>

                <TableContainer component={Paper} sx={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
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
      >
        <DialogTitle>
          Gerenciar Vínculos - {selectedUserForMemberships?.nome}
        </DialogTitle>
        <DialogContent dividers>
          {isLoadingMemberships ? (
            <Box sx={{ py: 2 }}>
              <Skeleton variant="text" width={200} height={28} sx={{ mb: 1.5 }} />
              {[1, 2, 3].map((i) => (
                <Skeleton key={i} variant="rectangular" height={36} sx={{ mb: 1 }} />
              ))}
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                Vínculos Atuais:
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'grey.50' }}>
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
                    {(memberships || []).map((m) => (
                      <TableRow key={m.id}>
                        <TableCell sx={{ fontWeight: 500 }}>{m.organizacaoNome || orgs.find((o) => o.id === m.organizacaoId)?.nome || '-'}</TableCell>
                        <TableCell>{m.igrejaVinculoNome || orgs.find((o) => o.id === m.igrejaVinculoId)?.nome || '-'}</TableCell>
                        <TableCell>{getOrgTipoLabel(m.tipoOrganizacao)}</TableCell>
                        <TableCell>{PAPEL_ECLESIAL_LABELS[m.papel] || m.papel}</TableCell>
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
                        <TableCell colSpan={6} align="center" sx={{ py: 2, color: 'text.secondary' }}>
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
                      .filter((o) => {
                        if (!o.ativo) return false;
                        if (!selectedUserForMemberships?.igrejaId) return true;
                        const userChurch = selectedUserForMemberships.igrejaId;
                        if (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE') {
                          return o.id === userChurch;
                        }
                        if (o.tipo === 'PASTORAL' || o.tipo === 'MOVIMENTO') {
                          return true;
                        }
                        return o.parentId === userChurch || !o.parentId;
                      })
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
                          .filter((o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE') && (!selectedUserForMemberships?.igrejaId || o.id === selectedUserForMemberships.igrejaId))
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
                  sx={{ fontWeight: 600, height: 40 }}
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
      >
        <DialogTitle>
          Membros - {selectedOrgForMembers?.nome}
        </DialogTitle>
        <DialogContent dividers>
          {isLoadingOrgMembers ? (
            <Box sx={{ py: 2 }}>
              <Skeleton variant="text" width={200} height={28} sx={{ mb: 1.5 }} />
              {[1, 2, 3].map((i) => (
                <Skeleton key={i} variant="rectangular" height={36} sx={{ mb: 1 }} />
              ))}
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                Membros Vinculados:
              </Typography>
              <TableContainer component={Paper} variant="outlined">
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
                        <TableCell>{PAPEL_ECLESIAL_LABELS[m.papel] || m.papel}</TableCell>
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
                      .filter((u) => {
                        if (!u.enabled) return false;
                        const targetChurch = newOrgMemberChurchId || selectedOrgForMembers?.parentId || (selectedOrgForMembers?.tipo === 'MATRIZ' || selectedOrgForMembers?.tipo === 'COMUNIDADE' ? selectedOrgForMembers?.id : undefined);
                        if (!targetChurch || !u.igrejaId) return true;
                        return u.igrejaId === targetChurch;
                      })
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
                  sx={{ fontWeight: 600, height: 40 }}
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
          setAdminRegisterChurchId('');
        }}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Cadastrar Novo Usuário / Membro
        </DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <TextField
              required
              fullWidth
              size="small"
              id="adminRegisterNome"
              label="Nome Completo"
              name="adminRegisterNome"
              value={adminRegisterNome}
              onChange={(e) => setAdminRegisterNome(e.target.value)}
            />
            <TextField
              required
              fullWidth
              size="small"
              id="adminRegisterUsername"
              label="E-mail"
              name="adminRegisterUsername"
              type="email"
              value={adminRegisterUsername}
              onChange={(e) => setAdminRegisterUsername(e.target.value)}
            />
            <TextField
              required
              fullWidth
              size="small"
              name="adminRegisterPassword"
              label="Senha"
              type="password"
              id="adminRegisterPassword"
              value={adminRegisterPassword}
              onChange={(e) => setAdminRegisterPassword(e.target.value)}
            />
            <TextField
              required
              fullWidth
              size="small"
              id="adminRegisterTelefone"
              label="Telefone"
              name="adminRegisterTelefone"
              value={adminRegisterTelefone}
              onChange={(e) => setAdminRegisterTelefone(e.target.value)}
            />
            <TextField
              required
              fullWidth
              size="small"
              id="adminRegisterDataNascimento"
              label="Data de Nascimento"
              name="adminRegisterDataNascimento"
              type="date"
              slotProps={{ inputLabel: { shrink: true } }}
              value={adminRegisterDataNascimento}
              onChange={(e) => setAdminRegisterDataNascimento(e.target.value)}
            />
            <FormControl fullWidth required size="small">
              <InputLabel id="admin-reg-church-label">Igreja / Comunidade</InputLabel>
              <Select
                labelId="admin-reg-church-label"
                id="admin-reg-church"
                value={adminRegisterChurchId}
                label="Igreja / Comunidade"
                onChange={(e) => setAdminRegisterChurchId(e.target.value)}
              >
                {orgs
                  .filter((o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'))
                  .map((o) => (
                    <MenuItem key={o.id} value={o.id}>
                      {o.nome} ({o.tipo === 'MATRIZ' ? 'Matriz' : 'Comunidade'})
                    </MenuItem>
                  ))}
              </Select>
            </FormControl>
            <Typography variant="subtitle2" sx={{ mt: 1, fontWeight: 600 }}>
              Indicadores Sacramentais
            </Typography>
            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 0.5 }}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={adminRegisterBatizado}
                    onChange={(e) => setAdminRegisterBatizado(e.target.checked)}
                  />
                }
                label="Batizado"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={adminRegisterPrimeiraComunhao}
                    onChange={(e) => setAdminRegisterPrimeiraComunhao(e.target.checked)}
                  />
                }
                label="1ª Comunhão"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={adminRegisterCrismado}
                    onChange={(e) => setAdminRegisterCrismado(e.target.checked)}
                  />
                }
                label="Crismado"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={adminRegisterCasadoIgrejaCatolica}
                    onChange={(e) => setAdminRegisterCasadoIgrejaCatolica(e.target.checked)}
                  />
                }
                label="Casado na Igreja"
              />
            </Box>
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
              setAdminRegisterChurchId('');
              setAdminRegisterBatizado(false);
              setAdminRegisterPrimeiraComunhao(false);
              setAdminRegisterCrismado(false);
              setAdminRegisterCasadoIgrejaCatolica(false);
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
              !adminRegisterChurchId ||
              registerUserMutation.isPending
            }
            onClick={() => {
              registerUserMutation.mutate({
                nome: adminRegisterNome,
                username: adminRegisterUsername,
                password: adminRegisterPassword,
                telefone: adminRegisterTelefone,
                dataNascimento: adminRegisterDataNascimento,
                igrejaId: adminRegisterChurchId,
                batizado: adminRegisterBatizado,
                primeiraComunhao: adminRegisterPrimeiraComunhao,
                crismado: adminRegisterCrismado,
                casadoIgrejaCatolica: adminRegisterCasadoIgrejaCatolica,
              });
            }}
            sx={{ fontWeight: 600 }}
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
        organizacoes={orgs}
        pastoraisOficiais={pastoraisOficiais}
      />

      {/* Official Catalog Form Modal */}
      <Dialog
        open={poModalOpen}
        onClose={() => setPoModalOpen(false)}
        fullWidth
        maxWidth="sm"
      >
        <form onSubmit={handlePOSubmit}>
          <DialogTitle sx={{ fontWeight: 600, color: 'primary.main' }}>
            {selectedPO ? 'Editar Tipo Oficial' : 'Novo Tipo Oficial'}
          </DialogTitle>
          <DialogContent dividers>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
              <TextField
                label="Nome"
                variant="outlined"
                fullWidth
                required
                size="small"
                value={poNome}
                onChange={(e) => {
                  setPoNome(e.target.value);
                  if (e.target.value.trim()) setPoErrors({});
                }}
                error={!!poErrors.nome}
                helperText={poErrors.nome}
                sx={{ '& .MuiOutlinedInput-root': {  } }}
              />
              <FormControl fullWidth required size="small">
                <InputLabel id="po-tipo-label">Tipo</InputLabel>
                <Select
                  labelId="po-tipo-label"
                  label="Tipo"
                  value={poTipo}
                  onChange={(e) => setPoTipo(e.target.value as 'PASTORAL' | 'MOVIMENTO')}
                 
                >
                  <MenuItem value="PASTORAL">Pastoral</MenuItem>
                  <MenuItem value="MOVIMENTO">Movimento</MenuItem>
                </Select>
              </FormControl>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setPoModalOpen(false)} color="inherit">
              Cancelar
            </Button>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={createPOMutation.isPending || updatePOMutation.isPending}
              sx={{ fontWeight: 600 }}
            >
              {(createPOMutation.isPending || updatePOMutation.isPending) ? 'Salvando...' : 'Salvar'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
      <ImportModal
        open={isImportModalOpen}
        onClose={() => setIsImportModalOpen(false)}
      />
    </Container>
  );
};

export default Settings;
