import React from 'react';
import {Grid, Typography} from '@mui/material';
import ActiveProjectsProgressList from '@/components/dashboard/ActiveProjectsProgressList';
import ProjetosPorStatusCard from '@/components/dashboard/ProjetosPorStatusCard';
import type {DashboardMetrics} from '@/api/types';

interface SecaoProjetosProps {
  metrics?: DashboardMetrics;
}

const SecaoProjetos: React.FC<SecaoProjetosProps> = ({metrics}) => (
  <>
    <Grid size={{xs: 12}}><Typography variant="h6" sx={{fontWeight: 700}}>Projetos</Typography></Grid>
    <Grid size={{xs: 12, md: 4}}>
      <ProjetosPorStatusCard data={metrics?.projetosPorStatus} />
    </Grid>
    <Grid size={{xs: 12, md: 8}}>
      <ActiveProjectsProgressList projects={metrics?.projetosAtivosDetalhes ?? []} />
    </Grid>
  </>
);

export default SecaoProjetos;
