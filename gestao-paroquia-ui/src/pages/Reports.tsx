import React, {useMemo, useState} from 'react';
import {
    Alert,
    Box,
    Card,
    CardContent,
    Chip,
    Grid,
    LinearProgress,
    Paper,
    Skeleton,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    Typography,
    Tabs,
    Tab,
} from '@mui/material';
import MetricasEstrategicasTab from '../components/reports/MetricasEstrategicasTab';
import {
    Assessment as AssessmentIcon,
    Assignment as ProjectIcon,
    Event as EventIcon,
    FilterList as FilterIcon,
    PendingActions as PendingIcon,
    Warning as WarningIcon,
} from '@mui/icons-material';
import {useQuery} from '@tanstack/react-query';
import {getDashboardMetrics} from '../api/dashboard';
import {getOrganizacoes} from '../api/organizacoes';
import apiClient from '../api/client';
import {DashboardMetrics, Organizacao, WeeklyEvent} from '../api/types';
import ReportFilters from '../components/reports/ReportFilters';
import ReportExportButtons from '../components/reports/ReportExportButtons';
import EventDistributionPieChart from '../components/dashboard/EventDistributionPieChart';
import EventVolumeBarChart from '../components/reports/EventVolumeBarChart';
import {downloadCSV, exportElementAsPDF, generateMetricsCSV} from '../utils/exportUtils';
import {useNotification} from '../context/NotificationContext';
import PageHeader from '../components/PageHeader';
import StatusIcon from '../components/StatusIcon';
import { getProjetoStatusMeta, type ProjetoStatus } from '../utils/statusMeta/projetoStatus';

const Reports: React.FC = () => {
  const { showError, showSuccess } = useNotification();
  const [appliedStartDate, setAppliedStartDate] = useState('');
  const [appliedEndDate, setAppliedEndDate] = useState('');
  const [isExporting, setIsExporting] = useState(false);
  const [currentTab, setCurrentTab] = useState(0);
  const [reportsPage, setReportsPage] = useState(0);
  const [reportsRowsPerPage, setReportsRowsPerPage] = useState(10);

  // Fetch baseline metrics from dashboard endpoint
  const {
    data: metricsData,
    isLoading: isMetricsLoading,
    error: metricsError,
  } = useQuery<DashboardMetrics>({
    queryKey: ['dashboardMetrics'],
    queryFn: getDashboardMetrics,
  });

  // Fetch organizations list for resolving names
  const { data: orgsData } = useQuery<Organizacao[]>({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
  });

  const orgMap = useMemo(() => {
    const map: Record<string, string> = {};
    if (orgsData) {
      orgsData.forEach((o) => {
        map[o.id] = o.nome;
      });
    }
    return map;
  }, [orgsData]);

  const isFiltered = Boolean(appliedStartDate || appliedEndDate);

  // Fetch filtered events when date filter is applied via button
  const {
    data: customEventsData,
    isLoading: isCustomEventsLoading,
  } = useQuery<WeeklyEvent[]>({
    queryKey: ['reportCustomEvents', appliedStartDate, appliedEndDate],
    queryFn: async () => {
      const params: Record<string, any> = { size: 500 };
      if (appliedStartDate) params.dataInicio = `${appliedStartDate}T00:00:00Z`;
      if (appliedEndDate) params.dataFim = `${appliedEndDate}T23:59:59Z`;

      const response = await apiClient.get<{ content?: Record<string, unknown>[] } | Record<string, unknown>[]>('/eventos', { params });
      const resData = response.data;
      const rawEvents = (resData && !Array.isArray(resData) && Array.isArray(resData.content))
        ? resData.content
        : Array.isArray(resData)
        ? resData
        : [];

      return rawEvents.map((ev: Record<string, unknown>) => ({
        titulo: (ev.titulo as string) || (ev.nome as string) || 'Evento sem título',
        inicio: (ev.inicio as string) || (ev.dataInicio as string) || (ev.criadoEmUtc as string) || '',
        localNome: (ev.local as { nome?: string })?.nome || (ev.localNome as string) || 'Sem Local',
        organizacaoNome:
          (ev.organizacao as { nome?: string })?.nome ||
          orgMap[ev.organizacaoResponsavelId as string] ||
          (ev.organizacaoNome as string) ||
          'Paróquia',
      }));
    },
    enabled: isFiltered,
  });

  const handleApplyFilters = (start: string, end: string) => {
    setAppliedStartDate(start);
    setAppliedEndDate(end);
  };

  const handleClearFilters = () => {
    setAppliedStartDate('');
    setAppliedEndDate('');
  };

  // Combine baseline metrics with custom date filter results
  const filteredMetrics = useMemo<DashboardMetrics | null>(() => {
    if (!metricsData) return null;

    if (!isFiltered) {
      return metricsData;
    }

    const eventsList = customEventsData || [];

    // Aggregate counts per organization from fetched filtered events
    const orgCountMap: Record<string, number> = {};
    eventsList.forEach((ev) => {
      const orgName = ev.organizacaoNome || 'Paróquia';
      orgCountMap[orgName] = (orgCountMap[orgName] || 0) + 1;
    });

    const filteredEventosPorOrganizacao = Object.entries(orgCountMap).map(
      ([organizacaoNome, quantidadeEventos]) => ({
        organizacaoNome,
        quantidadeEventos,
      })
    );

    return {
      ...metricsData,
      resumoGeral: {
        ...metricsData.resumoGeral,
        totalEventosMes: eventsList.length,
      },
      eventosPorOrganizacao: filteredEventosPorOrganizacao,
      eventosSemana: eventsList,
    };
  }, [metricsData, isFiltered, customEventsData]);

  // Status breakdown of projects
  const projectStatusBreakdown = useMemo(() => {
    if (!metricsData?.projetosAtivosDetalhes) return [];
    const counts: Record<string, number> = {};
    metricsData.projetosAtivosDetalhes.forEach((p) => {
      const st = p.status || 'OUTROS';
      counts[st] = (counts[st] || 0) + 1;
    });
    return Object.entries(counts).map(([status, count]) => ({ status, count }));
  }, [metricsData]);

  const handleExportCSV = () => {
    if (!filteredMetrics) return;
    try {
      const csvStr = generateMetricsCSV(filteredMetrics);
      downloadCSV(csvStr, `relatorio-gerencial-${new Date().toISOString().slice(0, 10)}.csv`);
      showSuccess('Relatório CSV exportado com sucesso!');
    } catch (err) {
      console.error('Error exporting CSV:', err);
      showError('Falha ao exportar relatório CSV.');
    }
  };

  const handleExportPDF = async () => {
    setIsExporting(true);
    try {
      const success = await exportElementAsPDF('reports-container', 'relatorio-gerencial.pdf');
      if (success) {
        showSuccess('Relatório visual preparado para impressão/PDF.');
      } else {
        showError('Falha ao gerar relatório PDF.');
      }
    } catch (err) {
      console.error('Error exporting PDF:', err);
      showError('Erro ao exportar PDF.');
    } finally {
      setIsExporting(false);
    }
  };

  const isLoading = isMetricsLoading || (isFiltered && isCustomEventsLoading);

  if (isLoading) {
    return (
      <Box sx={{ p: 1 }}>
        <Skeleton variant="text" width={320} height={50} sx={{ mb: 3 }} />
        <Grid container spacing={3}>
          {[1, 2, 3].map((i) => (
            <Grid key={i} size={{ xs: 12, md: 4 }}>
              <Skeleton variant="rounded" height={130} />
            </Grid>
          ))}
        </Grid>
        <Box sx={{ mt: 4 }}>
          <Skeleton variant="rounded" height={300} />
        </Box>
      </Box>
    );
  }

  if (metricsError || !metricsData || !filteredMetrics) {
    return (
      <Box sx={{ p: 1 }}>
        <PageHeader
          icon={<AssessmentIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
          title="Relatórios & Métricas Gerenciais"
        />
        <Alert severity="error">
          Não foi possível carregar os relatórios. Verifique sua conexão ou permissões.
        </Alert>
      </Box>
    );
  }

  const summaryCards = [
    {
      title: isFiltered ? 'Eventos no Período' : 'Total Eventos no Mês',
      value: filteredMetrics.resumoGeral?.totalEventosMes ?? 0,
      icon: EventIcon,
      color: '#1a237e',
      bgGradient: 'linear-gradient(135deg, #e8eaf6 0%, #c5cae9 100%)',
    },
    {
      title: 'Projetos Ativos',
      value: filteredMetrics.resumoGeral?.projetosAtivos ?? 0,
      icon: ProjectIcon,
      color: '#00796b',
      bgGradient: 'linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%)',
    },
    {
      title: 'Solicitações Pendentes',
      value: filteredMetrics.resumoGeral?.solicitacoesPendentes ?? 0,
      icon: PendingIcon,
      color: '#f57c00',
      bgGradient: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)',
    },
  ];

  return (
    <Box id="reports-container" sx={{ p: 1 }}>
      <PageHeader
        icon={<AssessmentIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Relatórios & Métricas Gerenciais"
        subtitle="Painel de controle com dados consolidados da paróquia e indicadores de desempenho."
        actions={
          <ReportExportButtons
            onExportCSV={handleExportCSV}
            onExportPDF={handleExportPDF}
            isExporting={isExporting}
          />
        }
      />

      {/* Tabs de Seleção de Relatório */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs
          value={currentTab}
          onChange={(_, val) => setCurrentTab(val)}
          indicatorColor="primary"
          textColor="primary"
        >
          <Tab label="Indicadores Operacionais" sx={{ fontWeight: 600 }} />
          <Tab label="Métricas Estratégicas & Preditivas" sx={{ fontWeight: 600 }} />
        </Tabs>
      </Box>

      {currentTab === 1 ? (
        <MetricasEstrategicasTab />
      ) : (
        <>


      {/* Filter Bar */}
      <Card
        elevation={0}
        sx={{
          p: 2,
          mb: 3,
          
          border: '1px solid',
          borderColor: 'divider',
          backgroundColor: '#fafafa',
        }}
      >
        <ReportFilters
          startDate={appliedStartDate}
          endDate={appliedEndDate}
          onApplyFilters={handleApplyFilters}
          onClearFilters={handleClearFilters}
        />
      </Card>

      {/* Active Filter Notification Banner */}
      {isFiltered && (
        <Alert
          severity="info"
          variant="outlined"
          icon={<FilterIcon />}
          sx={{ mb: 3, borderLeft: '4px solid #1a237e' }}
          onClose={handleClearFilters}
        >
          Filtro ativo de <strong>{appliedStartDate || 'início do registro'}</strong> até{' '}
          <strong>{appliedEndDate || 'data atual'}</strong> — Exibindo <strong>{filteredMetrics.eventosSemana.length}</strong> evento(s) no período selecionado.
        </Alert>
      )}

      {/* KPI Cards */}
      <Grid container spacing={3}>
        {summaryCards.map((card, idx) => {
          const IconComp = card.icon;
          return (
            <Grid key={idx} size={{ xs: 12, md: 4 }}>
              <Card
                elevation={0}
                sx={{
                  
                  border: '1px solid',
                  borderColor: 'divider',
                  background: 'linear-gradient(to bottom, #ffffff, #fcfcfc)',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.02)',
                  transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                  '&:hover': {
                    transform: 'translateY(-2px)',
                    boxShadow: '0 8px 20px rgba(0,0,0,0.06)',
                  },
                }}
              >
                <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Box>
                      <Typography color="text.secondary" variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
                        {card.title}
                      </Typography>
                      <Typography variant="h3" sx={{ fontWeight: 800, color: 'text.primary' }}>
                        {card.value}
                      </Typography>
                    </Box>
                    <Box
                      sx={{
                        p: 1.5,
                        background: card.bgGradient,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <IconComp sx={{ fontSize: 32, color: card.color }} />
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      {/* Charts Section */}
      <Grid container spacing={3} sx={{ mt: 1 }}>
        {/* PieChart - Event Distribution */}
        <Grid size={{ xs: 12, md: 6 }}>
          <EventDistributionPieChart data={filteredMetrics.eventosPorOrganizacao || []} />
        </Grid>

        {/* BarChart - Volume Comparison */}
        <Grid size={{ xs: 12, md: 6 }}>
          <EventVolumeBarChart data={filteredMetrics.eventosPorOrganizacao || []} />
        </Grid>
      </Grid>

      {/* Projects Status & Progress Section */}
      <Grid container spacing={3} sx={{ mt: 1 }}>
        <Grid size={{ xs: 12 }}>
          <Card
            elevation={0}
            sx={{
              p: 3,
              
              border: '1px solid',
              borderColor: 'divider',
            }}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <ProjectIcon sx={{ color: '#1a237e' }} />
                <Typography variant="h6" sx={{ fontWeight: 600, color: '#1a237e' }}>
                  Acompanhamento de Projetos Ativos & Status
                </Typography>
              </Box>

              {projectStatusBreakdown.length > 0 && (
                <Stack direction="row" spacing={1}>
                  {projectStatusBreakdown.map((b, i) => (
                    <Chip
                      key={i}
                      label={`${b.status}: ${b.count}`}
                      size="small"
                      variant="outlined"
                      sx={{ fontWeight: 600 }}
                    />
                  ))}
                </Stack>
              )}
            </Box>

            {filteredMetrics.projetosAtivosDetalhes && filteredMetrics.projetosAtivosDetalhes.length > 0 ? (
              <Grid container spacing={2} sx={{ mt: 0.5 }}>
                {filteredMetrics.projetosAtivosDetalhes.map((proj, idx) => (
                  <Grid key={idx} size={{ xs: 12, md: 6 }}>
                    <Box sx={{ p: 2, border: '1px solid #eee', backgroundColor: '#fafafa', height: '100%', boxSizing: 'border-box' }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                        <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#1a237e' }}>
                          {proj.nome}
                        </Typography>
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                          {proj.emRisco && (
                            <Chip
                              icon={<WarningIcon sx={{ fontSize: 14 }} />}
                              label="Em Risco"
                              color="error"
                              size="small"
                              sx={{ height: 22, fontSize: '0.7rem' }}
                            />
                          )}
                          <StatusIcon {...getProjetoStatusMeta(proj.status as ProjetoStatus)} size="small" sx={{ height: 22, fontSize: '0.7rem' }} />
                        </Stack>
                      </Box>

                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.8 }}>
                        <Typography variant="caption" color="text.secondary">
                          Eventos: {proj.eventosRealizados} / {proj.totalEventos} realizados
                        </Typography>
                        <Typography variant="caption" sx={{ fontWeight: 600 }}>
                          {Math.round(proj.percentualTempoDecorrido)}% tempo decorrido
                        </Typography>
                      </Box>

                      <LinearProgress
                        variant="determinate"
                        value={Math.min(100, Math.max(0, proj.percentualTempoDecorrido))}
                        sx={{
                          height: 8,
                          
                          backgroundColor: '#e0e0e0',
                          '& .MuiLinearProgress-bar': {
                            backgroundColor: proj.emRisco ? '#d32f2f' : '#1a237e',
                          },
                        }}
                      />

                      {proj.envolvidos && proj.envolvidos.length > 0 && (
                        <Box sx={{ mt: 1.5, display: 'flex', alignItems: 'center', gap: 0.5, flexWrap: 'wrap' }}>
                          <Typography variant="caption" color="text.secondary" sx={{ mr: 0.5 }}>
                            Envolvidos:
                          </Typography>
                          {proj.envolvidos.map((env, eIdx) => (
                            <Chip key={eIdx} label={env} size="small" variant="outlined" sx={{ height: 18, fontSize: '0.65rem' }} />
                          ))}
                        </Box>
                      )}
                    </Box>
                  </Grid>
                ))}
              </Grid>
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                Nenhum projeto ativo no momento.
              </Typography>
            )}
          </Card>
        </Grid>
      </Grid>

      {/* Events Table / Timeline Section */}
      <Card
        elevation={0}
        sx={{
          mt: 4,
          p: 3,
          
          border: '1px solid',
          borderColor: 'divider',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <AssessmentIcon sx={{ color: '#1a237e' }} />
          <Typography variant="h6" sx={{ fontWeight: 600, color: '#1a237e' }}>
            Atividades Recentes & Eventos {isFiltered ? 'no Período' : 'da Semana'}
          </Typography>
        </Box>

        {filteredMetrics.eventosSemana && filteredMetrics.eventosSemana.length > 0 ? (
          <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid #eee' }}>
            <Table size="small">
              <TableHead sx={{ backgroundColor: 'action.hover' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 700 }}>Título do Evento</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Organização</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Local</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Data / Horário</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredMetrics.eventosSemana
                  .slice(reportsPage * reportsRowsPerPage, reportsPage * reportsRowsPerPage + reportsRowsPerPage)
                  .map((ev, idx) => (
                    <TableRow key={idx} hover>
                      <TableCell sx={{ fontWeight: 600 }}>{ev.titulo}</TableCell>
                      <TableCell>{ev.organizacaoNome || '-'}</TableCell>
                      <TableCell>{ev.localNome || '-'}</TableCell>
                      <TableCell>
                        {new Date(ev.inicio).toLocaleString('pt-BR', {
                          day: '2-digit',
                          month: '2-digit',
                          year: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
            <TablePagination
              rowsPerPageOptions={[10, 20, 50, 100]}
              component="div"
              count={filteredMetrics.eventosSemana.length}
              rowsPerPage={reportsRowsPerPage}
              page={reportsPage}
              onPageChange={(_, newPage) => setReportsPage(newPage)}
              onRowsPerPageChange={(e) => {
                setReportsRowsPerPage(parseInt(e.target.value, 10));
                setReportsPage(0);
              }}
              labelRowsPerPage="Itens por página:"
            />
          </TableContainer>
        ) : (
          <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
            Nenhum evento encontrado no período selecionado.
          </Typography>
        )}
      </Card>
      </>
      )}
    </Box>
  );
};

export default Reports;
