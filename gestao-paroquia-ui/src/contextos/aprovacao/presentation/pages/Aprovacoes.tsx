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
    Tooltip,
    Typography,
    Chip,
    useTheme,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import CancelIcon from '@mui/icons-material/Cancel';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CloseIcon from '@mui/icons-material/Close';
import EventNoteIcon from '@mui/icons-material/EventNote';
import FactCheckIcon from '@mui/icons-material/FactCheck';
import VisibilityIcon from '@mui/icons-material/Visibility';
import WorkHistoryIcon from '@mui/icons-material/WorkHistory';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutlineOutlined';
import ReplayIcon from '@mui/icons-material/Replay';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlineOutlined';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PainelConflitosAprovacao from '@/contextos/aprovacao/presentation/components/PainelConflitosAprovacao';
import OcorrenciasRecorrenciaAprovacao from '@/contextos/aprovacao/presentation/components/OcorrenciasRecorrenciaAprovacao';
import {decideAprovacao, getAprovacoes, getAprovacaoOcorrencias, reexecutarAprovacao} from '@/contextos/aprovacao/infrastructure/http/aprovacoes';
import {getLocais} from '@/contextos/local/infrastructure/http/locais';
import {getAtivos} from '@/contextos/patrimonio/infrastructure/http/ativos';
import {decidirAprovacaoProjeto, obterAprovacoesProjeto} from '@/contextos/projeto/infrastructure/http/projetos';
import {approveUsuario, getUsuarios} from '@/contextos/iam/infrastructure/http/usuarios';
import {canApproveUsuario} from '@/contextos/iam/presentation/auth/permissions';
import {useAuth} from '@/contextos/iam/presentation/auth/AuthContext';
import {AprovacaoResponse, Projeto, ProjetoAprovacaoResponse} from '@/shared/domain/types/types';
import {getFormatoEventoLabel, getFormatoEventoIcon} from '@/contextos/calendario/presentation/utils/formatoEventoUtils';
import {formatarHorarioEvento, obterLabelTurno} from '@/contextos/calendario/presentation/utils/horarioEventoUtils';
import {formatarResumoRecorrencia} from '@/contextos/calendario/presentation/utils/recorrenciaFormatUtils';
import {useNotification} from '@/shared/presentation/context/NotificationContext';
import {extractErrorMessage} from '@/shared/utils/errorUtils';
import apiClient from '@/shared/infrastructure/http/client';
import PageHeader from '@/shared/presentation/components/PageHeader';
import ActionButton from '@/shared/presentation/components/ActionButton';
import TableActionMenu from '@/shared/presentation/components/TableActionMenu';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import {getUsuarioSituacaoMeta} from '@/shared/utils/statusMeta/situacaoStatus';
import {FormattedPhone} from '@/shared/presentation/components/FormattedCell';
import {getAprovacaoDetalhe, getAprovacaoStatusMeta, getAprovacaoTipoMeta} from '@/shared/utils/statusMeta/aprovacaoStatus';

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
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useNotification();
  // tipoAprovacao alterna a fonte de dados exibida (eventos x projetos x usuários); tabValue
  // filtra por status dentro dessa fonte (ver array `statuses`).
  const [tipoAprovacao, setTipoAprovacao] = useState<'EVENTOS' | 'PROJETOS' | 'USUARIOS'>('EVENTOS');
  const [tabValue, setTabValue] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const [projPage, setProjPage] = useState(0);
  const [projRowsPerPage, setProjRowsPerPage] = useState(10);

  const [usersPage, setUsersPage] = useState(0);
  const [usersRowsPerPage, setUsersRowsPerPage] = useState(10);
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
    queryFn: () => obterAprovacoesProjeto(getProjectStatus()),
    enabled: tipoAprovacao === 'PROJETOS',
  });

  const { data: usersData = [], isLoading: isUsersLoading, isError: isUsersError } = useQuery({
    queryKey: ['usuarios'],
    queryFn: getUsuarios,
    enabled: tipoAprovacao === 'USUARIOS',
  });

  const approveUserMutation = useMutation({
    mutationFn: (userId: string) => approveUsuario(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      showSuccess('Usuário aprovado com sucesso!');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao aprovar usuário.'));
    },
  });

  const { data: ocorrenciasFilhas } = useQuery<AprovacaoResponse[]>({
    queryKey: ['aprovacao-ocorrencias', aprovacaoSelecionada?.id],
    queryFn: () => getAprovacaoOcorrencias(aprovacaoSelecionada!.id),
    enabled: Boolean(aprovacaoSelecionada?.id && aprovacaoSelecionada?.totalOcorrenciasFilhas && aprovacaoSelecionada.totalOcorrenciasFilhas > 0),
  });

  const todasFilhasDecididas = Boolean(
    aprovacaoSelecionada?.totalOcorrenciasFilhas &&
    aprovacaoSelecionada.totalOcorrenciasFilhas > 0 &&
    ocorrenciasFilhas &&
    ocorrenciasFilhas.length > 0 &&
    ocorrenciasFilhas.every((item: AprovacaoResponse) => item.status !== 'PENDENTE')
  );

  const { data: locaisList = [] } = useQuery({
    queryKey: ['locais'],
    queryFn: getLocais,
  });

  const { data: ativosList = [] } = useQuery({
    queryKey: ['ativos'],
    queryFn: getAtivos,
  });

  const locaisMap = React.useMemo(() => {
    const map = new Map<string, string>();
    if (Array.isArray(locaisList)) {
      locaisList.forEach((loc) => map.set(loc.id, loc.nome));
    }
    return map;
  }, [locaisList]);

  const ativosMap = React.useMemo(() => {
    const map = new Map<string, string>();
    if (Array.isArray(ativosList)) {
      ativosList.forEach((at) => map.set(at.id, at.nome));
    }
    return map;
  }, [ativosList]);

  const obterNomeLocal = (id: string): string => locaisMap.get(id) || id;
  const obterNomeAtivo = (id: string): string => ativosMap.get(id) || id;

  const temConflitoRecurso = React.useMemo(() => {
    if (!aprovacaoSelecionada?.conflitosJson) return false;
    try {
      const list = JSON.parse(aprovacaoSelecionada.conflitosJson);
      if (!Array.isArray(list)) return false;
      return list.some((c: any) => {
        const t = (c.tipo || c.tipoConflito || '').toUpperCase();
        return t === 'LOCAL' || t === 'ATIVO';
      });
    } catch {
      return false;
    }
  }, [aprovacaoSelecionada]);

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
      queryClient.invalidateQueries({ queryKey: ['aprovacao-ocorrencias'] });
      showSuccess(variables.status === 'APROVADA' ? 'Solicitação aprovada com sucesso!' : 'Solicitação rejeitada com sucesso!');
      setAprovacaoSelecionada((prev) => (prev ? { ...prev, status: variables.status } : null));
      setObservacao('');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao registrar decisão.'));
    },
  });

  const decidirOcorrenciaMutation = useMutation({
    mutationFn: ({ id, status, observacao }: { id: string; status: 'APROVADA' | 'REPROVADA'; observacao: string }) =>
      decideAprovacao(id, { status, observacao }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['aprovacao-ocorrencias'] });
      queryClient.invalidateQueries({ queryKey: ['aprovacoes'] });
      showSuccess(variables.status === 'APROVADA' ? 'Ocorrência aprovada com sucesso!' : 'Ocorrência rejeitada com sucesso!');
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Erro ao registrar decisão da ocorrência.'));
    },
  });

  // Decisão de aprovação de PROJETO — mesma semântica de negócio do fluxo de
  // eventos (aprovar aplica a criação/cancelamento do projeto, rejeitar descarta a
  // solicitação), porém via endpoint e use case próprios de projeto (aceita um
  // booleano `aprovado` em vez do enum de status usado para eventos).
  const projectDecisionMutation = useMutation({
    mutationFn: ({ id, aprovado, observacao }: { id: string; aprovado: boolean; observacao: string }) =>
      decidirAprovacaoProjeto(id, aprovado, observacao),
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

  const reexecutarMutation = useMutation({
    mutationFn: (id: string) => reexecutarAprovacao(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['aprovacoes'] });
      showSuccess('Solicitação reexecutada e aprovada com sucesso!');
      setAprovacaoSelecionada((prev) => (prev ? {
        ...prev,
        status: 'APROVADA',
        mensagemErroExecucao: null,
        tipoFalhaExecucao: null,
        detalheErroExecucao: null,
      } : null));
    },
    onError: (error: unknown) => {
      showError(extractErrorMessage(error, 'Falha ao reexecutar aprovação.'));
      queryClient.invalidateQueries({ queryKey: ['aprovacoes'] });
    },
  });

  const handleRecadastrar = () => {
    if (!aprovacaoSelecionada) return;
    const payload = parsePayload(aprovacaoSelecionada.actionPayloadJson);
    if (payload) {
      try {
        sessionStorage.setItem('recadastro_evento_payload', JSON.stringify(payload));
      } catch {
      }
    }
    showSuccess('Dados da proposta carregados para recadastro. Redirecionando para o calendário...');
    navigate('/calendario');
  };

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
            {canApproveUsuario(user) && (
              <Tab label="Solicitações de Usuários" value="USUARIOS" sx={{ textTransform: 'none', fontWeight: 600 }} />
            )}
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
            <Tab label={tipoAprovacao === 'USUARIOS' ? 'Ativos' : 'Aprovadas'} />
            {tipoAprovacao !== 'USUARIOS' && <Tab label="Rejeitadas" />}
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
                      {data.content.filter((approval) => !approval.aprovacaoPaiId).map((approval) => (
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
                            <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5 }}>
                              {approval.conflitosJson && approval.conflitosJson !== '[]' && (
                                <Chip label="Com Conflitos" size="small" color="warning" sx={{ height: 20, fontSize: '0.7rem', fontWeight: 600 }} />
                              )}
                              {Boolean(approval.totalOcorrenciasFilhas && approval.totalOcorrenciasFilhas > 0) && (
                                <Chip label={`Série (${approval.totalOcorrenciasFilhas})`} size="small" color="info" sx={{ height: 20, fontSize: '0.7rem', fontWeight: 600 }} />
                              )}
                            </Box>
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

          {/* USER APPROVALS VIEW */}
          {tipoAprovacao === 'USUARIOS' && (
            <Box sx={{ p: 2.5, pb: 2, display: 'flex', alignItems: 'center', gap: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box sx={{ p: 1.2, bgcolor: 'rgba(26, 35, 126, 0.08)', display: 'flex' }}>
                <PersonAddIcon sx={{ fontSize: 26, color: 'primary.main' }} />
              </Box>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                  Solicitações de Cadastro de Usuários
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.2 }}>
                  Homologue cadastros de usuários submetidos pela secretaria paroquial para liberar o acesso ao sistema.
                </Typography>
              </Box>
            </Box>
          )}

          {/* USER APPROVALS CONTENT */}
          {tipoAprovacao === 'USUARIOS' && (
            isUsersLoading ? (
              <Box sx={{ p: 3 }}>
                <Skeleton variant="rounded" height={280} />
              </Box>
            ) : isUsersError ? (
              <Box sx={{ p: 4 }}>
                <Alert severity="error">Não foi possível carregar os usuários.</Alert>
              </Box>
            ) : (() => {
              const filteredUsers = usersData.filter((u) => (tabValue === 0 ? !u.enabled : u.enabled));
              return filteredUsers.length === 0 ? (
                <Box sx={{ p: 8, textAlign: 'center' }}>
                  <Typography variant="body1" color="textSecondary">
                    {tabValue === 0
                      ? 'Nenhum usuário pendente de aprovação.'
                      : 'Nenhum usuário ativo encontrado.'}
                  </Typography>
                </Box>
              ) : (
                <>
                  <TableContainer>
                    <Table>
                      <TableHead sx={{ backgroundColor: 'action.hover' }}>
                        <TableRow>
                          <TableCell sx={{ fontWeight: 600 }}>Usuário</TableCell>
                          <TableCell sx={{ fontWeight: 600 }}>Nome Completo</TableCell>
                          <TableCell sx={{ fontWeight: 600 }}>Telefone</TableCell>
                          <TableCell sx={{ fontWeight: 600 }}>Comunidade / Igreja</TableCell>
                          <TableCell sx={{ fontWeight: 600 }}>Vínculo</TableCell>
                          <TableCell sx={{ fontWeight: 600 }}>Situação</TableCell>
                          <TableCell sx={{ fontWeight: 600 }} align="right">Ações</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {filteredUsers
                          .slice(usersPage * usersRowsPerPage, usersPage * usersRowsPerPage + usersRowsPerPage)
                          .map((u) => {
                            const situacao = getUsuarioSituacaoMeta(u.enabled);
                            return (
                              <TableRow key={u.id} hover>
                                <TableCell sx={{ fontWeight: 600 }}>{u.username}</TableCell>
                                <TableCell>{u.nome || '-'}</TableCell>
                                <TableCell><FormattedPhone value={u.telefone} /></TableCell>
                                <TableCell>{u.igrejaNome || 'Matriz / Geral'}</TableCell>
                                <TableCell>
                                  {u.fielId ? (
                                    <Chip label="Fiel" size="small" color="primary" variant="outlined" />
                                  ) : (
                                    <Typography variant="caption" color="text.secondary">Geral</Typography>
                                  )}
                                </TableCell>
                                <TableCell>
                                  <StatusIcon {...situacao} />
                                </TableCell>
                                <TableCell align="right">
                                  {!u.enabled && canApproveUsuario(user) ? (
                                    <ActionButton
                                      title="Aprovar Usuário"
                                      icon={<CheckCircleIcon />}
                                      color="success"
                                      onClick={() => approveUserMutation.mutate(u.id)}
                                      disabled={approveUserMutation.isPending}
                                    />
                                  ) : null}
                                </TableCell>
                              </TableRow>
                            );
                          })}
                      </TableBody>
                    </Table>
                  </TableContainer>
                  <TablePagination
                    rowsPerPageOptions={[5, 10, 25]}
                    component="div"
                    count={filteredUsers.length}
                    rowsPerPage={usersRowsPerPage}
                    page={usersPage}
                    onPageChange={(_, newPage) => setUsersPage(newPage)}
                    onRowsPerPageChange={(e) => {
                      setUsersRowsPerPage(parseInt(e.target.value, 10));
                      setUsersPage(0);
                    }}
                    labelRowsPerPage="Linhas por página:"
                    labelDisplayedRows={({ from, to, count }) =>
                      `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
                    }
                  />
                </>
              );
            })()
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
            sx: { width: { xs: '100%', sm: 580, md: 680 }, p: { xs: 2.5, sm: 3.5 }, overflowY: 'auto' }
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

            {aprovacaoSelecionada.aprovacaoPaiId && (
              <Alert severity="info" sx={{ mb: 2.5, borderRadius: 2 }}>
                Esta solicitação é uma ocorrência individual vinculada a uma série recorrente.
              </Alert>
            )}

            {Boolean(aprovacaoSelecionada.totalOcorrenciasFilhas && aprovacaoSelecionada.totalOcorrenciasFilhas > 0) && (
              <Alert severity="info" sx={{ mb: 2.5, borderRadius: 2 }}>
                <strong>Série Recorrente:</strong> Esta solicitação agregadora abrange {aprovacaoSelecionada.totalOcorrenciasFilhas} ocorrências programadas.
              </Alert>
            )}

            <Typography variant="subtitle2" color="textSecondary" gutterBottom sx={{ fontWeight: 700 }}>
              INFORMAÇÕES GERAIS
            </Typography>
            <Box sx={{ mb: 3 }}>
              <Typography variant="body2" sx={{ mb: 0.75 }}><strong>Solicitante:</strong> {aprovacaoSelecionada.solicitanteId}</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.75 }}>
                <Typography variant="body2"><strong>Tipo:</strong></Typography>
                <StatusIcon {...getAprovacaoTipoMeta(aprovacaoSelecionada.tipoSolicitacao)} />
              </Box>
              <Typography variant="body2" sx={{ mb: 0.75 }}><strong>Criado em:</strong> {new Date(aprovacaoSelecionada.criadoEmUtc).toLocaleString('pt-BR')}</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.75 }}>
                <Typography variant="body2"><strong>Status:</strong></Typography>
                <StatusIcon {...getAprovacaoStatusMeta(aprovacaoSelecionada.status)} />
              </Box>
            </Box>

            {aprovacaoSelecionada.justificativa && (
              <Box sx={{ mb: 2.5, p: 2, bgcolor: 'action.hover', borderRadius: 2, borderLeft: '4px solid', borderColor: 'primary.main' }}>
                <Typography variant="caption" sx={{ fontWeight: 700, color: 'primary.main', textTransform: 'uppercase' }}>
                  Justificativa Formal do Solicitante
                </Typography>
                <Typography variant="body2" sx={{ mt: 0.5, fontStyle: 'italic' }}>
                  "{aprovacaoSelecionada.justificativa}"
                </Typography>
              </Box>
            )}

            <Typography variant="subtitle2" color="textSecondary" gutterBottom sx={{ mt: 2, fontWeight: 700 }}>
              DETALHES DA PROPOSTA
            </Typography>
            <Box sx={{ mb: 3, p: 2.5, borderRadius: 2, bgcolor: theme.custom?.subtlePanelBg ?? '#F8F9FA' }}>
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
                    <strong>Ativo:</strong>{' '}
                    {(() => {
                      const payload = parsePayload(aprovacaoSelecionada.actionPayloadJson);
                      return payload?.ativoNome || (payload?.ativoId ? obterNomeAtivo(payload.ativoId) : 'Não informado');
                    })()}
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
                  <Typography variant="body2" sx={{ mb: 1.25 }}>
                    <strong>Título do Evento:</strong> {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.titulo || 'Sem título'}
                  </Typography>
                  {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.formato && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, mb: 1.25 }}>
                      <Typography variant="body2" component="span">
                        <strong>Formato:</strong> {getFormatoEventoLabel(parsePayload(aprovacaoSelecionada.actionPayloadJson).formato)}
                      </Typography>
                      {getFormatoEventoIcon(parsePayload(aprovacaoSelecionada.actionPayloadJson).formato, 'small')}
                    </Box>
                  )}
                  {/* Data, Horário e Recorrência da Proposta */}
                  {(() => {
                    const payload = parsePayload(aprovacaoSelecionada.actionPayloadJson);
                    if (!payload) return null;

                    const rawData = payload.inicio || payload.startDate || payload.dataInicio || aprovacaoSelecionada.dataOcorrenciaUtc;
                    let dataFormatada: string | null = null;
                    if (rawData) {
                      const dt = new Date(rawData);
                      if (!isNaN(dt.getTime())) {
                        const rawStr = dt.toLocaleDateString('pt-BR', {
                          weekday: 'long',
                          day: '2-digit',
                          month: 'long',
                          year: 'numeric',
                        });
                        dataFormatada = rawStr.charAt(0).toUpperCase() + rawStr.slice(1);
                      }
                    }

                    const regraRecorrencia = payload.regraRecorrencia || payload.recurrence;
                    const textoRecorrencia = regraRecorrencia
                      ? formatarResumoRecorrencia(regraRecorrencia, rawData)
                      : null;

                    return (
                      <Box sx={{ mb: 1.5 }}>
                        {dataFormatada && (
                          <Typography variant="body2" sx={{ mb: 0.75 }}>
                            <strong>Data do Evento:</strong> {dataFormatada}
                          </Typography>
                        )}
                        <Typography variant="body2" sx={{ mb: 0.5 }}>
                          <strong>Horário e Período(s):</strong> {formatarHorarioEvento(payload, aprovacaoSelecionada.dataOcorrenciaUtc)}
                        </Typography>
                        {Array.isArray(payload.periodos) && payload.periodos.length > 0 && (
                          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 0.75, mb: 1 }}>
                            {payload.periodos.map((p: any) => (
                              <Chip
                                key={p}
                                label={obterLabelTurno(p)}
                                size="small"
                                color="primary"
                                variant="outlined"
                              />
                            ))}
                          </Box>
                        )}
                        {textoRecorrencia && (
                          <Typography variant="body2" sx={{ mt: 0.75, mb: 0.5 }}>
                            <strong>Recorrência:</strong> {textoRecorrencia}
                          </Typography>
                        )}
                      </Box>
                    );
                  })()}
                  {parsePayload(aprovacaoSelecionada.actionPayloadJson)?.descricao && (
                    <Typography variant="body2" sx={{ mb: 1.5 }}>
                      <strong>Descrição:</strong> {parsePayload(aprovacaoSelecionada.actionPayloadJson).descricao}
                    </Typography>
                  )}
                  {(() => {
                    const payload = parsePayload(aprovacaoSelecionada.actionPayloadJson);
                    const locaisList = Array.isArray(payload?.localIds) && payload.localIds.length > 0
                      ? payload.localIds
                      : (payload?.localId ? [payload.localId] : []);
                    if (locaisList.length === 0) return null;
                    return (
                      <Box sx={{ mt: 1.5, mb: 1.5 }}>
                        <Typography variant="body2" sx={{ mb: 0.75 }}>
                          <strong>{locaisList.length > 1 ? 'Locais Solicitados:' : 'Local Solicitado:'}</strong>
                        </Typography>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                          {locaisList.map((locId: string) => (
                            <Chip key={locId} label={obterNomeLocal(locId)} size="small" variant="outlined" />
                          ))}
                        </Box>
                      </Box>
                    );
                  })()}
                  {(() => {
                    const payload = parsePayload(aprovacaoSelecionada.actionPayloadJson);
                    const ativosList = Array.isArray(payload?.ativoIds) && payload.ativoIds.length > 0
                      ? payload.ativoIds
                      : (payload?.ativoId ? [payload.ativoId] : []);
                    if (ativosList.length === 0) return null;
                    return (
                      <Box sx={{ mt: 1.5, mb: 1 }}>
                        <Typography variant="body2" sx={{ mb: 0.75 }}>
                          <strong>{ativosList.length > 1 ? 'Ativos Solicitados:' : 'Ativo Solicitado:'}</strong>
                        </Typography>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                          {ativosList.map((atId: string) => (
                            <Chip key={atId} label={obterNomeAtivo(atId)} size="small" variant="outlined" />
                          ))}
                        </Box>
                      </Box>
                    );
                  })()}
                </>
              )}
            </Box>

            <PainelConflitosAprovacao conflitosJson={aprovacaoSelecionada.conflitosJson} />

            {Boolean(aprovacaoSelecionada.totalOcorrenciasFilhas && aprovacaoSelecionada.totalOcorrenciasFilhas > 0) && (
              <OcorrenciasRecorrenciaAprovacao
                aprovacaoPaiId={aprovacaoSelecionada.id}
                totalOcorrencias={aprovacaoSelecionada.totalOcorrenciasFilhas}
                statusPai={aprovacaoSelecionada.status}
                onDecidirOcorrencia={(id, status) =>
                  decidirOcorrenciaMutation.mutate({
                    id,
                    status,
                    observacao: observacao || 'Decisão pontual da ocorrência da série',
                  })
                }
                disabled={decidirOcorrenciaMutation.isPending || decisionMutation.isPending}
              />
            )}

            {aprovacaoSelecionada.status === 'PENDENTE' && !todasFilhasDecididas && (
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
                  <Tooltip
                    title={
                      temConflitoRecurso
                        ? 'Não é permitido aprovar: existe conflito de espaço físico ou ativo patrimonial já reservado por outro evento aprovado.'
                        : ''
                    }
                  >
                    <span>
                      <ActionButton
                        title={temConflitoRecurso ? 'Aprovação Bloqueada' : 'Aprovar solicitação'}
                        icon={<CheckCircleIcon />}
                        color="success"
                        onClick={() => decisionMutation.mutate({ id: aprovacaoSelecionada.id, status: 'APROVADA', observacao })}
                        disabled={decisionMutation.isPending || decidirOcorrenciaMutation.isPending || temConflitoRecurso}
                        sx={{ border: '1px solid', borderColor: 'success.main' }}
                      />
                    </span>
                  </Tooltip>
                  <ActionButton
                    title="Rejeitar solicitação"
                    icon={<CancelIcon />}
                    color="error"
                    onClick={() => decisionMutation.mutate({ id: aprovacaoSelecionada.id, status: 'REPROVADA', observacao })}
                    disabled={decisionMutation.isPending || decidirOcorrenciaMutation.isPending}
                    sx={{ border: '1px solid', borderColor: 'error.main' }}
                  />
                </Box>
              </Box>
            )}

            {aprovacaoSelecionada.status === 'FALHA_EXECUCAO' && aprovacaoSelecionada.tipoFalhaExecucao === 'NEGOCIO_CADASTRAL' && (
              <Box sx={{ mt: 4 }}>
                <Divider sx={{ mb: 3 }} />
                <Alert severity="warning" icon={<WarningAmberIcon />} sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    Falha de Negócio / Conflito Cadastral
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 0.5 }}>
                    <strong>Causa:</strong> {aprovacaoSelecionada.mensagemErroExecucao || 'Conflito de espaço físico ou ativo patrimonial já reservado.'}
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 1, fontStyle: 'italic' }}>
                    Conflitos de agenda e regras de negócio são irremediáveis nesta solicitação. É necessário realizar um novo cadastro.
                  </Typography>
                </Alert>
                <ActionButton
                  title="Recadastrar com Dados Desta Proposta"
                  icon={<AddCircleOutlineIcon />}
                  color="warning"
                  onClick={handleRecadastrar}
                  sx={{ border: '1px solid', borderColor: 'warning.main' }}
                />
              </Box>
            )}

            {aprovacaoSelecionada.status === 'FALHA_EXECUCAO' && aprovacaoSelecionada.tipoFalhaExecucao === 'SISTEMICA_APLICACAO' && (
              <Box sx={{ mt: 4 }}>
                <Divider sx={{ mb: 3 }} />
                <Alert severity="error" icon={<ErrorOutlineIcon />} sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    Falha Sistêmica / de Aplicação
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 0.5 }}>
                    <strong>Mensagem:</strong> {aprovacaoSelecionada.mensagemErroExecucao || 'Erro inesperado durante a execução.'}
                  </Typography>
                  {aprovacaoSelecionada.detalheErroExecucao && (
                    <Typography variant="body2" sx={{ mt: 1, fontFamily: 'monospace', fontSize: '0.75rem', whiteSpace: 'pre-wrap' }}>
                      <strong>Detalhes Técnicos:</strong> {aprovacaoSelecionada.detalheErroExecucao}
                    </Typography>
                  )}
                </Alert>
                <ActionButton
                  title="Reexecutar Aprovação"
                  icon={<ReplayIcon />}
                  color="primary"
                  onClick={() => reexecutarMutation.mutate(aprovacaoSelecionada.id)}
                  disabled={reexecutarMutation.isPending}
                  sx={{ border: '1px solid', borderColor: 'primary.main' }}
                />
              </Box>
            )}

            {aprovacaoSelecionada.status === 'PENDENTE' && todasFilhasDecididas && (
              <Box sx={{ mt: 3 }}>
                <Divider sx={{ mb: 2 }} />
                <Alert severity="info">
                  Todas as ocorrências filhas desta série recorrente já foram deliberadas individualmente. A solicitação agregadora não requer decisão manual adicional.
                </Alert>
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
            sx: { width: { xs: '100%', sm: 580, md: 680 }, p: { xs: 2.5, sm: 3.5 } }
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
