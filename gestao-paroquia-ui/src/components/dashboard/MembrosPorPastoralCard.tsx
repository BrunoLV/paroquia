import React from 'react';
import {Box, LinearProgress, Paper, Stack, Typography} from '@mui/material';
import {GeneralSummary} from '@/api/types';

interface MembrosPorPastoralCardProps {
  resumo?: GeneralSummary;
}

const MembrosPorPastoralCard: React.FC<MembrosPorPastoralCardProps> = ({resumo}) => {
  const engajados = resumo?.fieisEngajadosPastoral ?? 0;
  const semVinculo = resumo?.fieisSemVinculoPastoral ?? 0;
  const total = engajados + semVinculo;
  const percentualEngajado = total > 0 ? (engajados / total) * 100 : 0;

  return (
    <Paper elevation={0} sx={{p: 3, border: '1px solid', borderColor: 'divider', height: '100%'}}>
      <Typography variant="h6" sx={{fontWeight: 700, mb: 1}}>
        Engajamento Pastoral
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{mb: 2}}>
        Fiéis com vínculo a pastoral/movimento versus cadastrados sem vínculo
      </Typography>
      <Stack direction="row" sx={{justifyContent: 'space-between', mb: 1}}>
        <Typography variant="h4" sx={{fontWeight: 800, color: 'success.main'}}>{engajados}</Typography>
        <Typography variant="h4" sx={{fontWeight: 800, color: 'text.secondary'}}>{semVinculo}</Typography>
      </Stack>
      <LinearProgress
        variant="determinate"
        value={percentualEngajado}
        sx={{height: 8, borderRadius: 4}}
      />
      <Stack direction="row" sx={{justifyContent: 'space-between', mt: 0.5}}>
        <Typography variant="caption" color="text.secondary">Engajados</Typography>
        <Typography variant="caption" color="text.secondary">Sem vínculo</Typography>
      </Stack>
    </Paper>
  );
};

export default MembrosPorPastoralCard;
