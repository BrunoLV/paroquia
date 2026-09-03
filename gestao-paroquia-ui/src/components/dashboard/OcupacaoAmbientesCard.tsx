import React from 'react';
import {Box, LinearProgress, Paper, Stack, Tooltip, Typography} from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import {OcupacaoAmbientes} from '../../api/types';

interface OcupacaoAmbientesCardProps {
  data?: OcupacaoAmbientes;
}

const Medidor: React.FC<{label: string; valor: number}> = ({label, valor}) => (
  <Box>
    <Stack direction="row" sx={{justifyContent: 'space-between'}}>
      <Typography variant="body2" color="text.secondary">{label}</Typography>
      <Typography variant="body2" sx={{fontWeight: 700}}>{Math.round(valor)}%</Typography>
    </Stack>
    <LinearProgress variant="determinate" value={Math.min(100, valor)} sx={{height: 8, borderRadius: 4}} />
  </Box>
);

const OcupacaoAmbientesCard: React.FC<OcupacaoAmbientesCardProps> = ({data}) => (
  <Paper elevation={0} sx={{p: 3, border: '1px solid', borderColor: 'divider', height: '100%'}}>
    <Stack direction="row" sx={{alignItems: 'center', gap: 0.5, mb: 2}}>
      <Typography variant="h6" sx={{fontWeight: 700}}>Ocupação dos Ambientes</Typography>
      <Tooltip title="Estimativa: considera só eventos com local vinculado e janela operacional de 14h/dia.">
        <InfoOutlinedIcon fontSize="small" sx={{color: 'text.secondary'}} />
      </Tooltip>
    </Stack>
    <Stack spacing={2}>
      <Medidor label="Hoje" valor={data?.taxaDiaPercentual ?? 0} />
      <Medidor label="Semana" valor={data?.taxaSemanaPercentual ?? 0} />
      {(data?.porLocal ?? []).slice(0, 4).map((local) => (
        <Medidor key={local.localNome} label={local.localNome} valor={local.taxaSemanaPercentual} />
      ))}
    </Stack>
  </Paper>
);

export default OcupacaoAmbientesCard;
