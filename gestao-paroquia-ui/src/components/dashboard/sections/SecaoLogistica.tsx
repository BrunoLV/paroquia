import React from 'react';
import {Grid} from '@mui/material';
import ChoqueHorarioAlert from '@/components/dashboard/ChoqueHorarioAlert';
import OcupacaoAmbientesCard from '@/components/dashboard/OcupacaoAmbientesCard';
import EventosPorResponsavelCard from '@/components/dashboard/EventosPorResponsavelCard';
import {DashboardLogistica} from '@/api/types';

interface SecaoLogisticaProps {
  logistica?: DashboardLogistica;
}

const SecaoLogistica: React.FC<SecaoLogisticaProps> = ({logistica}) => (
  <>
    <Grid size={{xs: 12}}>
      <ChoqueHorarioAlert choques={logistica?.choquesHorario} />
    </Grid>
    <Grid size={{xs: 12, md: 6}}>
      <OcupacaoAmbientesCard data={logistica?.ocupacaoAmbientes} />
    </Grid>
    <Grid size={{xs: 12, md: 6}}>
      <EventosPorResponsavelCard data={logistica?.eventosPorResponsavel} />
    </Grid>
  </>
);

export default SecaoLogistica;
