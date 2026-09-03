import React from 'react';
import {Grid} from '@mui/material';
import ChoqueHorarioAlert from '../ChoqueHorarioAlert';
import OcupacaoAmbientesCard from '../OcupacaoAmbientesCard';
import EventosPorResponsavelCard from '../EventosPorResponsavelCard';
import {DashboardLogistica} from '../../../api/types';

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
