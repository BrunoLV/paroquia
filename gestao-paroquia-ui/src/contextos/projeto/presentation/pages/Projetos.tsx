import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import {useEffect, useRef, useState} from 'react';
import {useMutation, useQuery} from '@tanstack/react-query';
import { Alert, Box, Card, Checkbox, CircularProgress, Container, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Drawer, FormControl, FormControlLabel, Grid, LinearProgress, MenuItem, Paper, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from '@mui/material';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import BlockIcon from '@mui/icons-material/Block';
import BusinessIcon from '@mui/icons-material/Business';
import CalendarIcon from '@mui/icons-material/CalendarToday';
import CancelIcon from '@mui/icons-material/Cancel';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CloseIcon from '@mui/icons-material/Close';
import EditIcon from '@mui/icons-material/Edit';
import InfoIcon from '@mui/icons-material/Info';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import PostAddIcon from '@mui/icons-material/PostAdd';
import SaveIcon from '@mui/icons-material/Save';
import VisibilityIcon from '@mui/icons-material/Visibility';
import WarningIcon from '@mui/icons-material/Warning';
import {useAuth} from '@/contextos/iam/presentation/auth/AuthContext';
import PageHeader from '@/shared/presentation/components/PageHeader';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { getProjetoStatusMeta } from '@/shared/utils/statusMeta/projetoStatus';
import {
    canManageProjetos as userCanManageProjetos,
    canTransitionProjeto as userCanTransitionProjeto,
    isGlobalCreator as userIsGlobalCreator
} from '@/contextos/iam/presentation/auth/permissions';
import apiClient from '@/shared/infrastructure/http/client';
import { getOrganizacoes } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import {Projeto} from '@/shared/domain/types/types';
import {useNotification} from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import {cancelarProjeto, concluirProjeto, iniciarProjeto} from '@/contextos/projeto/infrastructure/http/projetos';

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

const Projetos = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();

  const projetosQuery = useQuery({
    queryKey: ['projetos'],
    queryFn: async () => (await apiClient.get<Projeto[]>('/projetos')).data ?? [],
  });
  const organizacoesQuery = useQuery({ queryKey: ['organizacoes'], queryFn: getOrganizacoes });
  const projects = projetosQuery.data ?? [];
  const organizations = organizacoesQuery.data ?? [];
  const isLoading = projetosQuery.isLoading || organizacoesQuery.isLoading;

  // Drawer states (Project Details & Summary)
  const [selectedProject, setSelectedProject] = useState<Projeto | null>(null);
  const resumoQuery = useQuery({
    queryKey: ['projeto-resumo', selectedProject?.id],
    queryFn: async () => (await apiClient.get<ProjetoResumo>(`/projetos/${selectedProject!.id}/resumo`)).data,
    enabled: Boolean(selectedProject?.id),
  });
  const resumo = resumoQuery.data ?? null;
  const isLoadingResumo = resumoQuery.isFetching;

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
  const canManageProjetos = () => {
    return userCanManageProjetos(user);
  };

  const isGlobalCreator = () => {
    return userIsGlobalCreator(user);
  };

  const canTransitionProjeto = (project: Projeto) => {
    return userCanTransitionProjeto(user, project.grupoParoquialResponsavelId);
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

  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);

  // Refetch manual dos projetos (ex.: após criar/editar/transicionar status) — o resumo
  // do projeto selecionado já refaz sozinho via `queryKey` quando `selectedProject` muda.
  const refetchProjetos = () => projetosQuery.refetch();

  const handleRowClick = (project: Projeto) => {
    setSelectedProject(project);
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

  // Abre o formulário de criação automaticamente quando chega de "Novo Projeto" no dashboard
  // (`/projects?novo=1`), depois limpa o parâmetro para não reabrir em refresh/voltar.
  const abriuPorParametro = useRef(false);
  useEffect(() => {
    if (isLoading || abriuPorParametro.current) return;
    if (new URLSearchParams(window.location.search).get('novo') !== '1') return;
    abriuPorParametro.current = true;
    handleOpenCreate();
    window.history.replaceState({}, '', window.location.pathname);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoading]);

  const handleOpenEdit = (project: Projeto) => {
    // TableCell já chama e.stopPropagation() antes de disparar o onClick da TableRow
    setFormId(project.id);
    setFormNome(project.nome);
    setFormDescricao(project.descricao || '');
    setFormOrgId(project.grupoParoquialResponsavelId);

    // Format dates to YYYY-MM-DD
    setFormInicio(project.inicio ? project.inicio.split('T')[0] : '');
    setFormFim(project.fim ? project.fim.split('T')[0] : '');
    setFormStatus(project.status);
    setIsEditMode(true);



    setDialogOpen(true);
  };

  const saveProjetoMutation = useMutation({
    mutationFn: (payload: Record<string, unknown>) =>
      isEditMode ? apiClient.patch(`/projetos/${formId}`, payload) : apiClient.post('/projetos', payload),
  });

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

    const payload: Record<string, unknown> = {
      nome: formNome,
      descricao: formDescricao || null,
      grupoParoquialResponsavelId: formOrgId,
      inicio: new Date(formInicio).toISOString(),
      fim: new Date(formFim).toISOString(),
    };

    if (isEditMode) {
      payload.status = formStatus;
    }

    try {
      if (isEditMode) {
        await saveProjetoMutation.mutateAsync(payload);
        showSuccess('Projeto atualizado com sucesso!');
      } else {
        const response = await saveProjetoMutation.mutateAsync(payload);
        if (response.status === 202) {
          showSuccess('Solicitação de criação de projeto encaminhada para aprovação (Ano Paroquial bloqueado).');
        } else {
          showSuccess('Projeto criado com sucesso!');
        }
      }
      setDialogOpen(false);
      refetchProjetos();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao salvar projeto.'));
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
        if (res.status === 'PENDENTE' || ('tipoSolicitacao' in res && res.tipoSolicitacao === 'CANCELAMENTO')) {
          showSuccess('Solicitação de cancelamento encaminhada para aprovação.');
        } else {
          showSuccess('Projeto cancelado com sucesso!');
        }
        setSelectedProject(null);
      }
      setTransitionDialogOpen(false);
      refetchProjetos();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao processar transição de status.'));
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
      <PageHeader
        icon={<AssignmentTurnedInIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Projetos Paroquiais"
        subtitle="Gerencie a criação, acompanhamento e prazos dos projetos pastorais e paroquiais."
        actions={
          canManageProjetos() && (
            <ActionButton title="Novo Projeto" icon={<PostAddIcon />} color="primary" onClick={handleOpenCreate} />
          )
        }
      />

      {isLoading ? (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Skeleton variant="rectangular" height={56} />
          <Skeleton variant="rectangular" height={56} />
          <Skeleton variant="rectangular" height={56} />
          <Skeleton variant="rectangular" height={56} />
        </Box>
      ) : projects.length === 0 ? (
        <Card sx={{ p: 4, textAlign: 'center', bgcolor: 'background.paper' }}>
          <Typography variant="h6" color="textSecondary" gutterBottom>
            Nenhum projeto cadastrado.
          </Typography>
          {canManageProjetos() && (
            <ActionButton title="Cadastrar Novo Projeto" icon={<PostAddIcon />} color="primary" onClick={handleOpenCreate} />
          )}
        </Card>
      ) : (
        <TableContainer component={Paper}>
            <Table>
              <TableHead sx={{ backgroundColor: 'action.hover' }}>
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
                {projects
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((project) => (
                    <TableRow
                      key={project.id}
                      hover
                      onClick={() => handleRowClick(project)}
                      sx={{ cursor: 'pointer', '&:last-child td, &:last-child th': { border: 0 } }}
                    >
                      <TableCell sx={{ fontWeight: 600 }}>{project.nome}</TableCell>
                      <TableCell>{getOrgName(project.grupoParoquialResponsavelId)}</TableCell>
                      <TableCell>{formatDate(project.inicio)}</TableCell>
                      <TableCell>{formatDate(project.fim)}</TableCell>
                      <TableCell>
                        <StatusIcon {...getProjetoStatusMeta(project.status)} variant="outlined" />
                      </TableCell>
                      <TableCell sx={{ textAlign: 'right' }} onClick={(e) => e.stopPropagation()}>
                        <TableActionMenu
                          actions={[
                            { label: 'Visualizar detalhes', icon: <VisibilityIcon fontSize="small" />, onClick: () => handleRowClick(project) },
                            ...(canManageProjetos()
                              ? [{ label: 'Editar projeto', icon: <EditIcon fontSize="small" />, onClick: () => handleOpenEdit(project) }]
                              : []),
                          ]}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
              <SearchableTablePagination colSpan={6}
                rowsPerPageOptions={[5, 10, 25]}
                count={projects.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={(_, newPage) => setPage(newPage)}
                onRowsPerPageChange={(e) => {
                  setRowsPerPage(parseInt(e.target.value, 10));
                  setPage(0);
                }}
                labelRowsPerPage="Linhas por página:"
                labelDisplayedRows={({ from, to, count }) =>
                  `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
                }
              />
            </Table>
          </TableContainer>
      )}

      {/* CREATE & EDIT DIALOG */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth sx={{ '& .MuiDialog-paper': {  } }}>
        <form onSubmit={(e) => { e.preventDefault(); handleSave(); }} noValidate>
          <DialogTitle>
            {isEditMode ? 'Editar Projeto' : 'Cadastrar Projeto'}
          </DialogTitle>
          <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
            <Stack spacing={2.5} sx={{ mt: 1 }}>
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

                <SearchableSelect
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
                </SearchableSelect>
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

                  <SearchableSelect
                    labelId="status-label"
                    value={formStatus}
                    label="Status"
                    onChange={(e) => setFormStatus(e.target.value as Projeto['status'])}
                  >
                    <MenuItem value="ABERTO">Aberto</MenuItem>
                    <MenuItem value="EM_ANDAMENTO">Em Andamento</MenuItem>
                    <MenuItem value="CANCELADO">Cancelado</MenuItem>
                    <MenuItem value="CONCLUIDO">Concluído</MenuItem>
                  </SearchableSelect>
                </FormControl>
              )}
            </Stack>
          </DialogContent>
          <DialogActions>
            <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={() => setDialogOpen(false)} color="secondary" />
            <ActionButton title="Salvar" icon={<SaveIcon />} type="submit" color="primary" />
          </DialogActions>
        </form>
      </Dialog>

      {/* DETAIL SIDEBAR DRAWER */}
      <Drawer
        anchor="right"
        open={selectedProject !== null}
        onClose={() => setSelectedProject(null)}
        sx={{
          '& .MuiDrawer-paper': { width: { xs: '100%', sm: 480, md: 540 }, p: 3, boxSizing: 'border-box' }
        }}
      >
        {selectedProject && (
          <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 3 }}>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.main', mb: 0.5 }}>
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
                    <Typography variant="body2">{getOrgName(selectedProject.grupoParoquialResponsavelId)}</Typography>
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 700, display: 'block', mb: 0.5 }}>
                    STATUS
                  </Typography>
                  <StatusIcon {...getProjetoStatusMeta(selectedProject.status)} variant="outlined" />
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
                      sx={{ height: 8 }}
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
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center' }}>
                          <Typography variant="caption" color="textSecondary" sx={{ display: 'block' }}>TOTAL</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 700 }}>{resumo.statusExecucao.totalEventos}</Typography>
                        </Paper>
                      </Grid>
                      <Grid size={{ xs: 4 }}>
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center', borderColor: 'success.light' }}>
                          <Typography variant="caption" color="success.main" sx={{ display: 'block' }}>REALIZADOS</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 700, color: 'success.main' }}>{resumo.statusExecucao.eventosRealizados}</Typography>
                        </Paper>
                      </Grid>
                      <Grid size={{ xs: 4 }}>
                        <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center', borderColor: 'warning.light' }}>
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
                          <StatusIcon key={env} color="default" icon={<InfoIcon />} label={env} tooltip="Pastoral/grupo envolvido no projeto" />
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

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 1.5, pt: 1 }}>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, alignItems: 'center' }}>
                {canManageProjetos() && selectedProject.status !== 'CANCELADO' && selectedProject.status !== 'CONCLUIDO' && (
                  <ActionButton title="Editar" icon={<EditIcon />} color="primary" onClick={() => handleOpenEdit(selectedProject)} />
                )}
                {selectedProject.status === 'ABERTO' && canTransitionProjeto(selectedProject) && (
                  <ActionButton title="Iniciar Projeto" icon={<PlayArrowIcon />} color="success" onClick={() => handleOpenTransition('INICIAR')} />
                )}
                {selectedProject.status === 'EM_ANDAMENTO' && canTransitionProjeto(selectedProject) && (
                  <ActionButton title="Concluir Projeto" icon={<CheckCircleIcon />} color="primary" onClick={() => handleOpenTransition('CONCLUIR')} />
                )}
                {(selectedProject.status === 'ABERTO' || selectedProject.status === 'EM_ANDAMENTO') && canTransitionProjeto(selectedProject) && (
                  <ActionButton title="Cancelar Projeto" icon={<CancelIcon />} color="error" onClick={() => handleOpenTransition('CANCELAR')} />
                )}
              </Box>
              <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={() => setSelectedProject(null)} />
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
        sx={{ '& .MuiDialog-paper': {  } }}
      >
        <DialogTitle>
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
        <DialogActions>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            onClick={() => setTransitionDialogOpen(false)}
            color="secondary"
            disabled={isTransitioning}
          />
          <ActionButton
            title={transitionType === 'CANCELAR' ? 'Confirmar Cancelamento' : transitionType === 'INICIAR' ? 'Iniciar Projeto' : 'Concluir Projeto'}
            icon={isTransitioning ? <CircularProgress size={20} color="inherit" /> : (transitionType === 'CANCELAR' ? <BlockIcon /> : transitionType === 'INICIAR' ? <PlayArrowIcon /> : <CheckCircleIcon />)}
            onClick={handleTransitionSubmit}
            color={transitionType === 'CANCELAR' ? 'error' : 'primary'}
            disabled={isTransitioning}
          />
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Projetos;
