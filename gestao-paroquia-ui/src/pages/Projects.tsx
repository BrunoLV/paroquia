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
  FormHelperText,
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
import { useNotification } from '../context/NotificationContext';

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
  const [formStatus, setFormStatus] = useState<'ATIVO' | 'INATIVO'>('ATIVO');

  // Form validation errors
  const [nomeError, setNomeError] = useState('');
  const [orgIdError, setOrgIdError] = useState('');
  const [inicioError, setInicioError] = useState('');
  const [fimError, setFimError] = useState('');

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
      showError('Não foi possível carregar os projetos e organizações.');
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
    setFormStatus('ATIVO');
    setIsEditMode(false);
    
    // Reset validation errors
    setNomeError('');
    setOrgIdError('');
    setInicioError('');
    setFimError('');
    
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
    
    // Reset validation errors
    setNomeError('');
    setOrgIdError('');
    setInicioError('');
    setFimError('');
    
    setDialogOpen(true);
  };

  const handleSave = async () => {
    let hasError = false;
    
    if (!formNome.trim()) {
      setNomeError('O nome do projeto é obrigatório.');
      hasError = true;
    }
    if (!formOrgId) {
      setOrgIdError('A organização responsável é obrigatória.');
      hasError = true;
    }
    if (!formInicio) {
      setInicioError('A data de início é obrigatória.');
      hasError = true;
    }
    if (!formFim) {
      setFimError('A data de término é obrigatória.');
      hasError = true;
    }
    if (formInicio && formFim && new Date(formInicio) > new Date(formFim)) {
      setInicioError('A data de início não pode ser posterior à data de término.');
      setFimError('A data de término não pode ser anterior à data de início.');
      hasError = true;
    }
    if (hasError) return;

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
        await apiClient.post('/projetos', payload);
        showSuccess('Projeto criado com sucesso!');
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
            sx={{ textTransform: 'none' }}
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
                      label={project.status === 'ATIVO' ? 'Ativo' : 'Inativo'}
                      color={project.status === 'ATIVO' ? 'success' : 'default'}
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
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 700, color: 'primary.main' }}>
          {isEditMode ? 'Editar Projeto' : 'Cadastrar Projeto'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1.5 }}>
            <TextField
              label="Nome do Projeto"
              required
              fullWidth
              value={formNome}
              onChange={(e) => {
                setFormNome(e.target.value);
                if (nomeError) setNomeError('');
              }}
              error={!!nomeError}
              helperText={nomeError}
              slotProps={{ htmlInput: { maxLength: 160 } }}
            />

            <TextField
              label="Descrição"
              fullWidth
              multiline
              rows={3}
              value={formDescricao}
              onChange={(e) => setFormDescricao(e.target.value)}
              slotProps={{ htmlInput: { maxLength: 2000 } }}
            />

            <FormControl fullWidth error={!!orgIdError}>
              <InputLabel id="org-responsavel-label">Organização Responsável</InputLabel>
              <Select
                labelId="org-responsavel-label"
                id="org-responsavel-select"
                value={formOrgId}
                label="Organização Responsável"
                onChange={(e) => {
                  setFormOrgId(e.target.value);
                  if (orgIdError) setOrgIdError('');
                }}
              >
                {getSelectableOrgs().map((org) => (
                  <MenuItem key={org.id} value={org.id}>
                    {org.nome}
                  </MenuItem>
                ))}
              </Select>
              {orgIdError && <FormHelperText>{orgIdError}</FormHelperText>}
            </FormControl>

            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Data de Início"
                  type="date"
                  required
                  fullWidth
                  value={formInicio}
                  onChange={(e) => {
                    setFormInicio(e.target.value);
                    if (inicioError) setInicioError('');
                  }}
                  error={!!inicioError}
                  helperText={inicioError}
                  slotProps={{ inputLabel: { shrink: true } }}
                />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Data de Término"
                  type="date"
                  required
                  fullWidth
                  value={formFim}
                  onChange={(e) => {
                    setFormFim(e.target.value);
                    if (fimError) setFimError('');
                  }}
                  error={!!fimError}
                  helperText={fimError}
                  slotProps={{ inputLabel: { shrink: true } }}
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
                    label={selectedProject.status === 'ATIVO' ? 'Ativo' : 'Inativo'}
                    color={selectedProject.status === 'ATIVO' ? 'success' : 'default'}
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
