import {Grid, Typography} from '@mui/material';
import type {DashboardComunidade, DashboardMetrics} from '@/shared/domain/types/types';
import EventDistributionPieChart from '@/contextos/painel/presentation/components/painel/EventDistributionPieChart';
import MembrosPorPastoralCard from '@/contextos/painel/presentation/components/painel/MembrosPorPastoralCard';
import RetencaoPastoraisCard from '@/contextos/painel/presentation/components/painel/RetencaoPastoraisCard';

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
