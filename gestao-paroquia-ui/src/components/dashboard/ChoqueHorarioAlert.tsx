import React from 'react';
import {Alert, AlertTitle, Box, Typography} from '@mui/material';
import {ChoqueHorario} from '@/api/types';

interface ChoqueHorarioAlertProps {
  choques?: ChoqueHorario[];
}

const formatar = (iso: string): string =>
  new Date(iso).toLocaleString('pt-BR', {day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit'});

const ChoqueHorarioAlert: React.FC<ChoqueHorarioAlertProps> = ({choques = []}) => {
  if (choques.length === 0) {
    return (
      <Alert severity="success" variant="outlined">
        Nenhum choque de horário detectado nos ambientes no período.
      </Alert>
    );
  }

  return (
    <Alert severity="warning" variant="outlined">
      <AlertTitle>Choque de horário em ambientes ({choques.length})</AlertTitle>
      <Box component="ul" sx={{m: 0, pl: 2}}>
        {choques.slice(0, 6).map((choque, index) => (
          <Typography component="li" variant="body2" key={index}>
            <strong>{choque.localNome}</strong> — “{choque.eventoATitulo}” × “{choque.eventoBTitulo}”
            {' '}({formatar(choque.inicio)})
          </Typography>
        ))}
      </Box>
    </Alert>
  );
};

export default ChoqueHorarioAlert;
