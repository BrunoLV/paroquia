import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  Chip,
  Drawer,
  Divider,
  Alert,
  LinearProgress,
  Grid,
  CircularProgress,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Visibility as VisibilityIcon,
  Info as InfoIcon,
  Business as BusinessIcon,
  CalendarToday as CalendarIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { useAuth } from '../auth/AuthContext';
import apiClient from '../api/client';
import { Projeto, Organizacao } from '../api/types';

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
  const [formStatus, setFormStatus] = useState<'ATIVO' | 'INATIVO'>('ATIVO');
  const [formError, setFormError] = useState('');

  // 1. Permission checks
  const canManageProjects = () => {
    if (!user) return false;
    return user.roles.some((role: string) => 
      role === 'ADMIN' || 
      role === 'ROLE_ADMIN' ||
      role.endsWith('_COORDENADOR') || 
      role.endsWith('_VICE_COORDENADOR') ||
      role.endsWith('_PAROCO') || 
      role.endsWith('_PADRE')
    );
  };

  const isGlobalCreator = () => {
    if (!user) return false;
    return user.roles.some((role: string) => 
      role === 'ADMIN' || 
      role === 'ROLE_ADMIN' ||
      role.includes('CONSELHO_COORDENADOR') || 
      role.includes('CONSELHO_VICE_COORDENADOR') ||
      role.endsWith('_PAROCO')
    );
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
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 3. Resumo Fetching
  const fetchResumo = async (projectId: string) => {
    setIsLoadingResumo(true);
    try {
      const res = await apiClient.get<ProjetoResumo>(`/projetos/${projectId}/resumo`);
      setResumo(res.data);
    } catch (err) {
      console.error('Erro ao buscar resumo:', err);
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
    setFormStatus('ATIVO');
    setFormError('');
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
    setFormError('');
    setIsEditMode(true);
    setDialogOpen(true);
  };

  const handleSave = async () => {
    setFormError('');
    if (!formNome.trim()) {
      setFormError('O nome do projeto é obrigatório.');
      return;
    }
    if (!formOrgId) {
      setFormError('A organização responsável é obrigatória.');
      return;
    }
    if (!formInicio || !formFim) {
      setFormError('As datas de início e fim são obrigatórias.');
      return;
    }
    if (new Date(formInicio) > new Date(formFim)) {
      setFormError('A data de início não pode ser posterior à data de término.');
      return;
    }

    const payload = {
      nome: formNome,
      descricao: formDescricao || null,
      organizacaoResponsavelId: formOrgId,
      inicio: new Date(formInicio).toISOString(),
      fim: new Date(formFim).toISOString(),
      status: formStatus
    };

    try {
      if (isEditMode) {
        await apiClient.patch(`/projetos/${formId}`, payload);
      } else {
        await apiClient.post('/projetos', payload);
      }
      setDialogOpen(false);
      loadData();
    } catch (err: any) {
      setFormError(err.response?.data?.message || 'Erro ao salvar projeto.');
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
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 700, color: 'primary.main' }}>
          Projetos Paroquiais
        </Typography>
        {canManageProjects() && (
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={handleOpenCreate}
            sx={{ textTransform: 'none', borderRadius: '8px' }}
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
        <TableContainer component={Paper} sx={{ borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
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
                      label={project.status === 'ATIVO' ? 'Ativo' : 'Inativo'}
                      color={project.status === 'ATIVO' ? 'success' : 'default'}
                      variant="outlined"
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
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 700, color: 'primary.main' }}>
          {isEditMode ? 'Editar Projeto' : 'Cadastrar Projeto'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1.5 }}>
            {formError && <Alert severity="error">{formError}</Alert>}
            
            <TextField
              label="Nome do Projeto"
              required
              fullWidth
              value={formNome}
              onChange={(e) => setFormNome(e.target.value)}
              inputProps={{ maxLength: 160 }}
            />

            <TextField
              label="Descrição"
              fullWidth
              multiline
              rows={3}
              value={formDescricao}
              onChange={(e) => setFormDescricao(e.target.value)}
              inputProps={{ maxLength: 2000 }}
            />

            <FormControl fullWidth>
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
              <Grid item xs={6}>
                <TextField
                  label="Data de Início"
                  type="date"
                  required
                  fullWidth
                  value={formInicio}
                  onChange={(e) => setFormInicio(e.target.value)}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Data de Término"
                  type="date"
                  required
                  fullWidth
                  value={formFim}
                  onChange={(e) => setFormFim(e.target.value)}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
            </Grid>

            {isEditMode && (
              <FormControl fullWidth>
                <InputLabel id="status-label">Status</InputLabel>
                <Select
                  labelId="status-label"
                  value={formStatus}
                  label="Status"
                  onChange={(e) => setFormStatus(e.target.value as 'ATIVO' | 'INATIVO')}
                >
                  <MenuItem value="ATIVO">Ativo</MenuItem>
                  <MenuItem value="INATIVO">Inativo</MenuItem>
                </Select>
              </FormControl>
            )}
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={() => setDialogOpen(false)} sx={{ textTransform: 'none' }}>Cancelar</Button>
          <Button onClick={handleSave} variant="contained" color="primary" sx={{ textTransform: 'none' }}>
            Salvar
          </Button>
        </DialogActions>
      </Dialog>

      {/* DETAIL SIDEBAR DRAWER */}
      <Drawer
        anchor="right"
        open={selectedProject !== null}
        onClose={() => setSelectedProject(null)}
        PaperProps={{ sx: { width: { xs: '100%', sm: 420 }, p: 3, boxSizing: 'border-box' } }}
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
                    <CalendarIcon size="small" color="action" sx={{ fontSize: '1rem' }} />
                    <Typography variant="body2">{formatDate(selectedProject.inicio)}</Typography>
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                    TÉRMINO
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <CalendarIcon size="small" color="action" sx={{ fontSize: '1rem' }} />
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
                    <BusinessIcon size="small" color="action" sx={{ fontSize: '1rem' }} />
                    <Typography variant="body2">{getOrgName(selectedProject.organizacaoResponsavelId)}</Typography>
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                    STATUS
                  </Typography>
                  <Chip
                    size="small"
                    label={selectedProject.status === 'ATIVO' ? 'Ativo' : 'Inativo'}
                    color={selectedProject.status === 'ATIVO' ? 'success' : 'default'}
                    variant="outlined"
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
                      sx={{ height: 8, borderRadius: 4 }}
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
                      <Grid item xs={4}>
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center' }}>
                          <Typography variant="caption" color="textSecondary" display="block">TOTAL</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 700 }}>{resumo.statusExecucao.totalEventos}</Typography>
                        </Paper>
                      </Grid>
                      <Grid item xs={4}>
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center', borderColor: 'success.light' }}>
                          <Typography variant="caption" color="success.main" display="block">REALIZADOS</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 700, color: 'success.main' }}>{resumo.statusExecucao.eventosRealizados}</Typography>
                        </Paper>
                      </Grid>
                      <Grid item xs={4}>
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center', borderColor: 'warning.light' }}>
                          <Typography variant="caption" color="warning.main" display="block">PENDENTES</Typography>
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
                            sx={{ borderRadius: '6px' }}
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

            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button variant="outlined" onClick={() => setSelectedProject(null)} sx={{ textTransform: 'none' }}>
                Fechar
              </Button>
            </Box>
          </Box>
        )}
      </Drawer>
    </Container>
  );
};

export default Projects;
