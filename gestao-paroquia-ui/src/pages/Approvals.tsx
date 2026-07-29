import React, {useEffect, useState} from 'react';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {
    Alert,
    Box,
    Button,
    Chip,
    CircularProgress,
    Container,
    Divider,
    Drawer,
    IconButton,
    Paper,
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
    Typography,
} from '@mui/material';
import {Close as CloseIcon, EventNote as EventNoteIcon, FactCheck as FactCheckIcon, WorkHistory as WorkHistoryIcon} from '@mui/icons-material';
import {decideApproval, getApprovals} from '../api/approvals';
import {decideProjectApproval, getProjectApprovals} from '../api/projects';
import {AprovacaoResponse, Projeto, ProjetoAprovacaoResponse} from '../api/types';
import {useNotification} from '../context/NotificationContext';
import apiClient from '../api/client';

const Approvals = () => {
  const { showError, showSuccess } = useNotification();
  const [approvalsType, setApprovalsType] = useState<'EVENTOS' | 'PROJETOS'>('EVENTOS');
  const [tabValue, setTabValue] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [selectedApproval, setSelectedApproval] = useState<AprovacaoResponse | null>(null);
  const [selectedProjectApproval, setSelectedProjectApproval] = useState<ProjetoAprovacaoResponse | null>(null);
  const [projectsMap, setProjectsMap] = useState<Record<string, string>>({});

  useEffect(() => {
    const loadProjects = async () => {
      try {
        const res = await apiClient.get<Projeto[]>('/projetos');
        const mapping: Record<string, string> = {};
        if (Array.isArray(res.data)) {
          res.data.forEach((p) => {
            mapping[p.id] = p.nome;
          });
        }
        setProjectsMap(mapping);
      } catch (err) {
        console.error('Error loading projects mapping:', err);
      }
    };
    loadProjects();
  }, []);

  const statuses: ('PENDENTE' | 'APROVADA' | 'REPROVADA' | 'FALHA_EXECUCAO')[] = [
    'PENDENTE',
    'APROVADA',
    'REPROVADA',
    'FALHA_EXECUCAO',
  ];

  const currentStatus = statuses[tabValue];

  const getProjectStatus = () => {
    if (tabValue === 3) return 'PENDENTE';
    return statuses[tabValue] as 'PENDENTE' | 'APROVADA' | 'REPROVADA';
  };

  const { data, isLoading, isError } = useQuery({
    queryKey: ['approvals', currentStatus, page, size],
    queryFn: () => getApprovals(currentStatus, page, size),
    enabled: approvalsType === 'EVENTOS',
  });

  const { data: projectData, isLoading: isProjectLoading, isError: isProjectError } = useQuery({
    queryKey: ['projectApprovals', getProjectStatus()],
    queryFn: () => getProjectApprovals(getProjectStatus()),
    enabled: approvalsType === 'PROJETOS',
  });

  const queryClient = useQueryClient();
  const [observacao, setObservacao] = useState('');

  const decisionMutation = useMutation({
    mutationFn: ({ id, status, observacao }: { id: string; status: 'APROVADA' | 'REPROVADA'; observacao: string }) =>
      decideApproval(id, { status, observacao }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['approvals'] });
      showSuccess(variables.status === 'APROVADA' ? 'Solicitação aprovada com sucesso!' : 'Solicitação rejeitada com sucesso!');
      setSelectedApproval(null);
      setObservacao('');
    },
    onError: (error: any) => {
      console.error('Error deciding approval:', error);
      const errMsg = error.response?.data?.message || 'Erro ao registrar decisão.';
      showError(errMsg);
    },
  });

  const projectDecisionMutation = useMutation({
    mutationFn: ({ id, aprovado, observacao }: { id: string; aprovado: boolean; observacao: string }) =>
      decideProjectApproval(id, aprovado, observacao),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['projectApprovals'] });
      showSuccess(variables.aprovado ? 'Solicitação de projeto aprovada com sucesso!' : 'Solicitação de projeto rejeitada com sucesso!');
      setSelectedProjectApproval(null);
      setObservacao('');
    },
    onError: (error: any) => {
      console.error('Error deciding project approval:', error);
      const errMsg = error.response?.data?.message || 'Erro ao registrar decisão.';
      showError(errMsg);
    },
  });

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setPage(0); // Reset page on filter change
  };

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSize(parseInt(event.target.value, 10));
    setPage(0);
  };

  const getTipoLabel = (tipo: string) => {
    switch (tipo) {
      case 'CRIACAO_EVENTO':
        return 'Criação de Evento';
      case 'EDICAO_EVENTO':
      case 'ALTERACAO_HORARIO':
        return 'Edição de Evento';
      case 'CANCELAMENTO':
        return 'Cancelamento de Evento';
      default:
        return tipo;
    }
  };

  const getStatusChip = (status: string) => {
    switch (status) {
      case 'PENDENTE':
        return <Chip label="Pendente" color="warning" size="small" />;
      case 'APROVADA':
        return <Chip label="Aprovada" color="success" size="small" />;
      case 'REPROVADA':
        return <Chip label="Rejeitada" color="error" size="small" />;
      case 'FALHA_EXECUCAO':
        return <Chip label="Falha" color="error" size="small" variant="outlined" />;
      default:
        return <Chip label={status} size="small" />;
    }
  };

  const parsePayload = (jsonStr: string | null) => {
    if (!jsonStr) return null;
    try {
      return JSON.parse(jsonStr);
    } catch {
      return null;
    }
  };

  const getEventTitle = (approval: AprovacaoResponse) => {
    const payload = parsePayload(approval.actionPayloadJson);
    return payload?.titulo || 'Sem título / Cancelamento';
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      {/* Header Padronizado */}
      <Box sx={{ mb: 4, display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ p: 1.5, bgcolor: 'rgba(26, 35, 126, 0.08)', borderRadius: 0, display: 'flex' }}>
            <FactCheckIcon sx={{ fontSize: 32, color: 'primary.main' }} />
          </Box>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main', letterSpacing: '-0.5px' }}>
              Solicitações de Aprovação
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Gerencie e analise as solicitações de alteração e novos eventos ou projetos no calendário paroquial.
            </Typography>
          </Box>
        </Box>
      </Box>

        {/* TOP LEVEL TAB SELECTOR */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
          <Tabs
            value={approvalsType}
            onChange={(e, val) => {
              setApprovalsType(val);
              setTabValue(0);
              setPage(0);
              setSelectedApproval(null);
              setSelectedProjectApproval(null);
            }}
            indicatorColor="primary"
            textColor="primary"
          >
            <Tab label="Solicitações de Eventos" value="EVENTOS" sx={{ textTransform: 'none', fontWeight: 600 }} />
            <Tab label="Solicitações de Projetos" value="PROJETOS" sx={{ textTransform: 'none', fontWeight: 600 }} />
          </Tabs>
        </Box>

        <Paper sx={{ width: '100%', mb: 4, borderRadius: 0, overflow: 'hidden', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
          <Tabs
            value={tabValue}
            onChange={handleTabChange}
            indicatorColor="primary"
            textColor="primary"
            sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: '#fff' }}
          >
            <Tab label="Pendentes" />
            <Tab label="Aprovadas" />
            <Tab label="Rejeitadas" />
            {approvalsType === 'EVENTOS' && <Tab label="Falhas" />}
          </Tabs>

          {/* EVENT APPROVALS VIEW */}
          {approvalsType === 'EVENTOS' && (
            <Box sx={{ p: 2.5, pb: 2, display: 'flex', alignItems: 'center', gap: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', borderRadius: 0, display: 'flex' }}>
                <EventNoteIcon sx={{ fontSize: 26, color: 'primary.main' }} />
              </Box>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                  Solicitações de Eventos Paroquiais
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.2 }}>
                  Gerencie e analise pedidos de inclusão, alteração e cancelamento de eventos no calendário.
                </Typography>
              </Box>
            </Box>
          )}

          {/* PROJECT APPROVALS VIEW */}
          {approvalsType === 'PROJETOS' && (
            <Box sx={{ p: 2.5, pb: 2, display: 'flex', alignItems: 'center', gap: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', borderRadius: 0, display: 'flex' }}>
                <WorkHistoryIcon sx={{ fontSize: 26, color: 'primary.main' }} />
              </Box>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                  Solicitações de Projetos Pastorais
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.2 }}>
                  Analise as propostas de novos projetos pastorais submetidos para aprovação paroquial.
                </Typography>
              </Box>
            </Box>
          )}

          {/* EVENT APPROVALS CONTENT */}
          {approvalsType === 'EVENTOS' && (
            isLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 8 }}>
                <CircularProgress />
              </Box>
            ) : isError ? (
              <Box sx={{ p: 4 }}>
                <Alert severity="error">Não foi possível carregar as solicitações de aprovação.</Alert>
              </Box>
            ) : !data || data.content.length === 0 ? (
              <Box sx={{ p: 8, textAlign: 'center' }}>
                <Typography variant="body1" color="textSecondary">
                  Nenhuma solicitação de evento encontrada para o status selecionado.
                </Typography>
              </Box>
            ) : (
              <>
                <TableContainer>
                  <Table>
                    <TableHead sx={{ bgcolor: '#f8f9fa' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Evento / Detalhe</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Solicitante</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Data Criação</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                        <TableCell sx={{ fontWeight: 600 }} align="right">Ações</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {data.content.map((approval) => (
                        <TableRow key={approval.id} hover>
                          <TableCell sx={{ fontWeight: 500 }}>
                            {getTipoLabel(approval.tipoSolicitacao)}
                          </TableCell>
                          <TableCell>
                            {approval.tipoSolicitacao === 'CANCELAMENTO' ? (
                              <Typography variant="body2" color="error" sx={{ fontWeight: 500 }}>
                                Cancelamento de Evento
                              </Typography>
                            ) : (
                              <Typography variant="body2" sx={{ fontWeight: 500 }}>
                                {getEventTitle(approval)}
                              </Typography>
                            )}
                          </TableCell>
                          <TableCell>{approval.solicitanteId}</TableCell>
                          <TableCell>
                            {new Date(approval.criadoEmUtc).toLocaleString()}
                          </TableCell>
                          <TableCell>{getStatusChip(approval.status)}</TableCell>
                          <TableCell align="right">
                            <Button
                              variant="outlined"
                              size="small"
                              onClick={() => setSelectedApproval(approval)}
                              sx={{ textTransform: 'none' }}
                            >
                              Visualizar
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
                <TablePagination
                  rowsPerPageOptions={[5, 10, 25]}
                  component="div"
                  count={data.totalElements}
                  rowsPerPage={size}
                  page={page}
                  onPageChange={handleChangePage}
                  onRowsPerPageChange={handleChangeRowsPerPage}
                />
              </>
            )
          )}

          {/* PROJECT APPROVALS VIEW */}
          {approvalsType === 'PROJETOS' && (
            isProjectLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 8 }}>
                <CircularProgress />
              </Box>
            ) : isProjectError ? (
              <Box sx={{ p: 4 }}>
                <Alert severity="error">Não foi possível carregar as solicitações de projetos.</Alert>
              </Box>
            ) : !projectData || projectData.length === 0 ? (
              <Box sx={{ p: 8, textAlign: 'center' }}>
                <Typography variant="body1" color="textSecondary">
                  Nenhuma solicitação de projeto encontrada para o status selecionado.
                </Typography>
              </Box>
            ) : (
              <TableContainer>
                <Table>
                  <TableHead sx={{ bgcolor: '#f8f9fa' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Projeto / Detalhe</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Solicitante</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Data Criação</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      <TableCell sx={{ fontWeight: 600 }} align="right">Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {projectData.map((approval) => (
                      <TableRow key={approval.id} hover>
                        <TableCell sx={{ fontWeight: 500 }}>
                          {approval.tipoSolicitacao === 'CRIACAO' ? 'Criação de Projeto' : 'Cancelamento de Projeto'}
                        </TableCell>
                        <TableCell>
                          {approval.tipoSolicitacao === 'CRIACAO' ? (
                            <Typography variant="body2" sx={{ fontWeight: 500 }}>
                              {parsePayload(approval.actionPayloadJson)?.nome || 'Novo Projeto'}
                            </Typography>
                          ) : (
                            <Typography variant="body2" color="error" sx={{ fontWeight: 500 }}>
                              {projectsMap[approval.projetoId || ''] || 'Projeto Solicitado'}
                            </Typography>
                          )}
                        </TableCell>
                        <TableCell>{approval.solicitanteId}</TableCell>
                        <TableCell>
                          {new Date(approval.criadoEmUtc).toLocaleString()}
                        </TableCell>
                        <TableCell>{getStatusChip(approval.status)}</TableCell>
                        <TableCell align="right">
                          <Button
                            variant="outlined"
                            size="small"
                            onClick={() => setSelectedProjectApproval(approval)}
                            sx={{ textTransform: 'none' }}
                          >
                            Visualizar
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                </TableContainer>
              )
            )}
        </Paper>

      {/* Details Drawer */}
      <Drawer
        anchor="right"
        open={Boolean(selectedApproval)}
        onClose={() => setSelectedApproval(null)}
        slotProps={{
          paper: {
            sx: { width: { xs: '100%', sm: 450 }, p: 3 }
          }
        }}
      >
        {selectedApproval && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Detalhes da Solicitação
              </Typography>
              <IconButton onClick={() => setSelectedApproval(null)}>
                <CloseIcon />
              </IconButton>
            </Box>
            <Divider sx={{ mb: 3 }} />

            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              INFORMAÇÕES GERAIS
            </Typography>
            <Box sx={{ mb: 3 }}>
              <Typography variant="body2"><strong>Solicitante:</strong> {selectedApproval.solicitanteId}</Typography>
              <Typography variant="body2"><strong>Tipo:</strong> {getTipoLabel(selectedApproval.tipoSolicitacao)}</Typography>
              <Typography variant="body2"><strong>Criado em:</strong> {new Date(selectedApproval.criadoEmUtc).toLocaleString()}</Typography>
              <Typography variant="body2"><strong>Status:</strong> {selectedApproval.status}</Typography>
            </Box>

            <Typography variant="subtitle2" color="textSecondary" gutterBottom sx={{ mt: 2 }}>
              DETALHES DA PROPOSTA
            </Typography>
            <Box sx={{ mb: 3, p: 2, bgcolor: '#f8f9fa', borderRadius: 0 }}>
              {selectedApproval.tipoSolicitacao === 'CANCELAMENTO' ? (
                <>
                  <Typography variant="body2" color="error" sx={{ fontWeight: 600, mb: 1 }}>
                    Solicitação de Cancelamento
                  </Typography>
                  <Typography variant="body2">
                    <strong>Motivo:</strong> {parsePayload(selectedApproval.actionPayloadJson)?.motivo || 'Não informado'}
                  </Typography>
                </>
              ) : (
                <>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Título do Evento:</strong> {parsePayload(selectedApproval.actionPayloadJson)?.titulo || 'Sem título'}
                  </Typography>
                  {parsePayload(selectedApproval.actionPayloadJson)?.inicio && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Início:</strong> {new Date(parsePayload(selectedApproval.actionPayloadJson).inicio).toLocaleString()}
                    </Typography>
                  )}
                  {parsePayload(selectedApproval.actionPayloadJson)?.fim && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Fim:</strong> {new Date(parsePayload(selectedApproval.actionPayloadJson).fim).toLocaleString()}
                    </Typography>
                  )}
                  {parsePayload(selectedApproval.actionPayloadJson)?.descricao && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Descrição:</strong> {parsePayload(selectedApproval.actionPayloadJson).descricao}
                    </Typography>
                  )}
                </>
              )}
            </Box>

            {selectedApproval.status === 'PENDENTE' && (
              <Box sx={{ mt: 4 }}>
                <Divider sx={{ mb: 3 }} />
                <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                  TOMADA DE DECISÃO
                </Typography>
                <TextField
                  fullWidth
                  label="Observações"
                  multiline
                  rows={4}
                  value={observacao}
                  onChange={(e) => setObservacao(e.target.value.slice(0, 2000))}
                  placeholder="Justificativa para aprovação ou rejeição (opcional)..."
                  variant="outlined"
                  sx={{ mb: 2 }}
                  helperText={`${observacao.length}/2000 caracteres`}
                />
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Button
                    variant="contained"
                    color="success"
                    fullWidth
                    onClick={() => decisionMutation.mutate({ id: selectedApproval.id, status: 'APROVADA', observacao })}
                    disabled={decisionMutation.isPending}
                  >
                    Aprovar
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    fullWidth
                    onClick={() => decisionMutation.mutate({ id: selectedApproval.id, status: 'REPROVADA', observacao })}
                    disabled={decisionMutation.isPending}
                  >
                    Rejeitar
                  </Button>
                </Box>
              </Box>
            )}
          </Box>
        )}
      </Drawer>

      {/* Project Details Drawer */}
      <Drawer
        anchor="right"
        open={Boolean(selectedProjectApproval)}
        onClose={() => setSelectedProjectApproval(null)}
        slotProps={{
          paper: {
            sx: { width: { xs: '100%', sm: 450 }, p: 3 }
          }
        }}
      >
        {selectedProjectApproval && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Detalhes da Solicitação de Projeto
              </Typography>
              <IconButton onClick={() => setSelectedProjectApproval(null)}>
                <CloseIcon />
              </IconButton>
            </Box>
            <Divider sx={{ mb: 3 }} />

            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              INFORMAÇÕES GERAIS
            </Typography>
            <Box sx={{ mb: 3 }}>
              <Typography variant="body2"><strong>Solicitante:</strong> {selectedProjectApproval.solicitanteId}</Typography>
              <Typography variant="body2">
                <strong>Tipo:</strong> {selectedProjectApproval.tipoSolicitacao === 'CRIACAO' ? 'Criação de Projeto' : 'Cancelamento de Projeto'}
              </Typography>
              <Typography variant="body2"><strong>Criado em:</strong> {new Date(selectedProjectApproval.criadoEmUtc).toLocaleString()}</Typography>
              <Typography variant="body2"><strong>Status:</strong> {selectedProjectApproval.status}</Typography>
            </Box>

            <Typography variant="subtitle2" color="textSecondary" gutterBottom sx={{ mt: 2 }}>
              DETALHES DA PROPOSTA
            </Typography>
            <Box sx={{ mb: 3, p: 2, bgcolor: '#f8f9fa', borderRadius: 0 }}>
              {selectedProjectApproval.tipoSolicitacao === 'CANCELAMENTO' ? (
                <>
                  <Typography variant="body2" color="error" sx={{ fontWeight: 600, mb: 1 }}>
                    Solicitação de Cancelamento
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Projeto:</strong> {projectsMap[selectedProjectApproval.projetoId || ''] || 'Projeto Solicitado'}
                  </Typography>
                  <Typography variant="body2">
                    <strong>Cancelar Eventos Vinculados:</strong> {selectedProjectApproval.cancelarEventosVinculados ? 'Sim' : 'Não (Apenas desvincular)'}
                  </Typography>
                </>
              ) : (
                <>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Nome do Projeto:</strong> {parsePayload(selectedProjectApproval.actionPayloadJson)?.nome || 'Sem nome'}
                  </Typography>
                  {parsePayload(selectedProjectApproval.actionPayloadJson)?.descricao && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Descrição:</strong> {parsePayload(selectedProjectApproval.actionPayloadJson).descricao}
                    </Typography>
                  )}
                  {parsePayload(selectedProjectApproval.actionPayloadJson)?.inicio && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Início:</strong> {new Date(parsePayload(selectedProjectApproval.actionPayloadJson).inicio).toLocaleString()}
                    </Typography>
                  )}
                  {parsePayload(selectedProjectApproval.actionPayloadJson)?.fim && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Fim:</strong> {new Date(parsePayload(selectedProjectApproval.actionPayloadJson).fim).toLocaleString()}
                    </Typography>
                  )}
                </>
              )}
            </Box>

            {selectedProjectApproval.status === 'PENDENTE' && (
              <Box sx={{ mt: 4 }}>
                <Divider sx={{ mb: 3 }} />
                <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                  TOMADA DE DECISÃO
                </Typography>
                <TextField
                  fullWidth
                  label="Observações"
                  multiline
                  rows={4}
                  value={observacao}
                  onChange={(e) => setObservacao(e.target.value.slice(0, 2000))}
                  placeholder="Justificativa para aprovação ou rejeição (opcional)..."
                  variant="outlined"
                  sx={{ mb: 2 }}
                  helperText={`${observacao.length}/2000 caracteres`}
                />
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Button
                    variant="contained"
                    color="success"
                    fullWidth
                    onClick={() => projectDecisionMutation.mutate({ id: selectedProjectApproval.id, aprovado: true, observacao })}
                    disabled={projectDecisionMutation.isPending}
                  >
                    Aprovar
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    fullWidth
                    onClick={() => projectDecisionMutation.mutate({ id: selectedProjectApproval.id, aprovado: false, observacao })}
                    disabled={projectDecisionMutation.isPending}
                  >
                    Rejeitar
                  </Button>
                </Box>
              </Box>
            )}
          </Box>
        )}
      </Drawer>
    </Container>
  );
};

export default Approvals;
