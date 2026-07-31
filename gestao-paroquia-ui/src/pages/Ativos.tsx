import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
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
} from '@mui/material';
import { Add as AddIcon, SwapHoriz as TransferIcon, EventAvailable as AllocateIcon } from '@mui/icons-material';
import { useAuth } from '../auth/AuthContext';
import { canManageAtivos, canTransferAtivo, canAlocarAtivo, isGlobalCreator } from '../auth/permissions';
import apiClient from '../api/client';
import { alocarAtivo, createAtivo, getAtivosPorOrganizacao, transferirAtivo } from '../api/ativos';
import { AtivoCategoria, AtivoPatrimonial, Organizacao } from '../api/types';
import { useNotification } from '../context/NotificationContext';

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
  const [formNome, setFormNome] = useState('');
  const [formCategoria, setFormCategoria] = useState<AtivoCategoria>('ELETRONICO');
  const [formReservavel, setFormReservavel] = useState(false);
  const [formDataAquisicao, setFormDataAquisicao] = useState('');
  const [formValorEstimado, setFormValorEstimado] = useState('');

  const [transferDialogAtivo, setTransferDialogAtivo] = useState<AtivoPatrimonial | null>(null);
  const [transferNovaOrgId, setTransferNovaOrgId] = useState('');
  const [transferJustificativa, setTransferJustificativa] = useState('');

  const [alocarDialogAtivo, setAlocarDialogAtivo] = useState<AtivoPatrimonial | null>(null);
  const [alocarEventoId, setAlocarEventoId] = useState('');
  const [alocarJustificativa, setAlocarJustificativa] = useState('');

  const getSelectableOrgs = () => {
    if (isGlobalCreator(user)) {
      return organizations;
    }
    if (user && Array.isArray(user.organizations)) {
      return user.organizations as unknown as Organizacao[];
    }
    return [];
  };

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
    setFormNome('');
    setFormCategoria('ELETRONICO');
    setFormReservavel(false);
    setFormDataAquisicao('');
    setFormValorEstimado('');
  };

  const handleCreate = async () => {
    if (!formNome.trim()) {
      showError('Informe o nome do ativo.');
      return;
    }
    if (!selectedOrgId) {
      showError('Selecione uma organização antes de cadastrar um ativo.');
      return;
    }
    try {
      await createAtivo({
        nome: formNome,
        categoria: formCategoria,
        organizacaoId: selectedOrgId,
        reservavel: formReservavel,
        dataAquisicao: formDataAquisicao || null,
        valorEstimado: formValorEstimado ? Number(formValorEstimado) : null,
      });
      showSuccess('Ativo cadastrado com sucesso!');
      setCreateDialogOpen(false);
      resetCreateForm();
      loadAtivos(selectedOrgId);
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
    if (!alocarEventoId.trim()) {
      showError('Informe o evento para o qual o ativo será alocado.');
      return;
    }
    if (alocarJustificativa.trim().length < 10) {
      showError('A justificativa de utilização deve ter pelo menos 10 caracteres.');
      return;
    }
    try {
      await alocarAtivo(alocarDialogAtivo.id, {
        eventoId: alocarEventoId,
        justificativaAlocacao: alocarJustificativa,
      });
      showSuccess('Ativo alocado com sucesso!');
      setAlocarDialogAtivo(null);
      setAlocarEventoId('');
      setAlocarJustificativa('');
      loadAtivos(selectedOrgId);
    } catch (err: any) {
      showError(extractErrorMessage(err, 'Erro ao alocar ativo.'));
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Ativos Patrimoniais</Typography>
        {canManageAtivos(user) && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            disabled={!selectedOrgId}
            onClick={() => setCreateDialogOpen(true)}
          >
            Novo Ativo
          </Button>
        )}
      </Box>

      <FormControl sx={{ minWidth: 280, mb: 3 }}>
        <InputLabel id="organizacao-select-label">Organização</InputLabel>
        <Select
          labelId="organizacao-select-label"
          label="Organização"
          value={selectedOrgId}
          onChange={(e) => setSelectedOrgId(e.target.value as string)}
        >
          {getSelectableOrgs().map((org) => (
            <MenuItem key={org.id} value={org.id}>
              {org.nome}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
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
                  <TableCell align="right">
                    {canTransferAtivo(user, ativo.organizacaoId) && (
                      <Button
                        size="small"
                        startIcon={<TransferIcon />}
                        onClick={() => setTransferDialogAtivo(ativo)}
                      >
                        Transferir
                      </Button>
                    )}
                    {canAlocarAtivo(user, ativo.organizacaoId) && ativo.status === 'DISPONIVEL' && (
                      <Button
                        size="small"
                        startIcon={<AllocateIcon />}
                        onClick={() => setAlocarDialogAtivo(ativo)}
                      >
                        Alocar
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {ativos.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    Nenhum ativo cadastrado para esta organização.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Novo Ativo Patrimonial</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          <TextField label="Nome" value={formNome} onChange={(e) => setFormNome(e.target.value)} fullWidth />
          <FormControl fullWidth>
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
          />
          <TextField
            label="Valor Estimado"
            type="number"
            value={formValorEstimado}
            onChange={(e) => setFormValorEstimado(e.target.value)}
            fullWidth
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleCreate}>
            Salvar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!transferDialogAtivo} onClose={() => setTransferDialogAtivo(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Transferir Ativo</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          <FormControl fullWidth>
            <InputLabel id="nova-organizacao-select-label">Nova Organização</InputLabel>
            <Select
              labelId="nova-organizacao-select-label"
              label="Nova Organização"
              value={transferNovaOrgId}
              onChange={(e) => setTransferNovaOrgId(e.target.value as string)}
            >
              {organizations.map((org) => (
                <MenuItem key={org.id} value={org.id}>
                  {org.nome}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            label="Justificativa"
            multiline
            minRows={2}
            value={transferJustificativa}
            onChange={(e) => setTransferJustificativa(e.target.value)}
            fullWidth
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTransferDialogAtivo(null)}>Cancelar</Button>
          <Button variant="contained" onClick={handleTransferir}>
            Transferir
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!alocarDialogAtivo} onClose={() => setAlocarDialogAtivo(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Alocar Ativo para Evento</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          <TextField
            label="ID do Evento"
            value={alocarEventoId}
            onChange={(e) => setAlocarEventoId(e.target.value)}
            fullWidth
          />
          <TextField
            label="Justificativa de Utilização"
            multiline
            minRows={2}
            value={alocarJustificativa}
            onChange={(e) => setAlocarJustificativa(e.target.value)}
            fullWidth
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAlocarDialogAtivo(null)}>Cancelar</Button>
          <Button variant="contained" onClick={handleAlocar}>
            Alocar
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Ativos;
