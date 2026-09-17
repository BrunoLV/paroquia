import React from 'react';
import {Grid} from '@mui/material';
import ChoqueHorarioAlert from '@/contextos/painel/presentation/components/painel/ChoqueHorarioAlert';
import OcupacaoAmbientesCard from '@/contextos/painel/presentation/components/painel/OcupacaoAmbientesCard';
import EventosPorResponsavelCard from '@/contextos/painel/presentation/components/painel/EventosPorResponsavelCard';
import {DashboardLogistica} from '@/shared/domain/types/types';

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
