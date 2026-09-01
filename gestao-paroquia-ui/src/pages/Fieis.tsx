import React, { useEffect, useState } from 'react';
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Button,
  Checkbox,
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
  MenuItem,
  Paper,
  Select,
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
  Tooltip,
  Typography,
} from '@mui/material';
import { FormattedAddress, FormattedEmail, FormattedPhone } from '../components/FormattedCell';
import { formatCep, formatPhone, formatRg } from '../utils/masks';
import { formatCpf } from '../utils/cpfUtils';
import {
  Add as AddIcon,
  Close as CloseIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  ExpandMore as ExpandMoreIcon,
  FamilyRestroom as FamilyIcon,
  Gavel as LgpdIcon,
  LocationOn as LocationIcon,
  People as PeopleIcon,
  PersonAdd as PromoteIcon,
  Save as SaveIcon,
  Search as SearchIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
} from '@mui/icons-material';
import { useAuth } from '../auth/AuthContext';
import { createFiel, getFieis, updateFiel } from '../api/fieis';
import { getOrganizacoes } from '../api/organizacoes';
import { promoverACoordenador } from '../api/membros';
import {
  CreateFielPayload,
  Dependente,
  EstadoCivil,
  Fiel,
  Organizacao,
  Parentesco,
  PreferenciaCanalComunicacao,
  Sexo,
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

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function CustomTabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} id={`fiel-tabpanel-${index}`} {...other}>
      {value === index && <Box sx={{ p: 2 }}>{children}</Box>}
    </div>
  );
}

const Fieis = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();

  const [fieis, setFieis] = useState<Fiel[]>([]);
  const [organizacoes, setOrganizacoes] = useState<Organizacao[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [showMaskedPii, setShowMaskedPii] = useState<boolean>(true);

  // Modal State
  const [modalOpen, setModalOpen] = useState<boolean>(false);
  const [activeTab, setActiveTab] = useState<number>(0);
  const [editingFiel, setEditingFiel] = useState<Fiel | null>(null);

  // Form State
  const [formValues, setFormValues] = useState<CreateFielPayload>({
    nome: '',
    cpf: '',
    rg: '',
    dataNascimento: '',
    sexo: 'NAO_INFORMADO',
    profissao: '',
    email: '',
    telefone: '',
    telefoneFixo: '',
    celularWhatsapp: '',
    preferenciaCanalComunicacao: 'WHATSAPP',
    endereco: {
      logradouro: '',
      numero: '',
      complemento: '',
      bairro: '',
      cep: '',
      cidade: '',
      estado: '',
    },
    estadoCivil: 'SOLTEIRO',
    conjugeId: '',
    vocacao: 'LEIGO',
    batizado: false,
    primeiraComunhao: false,
    crismado: false,
    casadoIgrejaCatolica: false,
    vidaSacramental: {
      batizado: false,
      dataBatismo: '',
      paroquiaBatismo: '',
      celebranteBatismo: '',
      livroBatismo: '',
      folhaBatismo: '',
      termoBatismo: '',
      padrinhosBatismo: '',
      primeiraComunhao: false,
      dataPrimeiraComunhao: '',
      paroquiaPrimeiraComunhao: '',
      turmaCatequese: '',
      crismado: false,
      dataCrisma: '',
      paroquiaCrisma: '',
      celebranteCrisma: '',
      padrinhoCrisma: '',
      casadoIgrejaCatolica: false,
      dataMatrimonio: '',
      paroquiaMatrimonio: '',
      numeroProcessoMatrimonial: '',
      certidaoMatrimonio: '',
    },
    dependentes: [],
    habilidadesVoluntariado: '',
    consentimentoLgpd: {
      termoConsentimentoAceito: true,
      dataHoraConsentimentoUtc: new Date().toISOString(),
      versaoTermoConsentimento: 'v1.0',
      usoImagemAutorizado: false,
      permitirDivulgacaoAniversario: true,
    },
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

  const resetForm = () => {
    setActiveTab(0);
    setFormValues({
      nome: '',
      cpf: '',
      rg: '',
      dataNascimento: '',
      sexo: 'NAO_INFORMADO',
      profissao: '',
      email: '',
      telefone: '',
      telefoneFixo: '',
      celularWhatsapp: '',
      preferenciaCanalComunicacao: 'WHATSAPP',
      endereco: {
        logradouro: '',
        numero: '',
        complemento: '',
        bairro: '',
        cep: '',
        cidade: '',
        estado: '',
      },
      estadoCivil: 'SOLTEIRO',
      conjugeId: '',
      vocacao: 'LEIGO',
      batizado: false,
      primeiraComunhao: false,
      crismado: false,
      casadoIgrejaCatolica: false,
      vidaSacramental: {
        batizado: false,
        dataBatismo: '',
        paroquiaBatismo: '',
        celebranteBatismo: '',
        livroBatismo: '',
        folhaBatismo: '',
        termoBatismo: '',
        padrinhosBatismo: '',
        primeiraComunhao: false,
        dataPrimeiraComunhao: '',
        paroquiaPrimeiraComunhao: '',
        turmaCatequese: '',
        crismado: false,
        dataCrisma: '',
        paroquiaCrisma: '',
        celebranteCrisma: '',
        padrinhoCrisma: '',
        casadoIgrejaCatolica: false,
        dataMatrimonio: '',
        paroquiaMatrimonio: '',
        numeroProcessoMatrimonial: '',
        certidaoMatrimonio: '',
      },
      dependentes: [],
      habilidadesVoluntariado: '',
      consentimentoLgpd: {
        termoConsentimentoAceito: true,
        dataHoraConsentimentoUtc: new Date().toISOString(),
        versaoTermoConsentimento: 'v1.0',
        usoImagemAutorizado: false,
        permitirDivulgacaoAniversario: true,
      },
    });
  };

  const handleOpenCreateModal = () => {
    setEditingFiel(null);
    resetForm();
    setModalOpen(true);
  };

  const handleOpenEditModal = (fiel: Fiel) => {
    setEditingFiel(fiel);
    setActiveTab(0);

    const endObj = typeof fiel.endereco === 'object' && fiel.endereco !== null
      ? fiel.endereco
      : { logradouro: typeof fiel.endereco === 'string' ? fiel.endereco : '', numero: '', complemento: '', bairro: '', cep: '', cidade: '', estado: '' };

    setFormValues({
      nome: fiel.nome || '',
      cpf: fiel.cpf || '',
      rg: fiel.rg || '',
      dataNascimento: fiel.dataNascimento || '',
      sexo: fiel.sexo || 'NAO_INFORMADO',
      profissao: fiel.profissao || '',
      email: fiel.email || '',
      telefone: fiel.telefone || '',
      telefoneFixo: fiel.telefoneFixo || '',
      celularWhatsapp: fiel.celularWhatsapp || fiel.telefone || '',
      preferenciaCanalComunicacao: fiel.preferenciaCanalComunicacao || 'WHATSAPP',
      endereco: endObj,
      estadoCivil: fiel.estadoCivil || 'SOLTEIRO',
      conjugeId: fiel.conjugeId || '',
      vocacao: fiel.vocacao || 'LEIGO',
      batizado: fiel.batizado || false,
      primeiraComunhao: fiel.primeiraComunhao || false,
      crismado: fiel.crismado || false,
      casadoIgrejaCatolica: fiel.casadoIgrejaCatolica || false,
      vidaSacramental: fiel.vidaSacramental || {
        batizado: fiel.batizado || false,
        primeiraComunhao: fiel.primeiraComunhao || false,
        crismado: fiel.crismado || false,
        casadoIgrejaCatolica: fiel.casadoIgrejaCatolica || false,
      },
      dependentes: fiel.dependentes || [],
      habilidadesVoluntariado: fiel.habilidadesVoluntariado || '',
      consentimentoLgpd: fiel.consentimentoLgpd || {
        termoConsentimentoAceito: true,
        versaoTermoConsentimento: 'v1.0',
        usoImagemAutorizado: false,
        permitirDivulgacaoAniversario: true,
      },
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

  const handleAddDependente = () => {
    const newDep: Dependente = {
      nome: '',
      dataNascimento: '',
      parentesco: 'FILHO',
      batizado: false,
      primeiraComunhao: false,
      crismado: false,
    };
    setFormValues({
      ...formValues,
      dependentes: [...(formValues.dependentes || []), newDep],
    });
  };

  const handleRemoveDependente = (index: number) => {
    const updated = [...(formValues.dependentes || [])];
    updated.splice(index, 1);
    setFormValues({ ...formValues, dependentes: updated });
  };

  const handleUpdateDependente = (index: number, field: keyof Dependente, value: any) => {
    const updated = [...(formValues.dependentes || [])];
    updated[index] = { ...updated[index], [field]: value };
    setFormValues({ ...formValues, dependentes: updated });
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
        subtitle="Cadastro unificado de paroquianos, vida sacramental e conformidade LGPD."
        actions={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title={showMaskedPii ? 'Exibir dados PII completos' : 'Mascarar dados PII (LGPD)'}>
              <IconButton color="secondary" onClick={() => setShowMaskedPii(!showMaskedPii)}>
                {showMaskedPii ? <VisibilityOffIcon /> : <VisibilityIcon />}
              </IconButton>
            </Tooltip>
            <ActionButton
              title="Novo Fiel"
              icon={<AddIcon />}
              color="primary"
              onClick={handleOpenCreateModal}
            />
          </Box>
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
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setPage(0);
            }}
          />
        </Box>
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead sx={{ backgroundColor: 'action.hover' }}>
            <TableRow>
              <TableCell>Nome / Endereço</TableCell>
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
              filteredFieis
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((fiel) => (
                  <TableRow key={fiel.id} hover>
                    <TableCell>
                      <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                        {fiel.nome}
                      </Typography>
                      <FormattedAddress value={fiel.endereco} />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {showMaskedPii ? formatCpf(fiel.cpf, 'masked_ends') : formatCpf(fiel.cpf)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <StatusIcon {...getVocacaoMeta(fiel.vocacao)} />
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.25 }}>
                        <FormattedEmail value={fiel.email} />
                        <FormattedPhone value={fiel.celularWhatsapp || fiel.telefone} variant="caption" color="text.secondary" />
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap' }}>
                        {(fiel.batizado || fiel.vidaSacramental?.batizado) && (
                          <SacramentoIcon {...SACRAMENTO_META.batizado} color="success" />
                        )}
                        {(fiel.primeiraComunhao || fiel.vidaSacramental?.primeiraComunhao) && (
                          <SacramentoIcon {...SACRAMENTO_META.primeiraComunhao} color="info" />
                        )}
                        {(fiel.crismado || fiel.vidaSacramental?.crismado) && (
                          <SacramentoIcon {...SACRAMENTO_META.crismado} color="secondary" />
                        )}
                        {(fiel.casadoIgrejaCatolica || fiel.vidaSacramental?.casadoIgrejaCatolica) && (
                          <SacramentoIcon {...SACRAMENTO_META.casadoIgrejaCatolica} color="warning" />
                        )}
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
      <TablePagination
        rowsPerPageOptions={[10, 20, 50, 100]}
        component="div"
        count={filteredFieis.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={(_, newPage) => setPage(newPage)}
        onRowsPerPageChange={(e) => {
          setRowsPerPage(parseInt(e.target.value, 10));
          setPage(0);
        }}
        labelRowsPerPage="Itens por página:"
      />

      {/* Modal de Cadastro / Edição de Fiel com Abas Estruturadas */}
      <Dialog open={modalOpen} onClose={() => setModalOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">
            {editingFiel ? `Editar Fiel: ${editingFiel.nome}` : 'Cadastrar Novo Fiel'}
          </Typography>
          <ActionButton title="Fechar formulário" icon={<CloseIcon />} size="small" onClick={() => setModalOpen(false)} />
        </DialogTitle>

        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={activeTab} onChange={(_, val) => setActiveTab(val)} variant="fullWidth">
            <Tab label="1. Dados & Contato" />
            <Tab label="2. Família & Filhos" icon={<FamilyIcon />} iconPosition="start" />
            <Tab label="3. Vida Sacramental" />
            <Tab label="4. Voluntariado & LGPD" icon={<LgpdIcon />} iconPosition="start" />
          </Tabs>
        </Box>

        <DialogContent dividers sx={{ p: 0 }}>
          {/* Aba 1: Dados Pessoais & Contato com Endereço Estruturado */}
          <CustomTabPanel value={activeTab} index={0}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Nome Completo"
                  fullWidth
                  required
                  value={formValues.nome}
                  onChange={(e) => setFormValues({ ...formValues, nome: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  label="CPF"
                  fullWidth
                  required
                  value={formValues.cpf}
                  onChange={(e) => setFormValues({ ...formValues, cpf: formatCpf(e.target.value) })}
                  slotProps={{ htmlInput: { maxLength: 14 } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  label="RG"
                  fullWidth
                  value={formValues.rg || ''}
                  onChange={(e) => setFormValues({ ...formValues, rg: formatRg(e.target.value) })}
                  slotProps={{ htmlInput: { maxLength: 12 } }}
                />
              </Grid>

              <Grid size={{ xs: 12, sm: 4 }}>
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
              <Grid size={{ xs: 12, sm: 4 }}>
                <FormControl fullWidth>
                  <InputLabel id="sexo-label">Sexo</InputLabel>
                  <Select
                    labelId="sexo-label"
                    label="Sexo"
                    value={formValues.sexo || 'NAO_INFORMADO'}
                    onChange={(e) => setFormValues({ ...formValues, sexo: e.target.value as Sexo })}
                  >
                    <MenuItem value="MASCULINO">Masculino</MenuItem>
                    <MenuItem value="FEMININO">Feminino</MenuItem>
                    <MenuItem value="NAO_INFORMADO">Não Informado</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  label="Profissão / Ocupação"
                  fullWidth
                  value={formValues.profissao || ''}
                  onChange={(e) => setFormValues({ ...formValues, profissao: e.target.value })}
                />
              </Grid>

              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  label="E-mail de Contato"
                  type="email"
                  fullWidth
                  required
                  value={formValues.email}
                  onChange={(e) => setFormValues({ ...formValues, email: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  label="Celular / WhatsApp"
                  fullWidth
                  required
                  value={formValues.celularWhatsapp || formValues.telefone}
                  onChange={(e) => {
                    const masked = formatPhone(e.target.value);
                    setFormValues({ ...formValues, celularWhatsapp: masked, telefone: masked });
                  }}
                  slotProps={{ htmlInput: { maxLength: 15 } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  label="Telefone Fixo (Opcional)"
                  fullWidth
                  value={formValues.telefoneFixo || ''}
                  onChange={(e) => setFormValues({ ...formValues, telefoneFixo: formatPhone(e.target.value) })}
                  slotProps={{ htmlInput: { maxLength: 14 } }}
                />
              </Grid>

              <Grid size={{ xs: 12, sm: 4 }}>
                <FormControl fullWidth>
                  <InputLabel id="canal-label">Canal de Comunicação Preferido</InputLabel>
                  <Select
                    labelId="canal-label"
                    label="Canal de Comunicação Preferido"
                    value={formValues.preferenciaCanalComunicacao || 'WHATSAPP'}
                    onChange={(e) => setFormValues({ ...formValues, preferenciaCanalComunicacao: e.target.value as PreferenciaCanalComunicacao })}
                  >
                    <MenuItem value="WHATSAPP">WhatsApp</MenuItem>
                    <MenuItem value="EMAIL">E-mail</MenuItem>
                    <MenuItem value="SMS">SMS</MenuItem>
                    <MenuItem value="TELEFONE">Ligação Telefônica</MenuItem>
                    <MenuItem value="NENHUM">Nenhum</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 12, sm: 8 }}>
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

              <Grid size={{ xs: 12 }}>
                <Typography variant="subtitle2" sx={{ mt: 1, mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <LocationIcon fontSize="small" color="primary" /> Endereço Residencial Estruturado
                </Typography>
              </Grid>

              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  label="CEP"
                  fullWidth
                  value={typeof formValues.endereco === 'object' ? formValues.endereco.cep || '' : ''}
                  onChange={(e) => {
                    const end = typeof formValues.endereco === 'object' ? formValues.endereco : {};
                    setFormValues({ ...formValues, endereco: { ...end, cep: formatCep(e.target.value) } });
                  }}
                  slotProps={{ htmlInput: { maxLength: 9 } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Logradouro / Rua"
                  fullWidth
                  value={typeof formValues.endereco === 'object' ? formValues.endereco.logradouro || '' : String(formValues.endereco || '')}
                  onChange={(e) => {
                    const end = typeof formValues.endereco === 'object' ? formValues.endereco : {};
                    setFormValues({ ...formValues, endereco: { ...end, logradouro: e.target.value } });
                  }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  label="Número"
                  fullWidth
                  value={typeof formValues.endereco === 'object' ? formValues.endereco.numero || '' : ''}
                  onChange={(e) => {
                    const end = typeof formValues.endereco === 'object' ? formValues.endereco : {};
                    setFormValues({ ...formValues, endereco: { ...end, numero: e.target.value } });
                  }}
                />
              </Grid>

              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  label="Complemento"
                  fullWidth
                  value={typeof formValues.endereco === 'object' ? formValues.endereco.complemento || '' : ''}
                  onChange={(e) => {
                    const end = typeof formValues.endereco === 'object' ? formValues.endereco : {};
                    setFormValues({ ...formValues, endereco: { ...end, complemento: e.target.value } });
                  }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  label="Bairro"
                  fullWidth
                  value={typeof formValues.endereco === 'object' ? formValues.endereco.bairro || '' : ''}
                  onChange={(e) => {
                    const end = typeof formValues.endereco === 'object' ? formValues.endereco : {};
                    setFormValues({ ...formValues, endereco: { ...end, bairro: e.target.value } });
                  }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  label="Cidade"
                  fullWidth
                  value={typeof formValues.endereco === 'object' ? formValues.endereco.cidade || '' : ''}
                  onChange={(e) => {
                    const end = typeof formValues.endereco === 'object' ? formValues.endereco : {};
                    setFormValues({ ...formValues, endereco: { ...end, cidade: e.target.value } });
                  }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 1 }}>
                <TextField
                  label="UF"
                  fullWidth
                  value={typeof formValues.endereco === 'object' ? formValues.endereco.estado || '' : ''}
                  onChange={(e) => {
                    const end = typeof formValues.endereco === 'object' ? formValues.endereco : {};
                    setFormValues({ ...formValues, endereco: { ...end, estado: e.target.value.toUpperCase().slice(0, 2) } });
                  }}
                  slotProps={{ htmlInput: { maxLength: 2 } }}
                />
              </Grid>
            </Grid>
          </CustomTabPanel>

          {/* Aba 2: Família & Dependentes */}
          <CustomTabPanel value={activeTab} index={1}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControl fullWidth>
                  <InputLabel id="estado-civil-label">Estado Civil</InputLabel>
                  <Select
                    labelId="estado-civil-label"
                    label="Estado Civil"
                    value={formValues.estadoCivil || 'SOLTEIRO'}
                    onChange={(e) => setFormValues({ ...formValues, estadoCivil: e.target.value as EstadoCivil })}
                  >
                    <MenuItem value="SOLTEIRO">Solteiro(a)</MenuItem>
                    <MenuItem value="CASADO_IGREJA">Casado(a) na Igreja Católica</MenuItem>
                    <MenuItem value="CASADO_CIVIL">Casado(a) apenas no Civil</MenuItem>
                    <MenuItem value="VIUVO">Viúvo(a)</MenuItem>
                    <MenuItem value="DIVORCIADO">Divorciado(a)</MenuItem>
                    <MenuItem value="UNIAO_ESTAVEL">União Estável</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControl fullWidth>
                  <InputLabel id="conjuge-label">Cônjuge (Fiel cadastrado)</InputLabel>
                  <Select
                    labelId="conjuge-label"
                    label="Cônjuge (Fiel cadastrado)"
                    value={formValues.conjugeId || ''}
                    onChange={(e) => setFormValues({ ...formValues, conjugeId: e.target.value })}
                  >
                    <MenuItem value="">Nenhum / Não cadastrado</MenuItem>
                    {fieis
                      .filter((f) => f.id !== editingFiel?.id)
                      .map((f) => (
                        <MenuItem key={f.id} value={f.id}>
                          {f.nome} ({formatCpf(f.cpf)})
                        </MenuItem>
                      ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid size={{ xs: 12 }} sx={{ mt: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                    Dependentes / Filhos (Catequese & Pastoral Familiar)
                  </Typography>
                  <Button startIcon={<AddIcon />} variant="outlined" size="small" onClick={handleAddDependente}>
                    Adicionar Filho/Dependente
                  </Button>
                </Box>

                {(!formValues.dependentes || formValues.dependentes.length === 0) ? (
                  <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic', py: 1 }}>
                    Nenhum dependente cadastrado.
                  </Typography>
                ) : (
                  formValues.dependentes.map((dep, idx) => (
                    <Paper key={idx} variant="outlined" sx={{ p: 2, mb: 1.5, position: 'relative' }}>
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => handleRemoveDependente(idx)}
                        sx={{ position: 'absolute', top: 8, right: 8 }}
                      >
                        <DeleteIcon />
                      </IconButton>
                      <Grid container spacing={1.5} sx={{ pr: 4 }}>
                        <Grid size={{ xs: 12, sm: 5 }}>
                          <TextField
                            label="Nome do Filho/Dependente"
                            size="small"
                            fullWidth
                            value={dep.nome}
                            onChange={(e) => handleUpdateDependente(idx, 'nome', e.target.value)}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 4 }}>
                          <TextField
                            label="Data de Nascimento"
                            type="date"
                            size="small"
                            fullWidth
                            slotProps={{ inputLabel: { shrink: true } }}
                            value={dep.dataNascimento || ''}
                            onChange={(e) => handleUpdateDependente(idx, 'dataNascimento', e.target.value)}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 3 }}>
                          <FormControl fullWidth size="small">
                            <InputLabel id={`parentesco-${idx}`}>Parentesco</InputLabel>
                            <Select
                              labelId={`parentesco-${idx}`}
                              label="Parentesco"
                              value={dep.parentesco || 'FILHO'}
                              onChange={(e) => handleUpdateDependente(idx, 'parentesco', e.target.value as Parentesco)}
                            >
                              <MenuItem value="FILHO">Filho(a)</MenuItem>
                              <MenuItem value="ENTEADO">Enteado(a)</MenuItem>
                              <MenuItem value="TUTELADO">Tutelado(a)</MenuItem>
                              <MenuItem value="OUTRO">Outro</MenuItem>
                            </Select>
                          </FormControl>
                        </Grid>

                        <Grid size={{ xs: 12 }}>
                          <Box sx={{ display: 'flex', gap: 2 }}>
                            <FormControlLabel
                              control={
                                <Checkbox
                                  size="small"
                                  checked={!!dep.batizado}
                                  onChange={(e) => handleUpdateDependente(idx, 'batizado', e.target.checked)}
                                />
                              }
                              label="Batizado"
                            />
                            <FormControlLabel
                              control={
                                <Checkbox
                                  size="small"
                                  checked={!!dep.primeiraComunhao}
                                  onChange={(e) => handleUpdateDependente(idx, 'primeiraComunhao', e.target.checked)}
                                />
                              }
                              label="1ª Eucaristia"
                            />
                            <FormControlLabel
                              control={
                                <Checkbox
                                  size="small"
                                  checked={!!dep.crismado}
                                  onChange={(e) => handleUpdateDependente(idx, 'crismado', e.target.checked)}
                                />
                              }
                              label="Crismado"
                            />
                          </Box>
                        </Grid>
                      </Grid>
                    </Paper>
                  ))
                )}
              </Grid>
            </Grid>
          </CustomTabPanel>

          {/* Aba 3: Vida Sacramental & Livros Paroquiais */}
          <CustomTabPanel value={activeTab} index={2}>
            <Grid container spacing={2}>
              {/* Batismo */}
              <Grid size={{ xs: 12 }}>
                <Accordion defaultExpanded>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                      1. Sacramento do Batismo
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Grid container spacing={1.5}>
                      <Grid size={{ xs: 12 }}>
                        <FormControlLabel
                          control={
                            <Checkbox
                              checked={!!formValues.vidaSacramental?.batizado || !!formValues.batizado}
                              onChange={(e) => {
                                const val = e.target.checked;
                                setFormValues({
                                  ...formValues,
                                  batizado: val,
                                  vidaSacramental: { ...formValues.vidaSacramental, batizado: val },
                                });
                              }}
                            />
                          }
                          label="Recebeu o Batismo"
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField
                          label="Data do Batismo"
                          type="date"
                          fullWidth
                          size="small"
                          slotProps={{ inputLabel: { shrink: true } }}
                          value={formValues.vidaSacramental?.dataBatismo || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, dataBatismo: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField
                          label="Paróquia / Local"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.paroquiaBatismo || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, paroquiaBatismo: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField
                          label="Celebrante (Padre/Diácono)"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.celebranteBatismo || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, celebranteBatismo: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Livro Nº"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.livroBatismo || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, livroBatismo: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Folha Nº"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.folhaBatismo || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, folhaBatismo: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Termo Nº"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.termoBatismo || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, termoBatismo: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Padrinhos"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.padrinhosBatismo || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, padrinhosBatismo: e.target.value } })}
                        />
                      </Grid>
                    </Grid>
                  </AccordionDetails>
                </Accordion>
              </Grid>

              {/* Primeira Comunhão */}
              <Grid size={{ xs: 12 }}>
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                      2. Primeira Eucaristia / Catequese
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Grid container spacing={1.5}>
                      <Grid size={{ xs: 12 }}>
                        <FormControlLabel
                          control={
                            <Checkbox
                              checked={!!formValues.vidaSacramental?.primeiraComunhao || !!formValues.primeiraComunhao}
                              onChange={(e) => {
                                const val = e.target.checked;
                                setFormValues({
                                  ...formValues,
                                  primeiraComunhao: val,
                                  vidaSacramental: { ...formValues.vidaSacramental, primeiraComunhao: val },
                                });
                              }}
                            />
                          }
                          label="Fez a Primeira Comunhão"
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField
                          label="Data da 1ª Comunhão"
                          type="date"
                          fullWidth
                          size="small"
                          slotProps={{ inputLabel: { shrink: true } }}
                          value={formValues.vidaSacramental?.dataPrimeiraComunhao || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, dataPrimeiraComunhao: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField
                          label="Paróquia"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.paroquiaPrimeiraComunhao || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, paroquiaPrimeiraComunhao: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField
                          label="Turma de Catequese"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.turmaCatequese || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, turmaCatequese: e.target.value } })}
                        />
                      </Grid>
                    </Grid>
                  </AccordionDetails>
                </Accordion>
              </Grid>

              {/* Crisma */}
              <Grid size={{ xs: 12 }}>
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                      3. Sacramento da Crisma
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Grid container spacing={1.5}>
                      <Grid size={{ xs: 12 }}>
                        <FormControlLabel
                          control={
                            <Checkbox
                              checked={!!formValues.vidaSacramental?.crismado || !!formValues.crismado}
                              onChange={(e) => {
                                const val = e.target.checked;
                                setFormValues({
                                  ...formValues,
                                  crismado: val,
                                  vidaSacramental: { ...formValues.vidaSacramental, crismado: val },
                                });
                              }}
                            />
                          }
                          label="Recebeu a Crisma"
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Data da Crisma"
                          type="date"
                          fullWidth
                          size="small"
                          slotProps={{ inputLabel: { shrink: true } }}
                          value={formValues.vidaSacramental?.dataCrisma || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, dataCrisma: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Paróquia"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.paroquiaCrisma || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, paroquiaCrisma: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Bispo / Celebrante"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.celebranteCrisma || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, celebranteCrisma: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Padrinho / Madrinha"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.padrinhoCrisma || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, padrinhoCrisma: e.target.value } })}
                        />
                      </Grid>
                    </Grid>
                  </AccordionDetails>
                </Accordion>
              </Grid>

              {/* Matrimônio */}
              <Grid size={{ xs: 12 }}>
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                      4. Matrimônio Católico
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Grid container spacing={1.5}>
                      <Grid size={{ xs: 12 }}>
                        <FormControlLabel
                          control={
                            <Checkbox
                              checked={!!formValues.vidaSacramental?.casadoIgrejaCatolica || !!formValues.casadoIgrejaCatolica}
                              onChange={(e) => {
                                const val = e.target.checked;
                                setFormValues({
                                  ...formValues,
                                  casadoIgrejaCatolica: val,
                                  vidaSacramental: { ...formValues.vidaSacramental, casadoIgrejaCatolica: val },
                                });
                              }}
                            />
                          }
                          label="Casado pela Igreja Católica"
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Data do Matrimônio"
                          type="date"
                          fullWidth
                          size="small"
                          slotProps={{ inputLabel: { shrink: true } }}
                          value={formValues.vidaSacramental?.dataMatrimonio || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, dataMatrimonio: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Paróquia"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.paroquiaMatrimonio || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, paroquiaMatrimonio: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Nº Processo Matrimonial"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.numeroProcessoMatrimonial || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, numeroProcessoMatrimonial: e.target.value } })}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 3 }}>
                        <TextField
                          label="Nº Certidão Matrimonial"
                          fullWidth
                          size="small"
                          value={formValues.vidaSacramental?.certidaoMatrimonio || ''}
                          onChange={(e) => setFormValues({ ...formValues, vidaSacramental: { ...formValues.vidaSacramental, certidaoMatrimonio: e.target.value } })}
                        />
                      </Grid>
                    </Grid>
                  </AccordionDetails>
                </Accordion>
              </Grid>
            </Grid>
          </CustomTabPanel>

          {/* Aba 4: Voluntariado & LGPD */}
          <CustomTabPanel value={activeTab} index={3}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12 }}>
                <TextField
                  label="Habilidades e Disponibilidade para Servir / Voluntariado"
                  fullWidth
                  multiline
                  rows={2}
                  placeholder="Ex: Som e Mídia, Ornamentação, Cozinha, Apoio Médico, Acolhida..."
                  value={formValues.habilidadesVoluntariado || ''}
                  onChange={(e) => setFormValues({ ...formValues, habilidadesVoluntariado: e.target.value })}
                />
              </Grid>

              <Grid size={{ xs: 12 }}>
                <Paper variant="outlined" sx={{ p: 2, bgcolor: 'action.hover', mt: 1 }}>
                  <Typography variant="subtitle2" color="primary" sx={{ fontWeight: 'bold', mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <LgpdIcon fontSize="small" /> Proteção de Dados (LGPD - Lei 13.709/2018)
                  </Typography>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={!!formValues.consentimentoLgpd?.termoConsentimentoAceito}
                        onChange={(e) => setFormValues({
                          ...formValues,
                          consentimentoLgpd: { ...formValues.consentimentoLgpd, termoConsentimentoAceito: e.target.checked },
                        })}
                      />
                    }
                    label="Termo de Consentimento Aceito (armazenamento e tratamento dos dados paroquiais)"
                  />
                  <br />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={!!formValues.consentimentoLgpd?.usoImagemAutorizado}
                        onChange={(e) => setFormValues({
                          ...formValues,
                          consentimentoLgpd: { ...formValues.consentimentoLgpd, usoImagemAutorizado: e.target.checked },
                        })}
                      />
                    }
                    label="Autoriza uso de imagem/vídeo em transmissões de missas e mídias sociais da Paróquia"
                  />
                  <br />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formValues.consentimentoLgpd?.permitirDivulgacaoAniversario !== false}
                        onChange={(e) => setFormValues({
                          ...formValues,
                          consentimentoLgpd: { ...formValues.consentimentoLgpd, permitirDivulgacaoAniversario: e.target.checked },
                        })}
                      />
                    }
                    label="Permite divulgação do nome na lista paroquial de aniversariantes do mês"
                  />
                </Paper>
              </Grid>
            </Grid>
          </CustomTabPanel>
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
