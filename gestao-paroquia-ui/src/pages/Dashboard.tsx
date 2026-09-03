import React, {useState} from 'react';
import {Alert, Box, Grid, Skeleton, Typography} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import MetricasEstrategicasWidget from '../components/dashboard/MetricasEstrategicasWidget';
import DashboardFilterBar from '../components/dashboard/DashboardFilterBar';
import SecaoCritica from '../components/dashboard/sections/SecaoCritica';
import SecaoLogistica from '../components/dashboard/sections/SecaoLogistica';
import SecaoProjetos from '../components/dashboard/sections/SecaoProjetos';
import SecaoComunidade from '../components/dashboard/sections/SecaoComunidade';
import CadastroEventoModal from '../components/CadastroEventoModal';
import PageHeader from '../components/PageHeader';
import {DashboardFilterProvider, useDashboardFilter} from '../context/DashboardFilterContext';
import {PeriodoDashboard} from '../api/types';
import {useDashboardMetrics} from '../hooks/useDashboardMetrics';
import {useDashboardLogistica} from '../hooks/useDashboardLogistica';
import {useAniversariantes, useDashboardComunidade} from '../hooks/useDashboardComunidade';
import {useDashboardEventoModal} from '../hooks/useDashboardEventoModal';

const DashboardSkeleton = () => (
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

const DashboardContent = () => {
  const {periodo, grupoParoquialId} = useDashboardFilter();
  const [janelaAniversario, setJanelaAniversario] = useState<PeriodoDashboard>('SEMANA');

  const {data: metrics, isLoading: isMetricsLoading, error: metricsError} =
    useDashboardMetrics(periodo, grupoParoquialId);
  const {data: logistica} = useDashboardLogistica(periodo, grupoParoquialId);
  const {data: aniversariantes} = useAniversariantes(janelaAniversario);
  const {data: comunidade} = useDashboardComunidade(grupoParoquialId);
  const modal = useDashboardEventoModal();

  if (modal.isLoading || isMetricsLoading) {
    return <DashboardSkeleton />;
  }

  const errorMsg =
    modal.error || (metricsError ? 'Não foi possível carregar os dados do dashboard.' : '');

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
        <SecaoCritica metrics={metrics} />
        <SecaoLogistica logistica={logistica} />

        <Grid size={{xs: 12}}>
          <MetricasEstrategicasWidget />
        </Grid>

        <SecaoProjetos
          metrics={metrics}
          proximosEventos={modal.events}
          carregandoEventos={modal.isLoading}
          onNovoEvento={modal.openModal}
        />

        <SecaoComunidade
          aniversariantes={aniversariantes}
          janelaAniversario={janelaAniversario}
          onJanelaChange={setJanelaAniversario}
          comunidade={comunidade}
        />
      </Grid>

      <CadastroEventoModal
        open={modal.isModalOpen}
        onClose={modal.closeModal}
        onSubmit={modal.handleCreateEvent}
        isSubmitting={modal.isSubmitting}
        organizations={modal.allowedOrgs.filter((org) => org.ativo)}
        locais={modal.locais.filter((l) => l.ativo)}
        projetos={modal.projetos.filter((p) => p.status === 'ABERTO' || p.status === 'EM_ANDAMENTO')}
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
