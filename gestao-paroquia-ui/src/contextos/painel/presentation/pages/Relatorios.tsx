import SearchableTablePagination from '@/shared/presentation/components/SearchableTablePagination';
import React, {useMemo, useState} from 'react';
import { Alert, alpha, Box, Card, CardContent, Grid, LinearProgress, Paper, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography, Tabs, Tab } from '@mui/material';
import MetricasEstrategicasTab from '@/contextos/painel/presentation/components/relatorios/MetricasEstrategicasTab';
import AssessmentIcon from '@mui/icons-material/Assessment';
import ProjectIcon from '@mui/icons-material/Assignment';
import AutoGraphIcon from '@mui/icons-material/AutoGraph';
import EventIcon from '@mui/icons-material/Event';
import FilterIcon from '@mui/icons-material/FilterList';
import ListIcon from '@mui/icons-material/FormatListBulleted';
import PendingIcon from '@mui/icons-material/PendingActions';
import WarningIcon from '@mui/icons-material/Warning';
import {useQuery} from '@tanstack/react-query';
import {getDashboardMetrics} from '@/contextos/painel/infrastructure/http/dashboard';
import {getOrganizacoes} from '@/contextos/organizacao/infrastructure/http/organizacoes';
import apiClient from '@/shared/infrastructure/http/client';
import {DashboardMetrics, Organizacao, WeeklyEvent} from '@/shared/domain/types/types';
import ReportFilters from '@/contextos/painel/presentation/components/relatorios/ReportFilters';
import ReportExportButtons from '@/contextos/painel/presentation/components/relatorios/ReportExportButtons';
import EventDistributionPieChart from '@/contextos/painel/presentation/components/painel/EventDistributionPieChart';
import EventVolumeBarChart from '@/contextos/painel/presentation/components/relatorios/EventVolumeBarChart';
import { usePainelMetricasEstrategicas } from '@/contextos/painel/presentation/hooks/useMetricasEstrategicas';
import {useNotification} from '@/shared/presentation/context/NotificationContext';
import PageHeader from '@/shared/presentation/components/PageHeader';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { getProjetoStatusMeta, type ProjetoStatus } from '@/shared/utils/statusMeta/projetoStatus';

const Relatorios: React.FC = () => {
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
    queryKey: ['dashboard', 'metrics', 'MES', null],
    queryFn: () => getDashboardMetrics({periodo: 'MES'}),
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
      const params: Record<string, string | number> = { size: 500 };
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
        grupoParoquialNome:
          (ev.organizacao as { nome?: string })?.nome ||
          orgMap[ev.grupoParoquialResponsavelId as string] ||
          (ev.grupoParoquialNome as string) ||
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
      const orgName = ev.grupoParoquialNome || 'Paróquia';
      orgCountMap[orgName] = (orgCountMap[orgName] || 0) + 1;
    });

    const filteredEventosPorGrupoParoquial = Object.entries(orgCountMap).map(
      ([grupoParoquialNome, quantidadeEventos]) => ({
        grupoParoquialNome,
        quantidadeEventos,
      })
    );

    return {
      ...metricsData,
      resumoGeral: {
        ...metricsData.resumoGeral,
        totalEventosPeriodo: eventsList.length,
      },
      eventosPorGrupoParoquial: filteredEventosPorGrupoParoquial,
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

  // Fetch strategic metrics for tab 1 export
  const { data: strategicData } = usePainelMetricasEstrategicas();

  const handleExportCSV = async () => {
    try {
      const {downloadCSV, generateMetricsCSV, generateStrategicMetricsCSV} = await import('@/shared/utils/exportUtils');
      if (currentTab === 1) {
        if (!strategicData) {
          showError('Dados de métricas estratégicas ainda não carregados.');
          return;
        }
        const csvStr = generateStrategicMetricsCSV(strategicData);
        downloadCSV(csvStr, `metricas-estrategicas-${new Date().toISOString().slice(0, 10)}.csv`);
        showSuccess('Métricas Estratégicas CSV exportadas com sucesso!');
      } else {
        if (!filteredMetrics) return;
        const csvStr = generateMetricsCSV(filteredMetrics);
        downloadCSV(csvStr, `relatorio-gerencial-${new Date().toISOString().slice(0, 10)}.csv`);
        showSuccess('Relatório CSV exportado com sucesso!');
      }
    } catch {
      showError('Falha ao exportar relatório CSV.');
    }
  };

  const handleExportPDF = async () => {
    setIsExporting(true);
    try {
      const {generateStrategicStructuredPDF, generateStructuredPDF} = await import('@/shared/utils/exportUtils');
      const dateStr = new Date().toISOString().slice(0, 10);
      let success = false;

      if (currentTab === 1) {
        if (!strategicData) {
          showError('Dados de métricas estratégicas ainda não carregados.');
          setIsExporting(false);
          return;
        }
        success = await generateStrategicStructuredPDF(strategicData, `metricas-estrategicas-${dateStr}.pdf`);
      } else {
        if (!filteredMetrics) {
          setIsExporting(false);
          return;
        }
        success = await generateStructuredPDF(filteredMetrics, `relatorio-gerencial-${dateStr}.pdf`);
      }

      if (success) {
        showSuccess('Relatório PDF exportado com sucesso!');
      } else {
        showError('Falha ao gerar relatório PDF.');
      }
    } catch {
      showError('Erro ao exportar PDF.');
    } finally {
      setIsExporting(false);
    }
  };

  const handleExportWord = async () => {
    setIsExporting(true);
    try {
      const {generateStrategicWordDocument, generateWordDocument} = await import('@/shared/utils/exportUtils');
      const dateStr = new Date().toISOString().slice(0, 10);
      let success = false;

      if (currentTab === 1) {
        if (!strategicData) {
          showError('Dados de métricas estratégicas ainda não carregados.');
          setIsExporting(false);
          return;
        }
        success = await generateStrategicWordDocument(strategicData, `metricas-estrategicas-${dateStr}.docx`);
      } else {
        if (!filteredMetrics) {
          setIsExporting(false);
          return;
        }
        success = await generateWordDocument(filteredMetrics, `relatorio-gerencial-${dateStr}.docx`);
      }

      if (success) {
        showSuccess('Relatório Word (.docx) exportado com sucesso!');
      } else {
        showError('Falha ao gerar documento Word.');
      }
    } catch {
      showError('Erro ao exportar Word.');
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

  if (metricsError || !filteredMetrics) {
    return (
      <Box sx={{ p: 2 }}>
        <Alert severity="error">
          Não foi possível carregar os relatórios. Verifique sua conexão ou permissões.
        </Alert>
      </Box>
    );
  }

  const summaryCards = [
    {
      title: isFiltered ? 'Eventos no Período' : 'Total Eventos no Mês',
      value: filteredMetrics.resumoGeral?.totalEventosPeriodo ?? 0,
      icon: EventIcon,
      paletteKey: 'primary' as const,
    },
    {
      title: 'Projetos Ativos',
      value: filteredMetrics.resumoGeral?.projetosAtivos ?? 0,
      icon: ProjectIcon,
      paletteKey: 'success' as const,
    },
    {
      title: 'Solicitações Pendentes',
      value: filteredMetrics.resumoGeral?.solicitacoesPendentes ?? 0,
      icon: PendingIcon,
      paletteKey: 'warning' as const,
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
            onExportWord={handleExportWord}
            isExporting={isExporting}
          />
        }
      />

      {/* Tabs de Seleção de Relatório com Ícones */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs
          value={currentTab}
          onChange={(_, val) => setCurrentTab(val)}
          indicatorColor="primary"
          textColor="primary"
        >
          <Tab icon={<AssessmentIcon />} iconPosition="start" label="Indicadores Operacionais" sx={{ fontWeight: 600 }} />
          <Tab icon={<AutoGraphIcon />} iconPosition="start" label="Métricas Estratégicas & Preditivas" sx={{ fontWeight: 600 }} />
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
          sx={{ mb: 3 }}
          onClose={handleClearFilters}
        >
          Filtro ativo de <strong>{appliedStartDate || 'início do registro'}</strong> até{' '}
          <strong>{appliedEndDate || 'data atual'}</strong> — Exibindo <strong>{filteredMetrics.eventosSemana.length}</strong> evento(s) no período selecionado.
        </Alert>
      )}

      {/* KPI Cards Vibrantes */}
      <Grid container spacing={3}>
        {summaryCards.map((card, idx) => {
          const IconComp = card.icon;
          return (
            <Grid key={idx} size={{ xs: 12, md: 4 }}>
              <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
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
                        borderRadius: 2,
                        bgcolor: (theme) => alpha(theme.palette[card.paletteKey].main, 0.1),
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <IconComp sx={{ fontSize: 24, color: `${card.paletteKey}.main` }} />
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
          <EventDistributionPieChart data={filteredMetrics.eventosPorGrupoParoquial || []} />
        </Grid>

        {/* BarChart - Volume Comparison */}
        <Grid size={{ xs: 12, md: 6 }}>
          <EventVolumeBarChart data={filteredMetrics.eventosPorGrupoParoquial || []} />
        </Grid>
      </Grid>

      {/* Projetos Status & Progress Section */}
      <Grid container spacing={3} sx={{ mt: 1 }}>
        <Grid size={{ xs: 12 }}>
          <Card elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <ProjectIcon sx={{ color: 'primary.main', fontSize: 20 }} />
                <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary' }}>
                  Acompanhamento de Projetos Ativos & Status
                </Typography>
              </Box>

              {projectStatusBreakdown.length > 0 && (
                <Stack direction="row" spacing={1}>
                  {projectStatusBreakdown.map((b, i) => (
                    <StatusIcon
                      key={i}
                      label={`${b.status}: ${b.count}`}
                      color="primary"
                      icon={<ListIcon />}
                      tooltip={`${b.count} projeto(s) com status ${b.status}`}
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
                    <Box
                      sx={{
                        p: 2.25,
                        border: '1px solid',
                        borderColor: proj.emRisco ? 'error.light' : 'divider',
                        bgcolor: proj.emRisco ? (theme) => alpha(theme.palette.error.main, 0.08) : 'background.paper',
                        borderRadius: 1.5,
                        height: '100%',
                        boxSizing: 'border-box',
                        position: 'relative',
                        overflow: 'hidden',
                        '&::before': proj.emRisco ? {
                          content: '""',
                          position: 'absolute',
                          left: 0,
                          top: 0,
                          bottom: 0,
                          width: '4px',
                          backgroundColor: 'error.main',
                        } : undefined,
                      }}
                    >
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                        <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'primary.main' }}>
                          {proj.nome}
                        </Typography>
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                          {proj.emRisco && (
                            <StatusIcon
                              icon={<WarningIcon />}
                              label="Em Risco"
                              color="error"
                              tooltip="Projeto com prazo ou entrega em risco"
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
                          bgcolor: 'grey.300',
                          '& .MuiLinearProgress-bar': {
                            bgcolor: proj.emRisco ? 'error.main' : 'primary.main',
                          },
                        }}
                      />

                      {proj.envolvidos && proj.envolvidos.length > 0 && (
                        <Box sx={{ mt: 1.5, display: 'flex', alignItems: 'center', gap: 0.5, flexWrap: 'wrap' }}>
                          <Typography variant="caption" color="text.secondary" sx={{ mr: 0.5 }}>
                            Envolvidos:
                          </Typography>
                          {proj.envolvidos.map((env, eIdx) => (
                            <StatusIcon key={eIdx} color="default" icon={<ListIcon />} label={env} tooltip="Envolvido no projeto" />
                          ))}
                        </Box>
                      )}
                    </Box>
                  </Grid>
                ))}
              </Grid>
            ) : (
              <Box sx={{ py: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
                <ProjectIcon sx={{ fontSize: 36, color: 'action.disabled' }} />
                <Typography variant="body2" color="text.secondary">
                  Nenhum projeto ativo no momento.
                </Typography>
              </Box>
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
          <AssessmentIcon sx={{ color: 'primary.main' }} />
          <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
            Atividades Recentes & Eventos {isFiltered ? 'no Período' : 'da Semana'}
          </Typography>
        </Box>

        {filteredMetrics.eventosSemana && filteredMetrics.eventosSemana.length > 0 ? (
          <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
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
                      <TableCell>{ev.grupoParoquialNome || '-'}</TableCell>
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
              <SearchableTablePagination colSpan={4}
                rowsPerPageOptions={[5, 10, 25]}
                count={filteredMetrics.eventosSemana.length}
                rowsPerPage={reportsRowsPerPage}
                page={reportsPage}
                onPageChange={(_, newPage) => setReportsPage(newPage)}
                onRowsPerPageChange={(e) => {
                  setReportsRowsPerPage(parseInt(e.target.value, 10));
                  setReportsPage(0);
                }}
                labelRowsPerPage="Linhas por página:"
                labelDisplayedRows={({ from, to, count }) =>
                  `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`
                }
              />
            </Table>
          </TableContainer>
        ) : (
          <Box sx={{ py: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
            <EventIcon sx={{ fontSize: 36, color: 'action.disabled' }} />
            <Typography variant="body2" color="text.secondary">
              Nenhum evento encontrado no período selecionado.
            </Typography>
          </Box>
        )}
      </Card>
      </>
      )}
    </Box>
  );
};

export default Relatorios;
