import React, {useEffect, useState} from 'react';
import {Alert, Box, Grid, Skeleton, Stack, Typography} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import SummaryCards from '../components/dashboard/SummaryCards';
import UpcomingEvents from '../components/dashboard/UpcomingEvents';
import QuickActions from '../components/dashboard/QuickActions';
import AprovacoesPendentes from '../components/dashboard/AprovacoesPendentes';
import EventDistributionPieChart from '../components/dashboard/EventDistributionPieChart';
import ActiveProjectsProgressList from '../components/dashboard/ActiveProjectsProgressList';
import WeeklyTimeline from '../components/dashboard/WeeklyTimeline';
import MetricasEstrategicasWidget from '../components/dashboard/MetricasEstrategicasWidget';
import ProjetosPorStatusCard from '../components/dashboard/ProjetosPorStatusCard';
import MembrosPorPastoralCard from '../components/dashboard/MembrosPorPastoralCard';
import EventosOrfaosCard from '../components/dashboard/EventosOrfaosCard';
import ChoqueHorarioAlert from '../components/dashboard/ChoqueHorarioAlert';
import OcupacaoAmbientesCard from '../components/dashboard/OcupacaoAmbientesCard';
import EventosPorResponsavelCard from '../components/dashboard/EventosPorResponsavelCard';
import ExportarAgendaButton from '../components/dashboard/ExportarAgendaButton';
import AniversariantesCard from '../components/dashboard/AniversariantesCard';
import RetencaoPastoraisCard from '../components/dashboard/RetencaoPastoraisCard';
import DashboardFilterBar from '../components/dashboard/DashboardFilterBar';
import apiClient from '../api/client';
import CadastroEventoModal from '../components/CadastroEventoModal';
import {useAuth} from '../auth/AuthContext';
import {useNotification} from '../context/NotificationContext';
import {DashboardFilterProvider, useDashboardFilter} from '../context/DashboardFilterContext';
import {Evento, Local, Organizacao, PeriodoDashboard, Projeto} from '../api/types';
import {useQueryClient} from '@tanstack/react-query';
import {useDashboardMetrics} from '../hooks/useDashboardMetrics';
import {useDashboardLogistica} from '../hooks/useDashboardLogistica';
import {useAniversariantes, useDashboardComunidade} from '../hooks/useDashboardComunidade';
import {getOrganizacoes} from '../api/organizacoes';
import PageHeader from '../components/PageHeader';
import {extractErrorMessage} from '../utils/errorUtils';

const DashboardContent = () => {
  const {user} = useAuth();
  const {showError} = useNotification();
  const queryClient = useQueryClient();
  const {periodo, grupoParoquialId} = useDashboardFilter();

  const [events, setEvents] = useState<Evento[]>([]);
  const [organizations, setOrganizations] = useState<Organizacao[]>([]);
  const [locais, setLocais] = useState<Local[]>([]);
  const [projetos, setProjetos] = useState<Projeto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {data: metrics, isLoading: isMetricsLoading, error: metricsError} =
    useDashboardMetrics(periodo, grupoParoquialId);
  const {data: logistica} = useDashboardLogistica(periodo, grupoParoquialId);
  const [janelaAniversario, setJanelaAniversario] = useState<PeriodoDashboard>('SEMANA');
  const {data: aniversariantes} = useAniversariantes(janelaAniversario);
  const {data: comunidade} = useDashboardComunidade(grupoParoquialId);

  const fetchData = async () => {
    setIsLoading(true);
    setError('');
    try {
      const [orgsData, locaisRes, projetosRes] = await Promise.all([
        getOrganizacoes().catch(() => []),
        apiClient.get<Local[]>('/locais').catch(() => ({data: []})),
        apiClient.get<Projeto[]>('/projetos').catch(() => ({data: []})),
      ]);
      if (Array.isArray(orgsData)) setOrganizations(orgsData);
      if (Array.isArray(locaisRes.data)) setLocais(locaisRes.data);
      if (Array.isArray(projetosRes.data)) setProjetos(projetosRes.data);
      await fetchEvents();
    } catch (err) {
      console.error('Error fetching dashboard metadata:', err);
      setError('Não foi possível carregar os dados do dashboard.');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchEvents = async () => {
    const now = new Date();
    const nextMonth = new Date();
    nextMonth.setDate(now.getDate() + 30);
    const params: Record<string, unknown> = {
      dataInicio: now.toISOString(),
      dataFim: nextMonth.toISOString(),
      size: 5,
      sort: 'inicioUtc,asc',
    };
    const isAdmin = !!user && (user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN'));
    if (!isAdmin) {
      const userOrgIds = user?.organizations.map((org) => org.id) ?? [];
      if (userOrgIds.length === 0) {
        setEvents([]);
        return;
      }
      params.organizacaoIds = userOrgIds;
    }
    const res = await apiClient.get<{content?: Evento[]} | Evento[]>('/eventos', {
      params,
      paramsSerializer: {indexes: null},
    });
    const data = res.data;
    if (data && !Array.isArray(data) && Array.isArray(data.content)) setEvents(data.content);
    else if (Array.isArray(data)) setEvents(data);
  };

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const handleCreateEvent = async (data: Record<string, unknown>) => {
    setIsSubmitting(true);
    try {
      const {recurrence, ...eventBaseData} = data;
      const requestData = {
        ...eventBaseData,
        projetoId: data.projetoId || null,
        localId: data.localId || null,
        participantes: data.participantes || [],
      };
      const idempotencyKey =
        typeof crypto !== 'undefined' && crypto.randomUUID
          ? crypto.randomUUID()
          : Math.random().toString(36).substring(2) + Date.now().toString(36);
      const res = await apiClient.post('/eventos', requestData, {headers: {'Idempotency-Key': idempotencyKey}});
      if (recurrence && res.data?.id) {
        await apiClient.put(`/eventos/${res.data.id}/recorrencia`, recurrence);
      }
      setIsModalOpen(false);
      queryClient.invalidateQueries({queryKey: ['dashboard']});
      fetchData();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao criar evento.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const allowedOrgs = React.useMemo(() => {
    if (!user) return [];
    const isAdmin = user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN');
    if (isAdmin) return organizations;
    const userOrgIds = user.organizations.map((org) => org.id);
    return organizations.filter((org) => userOrgIds.includes(org.id));
  }, [user, organizations]);

  const showSkeleton = isLoading || isMetricsLoading;
  const errorMsg = error || (metricsError ? 'Não foi possível carregar os dados do dashboard.' : '');

  if (showSkeleton) {
    return (
      <Box>
        <Skeleton variant="text" width={220} height={56} sx={{mb: 3}} />
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid key={i} size={{xs: 12, sm: 6, md: 3}}>
              <Skeleton variant="rounded" height={130} />
            </Grid>
          ))}
        </Grid>
        <Skeleton variant="rounded" height={320} sx={{mt: 3}} />
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader
        icon={<DashboardIcon sx={{fontSize: 32, color: 'primary.main'}} />}
        title="Dashboard"
        subtitle="Visão geral e indicadores consolidados das atividades, projetos e eventos da paróquia."
        actions={<DashboardFilterBar />}
      />

      {errorMsg && <Alert severity="error" sx={{mb: 3}}>{errorMsg}</Alert>}

      <Grid container spacing={3}>
        <Grid size={{xs: 12}}>
          <Typography variant="h6" sx={{mb: 2, fontWeight: 700}}>Visão Geral</Typography>
          <SummaryCards resumo={metrics?.resumoGeral} />
        </Grid>

        <Grid size={{xs: 12, md: 8}}>
          <ProjetosPorStatusCard data={metrics?.projetosPorStatus} />
        </Grid>
        <Grid size={{xs: 12, md: 4}}>
          <MembrosPorPastoralCard resumo={metrics?.resumoGeral} />
        </Grid>

        <Grid size={{xs: 12}}>
          <ChoqueHorarioAlert choques={logistica?.choquesHorario} />
        </Grid>
        <Grid size={{xs: 12, md: 6}}>
          <OcupacaoAmbientesCard data={logistica?.ocupacaoAmbientes} />
        </Grid>
        <Grid size={{xs: 12, md: 6}}>
          <EventosPorResponsavelCard data={logistica?.eventosPorResponsavel} />
        </Grid>

        <Grid size={{xs: 12}}>
          <MetricasEstrategicasWidget />
        </Grid>

        <Grid size={{xs: 12, md: 8}}>
          <Stack spacing={3}>
            <Grid container spacing={3}>
              <Grid size={{xs: 12, lg: 6}}>
                <EventDistributionPieChart data={metrics?.eventosPorGrupoParoquial || []} />
              </Grid>
              <Grid size={{xs: 12, lg: 6}}>
                <ActiveProjectsProgressList projects={metrics?.projetosAtivosDetalhes || []} />
              </Grid>
            </Grid>
            <EventosOrfaosCard eventos={metrics?.eventosOrfaos} />
            <Box>
              <Stack direction="row" sx={{justifyContent: 'flex-end', mb: 1}}>
                <ExportarAgendaButton eventosSemana={metrics?.eventosSemana} />
              </Stack>
              <WeeklyTimeline events={metrics?.eventosSemana || []} />
            </Box>
            <UpcomingEvents events={events} isLoading={isLoading} />
          </Stack>
        </Grid>

        <Grid size={{xs: 12, md: 4}}>
          <Stack spacing={3}>
            <QuickActions onNewEvent={() => setIsModalOpen(true)} />
            <AprovacoesPendentes />
          </Stack>
        </Grid>

        <Grid size={{xs: 12}}>
          <Typography variant="h6" sx={{mb: 2, fontWeight: 700}}>Vida Comunitária</Typography>
        </Grid>
        <Grid size={{xs: 12, md: 6}}>
          <AniversariantesCard
            aniversariantes={aniversariantes}
            janela={janelaAniversario}
            onJanelaChange={setJanelaAniversario}
          />
        </Grid>
        <Grid size={{xs: 12, md: 6}}>
          <RetencaoPastoraisCard pastorais={comunidade?.retencaoPastorais} />
        </Grid>
      </Grid>

      <CadastroEventoModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCreateEvent}
        isSubmitting={isSubmitting}
        organizations={allowedOrgs.filter((org) => org.ativo)}
        locais={locais.filter((l) => l.ativo)}
        projetos={projetos.filter((p) => p.status === 'ABERTO' || p.status === 'EM_ANDAMENTO')}
      />
    </Box>
  );
};

const Dashboard = () => (
  <DashboardFilterProvider>
    <DashboardContent />
  </DashboardFilterProvider>
);

export default Dashboard;
