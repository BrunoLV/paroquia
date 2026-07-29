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
    TableRow,
    Typography,
} from '@mui/material';
import {
    Assessment as AssessmentIcon,
    Assignment as ProjectIcon,
    BarChart as BarIcon,
    Event as EventIcon,
    FilterList as FilterIcon,
    PendingActions as PendingIcon,
    PieChart as PieIcon,
    Warning as WarningIcon,
} from '@mui/icons-material';
import {BarChart, PieChart} from '@mui/x-charts';
import {useQuery} from '@tanstack/react-query';
import {getDashboardMetrics} from '../api/dashboard';
import {getOrganizacoes} from '../api/organizacoes';
import apiClient from '../api/client';
import {DashboardMetrics, Organizacao, WeeklyEvent} from '../api/types';
import ReportFilters from '../components/reports/ReportFilters';
import ReportExportButtons from '../components/reports/ReportExportButtons';
import {downloadCSV, exportElementAsPDF, generateMetricsCSV} from '../utils/exportUtils';
import {useNotification} from '../context/NotificationContext';

const MARIAN_PALETTE = ['#1a237e', '#0288d1', '#00796b', '#7b1fa2', '#f57c00', '#d4af37', '#e91e63', '#455a64'];

const Reports: React.FC = () => {
  const { showError, showSuccess } = useNotification();
  const [appliedStartDate, setAppliedStartDate] = useState('');
  const [appliedEndDate, setAppliedEndDate] = useState('');
  const [isExporting, setIsExporting] = useState(false);

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

      const response = await apiClient.get<any>('/eventos', { params });
      const rawEvents = Array.isArray(response.data?.content)
        ? response.data.content
        : Array.isArray(response.data)
        ? response.data
        : [];

      return rawEvents.map((ev: any) => ({
        titulo: ev.titulo || ev.nome || 'Evento sem título',
        inicio: ev.inicio || ev.dataInicio || ev.criadoEmUtc || '',
        localNome: ev.local?.nome || ev.localNome || 'Sem Local',
        organizacaoNome:
          ev.organizacao?.nome ||
          orgMap[ev.organizacaoResponsavelId] ||
          ev.organizacaoNome ||
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
              <Skeleton variant="rounded" height={130} sx={{ borderRadius: 0 }} />
            </Grid>
          ))}
        </Grid>
        <Box sx={{ mt: 4 }}>
          <Skeleton variant="rounded" height={300} sx={{ borderRadius: 0 }} />
        </Box>
      </Box>
    );
  }

  if (metricsError || !metricsData || !filteredMetrics) {
    return (
      <Box sx={{ p: 1 }}>
        <Typography variant="h4" sx={{ mb: 3, fontWeight: 700, color: '#1a237e' }}>
          Relatórios & Métricas Gerenciais
        </Typography>
        <Alert severity="error" sx={{ borderRadius: 0 }}>
          Não foi possível carregar os relatórios. Verifique sua conexão ou permissões.
        </Alert>
      </Box>
    );
  }

  // Data formatted for PieChart with Marian Palette colors & labels
  const pieChartData = (filteredMetrics.eventosPorOrganizacao || []).map((item, index) => ({
    id: index,
    value: item.quantidadeEventos,
    label: `${item.organizacaoNome} (${item.quantidadeEventos})`,
    color: MARIAN_PALETTE[index % MARIAN_PALETTE.length],
  }));

  // Series per organization for BarChart
  const barChartSeries = (filteredMetrics.eventosPorOrganizacao || []).map((item, index) => ({
    data: [item.quantidadeEventos],
    label: item.organizacaoNome,
    color: MARIAN_PALETTE[index % MARIAN_PALETTE.length],
  }));

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
      {/* Top Header & Actions Padronizado */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 2,
          mb: 4,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ p: 1.5, bgcolor: 'rgba(26, 35, 126, 0.08)', borderRadius: 0, display: 'flex' }}>
            <AssessmentIcon sx={{ fontSize: 32, color: 'primary.main' }} />
          </Box>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main', letterSpacing: '-0.5px' }}>
              Relatórios & Métricas Gerenciais
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Painel de controle com dados consolidados da paróquia e indicadores de desempenho.
            </Typography>
          </Box>
        </Box>

        <ReportExportButtons
          onExportCSV={handleExportCSV}
          onExportPDF={handleExportPDF}
          isExporting={isExporting}
        />
      </Box>

      {/* Filter Bar */}
      <Card
        elevation={0}
        sx={{
          p: 2,
          mb: 3,
          borderRadius: 0,
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
          sx={{ mb: 3, borderRadius: 0, borderLeft: '4px solid #1a237e' }}
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
                  borderRadius: 0,
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
          <Card
            elevation={0}
            sx={{
              p: 3,
              height: '100%',
              borderRadius: 0,
              border: '1px solid',
              borderColor: 'divider',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <PieIcon sx={{ color: '#1a237e' }} />
              <Typography variant="h6" sx={{ fontWeight: 600, color: '#1a237e' }}>
                Distribuição de Eventos por Organização
              </Typography>
            </Box>

            {pieChartData.length > 0 ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 320, width: '100%' }}>
                <PieChart
                  series={[
                    {
                      data: pieChartData,
                      innerRadius: 40,
                      outerRadius: 95,
                      paddingAngle: 3,
                      cornerRadius: 0,
                      highlightScope: { fade: 'global', highlight: 'item' },
                    },
                  ]}
                  height={310}
                  margin={{ top: 20, bottom: 50, left: 20, right: 20 }}
                  slotProps={{
                    legend: {
                      direction: 'horizontal',
                      position: { vertical: 'bottom', horizontal: 'center' },
                    },
                  }}
                />
              </Box>
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ py: 6, textAlign: 'center' }}>
                Nenhum evento encontrado para as organizações no período selecionado.
              </Typography>
            )}
          </Card>
        </Grid>

        {/* BarChart - Volume Comparison */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card
            elevation={0}
            sx={{
              p: 3,
              height: '100%',
              borderRadius: 0,
              border: '1px solid',
              borderColor: 'divider',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <BarIcon sx={{ color: '#1a237e' }} />
              <Typography variant="h6" sx={{ fontWeight: 600, color: '#1a237e' }}>
                Volume de Eventos por Organização
              </Typography>
            </Box>

            {barChartSeries.length > 0 ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 320, width: '100%' }}>
                <BarChart
                  xAxis={[{ scaleType: 'band', data: ['Quantidade de Eventos'] }]}
                  series={barChartSeries}
                  height={310}
                  margin={{ top: 20, bottom: 50, left: 40, right: 20 }}
                  slotProps={{
                    legend: {
                      direction: 'horizontal',
                      position: { vertical: 'bottom', horizontal: 'center' },
                    },
                  }}
                />
              </Box>
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ py: 6, textAlign: 'center' }}>
                Nenhum dado disponível para exibição no gráfico de barras.
              </Typography>
            )}
          </Card>
        </Grid>
      </Grid>

      {/* Projects Status & Progress Section */}
      <Grid container spacing={3} sx={{ mt: 1 }}>
        <Grid size={{ xs: 12 }}>
          <Card
            elevation={0}
            sx={{
              p: 3,
              borderRadius: 0,
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
                      sx={{ borderRadius: 0, fontWeight: 600 }}
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
                              sx={{ borderRadius: 0, height: 22, fontSize: '0.7rem' }}
                            />
                          )}
                          <Chip
                            label={proj.status}
                            size="small"
                            sx={{ borderRadius: 0, height: 22, fontSize: '0.7rem', backgroundColor: '#e3f2fd', color: '#0d47a1' }}
                          />
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
                          borderRadius: 0,
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
                            <Chip key={eIdx} label={env} size="small" variant="outlined" sx={{ borderRadius: 0, height: 18, fontSize: '0.65rem' }} />
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
          borderRadius: 0,
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
          <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid #eee', borderRadius: 0 }}>
            <Table size="small">
              <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 700 }}>Título do Evento</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Organização</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Local</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Data / Horário</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredMetrics.eventosSemana.map((ev, idx) => (
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
          </TableContainer>
        ) : (
          <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
            Nenhum evento encontrado no período selecionado.
          </Typography>
        )}
      </Card>
    </Box>
  );
};

export default Reports;
