import React from 'react';
import {Box, LinearProgress, Paper, Stack, Typography} from '@mui/material';
import {EventosPorResponsavel} from '@/shared/domain/types/types';

interface EventosPorResponsavelCardProps {
  data?: EventosPorResponsavel[];
}

const EventosPorResponsavelCard: React.FC<EventosPorResponsavelCardProps> = ({data = []}) => {
  const maximo = data.reduce((max, item) => Math.max(max, item.quantidade), 0);

  return (
    <Paper elevation={0} sx={{p: 3, border: '1px solid', borderColor: 'divider', height: '100%'}}>
      <Typography variant="h6" sx={{fontWeight: 700, mb: 2}}>Eventos por Responsável</Typography>
      {data.length === 0 ? (
        <Typography variant="body2" color="text.secondary">Nenhum evento no período.</Typography>
      ) : (
        <Stack spacing={1.5}>
          {data.slice(0, 8).map((item) => (
            <Box key={item.organizacaoNome}>
              <Stack direction="row" sx={{justifyContent: 'space-between'}}>
                <Typography variant="body2">{item.organizacaoNome}</Typography>
                <Typography variant="body2" sx={{fontWeight: 700}}>{item.quantidade}</Typography>
              </Stack>
              <LinearProgress
                variant="determinate"
                value={maximo > 0 ? (item.quantidade / maximo) * 100 : 0}
                sx={{height: 6, borderRadius: 3}}
              />
            </Box>
          ))}
        </Stack>
      )}
    </Paper>
  );
};

export default EventosPorResponsavelCard;
