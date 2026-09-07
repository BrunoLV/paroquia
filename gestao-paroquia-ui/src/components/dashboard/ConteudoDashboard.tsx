import {Alert, Box, Grid} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import type {DashboardComunidade, DashboardLogistica, DashboardMetrics, Evento} from '@/api/types';
import PageHeader from '@/components/PageHeader';
import AprovacoesPendentes from '@/components/dashboard/AprovacoesPendentes';
import DashboardFilterBar from '@/components/dashboard/DashboardFilterBar';
import PainelResumoPeriodo from '@/components/dashboard/PainelResumoPeriodo';
import QuickActions from '@/components/dashboard/QuickActions';
import SecaoEventos from '@/components/dashboard/sections/SecaoEventos';
import SecaoPastorais from '@/components/dashboard/sections/SecaoPastorais';
import SecaoProjetos from '@/components/dashboard/sections/SecaoProjetos';

interface ConteudoDashboardProps {
  metrics?: DashboardMetrics;
  logistica?: DashboardLogistica;
  comunidade?: DashboardComunidade;
  proximosEventos: Evento[];
  carregandoEventos: boolean;
  erro?: string;
  onNovoEvento: () => void;
}

const ConteudoDashboard = ({
  metrics, logistica, comunidade, proximosEventos, carregandoEventos, erro, onNovoEvento,
}: ConteudoDashboardProps) => (
  <Box>
    <PageHeader
      icon={<DashboardIcon sx={{fontSize: 32, color: 'primary.main'}} />}
      title="Dashboard"
      subtitle="Eventos, projetos e pastorais em uma visão rápida da paróquia."
      actions={<DashboardFilterBar />}
    />
    {erro && <Alert severity="error" sx={{mb: 2}}>{erro}</Alert>}
    <QuickActions onNewEvent={onNovoEvento} />
    <Grid container spacing={2} sx={{mt: 0.5}}>
      <Grid size={{xs: 12}}><PainelResumoPeriodo resumo={metrics?.resumoGeral} /></Grid>
      <Grid size={{xs: 12, lg: 4}}><AprovacoesPendentes /></Grid>
      <Grid size={{xs: 12, lg: 8}}><Grid container spacing={2}><SecaoEventos choques={logistica?.choquesHorario} eventosSemana={metrics?.eventosSemana ?? []} proximosEventos={proximosEventos} carregandoEventos={carregandoEventos} /></Grid></Grid>
      <SecaoProjetos metrics={metrics} />
      <SecaoPastorais metrics={metrics} comunidade={comunidade} />
    </Grid>
  </Box>
);

export default ConteudoDashboard;
