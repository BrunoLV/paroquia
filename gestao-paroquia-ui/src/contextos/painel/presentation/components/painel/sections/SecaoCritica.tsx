import React from 'react';
import {Grid, Typography} from '@mui/material';
import SummaryCards from '@/contextos/painel/presentation/components/painel/SummaryCards';
import ProjetosPorStatusCard from '@/contextos/painel/presentation/components/painel/ProjetosPorStatusCard';
import MembrosPorPastoralCard from '@/contextos/painel/presentation/components/painel/MembrosPorPastoralCard';
import {DashboardMetrics} from '@/shared/domain/types/types';

interface SecaoCriticaProps {
  metrics?: DashboardMetrics;
}

const SecaoCritica: React.FC<SecaoCriticaProps> = ({metrics}) => (
  <>
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
  </>
);

export default SecaoCritica;
