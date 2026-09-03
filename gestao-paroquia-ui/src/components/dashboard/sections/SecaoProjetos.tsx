import React from 'react';
import {Box, Grid, Stack} from '@mui/material';
import EventDistributionPieChart from '../EventDistributionPieChart';
import ActiveProjectsProgressList from '../ActiveProjectsProgressList';
import EventosOrfaosCard from '../EventosOrfaosCard';
import ExportarAgendaButton from '../ExportarAgendaButton';
import WeeklyTimeline from '../WeeklyTimeline';
import UpcomingEvents from '../UpcomingEvents';
import QuickActions from '../QuickActions';
import AprovacoesPendentes from '../AprovacoesPendentes';
import {DashboardMetrics, Evento} from '../../../api/types';

interface SecaoProjetosProps {
  metrics?: DashboardMetrics;
  proximosEventos: Evento[];
  carregandoEventos: boolean;
  onNovoEvento: () => void;
}

const SecaoProjetos: React.FC<SecaoProjetosProps> = ({
  metrics,
  proximosEventos,
  carregandoEventos,
  onNovoEvento,
}) => (
  <>
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
        <UpcomingEvents events={proximosEventos} isLoading={carregandoEventos} />
      </Stack>
    </Grid>
    <Grid size={{xs: 12, md: 4}}>
      <Stack spacing={3}>
        <QuickActions onNewEvent={onNovoEvento} />
        <AprovacoesPendentes />
      </Stack>
    </Grid>
  </>
);

export default SecaoProjetos;
