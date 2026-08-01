import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
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
  IconButton,
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
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import {
  Add as AddIcon,
  SwapHoriz as TransferIcon,
  EventAvailable as AllocateIcon,
  Inventory as InventoryIcon,
  MoreVert as MoreVertIcon,
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
import { AtivoCategoria, AtivoPatrimonial, Evento, Organizacao } from '../api/types';
import { useNotification } from '../context/NotificationContext';
import PageHeader from '../components/PageHeader';
import AtivoAlocacoesModal from '../components/AtivoAlocacoesModal';
import { formatCurrencyBRL } from '../utils/currencyUtils';

const isOwnerEligibleOrg = (org: Organizacao): boolean => org.tipo === 'MATRIZ' || org.tipo === 'COMUNIDADE';

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

const STATUS_COLORS: Record<string, 'success' | 'warning' | 'default' | 'info'> = {
  DISPONIVEL: 'success',
  ALOCADO: 'info',
  EM_MANUTENCAO: 'warning',
  BAIXADO: 'default',
};

const Ativos = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();

  const [organizations, setOrganizations] = useState<Organizacao[]>([]);
  const [selectedOrgId, setSelectedOrgId] = useState('');
  const [ativos, setAtivos] = useState<AtivoPatrimonial[]>([]);
  const [isLoading, setIsLoading] = useState(false);

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
      return user.organizations as unknown as Organizacao[];
    }
    return [];
  };

  const getOwnerEligibleOrgs = () => getSelectableOrgs().filter(isOwnerEligibleOrg);

  const loadOrganizations = async () => {
    try {
      const response = await apiClient.get<Organizacao[]>('/organizacoes');
      setOrganizations(response.data || []);
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

  const extractErrorMessage = (err: any, fallback: string): string => {
    if (err.response?.data) {
      const data = err.response.data;
      if (Array.isArray(data.errors) && data.errors.length > 0) {
        return 'Por favor, corrija os seguintes erros:\n' + data.errors.map((e: any) => `• ${e.message || e.defaultMessage}`).join('\n');
      }
      if (data.message) {
        return data.message;
      }
    }
    return fallback;
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
    const eligibleOrgs = getOwnerEligibleOrgs();
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
    } catch (err: any) {
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
    } catch (err: any) {
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
    } catch (err: any) {
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
    } catch (err: any) {
      showError(extractErrorMessage(err, 'Erro ao iniciar manutenção.'));
    }
  };

  const handleFinalizarManutencao = async (ativo: AtivoPatrimonial) => {
    try {
      await finalizarManutencao(ativo.id);
      showSuccess('Manutenção finalizada — ativo disponível novamente.');
      loadAtivos(selectedOrgId);
    } catch (err: any) {
      showError(extractErrorMessage(err, 'Erro ao finalizar manutenção.'));
    }
  };

  const handleDesalocar = async (ativo: AtivoPatrimonial) => {
    try {
      await desalocarAtivo(ativo.id);
      showSuccess('Ativo desalocado — devolvido para a organização dona.');
      loadAtivos(selectedOrgId);
    } catch (err: any) {
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
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              disabled={!selectedOrgId}
              onClick={openCreateDialog}
            >
              Novo Ativo
            </Button>
          )
        }
      />

      <FormControl sx={{ minWidth: 280, mb: 3 }}>
        <InputLabel id="organizacao-select-label">Organização</InputLabel>
        <Select
          labelId="organizacao-select-label"
          label="Organização"
          value={selectedOrgId}
          onChange={(e) => setSelectedOrgId(e.target.value as string)}
        >
          {getOwnerEligibleOrgs().map((org) => (
            <MenuItem key={org.id} value={org.id}>
              {org.nome}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {isLoading ? (
        <Box>
          <Skeleton variant="rounded" height={56} sx={{ mb: 1 }} />
          <Skeleton variant="rounded" height={56} sx={{ mb: 1 }} />
          <Skeleton variant="rounded" height={56} sx={{ mb: 1 }} />
        </Box>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
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
              {ativos.map((ativo) => (
                <TableRow key={ativo.id}>
                  <TableCell>{ativo.nome}</TableCell>
                  <TableCell>{ativo.numeroTombamento}</TableCell>
                  <TableCell>{ativo.categoria}</TableCell>
                  <TableCell>
                    <Chip label={ativo.status} color={STATUS_COLORS[ativo.status] || 'default'} size="small" />
                  </TableCell>
                  <TableCell align="right">{formatCurrencyBRL(ativo.valorEstimado)}</TableCell>
                  <TableCell align="right">
                    <IconButton
                      size="small"
                      aria-label="Ações"
                      onClick={(e) => setActionMenu({ anchorEl: e.currentTarget, ativo })}
                    >
                      <MoreVertIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {ativos.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    Nenhum ativo cadastrado para esta organização.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
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
            <TransferIcon fontSize="small" sx={{ mr: 1 }} />
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
            <AllocateIcon fontSize="small" sx={{ mr: 1 }} />
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
            <InventoryIcon fontSize="small" sx={{ mr: 1 }} />
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
                {getOwnerEligibleOrgs().map((org) => (
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
                    {categoria}
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
          <Button onClick={() => setCreateDialogOpen(false)} color="secondary">Cancelar</Button>
          <Button variant="contained" onClick={handleCreate}>
            Salvar
          </Button>
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
                {organizations.filter(isOwnerEligibleOrg).map((org) => (
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
          <Button onClick={() => setTransferDialogAtivo(null)} color="secondary">Cancelar</Button>
          <Button variant="contained" onClick={handleTransferir}>
            Transferir
          </Button>
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
          <Button onClick={closeAlocarDialog} color="secondary">Cancelar</Button>
          <Button variant="contained" onClick={handleAlocar}>
            Alocar
          </Button>
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
          <Button onClick={closeManutencaoDialog} color="secondary">Cancelar</Button>
          <Button variant="contained" onClick={handleIniciarManutencao}>
            Salvar
          </Button>
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
