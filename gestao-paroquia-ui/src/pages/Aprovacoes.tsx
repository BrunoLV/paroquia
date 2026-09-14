import React, {useEffect, useState} from 'react';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {
    Alert,
    Box,
    Card,
    Container,
    Divider,
    Drawer,
    Skeleton,
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
    useTheme,
} from '@mui/material';
import {
    Cancel as CancelIcon,
    CheckCircle as CheckCircleIcon,
    Close as CloseIcon,
    EventNote as EventNoteIcon,
    FactCheck as FactCheckIcon,
    Visibility as VisibilityIcon,
    WorkHistory as WorkHistoryIcon,
} from '@mui/icons-material';
import {decideAprovacao, getAprovacoes} from '../api/aprovacoes';
import {decideAprovacaoProjeto, getAprovacoesProjeto} from '../api/projects';
import {AprovacaoResponse, Projeto, ProjetoAprovacaoResponse} from '../api/types';
import {useNotification} from '../context/NotificationContext';
import {extractErrorMessage} from '../utils/errorUtils';
import apiClient from '../api/client';
import PageHeader from '../components/PageHeader';
import ActionButton from '../components/ActionButton';
import TableActionMenu from '../components/TableActionMenu';
import StatusIcon from '../components/StatusIcon';
import {getAprovacaoDetalhe, getAprovacaoStatusMeta, getAprovacaoTipoMeta} from '../utils/statusMeta/aprovacaoStatus';

/**
 * Tela de "caixa de entrada" de aprovações pastorais.
 *
 * Eventos e Projetos têm fluxos de aprovação independentes no backend (endpoints,
 * DTOs e regras de transição próprios — AprovacaoEntity para eventos/ativos e um
 * mecanismo equivalente para projetos), por isso esta tela usa duas fontes de dados
 * e dois conjuntos de estado (`tipoAprovacao` chaveia qual delas é exibida) em vez de
 * uma única lista unificada. Cada solicitação pendente representa uma mudança que
 * ainda não foi aplicada ao calendário/projeto real — ela só é efetivada quando um
 * aprovador decide (ver `decisionMutation`/`projectDecisionMutation` abaixo).
 */
const Aprovacoes = () => {
  const theme = useTheme();
  const { showError, showSuccess } = useNotification();
  // tipoAprovacao alterna a fonte de dados exibida (eventos x projetos); tabValue
  // filtra por status dentro dessa fonte (ver array `statuses`).
  const [tipoAprovacao, setTipoAprovacao] = useState<'EVENTOS' | 'PROJETOS'>('EVENTOS');
  const [tabValue, setTabValue] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const [projPage, setProjPage] = useState(0);
  const [projRowsPerPage, setProjRowsPerPage] = useState(10);
  // Solicitação selecionada abre o drawer de detalhes/decisão; cada tipo de
  // aprovação tem seu próprio drawer porque os payloads (actionPayloadJson) e as
  // regras de exibição diferem entre evento e projeto.
  const [aprovacaoSelecionada, setAprovacaoSelecionada] = useState<AprovacaoResponse | null>(null);
  const [aprovacaoProjetoSelecionada, setAprovacaoProjetoSelecionada] = useState<ProjetoAprovacaoResponse | null>(null);
  // Cache local id->nome de projetos, usado apenas para exibir o nome do projeto em
  // solicitações de cancelamento (que referenciam só o projetoId, não o nome).
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
      }
    };
    loadProjects();
  }, []);

  // Espelha o ciclo de vida de AprovacaoStatus no backend: toda solicitação nasce
  // PENDENTE e transiciona para APROVADA/REPROVADA quando um aprovador decide, ou
  // para FALHA_EXECUCAO quando a decisão foi aprovada mas a aplicação da mudança no
  // domínio (ex.: criar o evento de fato) falhou depois — aba exclusiva de Eventos,
  // pois projetos não têm hoje uma etapa de execução assíncrona separada da decisão.
  const statuses: ('PENDENTE' | 'APROVADA' | 'REPROVADA' | 'FALHA_EXECUCAO')[] = [
    'PENDENTE',
    'APROVADA',
    'REPROVADA',
    'FALHA_EXECUCAO',
  ];

  const currentStatus = statuses[tabValue];

  // Projetos não têm a aba "Falhas" (tabValue 3 não existe na UI de projetos), mas
  // como as duas listas de abas compartilham o mesmo `tabValue`, se o usuário tiver
  // deixado a aba em índice 3 ao trocar de EVENTOS->PROJETOS caímos de volta em
  // PENDENTE em vez de estourar o índice inexistente.
  const getProjectStatus = () => {
    if (tabValue === 3) return 'PENDENTE';
    return statuses[tabValue] as 'PENDENTE' | 'APROVADA' | 'REPROVADA';
  };

  const { data, isLoading, isError } = useQuery({
    queryKey: ['aprovacoes', currentStatus, page, size],
    queryFn: () => getAprovacoes(currentStatus, page, size),
    enabled: tipoAprovacao === 'EVENTOS',
  });

  const { data: projectData, isLoading: isProjectLoading, isError: isProjectError } = useQuery({
    queryKey: ['aprovacoesProjeto', getProjectStatus()],
    queryFn: () => getAprovacoesProjeto(getProjectStatus()),
    enabled: tipoAprovacao === 'PROJETOS',
  });

  const queryClient = useQueryClient();
  const [observacao, setObservacao] = useState('');

  // Decisão de aprovação de EVENTO/ATIVO. No backend isso dispara
  // DecideSolicitacaoAprovacaoUseCase: se status = APROVADA, o payload gravado na
  // solicitação (actionPayloadJson) é finalmente aplicado ao domínio (criação/edição/
  // cancelamento do evento ou empréstimo do ativo) — é só nesse momento que a mudança
  // passa a existir de fato; se REPROVADA, o payload é descartado e nada muda no
  // calendário. Caso a aplicação em si falhe após a aprovação, o status vira
  // FALHA_EXECUCAO (fora do controle desta mutation, refletido só ao recarregar).
  const decisionMutation = useMutation({
    mutationFn: ({ id, status, observacao }: { id: string; status: 'APROVADA' | 'REPROVADA'; observacao: string }) =>
      decideAprovacao(id, { status, observacao }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['aprovacoes'] });
      showSuccess(variables.status === 'APROVADA' ? 'Solicitação aprovada com sucesso!' : 'Solicitação rejeitada com sucesso!');
      setAprovacaoSelecionada(null);
      setObservacao('');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao registrar decisão.'));
    },
  });

  // Decisão de aprovação de PROJETO — mesma semântica de negócio do fluxo de
  // eventos (aprovar aplica a criação/cancelamento do projeto, rejeitar descarta a
  // solicitação), porém via endpoint e use case próprios de projeto (aceita um
  // booleano `aprovado` em vez do enum de status usado para eventos).
  const projectDecisionMutation = useMutation({
    mutationFn: ({ id, aprovado, observacao }: { id: string; aprovado: boolean; observacao: string }) =>
      decideAprovacaoProjeto(id, aprovado, observacao),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['aprovacoesProjeto'] });
      showSuccess(variables.aprovado ? 'Solicitação de projeto aprovada com sucesso!' : 'Solicitação de projeto rejeitada com sucesso!');
      setAprovacaoProjetoSelecionada(null);
      setObservacao('');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao registrar decisão.'));
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

  const parsePayload = (jsonStr: string | null) => {
    if (!jsonStr) return null;
    try {
      return JSON.parse(jsonStr);
    } catch {
      return null;
    }
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <PageHeader
        icon={<FactCheckIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Solicitações de Aprovação"
        subtitle="Gerencie e analise as solicitações de alteração e novos eventos ou projetos no calendário paroquial."
      />

        {/* TOP LEVEL TAB SELECTOR: alterna entre as duas fontes de dados independentes
            (aprovações de EVENTOS x de PROJETOS). Trocar aqui reseta a subaba de
            status, a paginação e fecha qualquer drawer aberto, pois os índices de
            aba/página e as seleções não fazem sentido ao mudar de contexto. */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
          <Tabs
            value={tipoAprovacao}
            onChange={(e, val) => {
              setTipoAprovacao(val);
              setTabValue(0);
              setPage(0);
              setAprovacaoSelecionada(null);
              setAprovacaoProjetoSelecionada(null);
            }}
            indicatorColor="primary"
            textColor="primary"
          >
            <Tab label="Solicitações de Eventos" value="EVENTOS" sx={{ textTransform: 'none', fontWeight: 600 }} />
            <Tab label="Solicitações de Projetos" value="PROJETOS" sx={{ textTransform: 'none', fontWeight: 600 }} />
          </Tabs>
        </Box>

        <Card sx={{ width: '100%', mb: 4, overflow: 'hidden' }}>
          <Tabs
            value={tabValue}
            onChange={handleTabChange}
            indicatorColor="primary"
            textColor="primary"
            sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: 'background.paper' }}
          >
            <Tab label="Pendentes" />
            <Tab label="Aprovadas" />
            <Tab label="Rejeitadas" />
            {tipoAprovacao === 'EVENTOS' && <Tab label="Falhas" />}
          </Tabs>

          {/* EVENT APPROVALS VIEW */}
          {tipoAprovacao === 'EVENTOS' && (
            <Box sx={{ p: 2.5, pb: 2, display: 'flex', alignItems: 'center', gap: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', display: 'flex' }}>
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
          {tipoAprovacao === 'PROJETOS' && (
            <Box sx={{ p: 2.5, pb: 2, display: 'flex', alignItems: 'center', gap: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', display: 'flex' }}>
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
          {tipoAprovacao === 'EVENTOS' && (
            isLoading ? (
              <Box sx={{ p: 3 }}>
                <Skeleton variant="rounded" height={280} />
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
                    <TableHead sx={{ backgroundColor: 'action.hover' }}>
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
                            <StatusIcon {...getAprovacaoTipoMeta(approval.tipoSolicitacao)} />
                          </TableCell>
                          <TableCell>
                            {approval.tipoSolicitacao === 'CANCELAMENTO' ? (
                              <Typography variant="body2" color="error" sx={{ fontWeight: 500 }}>
                                Cancelamento de Evento
                              </Typography>
                            ) : (
                              <Typography variant="body2" sx={{ fontWeight: 500 }}>
                                {getAprovacaoDetalhe(approval)}
                              </Typography>
                            )}
                          </TableCell>
                          <TableCell>{approval.solicitanteId}</TableCell>
                          <TableCell>
                            {new Date(approval.criadoEmUtc).toLocaleString()}
                          </TableCell>
                          <TableCell><StatusIcon {...getAprovacaoStatusMeta(approval.status)} /></TableCell>
                          <TableCell align="right">
                            <TableActionMenu actions={[{ label: 'Visualizar detalhes', icon: <VisibilityIcon fontSize="small" />, onClick: () => setAprovacaoSelecionada(approval) }]} />
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
                  labelRowsPerPage="Linhas por página:"
                  labelDisplayedRows={({ from, to, count }) =>
                    `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
                  }
                />
              </>
            )
          )}

          {/* PROJECT APPROVALS VIEW */}
          {tipoAprovacao === 'PROJETOS' && (
            isProjectLoading ? (
              <Box sx={{ p: 3 }}>
                <Skeleton variant="rounded" height={280} />
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
              <>
                <TableContainer>
                  <Table>
                    <TableHead sx={{ backgroundColor: 'action.hover' }}>
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
                      {projectData
                        .slice(projPage * projRowsPerPage, projPage * projRowsPerPage + projRowsPerPage)
                        .map((approval) => (
                          <TableRow key={approval.id} hover>
                            <TableCell sx={{ fontWeight: 500 }}>
                              <StatusIcon {...getAprovacaoTipoMeta(approval.tipoSolicitacao)} />
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
                            <TableCell><StatusIcon {...getAprovacaoStatusMeta(approval.status)} /></TableCell>
                            <TableCell align="right">
                              <TableActionMenu actions={[{ label: 'Visualizar detalhes', icon: <VisibilityIcon fontSize="small" />, onClick: () => setAprovacaoProjetoSelecionada(approval) }]} />
                            </TableCell>
                          </TableRow>
                        ))}
                    </TableBody>
                  </Table>
                </TableContainer>
                <TablePagination
                  rowsPerPageOptions={[5, 10, 25]}
                  component="div"
                  count={projectData.length}
                  rowsPerPage={projRowsPerPage}
                  page={projPage}
                  onPageChange={(_, newPage) => setProjPage(newPage)}
                  onRowsPerPageChange={(e) => {
                    setProjRowsPerPage(parseInt(e.target.value, 10));
                    setProjPage(0);
                  }}
                  labelRowsPerPage="Linhas por página:"
                  labelDisplayedRows={({ from, to, count }) =>
                    `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
                  }
                />
              </>
            )
          )}
        </Card>

      {/* Details Drawer: mostra o payload da solicitação de evento/ativo e, apenas
          quando status === PENDENTE, expõe os botões Aprovar/Rejeitar — solicitações
          já decididas (APROVADA/REPROVADA/FALHA_EXECUCAO) são somente leitura, pois
          a decisão no backend é definitiva e não pode ser revertida por aqui. */}
      <Drawer
        anchor="right"
        open={Boolean(aprovacaoSelecionada)}
        onClose={() => setAprovacaoSelecionada(null)}
        slotProps={{
          paper: {
            sx: { width: { xs: '100%', sm: 480, md: 540 }, p: 3 }
          }
        }}
      >
        {aprovacaoSelecionada && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Detalhes da Solicitação
              </Typography>
              <ActionButton title="Fechar" icon={<CloseIcon />} onClick={() => setAprovacaoSelecionada(null)} />
            </Box>
            <Divider sx={{ mb: 3 }} />

            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              INFORMAÇÕES GERAIS
            </Typography>
            <Box sx={{ mb: 3 }}>
              <Typography variant="body2"><strong>Solicitante:</strong> {aprovacaoSelecionada.solicitanteId}</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                <Typography variant="body2"><strong>Tipo:</strong></Typography>
                <StatusIcon {...getAprovacaoTipoMeta(aprovacaoSelecionada.tipoSolicitacao)} />
              </Box>
              <Typography variant="body2"><strong>Criado em:</strong> {new Date(aprovacaoSelecionada.criadoEmUtc).toLocaleString()}</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                <Typography variant="body2"><strong>Status:</strong></Typography>
                <StatusIcon {...getAprovacaoStatusMeta(aprovacaoSelecionada.status)} />
              </Box>
            </Box>

            <Typography variant="subtitle2" color="textSecondary" gutterBottom sx={{ mt: 2 }}>
              DETALHES DA PROPOSTA
            </Typography>
            <Box sx={{ mb: 3, p: 2, bgcolor: theme.custom?.subtlePanelBg ?? '#F8F9FA' }}>
              {aprovacaoSelecionada.tipoSolicitacao === 'CANCELAMENTO' ? (
                <>
                  <Typography variant="body2" color="error" sx={{ fontWeight: 600, mb: 1 }}>
                    Solicitação de Cancelamento
                  </Typography>
                  <Typography variant="body2">
                    <strong>Motivo:</strong> {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.motivo || 'Não informado'}
                  </Typography>
                </>
              ) : aprovacaoSelecionada.tipoSolicitacao === 'ALOCACAO_ATIVO_EMPRESTIMO' ? (
                <>
                  <Typography variant="body2" sx={{ fontWeight: 600, mb: 1 }}>
                    Solicitação de Empréstimo de Ativo
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Ativo:</strong> {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.ativoNome || 'Não informado'}
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Organização dona:</strong> {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.grupoParoquialResponsavelId || 'Não informado'}
                  </Typography>
                  <Typography variant="body2">
                    <strong>Justificativa:</strong> {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.motivo || 'Não informado'}
                  </Typography>
                </>
              ) : (
                <>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Título do Evento:</strong> {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.titulo || 'Sem título'}
                  </Typography>
                  {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.inicio && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Início:</strong> {new Date(parsePayload(aprovacaoSelecionada.actionPayloadJson).inicio).toLocaleString()}
                    </Typography>
                  )}
                  {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.fim && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Fim:</strong> {new Date(parsePayload(aprovacaoSelecionada.actionPayloadJson).fim).toLocaleString()}
                    </Typography>
                  )}
                  {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.descricao && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Descrição:</strong> {parsePayload(aprovacaoSelecionada.actionPayloadJson).descricao}
                    </Typography>
                  )}
                </>
              )}
            </Box>

            {aprovacaoSelecionada.status === 'PENDENTE' && (
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
                  <ActionButton
                    title="Aprovar solicitação"
                    icon={<CheckCircleIcon />}
                    color="success"
                    onClick={() => decisionMutation.mutate({ id: aprovacaoSelecionada.id, status: 'APROVADA', observacao })}
                    disabled={decisionMutation.isPending}
                    sx={{ border: '1px solid', borderColor: 'success.main' }}
                  />
                  <ActionButton
                    title="Rejeitar solicitação"
                    icon={<CancelIcon />}
                    color="error"
                    onClick={() => decisionMutation.mutate({ id: aprovacaoSelecionada.id, status: 'REPROVADA', observacao })}
                    disabled={decisionMutation.isPending}
                    sx={{ border: '1px solid', borderColor: 'error.main' }}
                  />
                </Box>
              </Box>
            )}
          </Box>
        )}
      </Drawer>

      {/* Project Details Drawer: mesma lógica do drawer de eventos acima, aplicada à
          solicitação de projeto — Aprovar/Rejeitar só aparece com status PENDENTE. */}
      <Drawer
        anchor="right"
        open={Boolean(aprovacaoProjetoSelecionada)}
        onClose={() => setAprovacaoProjetoSelecionada(null)}
        slotProps={{
          paper: {
            sx: { width: { xs: '100%', sm: 480, md: 540 }, p: 3 }
          }
        }}
      >
        {aprovacaoProjetoSelecionada && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Detalhes da Solicitação de Projeto
              </Typography>
              <ActionButton
                title="Fechar painel"
                icon={<CloseIcon />}
                onClick={() => setAprovacaoProjetoSelecionada(null)}
              />
            </Box>
            <Divider sx={{ mb: 3 }} />

            <Typography variant="subtitle2" color="textSecondary" gutterBottom>
              INFORMAÇÕES GERAIS
            </Typography>
            <Box sx={{ mb: 3 }}>
              <Typography variant="body2"><strong>Solicitante:</strong> {aprovacaoProjetoSelecionada.solicitanteId}</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                <Typography variant="body2"><strong>Tipo:</strong></Typography>
                <StatusIcon {...getAprovacaoTipoMeta(aprovacaoProjetoSelecionada.tipoSolicitacao)} />
              </Box>
              <Typography variant="body2"><strong>Criado em:</strong> {new Date(aprovacaoProjetoSelecionada.criadoEmUtc).toLocaleString()}</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                <Typography variant="body2"><strong>Status:</strong></Typography>
                <StatusIcon {...getAprovacaoStatusMeta(aprovacaoProjetoSelecionada.status)} />
              </Box>
            </Box>

            <Typography variant="subtitle2" color="textSecondary" gutterBottom sx={{ mt: 2 }}>
              DETALHES DA PROPOSTA
            </Typography>
            <Box sx={{ mb: 3, p: 2, bgcolor: theme.custom?.subtlePanelBg ?? '#F8F9FA' }}>
              {aprovacaoProjetoSelecionada.tipoSolicitacao === 'CANCELAMENTO' ? (
                <>
                  <Typography variant="body2" color="error" sx={{ fontWeight: 600, mb: 1 }}>
                    Solicitação de Cancelamento
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Projeto:</strong> {projectsMap[aprovacaoProjetoSelecionada.projetoId || ''] || 'Projeto Solicitado'}
                  </Typography>
                  <Typography variant="body2">
                    <strong>Cancelar Eventos Vinculados:</strong> {aprovacaoProjetoSelecionada.cancelarEventosVinculados ? 'Sim' : 'Não (Apenas desvincular)'}
                  </Typography>
                </>
              ) : (
                <>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Nome do Projeto:</strong> {parsePayload(aprovacaoProjetoSelecionada.actionPayloadJson)?.nome || 'Sem nome'}
                  </Typography>
                  {parsePayload(aprovacaoProjetoSelecionada.actionPayloadJson)?.descricao && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Descrição:</strong> {parsePayload(aprovacaoProjetoSelecionada.actionPayloadJson).descricao}
                    </Typography>
                  )}
                  {parsePayload(aprovacaoProjetoSelecionada.actionPayloadJson)?.inicio && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Início:</strong> {new Date(parsePayload(aprovacaoProjetoSelecionada.actionPayloadJson).inicio).toLocaleString()}
                    </Typography>
                  )}
                  {parsePayload(aprovacaoProjetoSelecionada.actionPayloadJson)?.fim && (
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      <strong>Fim:</strong> {new Date(parsePayload(aprovacaoProjetoSelecionada.actionPayloadJson).fim).toLocaleString()}
                    </Typography>
                  )}
                </>
              )}
            </Box>

            {aprovacaoProjetoSelecionada.status === 'PENDENTE' && (
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
                  <ActionButton
                    title="Aprovar solicitação de projeto"
                    icon={<CheckCircleIcon />}
                    color="success"
                    onClick={() => projectDecisionMutation.mutate({ id: aprovacaoProjetoSelecionada.id, aprovado: true, observacao })}
                    disabled={projectDecisionMutation.isPending}
                    sx={{ border: '1px solid', borderColor: 'success.main' }}
                  />
                  <ActionButton
                    title="Rejeitar solicitação de projeto"
                    icon={<CancelIcon />}
                    color="error"
                    onClick={() => projectDecisionMutation.mutate({ id: aprovacaoProjetoSelecionada.id, aprovado: false, observacao })}
                    disabled={projectDecisionMutation.isPending}
                    sx={{ border: '1px solid', borderColor: 'error.main' }}
                  />
                </Box>
              </Box>
            )}
          </Box>
        )}
      </Drawer>
    </Container>
  );
};

export default Aprovacoes;
