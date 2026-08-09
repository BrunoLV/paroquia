import React, { useEffect, useMemo, useState } from 'react';
import {
  Box,
  Checkbox,
  Chip,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  Grid,
  InputAdornment,
  InputLabel,
  Menu,
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
} from '@mui/material';
import { FormattedCurrency } from '../components/FormattedCell';
import {
  Add as AddIcon,
  Build as BuildIcon,
  CheckCircle as CheckCircleIcon,
  Close as CloseIcon,
  SwapHoriz as TransferIcon,
  EventAvailable as AllocateIcon,
  EventBusy as DeallocateIcon,
  Inventory as InventoryIcon,
  MoreVert as MoreVertIcon,
  Save as SaveIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { useAuth } from '../auth/AuthContext';
import {
  canManageAtivos,
  canTransferAtivo,
  canAlocarAtivo,
  canManageManutencaoAtivo,
  isGlobalCreator,
} from '../auth/permissions';
import apiClient from '../api/client';
import {
  alocarAtivo,
  createAtivo,
  desalocarAtivo,
  finalizarManutencao,
  getAtivosPorOrganizacao,
  iniciarManutencao,
  transferirAtivo,
} from '../api/ativos';
import { listEventos } from '../api/eventos';
import { getOrganizacoes } from '../api/organizacoes';
import { AtivoCategoria, AtivoPatrimonial, Evento, Organizacao } from '../api/types';
import { useNotification } from '../context/NotificationContext';
import PageHeader from '../components/PageHeader';
import AtivoAlocacoesModal from '../components/AtivoAlocacoesModal';
import ActionButton from '../components/ActionButton';
import StatusIcon from '../components/StatusIcon';
import { getAtivoStatusMeta } from '../utils/statusMeta/ativoStatus';
import { getCategoriaAtivoMeta } from '../utils/statusMeta/categoriaAtivoStatus';
import { formatCurrencyBRL } from '../utils/currencyUtils';
import { extractErrorMessage } from '../utils/errorUtils';

const isOrganizacaoElegivelProprietaria = (org: Organizacao): boolean => org.tipo === 'MATRIZ' || org.tipo === 'COMUNIDADE';

const addDays = (date: Date, days: number): string => {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result.toISOString().slice(0, 10);
};

const formatEventoOption = (evento: Evento): string => {
  const inicio = evento.inicio ? new Date(evento.inicio).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }) : '';
  return inicio ? `${evento.titulo} — ${inicio}` : evento.titulo;
};

const CATEGORIAS: AtivoCategoria[] = ['ELETRONICO', 'MOBILIARIO', 'PARAMENTO', 'INFRAESTRUTURA'];

const Ativos = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();

  const [organizations, setOrganizations] = useState<Organizacao[]>([]);
  const [selectedOrgId, setSelectedOrgId] = useState('');
  const [ativos, setAtivos] = useState<AtivoPatrimonial[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<string>('TODAS');
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [formOrgId, setFormOrgId] = useState('');
  const [formNome, setFormNome] = useState('');
  const [formCategoria, setFormCategoria] = useState<AtivoCategoria>('ELETRONICO');
  const [formReservavel, setFormReservavel] = useState(false);
  const [formDataAquisicao, setFormDataAquisicao] = useState('');
  const [formValorEstimado, setFormValorEstimado] = useState('');

  const [transferDialogAtivo, setTransferDialogAtivo] = useState<AtivoPatrimonial | null>(null);
  const [transferNovaOrgId, setTransferNovaOrgId] = useState('');
  const [transferJustificativa, setTransferJustificativa] = useState('');

  const [manutencaoDialogAtivo, setManutencaoDialogAtivo] = useState<AtivoPatrimonial | null>(null);
  const [manutencaoDataInicio, setManutencaoDataInicio] = useState('');
  const [manutencaoDataFim, setManutencaoDataFim] = useState('');
  const [manutencaoMotivo, setManutencaoMotivo] = useState('');

  const [alocacoesDialogAtivo, setAlocacoesDialogAtivo] = useState<AtivoPatrimonial | null>(null);
  const [actionMenu, setActionMenu] = useState<{ anchorEl: HTMLElement; ativo: AtivoPatrimonial } | null>(null);

  const [alocarDialogAtivo, setAlocarDialogAtivo] = useState<AtivoPatrimonial | null>(null);
  const [alocarDataInicio, setAlocarDataInicio] = useState('');
  const [alocarDataFim, setAlocarDataFim] = useState('');
  const [alocarEventoId, setAlocarEventoId] = useState('');
  const [alocarJustificativa, setAlocarJustificativa] = useState('');
  const [eventosDisponiveis, setEventosDisponiveis] = useState<Evento[]>([]);
  const [isLoadingEventos, setIsLoadingEventos] = useState(false);

  const getSelectableOrgs = () => {
    if (isGlobalCreator(user)) {
      return organizations;
    }
    if (user && Array.isArray(user.organizations)) {
      const userOrgIds = user.organizations.map((o: { id: string }) => o.id);
      return organizations.filter((o) => userOrgIds.includes(o.id));
    }
    return [];
  };

  const getOrganizacoesElegiveisProprietarias = () => getSelectableOrgs().filter(isOrganizacaoElegivelProprietaria);

  const filteredAtivos = useMemo(() => {
    return ativos.filter((a) => {
      const search = searchTerm.trim().toLowerCase();
      const matchesSearch =
        !search ||
        a.nome.toLowerCase().includes(search) ||
        a.numeroTombamento.toLowerCase().includes(search);
      const matchesCategory = categoryFilter === 'TODAS' || a.categoria === categoryFilter;
      return matchesSearch && matchesCategory;
    });
  }, [ativos, searchTerm, categoryFilter]);

  const loadOrganizations = async () => {
    try {
      const orgs = await getOrganizacoes();
      setOrganizations(orgs || []);
    } catch (err) {
      console.error('Erro ao buscar organizações:', err);
      showError('Não foi possível carregar as organizações.');
    }
  };

  const loadAtivos = async (organizacaoId: string) => {
    if (!organizacaoId) {
      setAtivos([]);
      return;
    }
    setIsLoading(true);
    try {
      const data = await getAtivosPorOrganizacao(organizacaoId);
      setAtivos(data || []);
    } catch (err) {
      console.error('Erro ao buscar ativos:', err);
      showError('Não foi possível carregar os ativos patrimoniais.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadOrganizations();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!selectedOrgId && organizations.length > 0) {
      const elegiveis = getOrganizacoesElegiveisProprietarias();
      if (elegiveis.length > 0) {
        setSelectedOrgId(elegiveis[0].id);
      }
    }
  }, [organizations, selectedOrgId]);

  useEffect(() => {
    if (selectedOrgId) {
      loadAtivos(selectedOrgId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedOrgId]);

  const loadEventos = async (dataInicio: string, dataFim: string) => {
    if (!dataInicio || !dataFim) {
      setEventosDisponiveis([]);
      return;
    }
    setIsLoadingEventos(true);
    try {
      // Não filtra por organização: o ativo pode ser alocado a um evento de qualquer
      // organização (empréstimo entre organizações), não só da organização dona do ativo.
      const data = await listEventos({
        dataInicio: new Date(dataInicio).toISOString(),
        dataFim: new Date(`${dataFim}T23:59:59`).toISOString(),
      });
      setEventosDisponiveis(data || []);
    } catch (err) {
      console.error('Erro ao buscar eventos:', err);
      showError('Não foi possível carregar os eventos do período informado.');
      setEventosDisponiveis([]);
    } finally {
      setIsLoadingEventos(false);
    }
  };

  useEffect(() => {
    if (alocarDialogAtivo) {
      const inicio = addDays(new Date(), 0);
      const fim = addDays(new Date(), 30);
      setAlocarDataInicio(inicio);
      setAlocarDataFim(fim);
      loadEventos(inicio, fim);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [alocarDialogAtivo]);

  const closeAlocarDialog = () => {
    setAlocarDialogAtivo(null);
    setAlocarEventoId('');
    setAlocarJustificativa('');
    setEventosDisponiveis([]);
  };

  const handleAlocarPeriodoChange = (dataInicio: string, dataFim: string) => {
    setAlocarDataInicio(dataInicio);
    setAlocarDataFim(dataFim);
    setAlocarEventoId('');
    loadEventos(dataInicio, dataFim);
  };



  const resetCreateForm = () => {
    setFormOrgId('');
    setFormNome('');
    setFormCategoria('ELETRONICO');
    setFormReservavel(false);
    setFormDataAquisicao('');
    setFormValorEstimado('');
  };

  const openCreateDialog = () => {
    const eligibleOrgs = getOrganizacoesElegiveisProprietarias();
    const defaultOrgId = eligibleOrgs.some((org) => org.id === selectedOrgId)
      ? selectedOrgId
      : (eligibleOrgs[0]?.id || '');
    setFormOrgId(defaultOrgId);
    setCreateDialogOpen(true);
  };

  const handleCreate = async () => {
    if (!formNome.trim()) {
      showError('Informe o nome do ativo.');
      return;
    }
    if (!formOrgId) {
      showError('Selecione uma organização (Matriz ou Comunidade) antes de cadastrar um ativo.');
      return;
    }
    try {
      await createAtivo({
        nome: formNome,
        categoria: formCategoria,
        organizacaoId: formOrgId,
        reservavel: formReservavel,
        dataAquisicao: formDataAquisicao || null,
        valorEstimado: formValorEstimado ? Number(formValorEstimado) : null,
      });
      showSuccess('Ativo cadastrado com sucesso!');
      setCreateDialogOpen(false);
      resetCreateForm();
      if (formOrgId === selectedOrgId) {
        loadAtivos(selectedOrgId);
      }
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao cadastrar ativo.'));
    }
  };

  const handleTransferir = async () => {
    if (!transferDialogAtivo) return;
    if (!transferNovaOrgId) {
      showError('Selecione a organização de destino.');
      return;
    }
    if (transferJustificativa.trim().length < 10) {
      showError('A justificativa deve ter pelo menos 10 caracteres.');
      return;
    }
    try {
      await transferirAtivo(transferDialogAtivo.id, {
        novaOrganizacaoId: transferNovaOrgId,
        justificativa: transferJustificativa,
      });
      showSuccess('Ativo transferido com sucesso!');
      setTransferDialogAtivo(null);
      setTransferNovaOrgId('');
      setTransferJustificativa('');
      loadAtivos(selectedOrgId);
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao transferir ativo.'));
    }
  };

  const handleAlocar = async () => {
    if (!alocarDialogAtivo) return;
    if (alocarJustificativa.trim().length < 10) {
      showError('A justificativa de utilização deve ter pelo menos 10 caracteres.');
      return;
    }
    try {
      const resultado = await alocarAtivo(alocarDialogAtivo.id, {
        eventoId: alocarEventoId || null,
        justificativaAlocacao: alocarJustificativa,
        dataInicio: alocarDataInicio,
        dataFim: alocarDataFim,
      });
      if (resultado.kind === 'PENDENTE') {
        showSuccess('Empréstimo solicitado — aguardando aprovação do responsável pela organização dona do ativo.');
      } else {
        showSuccess('Ativo alocado com sucesso!');
      }
      closeAlocarDialog();
      loadAtivos(selectedOrgId);
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao alocar ativo.'));
    }
  };

  const closeManutencaoDialog = () => {
    setManutencaoDialogAtivo(null);
    setManutencaoDataInicio('');
    setManutencaoDataFim('');
    setManutencaoMotivo('');
  };

  const openManutencaoDialog = (ativo: AtivoPatrimonial) => {
    setManutencaoDialogAtivo(ativo);
    setManutencaoDataInicio(addDays(new Date(), 0));
    setManutencaoDataFim(addDays(new Date(), 7));
    setManutencaoMotivo('');
  };

  const handleIniciarManutencao = async () => {
    if (!manutencaoDialogAtivo) return;
    if (manutencaoMotivo.trim().length < 10) {
      showError('O motivo da manutenção deve ter pelo menos 10 caracteres.');
      return;
    }
    try {
      await iniciarManutencao(manutencaoDialogAtivo.id, {
        dataInicio: manutencaoDataInicio,
        dataFim: manutencaoDataFim,
        motivo: manutencaoMotivo,
      });
      showSuccess('Manutenção iniciada com sucesso!');
      closeManutencaoDialog();
      loadAtivos(selectedOrgId);
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao iniciar manutenção.'));
    }
  };

  const handleFinalizarManutencao = async (ativo: AtivoPatrimonial) => {
    try {
      await finalizarManutencao(ativo.id);
      showSuccess('Manutenção finalizada — ativo disponível novamente.');
      loadAtivos(selectedOrgId);
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao finalizar manutenção.'));
    }
  };

  const handleDesalocar = async (ativo: AtivoPatrimonial) => {
    try {
      await desalocarAtivo(ativo.id);
      showSuccess('Ativo desalocado — devolvido para a organização dona.');
      loadAtivos(selectedOrgId);
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao desalocar ativo.'));
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <PageHeader
        icon={<InventoryIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Ativos Patrimoniais"
        subtitle="Gerencie o cadastro, transferência e alocação de ativos patrimoniais da paróquia."
        actions={
          canManageAtivos(user) && (
            <ActionButton
              title="Novo Ativo"
              icon={<AddIcon />}
              color="primary"
              disabled={!selectedOrgId}
              onClick={openCreateDialog}
              sx={{ border: '1px solid', borderColor: 'primary.main' }}
            />
          )
        }
      />

      <Paper sx={{ p: 2.5, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid size={{ xs: 12, md: 4 }}>
            <FormControl fullWidth size="small">
              <InputLabel id="organizacao-select-label">Organização</InputLabel>
              <Select
                labelId="organizacao-select-label"
                label="Organização"
                value={selectedOrgId}
                onChange={(e) => setSelectedOrgId(e.target.value as string)}
              >
                {getOrganizacoesElegiveisProprietarias().map((org) => (
                  <MenuItem key={org.id} value={org.id}>
                    {org.nome}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <FormControl fullWidth size="small">
              <InputLabel id="categoria-select-label">Categoria</InputLabel>
              <Select
                labelId="categoria-select-label"
                label="Categoria"
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value as string)}
              >
                <MenuItem value="TODAS">Todas as Categorias</MenuItem>
                {CATEGORIAS.map((cat) => (
                  <MenuItem key={cat} value={cat}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <StatusIcon {...getCategoriaAtivoMeta(cat)} />
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              size="small"
              placeholder="Buscar por Nome ou Tombamento..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setPage(0);
              }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon fontSize="small" color="action" />
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Grid>
        </Grid>
      </Paper>

      {isLoading ? (
        <Box>
          <Skeleton variant="rounded" height={56} sx={{ mb: 1 }} />
          <Skeleton variant="rounded" height={56} sx={{ mb: 1 }} />
          <Skeleton variant="rounded" height={56} sx={{ mb: 1 }} />
          <Skeleton variant="rounded" height={56} sx={{ mb: 1 }} />
        </Box>
      ) : (
        <>
          <TableContainer component={Paper}>
            <Table>
              <TableHead sx={{ backgroundColor: 'action.hover' }}>
                <TableRow>
                  <TableCell>Nome</TableCell>
                  <TableCell>Tombamento</TableCell>
                  <TableCell>Categoria</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Valor Estimado</TableCell>
                  <TableCell align="right">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredAtivos
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((ativo) => (
                    <TableRow key={ativo.id} hover>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                            {ativo.nome}
                          </Typography>
                          {ativo.reservavel && (
                            <Chip
                              size="small"
                              label="Reservável"
                              color="primary"
                              variant="outlined"
                              sx={{ height: 20, fontSize: '0.68rem' }}
                            />
                          )}
                        </Box>
                      </TableCell>
                      <TableCell>{ativo.numeroTombamento}</TableCell>
                      <TableCell>
                        <StatusIcon {...getCategoriaAtivoMeta(ativo.categoria)} />
                      </TableCell>
                      <TableCell>
                        <StatusIcon {...getAtivoStatusMeta(ativo.status)} />
                      </TableCell>
                      <TableCell align="right">
                        <FormattedCurrency value={ativo.valorEstimado} />
                      </TableCell>
                      <TableCell align="right">
                        <ActionButton
                          title="Opções e Ações"
                          ariaLabel="Ações"
                          icon={<MoreVertIcon />}
                          size="small"
                          onClick={(e) => setActionMenu({ anchorEl: e.currentTarget, ativo })}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                {filteredAtivos.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                      <Typography variant="body2" color="text.secondary">
                        {ativos.length === 0
                          ? 'Nenhum ativo cadastrado para esta organização.'
                          : 'Nenhum ativo encontrado com os filtros aplicados.'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            rowsPerPageOptions={[10, 20, 50, 100]}
            component="div"
            count={filteredAtivos.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
            }}
            labelRowsPerPage="Itens por página:"
          />
        </>
      )}

      <Menu
        anchorEl={actionMenu?.anchorEl}
        open={!!actionMenu}
        onClose={() => setActionMenu(null)}
      >
        {actionMenu && canTransferAtivo(user, actionMenu.ativo.organizacaoId) && (
          <MenuItem
            onClick={() => {
              setTransferDialogAtivo(actionMenu.ativo);
              setActionMenu(null);
            }}
          >
            <TransferIcon fontSize="small" color="action" sx={{ mr: 1.5 }} />
            Transferir
          </MenuItem>
        )}
        {actionMenu && canAlocarAtivo(user, actionMenu.ativo.organizacaoId) && actionMenu.ativo.status === 'DISPONIVEL' && (
          <MenuItem
            onClick={() => {
              setAlocarDialogAtivo(actionMenu.ativo);
              setActionMenu(null);
            }}
          >
            <AllocateIcon fontSize="small" color="primary" sx={{ mr: 1.5 }} />
            Alocar
          </MenuItem>
        )}
        {actionMenu && canAlocarAtivo(user, actionMenu.ativo.organizacaoId) && actionMenu.ativo.status === 'ALOCADO' && (
          <MenuItem
            onClick={() => {
              handleDesalocar(actionMenu.ativo);
              setActionMenu(null);
            }}
          >
            <DeallocateIcon fontSize="small" color="warning" sx={{ mr: 1.5 }} />
            Desalocar
          </MenuItem>
        )}
        {actionMenu && canManageManutencaoAtivo(user, actionMenu.ativo.organizacaoId) && actionMenu.ativo.status === 'DISPONIVEL' && (
          <MenuItem
            onClick={() => {
              openManutencaoDialog(actionMenu.ativo);
              setActionMenu(null);
            }}
          >
            <BuildIcon fontSize="small" color="warning" sx={{ mr: 1.5 }} />
            Iniciar Manutenção
          </MenuItem>
        )}
        {actionMenu && canManageManutencaoAtivo(user, actionMenu.ativo.organizacaoId) && actionMenu.ativo.status === 'EM_MANUTENCAO' && (
          <MenuItem
            onClick={() => {
              handleFinalizarManutencao(actionMenu.ativo);
              setActionMenu(null);
            }}
          >
            <CheckCircleIcon fontSize="small" color="success" sx={{ mr: 1.5 }} />
            Finalizar Manutenção
          </MenuItem>
        )}
        {actionMenu && (
          <MenuItem
            onClick={() => {
              setAlocacoesDialogAtivo(actionMenu.ativo);
              setActionMenu(null);
            }}
          >
            <InventoryIcon fontSize="small" color="info" sx={{ mr: 1.5 }} />
            Ver Alocações
          </MenuItem>
        )}
      </Menu>

      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Novo Ativo Patrimonial</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <FormControl fullWidth size="small" required>
              <InputLabel id="create-organizacao-select-label">Organização Dona</InputLabel>
              <Select
                labelId="create-organizacao-select-label"
                label="Organização Dona"
                value={formOrgId}
                onChange={(e) => setFormOrgId(e.target.value as string)}
              >
                {getOrganizacoesElegiveisProprietarias().map((org) => (
                  <MenuItem key={org.id} value={org.id}>
                    {org.nome}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="Nome"
              value={formNome}
              onChange={(e) => setFormNome(e.target.value)}
              fullWidth
              required
              size="small"
            />
            <FormControl fullWidth size="small">
              <InputLabel id="categoria-select-label">Categoria</InputLabel>
              <Select
                labelId="categoria-select-label"
                label="Categoria"
                value={formCategoria}
                onChange={(e) => setFormCategoria(e.target.value as AtivoCategoria)}
              >
                {CATEGORIAS.map((categoria) => (
                  <MenuItem key={categoria} value={categoria}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <StatusIcon {...getCategoriaAtivoMeta(categoria)} />
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControlLabel
              control={<Checkbox checked={formReservavel} onChange={(e) => setFormReservavel(e.target.checked)} />}
              label="Reservável"
            />
            <TextField
              label="Data de Aquisição"
              type="date"
              value={formDataAquisicao}
              onChange={(e) => setFormDataAquisicao(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              fullWidth
              size="small"
            />
            <TextField
              label="Valor Estimado"
              type="number"
              value={formValorEstimado}
              onChange={(e) => setFormValorEstimado(e.target.value)}
              fullWidth
              size="small"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={() => setCreateDialogOpen(false)} />
          <ActionButton title="Salvar Ativo" icon={<SaveIcon />} color="primary" onClick={handleCreate} />
        </DialogActions>
      </Dialog>

      <Dialog open={!!transferDialogAtivo} onClose={() => setTransferDialogAtivo(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Transferir Ativo</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <FormControl fullWidth size="small">
              <InputLabel id="nova-organizacao-select-label">Nova Organização</InputLabel>
              <Select
                labelId="nova-organizacao-select-label"
                label="Nova Organização"
                value={transferNovaOrgId}
                onChange={(e) => setTransferNovaOrgId(e.target.value as string)}
              >
                {organizations.filter(isOrganizacaoElegivelProprietaria).map((org) => (
                  <MenuItem key={org.id} value={org.id}>
                    {org.nome}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="Justificativa"
              multiline
              rows={3}
              value={transferJustificativa}
              onChange={(e) => setTransferJustificativa(e.target.value)}
              fullWidth
              required
              size="small"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={() => setTransferDialogAtivo(null)} />
          <ActionButton title="Transferir Ativo" icon={<TransferIcon />} color="primary" onClick={handleTransferir} />
        </DialogActions>
      </Dialog>

      <Dialog open={!!alocarDialogAtivo} onClose={closeAlocarDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Alocar / Reservar Ativo</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Período - Início"
                  type="date"
                  value={alocarDataInicio}
                  onChange={(e) => handleAlocarPeriodoChange(e.target.value, alocarDataFim)}
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                  size="small"
                />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Período - Fim"
                  type="date"
                  value={alocarDataFim}
                  onChange={(e) => handleAlocarPeriodoChange(alocarDataInicio, e.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                  size="small"
                />
              </Grid>
            </Grid>
            <FormControl fullWidth disabled={isLoadingEventos} size="small">
              <InputLabel id="evento-select-label">Evento (opcional)</InputLabel>
              <Select
                labelId="evento-select-label"
                label="Evento (opcional)"
                value={alocarEventoId}
                onChange={(e) => setAlocarEventoId(e.target.value as string)}
              >
                <MenuItem value="">
                  <em>Nenhum — apenas reservar o ativo</em>
                </MenuItem>
                {eventosDisponiveis.map((evento) => (
                  <MenuItem key={evento.id} value={evento.id}>
                    {formatEventoOption(evento)}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Typography variant="body2" color="text.secondary">
              Não é necessário vincular a um evento — o ativo pode ser reservado para o período informado
              apenas com a justificativa. Se o evento escolhido pertencer a outra organização, a alocação
              se torna um empréstimo e fica pendente de aprovação.
            </Typography>
            {!isLoadingEventos && eventosDisponiveis.length === 0 && (
              <Typography variant="body2" color="text.secondary">
                Nenhum evento encontrado neste período.
              </Typography>
            )}
            <TextField
              label="Justificativa de Utilização"
              multiline
              rows={3}
              value={alocarJustificativa}
              onChange={(e) => setAlocarJustificativa(e.target.value)}
              fullWidth
              required
              size="small"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={closeAlocarDialog} />
          <ActionButton title="Alocar Ativo" icon={<AllocateIcon />} color="primary" onClick={handleAlocar} />
        </DialogActions>
      </Dialog>

      <Dialog open={!!manutencaoDialogAtivo} onClose={closeManutencaoDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Iniciar Manutenção</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Período - Início"
                  type="date"
                  value={manutencaoDataInicio}
                  onChange={(e) => setManutencaoDataInicio(e.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                  size="small"
                />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Período - Fim"
                  type="date"
                  value={manutencaoDataFim}
                  onChange={(e) => setManutencaoDataFim(e.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                  size="small"
                />
              </Grid>
            </Grid>
            <TextField
              label="Motivo da Manutenção"
              multiline
              rows={3}
              value={manutencaoMotivo}
              onChange={(e) => setManutencaoMotivo(e.target.value)}
              fullWidth
              required
              size="small"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={closeManutencaoDialog} />
          <ActionButton title="Iniciar Manutenção" icon={<BuildIcon />} color="primary" onClick={handleIniciarManutencao} />
        </DialogActions>
      </Dialog>

      <AtivoAlocacoesModal
        ativoId={alocacoesDialogAtivo?.id || null}
        ativoNome={alocacoesDialogAtivo?.nome}
        onClose={() => setAlocacoesDialogAtivo(null)}
        onChanged={() => loadAtivos(selectedOrgId)}
      />
    </Container>
  );
};

export default Ativos;
