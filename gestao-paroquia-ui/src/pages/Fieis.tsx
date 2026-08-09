import React, { useEffect, useState } from 'react';
import {
  Box,
  Checkbox,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  Grid,
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
import {
  Add as AddIcon,
  Close as CloseIcon,
  Edit as EditIcon,
  People as PeopleIcon,
  PersonAdd as PromoteIcon,
  Save as SaveIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { useAuth } from '../auth/AuthContext';
import { createFiel, getFieis, updateFiel } from '../api/fieis';
import { getOrganizacoes } from '../api/organizacoes';
import { promoverACoordenador } from '../api/membros';
import {
  CreateFielPayload,
  Fiel,
  Organizacao,
  UpdateFielPayload,
  Vocacao,
} from '../api/types';
import { useNotification } from '../context/NotificationContext';
import { extractErrorMessage } from '../utils/errorUtils';
import ActionButton from '../components/ActionButton';
import PageHeader from '../components/PageHeader';
import StatusIcon from '../components/StatusIcon';
import SacramentoIcon from '../components/SacramentoIcon';
import { SACRAMENTO_META } from '../utils/statusMeta/sacramentos';
import { getVocacaoMeta } from '../utils/statusMeta/vocacaoStatus';

const Fieis = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();

  const [fieis, setFieis] = useState<Fiel[]>([]);
  const [organizacoes, setOrganizacoes] = useState<Organizacao[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState<string>('');

  // Fiel Modal State
  const [modalOpen, setModalOpen] = useState<boolean>(false);
  const [editingFiel, setEditingFiel] = useState<Fiel | null>(null);
  const [formValues, setFormValues] = useState<CreateFielPayload>({
    nome: '',
    cpf: '',
    dataNascimento: '',
    email: '',
    telefone: '',
    endereco: '',
    vocacao: 'LEIGO',
    batizado: false,
    primeiraComunhao: false,
    crismado: false,
    casadoIgrejaCatolica: false,
  });

  // Promote Modal State
  const [promoteModalOpen, setPromoteModalOpen] = useState<boolean>(false);
  const [promotingFiel, setPromotingFiel] = useState<Fiel | null>(null);
  const [promoteValues, setPromoteValues] = useState({
    grupoParoquialId: '',
    papel: 'COORD_PASTORAL',
    username: '',
    password: '',
  });

  const isCoordinatorPromoter = () => {
    if (!user || !user.roles) return false;
    const allowed = ['ADMIN', 'PAROCO', 'VIGARIO', 'MESA_CPP', 'BISPO_DIOCESANO', 'SECRETARIO', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO'];
    return user.roles.some((r: string) => allowed.includes(r));
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const [fieisData, orgsData] = await Promise.all([
        getFieis(),
        getOrganizacoes(),
      ]);
      setFieis(fieisData);
      setOrganizacoes(orgsData);
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao carregar lista de fiéis'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleOpenCreateModal = () => {
    setEditingFiel(null);
    setFormValues({
      nome: '',
      cpf: '',
      dataNascimento: '',
      email: '',
      telefone: '',
      endereco: '',
      vocacao: 'LEIGO',
      batizado: false,
      primeiraComunhao: false,
      crismado: false,
      casadoIgrejaCatolica: false,
    });
    setModalOpen(true);
  };

  const handleOpenEditModal = (fiel: Fiel) => {
    setEditingFiel(fiel);
    setFormValues({
      nome: fiel.nome,
      cpf: fiel.cpf,
      dataNascimento: fiel.dataNascimento,
      email: fiel.email,
      telefone: fiel.telefone,
      endereco: fiel.endereco,
      vocacao: fiel.vocacao,
      batizado: fiel.batizado,
      primeiraComunhao: fiel.primeiraComunhao,
      crismado: fiel.crismado,
      casadoIgrejaCatolica: fiel.casadoIgrejaCatolica,
    });
    setModalOpen(true);
  };

  const handleSaveFiel = async () => {
    if (!formValues.nome || !formValues.cpf || !formValues.email) {
      showError('Preencha os campos obrigatórios (Nome, CPF, E-mail)');
      return;
    }

    try {
      if (editingFiel) {
        const updatePayload: UpdateFielPayload = { ...formValues };
        await updateFiel(editingFiel.id, updatePayload);
        showSuccess('Fiel atualizado com sucesso!');
      } else {
        await createFiel(formValues);
        showSuccess('Fiel cadastrado com sucesso!');
      }
      setModalOpen(false);
      loadData();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao salvar fiel'));
    }
  };

  const handleOpenPromoteModal = (fiel: Fiel) => {
    setPromotingFiel(fiel);
    setPromoteValues({
      grupoParoquialId: organizacoes.length > 0 ? organizacoes[0].id : '',
      papel: 'COORD_PASTORAL',
      username: '',
      password: '',
    });
    setPromoteModalOpen(true);
  };

  const handleExecutePromote = async () => {
    if (!promotingFiel || !promoteValues.grupoParoquialId) {
      showError('Selecione uma organização/grupo paroquial');
      return;
    }

    try {
      const res = await promoverACoordenador({
        fielId: promotingFiel.id,
        grupoParoquialId: promoteValues.grupoParoquialId,
        papel: promoteValues.papel,
        username: promoteValues.username || undefined,
        password: promoteValues.password || undefined,
      });

      showSuccess(
        res.novoUsuarioCriado
          ? 'Fiel promovido e conta de usuário criada com sucesso!'
          : 'Fiel promovido a coordenador com sucesso!'
      );
      setPromoteModalOpen(false);
      loadData();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao promover fiel'));
    }
  };

  const filteredFieis = fieis.filter(
    (f) =>
      f.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
      f.cpf.includes(searchTerm) ||
      f.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <PageHeader
        icon={<PeopleIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Gestão de Fiéis"
        subtitle="Cadastro unificado de paroquianos e dados sacramentais (LGPD)."
        actions={
          <ActionButton
            title="Novo Fiel"
            icon={<AddIcon />}
            color="primary"
            onClick={handleOpenCreateModal}
          />
        }
      />

      <Paper sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <SearchIcon color="action" />
          <TextField
            fullWidth
            size="small"
            placeholder="Buscar fiel por Nome, CPF ou E-mail..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </Box>
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead sx={{ backgroundColor: 'action.hover' }}>
            <TableRow>
              <TableCell>Nome</TableCell>
              <TableCell>CPF</TableCell>
              <TableCell>Vocação</TableCell>
              <TableCell>Contato</TableCell>
              <TableCell>Sacramentos</TableCell>
              <TableCell align="right">Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredFieis.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                  <Typography variant="body2" color="text.secondary">
                    {loading ? 'Carregando fiéis...' : 'Nenhum fiel encontrado.'}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredFieis.map((fiel) => (
                <TableRow key={fiel.id} hover>
                  <TableCell>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                      {fiel.nome}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {fiel.endereco}
                    </Typography>
                  </TableCell>
                  <TableCell>{fiel.cpf}</TableCell>
                  <TableCell>
                    <StatusIcon {...getVocacaoMeta(fiel.vocacao)} />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">{fiel.email}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {fiel.telefone}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap' }}>
                      {fiel.batizado && <SacramentoIcon {...SACRAMENTO_META.batizado} color="success" />}
                      {fiel.primeiraComunhao && <SacramentoIcon {...SACRAMENTO_META.primeiraComunhao} color="info" />}
                      {fiel.crismado && <SacramentoIcon {...SACRAMENTO_META.crismado} color="secondary" />}
                      {fiel.casadoIgrejaCatolica && <SacramentoIcon {...SACRAMENTO_META.casadoIgrejaCatolica} color="warning" />}
                    </Box>
                  </TableCell>
                  <TableCell align="right">
                    <ActionButton
                      title="Editar Fiel"
                      icon={<EditIcon fontSize="small" />}
                      size="small"
                      color="primary"
                      onClick={() => handleOpenEditModal(fiel)}
                    />

                    {isCoordinatorPromoter() && (
                      <ActionButton
                        title="Promover a Coordenador"
                        icon={<PromoteIcon fontSize="small" />}
                        size="small"
                        color="secondary"
                        onClick={() => handleOpenPromoteModal(fiel)}
                      />
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Modal de Cadastro / Edição de Fiel */}
      <Dialog open={modalOpen} onClose={() => setModalOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">
            {editingFiel ? 'Editar Fiel' : 'Cadastrar Fiel'}
          </Typography>
          <ActionButton title="Fechar formulário" icon={<CloseIcon />} size="small" onClick={() => setModalOpen(false)} />
        </DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ pt: 1 }}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Nome Completo"
                fullWidth
                required
                value={formValues.nome}
                onChange={(e) => setFormValues({ ...formValues, nome: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="CPF"
                fullWidth
                required
                value={formValues.cpf}
                onChange={(e) => setFormValues({ ...formValues, cpf: e.target.value })}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Data de Nascimento"
                type="date"
                fullWidth
                required
                slotProps={{ inputLabel: { shrink: true } }}
                value={formValues.dataNascimento}
                onChange={(e) => setFormValues({ ...formValues, dataNascimento: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth>
                <InputLabel id="vocacao-label">Vocação / Estado de Vida</InputLabel>
                <Select
                  labelId="vocacao-label"
                  label="Vocação / Estado de Vida"
                  value={formValues.vocacao}
                  onChange={(e) => setFormValues({ ...formValues, vocacao: e.target.value as Vocacao })}
                >
                  <MenuItem value="LEIGO">Leigo(a)</MenuItem>
                  <MenuItem value="SACERDOTE">Sacerdote</MenuItem>
                  <MenuItem value="DIACONO">Diácono</MenuItem>
                  <MenuItem value="RELIGIOSO">Religioso(a)</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="E-mail de Contato"
                type="email"
                fullWidth
                required
                value={formValues.email}
                onChange={(e) => setFormValues({ ...formValues, email: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Telefone / WhatsApp"
                fullWidth
                required
                value={formValues.telefone}
                onChange={(e) => setFormValues({ ...formValues, telefone: e.target.value })}
              />
            </Grid>

            <Grid size={{ xs: 12 }}>
              <TextField
                label="Endereço"
                fullWidth
                required
                value={formValues.endereco}
                onChange={(e) => setFormValues({ ...formValues, endereco: e.target.value })}
              />
            </Grid>

            <Grid size={{ xs: 12 }}>
              <Typography variant="subtitle2" sx={{ mt: 1, mb: 0.5 }}>
                Sacramentos Recebidos
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formValues.batizado}
                      onChange={(e) => setFormValues({ ...formValues, batizado: e.target.checked })}
                    />
                  }
                  label="Batizado"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formValues.primeiraComunhao}
                      onChange={(e) => setFormValues({ ...formValues, primeiraComunhao: e.target.checked })}
                    />
                  }
                  label="1ª Comunhão"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formValues.crismado}
                      onChange={(e) => setFormValues({ ...formValues, crismado: e.target.checked })}
                    />
                  }
                  label="Crismado"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formValues.casadoIgrejaCatolica}
                      onChange={(e) => setFormValues({ ...formValues, casadoIgrejaCatolica: e.target.checked })}
                    />
                  }
                  label="Casado pela Igreja Católica"
                />
              </Box>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <ActionButton title="Cancelar e fechar formulário" icon={<CloseIcon />} onClick={() => setModalOpen(false)} color="secondary" />
          <ActionButton title="Salvar dados do fiel" icon={<SaveIcon />} color="primary" onClick={handleSaveFiel} />
        </DialogActions>
      </Dialog>

      {/* Modal de Promoção a Coordenador */}
      <Dialog open={promoteModalOpen} onClose={() => setPromoteModalOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">
            Promover a Coordenador: {promotingFiel?.nome}
          </Typography>
          <ActionButton title="Fechar formulário" icon={<CloseIcon />} size="small" onClick={() => setPromoteModalOpen(false)} />
        </DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ pt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <FormControl fullWidth required>
                <InputLabel id="grupo-paroquial-label">Grupo Paroquial / Organização</InputLabel>
                <Select
                  labelId="grupo-paroquial-label"
                  label="Grupo Paroquial / Organização"
                  value={promoteValues.grupoParoquialId}
                  onChange={(e) => setPromoteValues({ ...promoteValues, grupoParoquialId: e.target.value })}
                >
                  {organizacoes.map((org) => (
                    <MenuItem key={org.id} value={org.id}>
                      {org.nome} ({org.tipo})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12 }}>
              <FormControl fullWidth required>
                <InputLabel id="papel-promocao-label">Papel de Liderança</InputLabel>
                <Select
                  labelId="papel-promocao-label"
                  label="Papel de Liderança"
                  value={promoteValues.papel}
                  onChange={(e) => setPromoteValues({ ...promoteValues, papel: e.target.value })}
                >
                  <MenuItem value="COORD_PASTORAL">Coordenador Pastoral</MenuItem>
                  <MenuItem value="COORD_MOVIMENTO">Coordenador de Movimento</MenuItem>
                  <MenuItem value="VICE_COORDENADOR">Vice-Coordenador</MenuItem>
                  <MenuItem value="SECRETARIO">Secretário</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Username (Login)"
                fullWidth
                placeholder="Ex: joao.silva"
                value={promoteValues.username}
                onChange={(e) => setPromoteValues({ ...promoteValues, username: e.target.value })}
                helperText="Se o Fiel não tiver conta, informe para criar um usuário."
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Senha Inicial"
                type="password"
                fullWidth
                value={promoteValues.password}
                onChange={(e) => setPromoteValues({ ...promoteValues, password: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <ActionButton title="Cancelar promoção" icon={<CloseIcon />} onClick={() => setPromoteModalOpen(false)} color="secondary" />
          <ActionButton title="Promover fiel para papel de liderança" icon={<PromoteIcon />} color="secondary" onClick={handleExecutePromote} />
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Fieis;
