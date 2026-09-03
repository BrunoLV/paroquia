import React from 'react';
import {Box, Grid, Paper, Typography} from '@mui/material';
import {ProjetosPorStatus} from '../../api/types';

interface ProjetosPorStatusCardProps {
  data?: ProjetosPorStatus;
}

const FAIXAS: Array<{ key: keyof ProjetosPorStatus; label: string; color: string }> = [
  {key: 'planejamento', label: 'Planejamento', color: 'info.main'},
  {key: 'emExecucao', label: 'Em Execução', color: 'success.main'},
  {key: 'concluidos', label: 'Concluídos', color: 'text.secondary'},
  {key: 'atrasados', label: 'Atrasados', color: 'error.main'},
];

const ProjetosPorStatusCard: React.FC<ProjetosPorStatusCardProps> = ({data}) => (
  <Paper elevation={0} sx={{p: 3, border: '1px solid', borderColor: 'divider', height: '100%'}}>
    <Typography variant="h6" sx={{fontWeight: 700, mb: 2}}>
      Projetos por Status
    </Typography>
    <Grid container spacing={2}>
      {FAIXAS.map((faixa) => (
        <Grid key={faixa.key} size={{xs: 6, sm: 3}}>
          <Box sx={{textAlign: 'center'}}>
            <Typography variant="h4" sx={{fontWeight: 800, color: faixa.color}}>
              {data ? data[faixa.key] : '--'}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {faixa.label}
            </Typography>
          </Box>
        </Grid>
      ))}
    </Grid>
  </Paper>
);

export default ProjetosPorStatusCard;
