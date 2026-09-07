import {Box, Grid, Skeleton} from '@mui/material';
import CadastroEventoModal from '@/components/CadastroEventoModal';
import ConteudoDashboard from '@/components/dashboard/ConteudoDashboard';
import {DashboardFilterProvider, useDashboardFilter} from '@/context/DashboardFilterContext';
import {useDashboardComunidade} from '@/hooks/useDashboardComunidade';
import {useDashboardEventoModal} from '@/hooks/useDashboardEventoModal';
import {useDashboardLogistica} from '@/hooks/useDashboardLogistica';
import {useDashboardMetrics} from '@/hooks/useDashboardMetrics';

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
  const {data: metrics, isLoading: isMetricsLoading, error: metricsError} =
    useDashboardMetrics(periodo, grupoParoquialId);
  const {data: logistica} = useDashboardLogistica(periodo, grupoParoquialId);
  const {data: comunidade} = useDashboardComunidade(grupoParoquialId);
  const modal = useDashboardEventoModal();

  if (modal.isLoading || isMetricsLoading) {
    return <DashboardSkeleton />;
  }

  const erro = modal.error || (metricsError ? 'Não foi possível carregar os dados do dashboard.' : undefined);

  return (
    <>
      <ConteudoDashboard
        metrics={metrics}
        logistica={logistica}
        comunidade={comunidade}
        proximosEventos={modal.events}
        carregandoEventos={modal.isLoading}
        erro={erro}
        onNovoEvento={modal.openModal}
      />

      <CadastroEventoModal
        open={modal.isModalOpen}
        onClose={modal.closeModal}
        onSubmit={modal.handleCreateEvent}
        isSubmitting={modal.isSubmitting}
        organizations={modal.allowedOrgs.filter((org) => org.ativo)}
        locais={modal.locais.filter((l) => l.ativo)}
        projetos={modal.projetos.filter((p) => p.status === 'ABERTO' || p.status === 'EM_ANDAMENTO')}
      />
    </>
  );
};

const Dashboard = () => (
  <DashboardFilterProvider>
    <DashboardContent />
  </DashboardFilterProvider>
);

export default Dashboard;
