import React, { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Skeleton,
  Alert,
  Stack,
  LinearProgress,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import {
  Event as EventIcon,
  Assignment as ProjectIcon,
  PendingActions as PendingIcon,
  Assessment as AssessmentIcon,
} from '@mui/icons-material';
import { PieChart } from '@mui/x-charts';
import { useQuery } from '@tanstack/react-query';
import { getDashboardMetrics } from '../api/dashboard';
import { DashboardMetrics } from '../api/types';
import ReportFilters from '../components/reports/ReportFilters';
import ReportExportButtons from '../components/reports/ReportExportButtons';
import { generateMetricsCSV, downloadCSV, exportElementAsPDF } from '../utils/exportUtils';
import { useNotification } from '../context/NotificationContext';

const MARIAN_PALETTE = ['#1a237e', '#0288d1', '#00796b', '#7b1fa2', '#f57c00', '#d4af37'];

const Reports: React.FC = () => {
  const { showError, showSuccess } = useNotification();
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isExporting, setIsExporting] = useState(false);

  const {
    data,
    isLoading,
    error,
  } = useQuery<DashboardMetrics>({
    queryKey: ['dashboardMetrics'],
    queryFn: getDashboardMetrics,
  });

  const handleClearFilters = () => {
    setStartDate('');
    setEndDate('');
  };

  const handleExportCSV = () => {
    if (!data) return;
    try {
      const csvStr = generateMetricsCSV(data);
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

  if (error || !data) {
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

  const pieChartData = (data.eventosPorOrganizacao || []).map((item, index) => ({
    id: index,
    value: item.quantidadeEventos,
    label: item.organizacaoNome,
    color: MARIAN_PALETTE[index % MARIAN_PALETTE.length],
  }));

  const summaryCards = [
    {
      title: 'Eventos no Mês',
      value: data.resumoGeral?.totalEventosMes ?? 0,
      icon: EventIcon,
      color: '#1a237e',
      bgGradient: 'linear-gradient(135deg, #e8eaf6 0%, #c5cae9 100%)',
    },
    {
      title: 'Projetos Ativos',
      value: data.resumoGeral?.projetosAtivos ?? 0,
      icon: ProjectIcon,
      color: '#00796b',
      bgGradient: 'linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%)',
    },
    {
      title: 'Solicitações Pendentes',
      value: data.resumoGeral?.solicitacoesPendentes ?? 0,
      icon: PendingIcon,
      color: '#f57c00',
      bgGradient: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)',
    },
  ];

  return (
    <Box id="reports-container" sx={{ p: 1 }}>
      {/* Top Header & Actions */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: 2,
          mb: 3,
        }}
      >
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, color: '#1a237e', letterSpacing: '-0.5px' }}>
            Relatórios & Métricas Gerenciais
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Painel de controle com dados consolidados da paróquia e indicadores de desempenho.
          </Typography>
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
          mb: 4,
          borderRadius: 0,
          border: '1px solid',
          borderColor: 'divider',
          backgroundColor: '#fafafa',
        }}
      >
        <ReportFilters
          startDate={startDate}
          endDate={endDate}
          onStartDateChange={setStartDate}
          onEndDateChange={setEndDate}
          onClearFilters={handleClearFilters}
        />
      </Card>

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
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: '#1a237e' }}>
              Distribuição de Eventos por Organização
            </Typography>

            {pieChartData.length > 0 ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 280 }}>
                <PieChart
                  series={[
                    {
                      data: pieChartData,
                      innerRadius: 45,
                      outerRadius: 100,
                      paddingAngle: 2,
                      cornerRadius: 0,
                    },
                  ]}
                  width={420}
                  height={260}
                />
              </Box>
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
                Nenhum evento registrado por organização.
              </Typography>
            )}
          </Card>
        </Grid>

        {/* Projects Status & Progress */}
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
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600, color: '#1a237e' }}>
              Projetos Ativos & Progresso
            </Typography>

            {data.projetosAtivosDetalhes && data.projetosAtivosDetalhes.length > 0 ? (
              <Stack spacing={2.5} sx={{ mt: 1 }}>
                {data.projetosAtivosDetalhes.map((proj, idx) => (
                  <Box key={idx} sx={{ p: 1.5, border: '1px solid #eee', backgroundColor: '#fafafa' }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                        {proj.nome}
                      </Typography>
                      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                        {proj.emRisco && (
                          <Chip label="Em Risco" color="error" size="small" sx={{ borderRadius: 0, height: 22, fontSize: '0.7rem' }} />
                        )}
                        <Chip
                          label={proj.status}
                          size="small"
                          sx={{ borderRadius: 0, height: 22, fontSize: '0.7rem', backgroundColor: '#e3f2fd', color: '#0d47a1' }}
                        />
                      </Stack>
                    </Box>

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
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
                  </Box>
                ))}
              </Stack>
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
                Nenhum projeto ativo no momento.
              </Typography>
            )}
          </Card>
        </Grid>
      </Grid>

      {/* Events Table / Timeline Section */}
      {data.eventosSemana && data.eventosSemana.length > 0 && (
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
              Atividades Recentes & Eventos da Semana
            </Typography>
          </Box>

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
                {data.eventosSemana.map((ev, idx) => (
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
        </Card>
      )}
    </Box>
  );
};

export default Reports;
