import {Grid, Typography} from '@mui/material';
import type {ChoqueHorario, Evento, WeeklyEvent} from '@/shared/domain/types/types';
import ChoqueHorarioAlert from '@/contextos/painel/presentation/components/painel/ChoqueHorarioAlert';
import UpcomingEvents from '@/contextos/painel/presentation/components/painel/UpcomingEvents';
import WeeklyTimeline from '@/contextos/painel/presentation/components/painel/WeeklyTimeline';

interface SecaoEventosProps {
  choques?: ChoqueHorario[];
  eventosSemana: WeeklyEvent[];
  proximosEventos: Evento[];
  carregandoEventos: boolean;
}

const SecaoEventos = ({choques, eventosSemana, proximosEventos, carregandoEventos}: SecaoEventosProps) => (
  <>
    <Grid size={{xs: 12}}>
      <Typography variant="h6" sx={{fontWeight: 700}}>Eventos</Typography>
    </Grid>
    <Grid size={{xs: 12}}><ChoqueHorarioAlert choques={choques} /></Grid>
    <Grid size={{xs: 12, md: 7}}><WeeklyTimeline events={eventosSemana} /></Grid>
    <Grid size={{xs: 12, md: 5}}><UpcomingEvents events={proximosEventos} isLoading={carregandoEventos} /></Grid>
  </>
);

export default SecaoEventos;
