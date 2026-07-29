import React, {useEffect, useState} from 'react';
import {
    Alert,
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
    Divider,
    Drawer,
    FormControl,
    FormControlLabel,
    Grid,
    IconButton,
    InputLabel,
    LinearProgress,
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
    Assignment as WorkIcon,
    Cancel as CancelIcon,
    Business as BusinessIcon,
    CalendarToday as CalendarIcon,
    Edit as EditIcon,
    Info as InfoIcon,
    Visibility as VisibilityIcon,
    Warning as WarningIcon,
} from '@mui/icons-material';
import {useAuth} from '../auth/AuthContext';
import {
    canManageProjects as userCanManageProjects,
    canTransitionProject as userCanTransitionProject,
    isGlobalCreator as userIsGlobalCreator
} from '../auth/permissions';
import apiClient from '../api/client';
import {Organizacao, Projeto} from '../api/types';
import {useNotification} from '../context/NotificationContext';
import {cancelarProjeto, concluirProjeto, iniciarProjeto} from '../api/projects';

// DTO interfaces for project summaries
interface StatusExecucao {
  totalEventos: number;
  eventosRealizados: number;
  eventosPendentes: number;
}

interface MapaColaboracao {
  envolvidos: string[];
}

interface SaudeTemporal {
  percentualTempoDecorrido: number;
  emRisco: boolean;
}

interface ProjetoResumo {
  statusExecucao: StatusExecucao;
  mapaColaboracao: MapaColaboracao;
  saudeTemporal: SaudeTemporal;
}

const Projects = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();
  
  const [projects, setProjects] = useState<Projeto[]>([]);
  const [organizations, setOrganizations] = useState<Organizacao[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // Drawer states (Project Details & Summary)
  const [selectedProject, setSelectedProject] = useState<Projeto | null>(null);
  const [resumo, setResumo] = useState<ProjetoResumo | null>(null);
  const [isLoadingResumo, setIsLoadingResumo] = useState(false);

  // Dialog states (Create / Edit Form)
  const [dialogOpen, setDialogOpen] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [formId, setFormId] = useState('');
  const [formNome, setFormNome] = useState('');
  const [formDescricao, setFormDescricao] = useState('');
  const [formOrgId, setFormOrgId] = useState('');
  const [formInicio, setFormInicio] = useState('');
  const [formFim, setFormFim] = useState('');
  const [formStatus, setFormStatus] = useState<Projeto['status']>('ABERTO');

  // Status transition states
  const [transitionDialogOpen, setTransitionDialogOpen] = useState(false);
  const [transitionType, setTransitionType] = useState<'INICIAR' | 'CONCLUIR' | 'CANCELAR' | null>(null);
  const [cancelarEventos, setCancelarEventos] = useState(false);
  const [isTransitioning, setIsTransitioning] = useState(false);



  // 1. Permission checks
  const canManageProjects = () => {
    return userCanManageProjects(user);
  };

  const isGlobalCreator = () => {
    return userIsGlobalCreator(user);
  };

  const canTransitionProject = (project: Projeto) => {
    return userCanTransitionProject(user, project.organizacaoResponsavelId);
  };

  // Determine which organizations are selectable in the form
  const getSelectableOrgs = () => {
    if (isGlobalCreator()) {
      return organizations;
    }
    // Lock to user's linked organizations
    if (user && Array.isArray(user.organizations)) {
      return user.organizations;
    }
    return [];
  };

  // 2. Fetch Initial Data
  const loadData = async () => {
    setIsLoading(true);
    try {
      const [projRes, orgsRes] = await Promise.all([
        apiClient.get<Projeto[]>('/projetos'),
        apiClient.get<Organizacao[]>('/organizacoes')
      ]);
      setProjects(projRes.data || []);
      setOrganizations(orgsRes.data || []);
    } catch (err) {
      console.error('Erro ao buscar dados:', err);
      showError('Não foi possível carregar os projetos e organizações.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 3. Resumo Fetching
  const fetchResumo = async (projectId: string) => {
    setIsLoadingResumo(true);
    try {
      const res = await apiClient.get<ProjetoResumo>(`/projetos/${projectId}/resumo`);
      setResumo(res.data);
    } catch (err) {
      console.error('Erro ao buscar resumo:', err);
      showError('Não foi possível carregar o resumo do projeto.');
      setResumo(null);
    } finally {
      setIsLoadingResumo(false);
    }
  };

  const handleRowClick = (project: Projeto) => {
    setSelectedProject(project);
    fetchResumo(project.id);
  };

  // 4. Form Actions
  const handleOpenCreate = () => {
    setFormId('');
    setFormNome('');
    setFormDescricao('');
    
    // Auto-select first available organization if limited
    const selectable = getSelectableOrgs();
    setFormOrgId(selectable.length > 0 ? selectable[0].id : '');
    
    // Set default dates (today to +3 months)
    const today = new Date().toISOString().split('T')[0];
    const threeMonthsLater = new Date();
    threeMonthsLater.setMonth(threeMonthsLater.getMonth() + 3);
    const endDate = threeMonthsLater.toISOString().split('T')[0];
    
    setFormInicio(today);
    setFormFim(endDate);
    setFormStatus('ABERTO');
    setIsEditMode(false);
    

    
    setDialogOpen(true);
  };

  const handleOpenEdit = (project: Projeto, e: React.MouseEvent) => {
    e.stopPropagation(); // Avoid triggering details sidebar
    setFormId(project.id);
    setFormNome(project.nome);
    setFormDescricao(project.descricao || '');
    setFormOrgId(project.organizacaoResponsavelId);
    
    // Format dates to YYYY-MM-DD
    setFormInicio(project.inicio ? project.inicio.split('T')[0] : '');
    setFormFim(project.fim ? project.fim.split('T')[0] : '');
    setFormStatus(project.status);
    setIsEditMode(true);
    

    
    setDialogOpen(true);
  };

  const handleSave = async () => {
    if (!formNome.trim()) {
      showError('O nome do projeto é obrigatório.');
      return;
    }
    if (!formOrgId) {
      showError('A organização responsável é obrigatória.');
      return;
    }
    if (!formInicio) {
      showError('A data de início é obrigatória.');
      return;
    }
    if (!formFim) {
      showError('A data de término é obrigatória.');
      return;
    }
    if (formInicio && formFim && new Date(formInicio) > new Date(formFim)) {
      showError('A data de início não pode ser posterior à data de término.');
      return;
    }

    const payload: any = {
      nome: formNome,
      descricao: formDescricao || null,
      organizacaoResponsavelId: formOrgId,
      inicio: new Date(formInicio).toISOString(),
      fim: new Date(formFim).toISOString(),
    };

    if (isEditMode) {
      payload.status = formStatus;
    }

    try {
      if (isEditMode) {
        await apiClient.patch(`/projetos/${formId}`, payload);
        showSuccess('Projeto atualizado com sucesso!');
      } else {
        const response = await apiClient.post('/projetos', payload);
        if (response.status === 202) {
          showSuccess('Solicitação de criação de projeto encaminhada para aprovação (Ano Paroquial bloqueado).');
        } else {
          showSuccess('Projeto criado com sucesso!');
        }
      }
      setDialogOpen(false);
      loadData();
    } catch (err: any) {
      let errorMsg = 'Erro ao salvar projeto.';
      if (err.response?.data) {
        const data = err.response.data;
        if (Array.isArray(data.errors) && data.errors.length > 0) {
          errorMsg = 'Por favor, corrija os seguintes erros:\n' + data.errors.map((e: any) => `• ${e.message || e.defaultMessage}`).join('\n');
        } else if (data.message) {
          errorMsg = data.message;
        }
      }
      showError(errorMsg);
    }
  };

  const handleOpenTransition = (type: 'INICIAR' | 'CONCLUIR' | 'CANCELAR') => {
    setTransitionType(type);
    setCancelarEventos(false);
    setTransitionDialogOpen(true);
  };

  const handleTransitionSubmit = async () => {
    if (!selectedProject || !transitionType) return;
    setIsTransitioning(true);
    try {
      if (transitionType === 'INICIAR') {
        const updated = await iniciarProjeto(selectedProject.id);
        showSuccess('Projeto iniciado com sucesso!');
        setSelectedProject(updated);
      } else if (transitionType === 'CONCLUIR') {
        const updated = await concluirProjeto(selectedProject.id);
        showSuccess('Projeto concluído com sucesso!');
        setSelectedProject(updated);
      } else if (transitionType === 'CANCELAR') {
        const res = await cancelarProjeto(selectedProject.id, cancelarEventos);
        if (res.status === 'PENDENTE' || res.tipoSolicitacao === 'CANCELAMENTO') {
          showSuccess('Solicitação de cancelamento encaminhada para aprovação.');
        } else {
          showSuccess('Projeto cancelado com sucesso!');
        }
        setSelectedProject(null);
      }
      setTransitionDialogOpen(false);
      loadData();
    } catch (err: any) {
      console.error('Erro na transição de status:', err);
      const errMsg = err.response?.data?.message || 'Erro ao processar transição de status.';
      showError(errMsg);
    } finally {
      setIsTransitioning(false);
    }
  };

  // 5. Utility formatting
  const getOrgName = (orgId: string) => {
    const org = organizations.find((o) => o.id === orgId);
    return org ? org.nome : 'N/A';
  };

  const formatDate = (isoStr: string | null) => {
    if (!isoStr) return '-';
    try {
      const d = new Date(isoStr);
      return d.toLocaleDateString('pt-BR', { timeZone: 'UTC' });
    } catch {
      return '-';
    }
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      {/* Header Padronizado */}
      <Box sx={{ mb: 4, display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ p: 1.5, bgcolor: 'rgba(26, 35, 126, 0.08)', borderRadius: 0, display: 'flex' }}>
            <WorkIcon sx={{ fontSize: 32, color: 'primary.main' }} />
          </Box>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main', letterSpacing: '-0.5px' }}>
              Projetos Paroquiais
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Gerencie a criação, acompanhamento e prazos dos projetos pastorais e paroquiais.
            </Typography>
          </Box>
        </Box>

        {canManageProjects() && (
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={handleOpenCreate}
            sx={{ borderRadius: 0, textTransform: 'none', fontWeight: 600, px: 3, py: 1 }}
          >
            Novo Projeto
          </Button>
        )}
      </Box>

      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}>
          <CircularProgress />
        </Box>
      ) : projects.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center', bgcolor: 'background.paper' }}>
          <Typography variant="h6" color="textSecondary" gutterBottom>
            Nenhum projeto cadastrado.
          </Typography>
          {canManageProjects() && (
            <Button
              variant="outlined"
              color="primary"
              startIcon={<AddIcon />}
              onClick={handleOpenCreate}
              sx={{ mt: 2, textTransform: 'none' }}
            >
              Cadastrar Primeiro Projeto
            </Button>
          )}
        </Paper>
      ) : (
        <TableContainer component={Paper} sx={{ borderRadius: 0, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
          <Table>
            <TableHead sx={{ bgcolor: 'rgba(26, 35, 126, 0.04)' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Nome</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Responsável</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Início</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Término</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
                <TableCell sx={{ fontWeight: 700, textAlign: 'right' }}>Ações</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {projects.map((project) => (
                <TableRow
                  key={project.id}
                  hover
                  onClick={() => handleRowClick(project)}
                  sx={{ cursor: 'pointer', '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell sx={{ fontWeight: 600 }}>{project.nome}</TableCell>
                  <TableCell>{getOrgName(project.organizacaoResponsavelId)}</TableCell>
                  <TableCell>{formatDate(project.inicio)}</TableCell>
                  <TableCell>{formatDate(project.fim)}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={
                        project.status === 'ABERTO' ? 'Aberto' :
                        project.status === 'EM_ANDAMENTO' ? 'Em Andamento' :
                        project.status === 'CANCELADO' ? 'Cancelado' :
                        project.status === 'CONCLUIDO' ? 'Concluído' :
                        project.status
                      }
                      color={
                        project.status === 'EM_ANDAMENTO' ? 'success' :
                        project.status === 'ABERTO' ? 'info' :
                        project.status === 'CANCELADO' ? 'error' :
                        'default'
                      }
                      variant="outlined"
                      sx={{ borderRadius: 0 }}
                    />
                  </TableCell>
                  <TableCell sx={{ textAlign: 'right' }} onClick={(e) => e.stopPropagation()}>
                    <IconButton size="small" color="primary" onClick={() => handleRowClick(project)} title="Visualizar Indicadores">
                      <VisibilityIcon />
                    </IconButton>
                    {canManageProjects() && (
                      <IconButton size="small" color="secondary" onClick={(e) => handleOpenEdit(project, e)} title="Editar Projeto" sx={{ ml: 1 }}>
                        <EditIcon />
                      </IconButton>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* CREATE & EDIT DIALOG */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth sx={{ '& .MuiDialog-paper': { borderRadius: 0 } }}>
        <form onSubmit={(e) => { e.preventDefault(); handleSave(); }} noValidate>
          <DialogTitle sx={{ borderBottom: '1px solid #eee', pb: 2, fontWeight: 700, color: '#37474f', fontSize: '1.25rem' }}>
            {isEditMode ? 'Editar Projeto' : 'Cadastrar Projeto'}
          </DialogTitle>
          <DialogContent>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
              <TextField
                label="Nome do Projeto"
                required
                fullWidth
                value={formNome}
                onChange={(e) => setFormNome(e.target.value)}
                slotProps={{ htmlInput: { maxLength: 160 } }}
                size="small"
              />

              <TextField
                label="Descrição"
                fullWidth
                multiline
                rows={3}
                value={formDescricao}
                onChange={(e) => setFormDescricao(e.target.value)}
                slotProps={{ htmlInput: { maxLength: 2000 } }}
                size="small"
              />

              <FormControl fullWidth required size="small">
                <InputLabel id="org-responsavel-label">Organização Responsável</InputLabel>
                <Select
                  labelId="org-responsavel-label"
                  id="org-responsavel-select"
                  value={formOrgId}
                  label="Organização Responsável"
                  onChange={(e) => setFormOrgId(e.target.value)}
                >
                  {getSelectableOrgs().map((org) => (
                    <MenuItem key={org.id} value={org.id}>
                      {org.nome}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}>
                  <TextField
                    label="Data de Início"
                    type="date"
                    required
                    fullWidth
                    value={formInicio}
                    onChange={(e) => setFormInicio(e.target.value)}
                    slotProps={{ inputLabel: { shrink: true } }}
                    size="small"
                  />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField
                    label="Data de Término"
                    type="date"
                    required
                    fullWidth
                    value={formFim}
                    onChange={(e) => setFormFim(e.target.value)}
                    slotProps={{ inputLabel: { shrink: true } }}
                    size="small"
                  />
                </Grid>
              </Grid>

              {isEditMode && (
                <FormControl fullWidth size="small">
                  <InputLabel id="status-label">Status</InputLabel>
                  <Select
                    labelId="status-label"
                    value={formStatus}
                    label="Status"
                    onChange={(e) => setFormStatus(e.target.value as Projeto['status'])}
                  >
                    <MenuItem value="ABERTO">Aberto</MenuItem>
                    <MenuItem value="EM_ANDAMENTO">Em Andamento</MenuItem>
                    <MenuItem value="CANCELADO">Cancelado</MenuItem>
                    <MenuItem value="CONCLUIDO">Concluído</MenuItem>
                  </Select>
                </FormControl>
              )}
            </Box>
          </DialogContent>
          <DialogActions sx={{ borderTop: '1px solid #eee', px: 3, py: 2 }}>
            <Button onClick={() => setDialogOpen(false)} color="secondary" sx={{ fontWeight: 600 }}>Cancelar</Button>
            <Button type="submit" variant="contained" color="primary" sx={{ fontWeight: 600 }}>
              Salvar
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* DETAIL SIDEBAR DRAWER */}
      <Drawer
        anchor="right"
        open={selectedProject !== null}
        onClose={() => setSelectedProject(null)}
        sx={{
          '& .MuiDrawer-paper': { width: { xs: '100%', sm: 420 }, p: 3, boxSizing: 'border-box' }
        }}
      >
        {selectedProject && (
          <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 3.5 }}>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.main', mb: 1 }}>
                Detalhes do Projeto
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Visão consolidada de dados e indicadores.
              </Typography>
            </Box>
            
            <Divider />

            {/* Basic Info */}
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box>
                <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                  NOME
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 600 }}>{selectedProject.nome}</Typography>
              </Box>

              {selectedProject.descricao && (
                <Box>
                  <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                    DESCRIÇÃO
                  </Typography>
                  <Typography variant="body2">{selectedProject.descricao}</Typography>
                </Box>
              )}

              <Box sx={{ display: 'flex', gap: 3 }}>
                <Box>
                  <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                    INÍCIO
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <CalendarIcon fontSize="small" color="action" sx={{ fontSize: '1rem' }} />
                    <Typography variant="body2">{formatDate(selectedProject.inicio)}</Typography>
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                    TÉRMINO
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <CalendarIcon fontSize="small" color="action" sx={{ fontSize: '1rem' }} />
                    <Typography variant="body2">{formatDate(selectedProject.fim)}</Typography>
                  </Box>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', gap: 3 }}>
                <Box>
                  <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                    RESPONSÁVEL
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <BusinessIcon fontSize="small" color="action" sx={{ fontSize: '1rem' }} />
                    <Typography variant="body2">{getOrgName(selectedProject.organizacaoResponsavelId)}</Typography>
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                    STATUS
                  </Typography>
                  <Chip
                    size="small"
                    label={
                      selectedProject.status === 'ABERTO' ? 'Aberto' :
                      selectedProject.status === 'EM_ANDAMENTO' ? 'Em Andamento' :
                      selectedProject.status === 'CANCELADO' ? 'Cancelado' :
                      selectedProject.status === 'CONCLUIDO' ? 'Concluído' :
                      selectedProject.status
                    }
                    color={
                      selectedProject.status === 'EM_ANDAMENTO' ? 'success' :
                      selectedProject.status === 'ABERTO' ? 'info' :
                      selectedProject.status === 'CANCELADO' ? 'error' :
                      'default'
                    }
                    variant="outlined"
                    sx={{ borderRadius: 0 }}
                  />
                </Box>
              </Box>
            </Box>

            <Divider />

            {/* Metrics and indicators summary */}
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary', mb: 2 }}>
                Painel de Indicadores
              </Typography>
              
              {isLoadingResumo ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}>
                  <CircularProgress size={30} />
                </Box>
              ) : resumo ? (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                  
                  {/* Saúde Temporal */}
                  <Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        Saúde Temporal (Tempo Decorrido)
                      </Typography>
                      <Typography variant="body2" color="primary" sx={{ fontWeight: 700 }}>
                        {resumo.saudeTemporal.percentualTempoDecorrido.toFixed(1)}%
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={Math.min(resumo.saudeTemporal.percentualTempoDecorrido, 100)}
                      color={resumo.saudeTemporal.emRisco ? 'error' : 'primary'}
                      sx={{ height: 8, borderRadius: 0 }}
                    />
                    {resumo.saudeTemporal.emRisco && (
                      <Alert severity="warning" icon={<WarningIcon />} sx={{ mt: 1.5, py: 0.5 }}>
                        Projeto em risco: prazo apertado com pendências.
                      </Alert>
                    )}
                  </Box>

                  {/* Status Execução */}
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 600, mb: 1.5 }}>
                      Status de Execução de Eventos
                    </Typography>
                    <Grid container spacing={2}>
                      <Grid size={{ xs: 4 }}>
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center', borderRadius: 0 }}>
                          <Typography variant="caption" color="textSecondary" sx={{ display: 'block' }}>TOTAL</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 700 }}>{resumo.statusExecucao.totalEventos}</Typography>
                        </Paper>
                      </Grid>
                      <Grid size={{ xs: 4 }}>
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center', borderColor: 'success.light', borderRadius: 0 }}>
                          <Typography variant="caption" color="success.main" sx={{ display: 'block' }}>REALIZADOS</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 700, color: 'success.main' }}>{resumo.statusExecucao.eventosRealizados}</Typography>
                        </Paper>
                      </Grid>
                      <Grid size={{ xs: 4 }}>
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center', borderColor: 'warning.light', borderRadius: 0 }}>
                          <Typography variant="caption" color="warning.main" sx={{ display: 'block' }}>PENDENTES</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 700, color: 'warning.main' }}>{resumo.statusExecucao.eventosPendentes}</Typography>
                        </Paper>
                      </Grid>
                    </Grid>
                  </Box>

                  {/* Mapa Colaboração */}
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 600, mb: 1 }}>
                      Mapa de Colaboração (Pastorais/Grupos)
                    </Typography>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                      {resumo.mapaColaboracao.envolvidos.length === 0 ? (
                        <Typography variant="caption" color="textSecondary">
                          Nenhuma pastoral envolvida diretamente.
                        </Typography>
                      ) : (
                        resumo.mapaColaboracao.envolvidos.map((env) => (
                          <Chip
                            key={env}
                            label={env}
                            size="small"
                            variant="outlined"
                            icon={<InfoIcon sx={{ fontSize: '0.8rem' }} />}
                            sx={{ borderRadius: 0 }}
                          />
                        ))
                      )}
                    </Box>
                  </Box>
                </Box>
              ) : (
                <Alert severity="error">Não foi possível carregar os indicadores do projeto.</Alert>
              )}
            </Box>

            <Divider />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box sx={{ display: 'flex', gap: 1 }}>
                {selectedProject.status === 'ABERTO' && canTransitionProject(selectedProject) && (
                  <Button
                    variant="contained"
                    color="success"
                    onClick={() => handleOpenTransition('INICIAR')}
                    sx={{ textTransform: 'none', borderRadius: 0 }}
                  >
                    Iniciar
                  </Button>
                )}
                {selectedProject.status === 'EM_ANDAMENTO' && canTransitionProject(selectedProject) && (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => handleOpenTransition('CONCLUIR')}
                    sx={{ textTransform: 'none', borderRadius: 0 }}
                  >
                    Concluir
                  </Button>
                )}
                {(selectedProject.status === 'ABERTO' || selectedProject.status === 'EM_ANDAMENTO') && canTransitionProject(selectedProject) && (
                  <Button
                    variant="contained"
                    color="error"
                    onClick={() => handleOpenTransition('CANCELAR')}
                    sx={{ textTransform: 'none', borderRadius: 0 }}
                  >
                    Cancelar
                  </Button>
                )}
              </Box>
              <Button variant="outlined" onClick={() => setSelectedProject(null)} sx={{ textTransform: 'none', borderRadius: 0 }}>
                Fechar
              </Button>
            </Box>
          </Box>
        )}
      </Drawer>
      {/* STATUS TRANSITION DIALOG */}
      <Dialog
        open={transitionDialogOpen}
        onClose={() => !isTransitioning && setTransitionDialogOpen(false)}
        maxWidth="xs"
        fullWidth
        sx={{ '& .MuiDialog-paper': { borderRadius: 0 } }}
      >
        <DialogTitle sx={{ fontWeight: 700 }}>
          {transitionType === 'INICIAR' && 'Iniciar Projeto'}
          {transitionType === 'CONCLUIR' && 'Concluir Projeto'}
          {transitionType === 'CANCELAR' && 'Cancelar Projeto'}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: transitionType === 'CANCELAR' ? 2 : 0 }}>
            {transitionType === 'INICIAR' && 'Tem certeza de que deseja iniciar este projeto? O status passará para Em Andamento.'}
            {transitionType === 'CONCLUIR' && 'Tem certeza de que deseja concluir este projeto? O status passará para Concluído e não será mais possível vincular eventos a ele.'}
            {transitionType === 'CANCELAR' && 'Tem certeza de que deseja cancelar este projeto?'}
          </Typography>
          {transitionType === 'CANCELAR' && (
            <FormControlLabel
              control={
                <Checkbox
                  checked={cancelarEventos}
                  onChange={(e) => setCancelarEventos(e.target.checked)}
                  color="primary"
                />
              }
              label="Deseja cancelar também todos os eventos vinculados a este projeto?"
              sx={{ '& .MuiFormControlLabel-label': { fontSize: '0.875rem' } }}
            />
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button
            onClick={() => setTransitionDialogOpen(false)}
            color="secondary"
            disabled={isTransitioning}
            sx={{ fontWeight: 600 }}
          >
            Não
          </Button>
          <Button
            onClick={handleTransitionSubmit}
            variant="contained"
            color={transitionType === 'CANCELAR' ? 'error' : 'primary'}
            disabled={isTransitioning}
            sx={{ fontWeight: 600 }}
          >
            {isTransitioning ? <CircularProgress size={24} /> : 'Sim'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Projects;
