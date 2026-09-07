import {Grid, Typography} from '@mui/material';
import type {DashboardComunidade, DashboardMetrics} from '@/api/types';
import EventDistributionPieChart from '@/components/dashboard/EventDistributionPieChart';
import MembrosPorPastoralCard from '@/components/dashboard/MembrosPorPastoralCard';
import RetencaoPastoraisCard from '@/components/dashboard/RetencaoPastoraisCard';

interface SecaoPastoraisProps {
  metrics?: DashboardMetrics;
  comunidade?: DashboardComunidade;
}

const SecaoPastorais = ({metrics, comunidade}: SecaoPastoraisProps) => (
  <>
    <Grid size={{xs: 12}}>
      <Typography variant="h6" sx={{fontWeight: 700}}>Pastorais e Movimentos</Typography>
    </Grid>
    <Grid size={{xs: 12, md: 5}}><EventDistributionPieChart data={metrics?.eventosPorGrupoParoquial ?? []} /></Grid>
    <Grid size={{xs: 12, md: 3}}><MembrosPorPastoralCard resumo={metrics?.resumoGeral} /></Grid>
    <Grid size={{xs: 12, md: 4}}><RetencaoPastoraisCard pastorais={comunidade?.retencaoPastorais} /></Grid>
  </>
);

export default SecaoPastorais;
