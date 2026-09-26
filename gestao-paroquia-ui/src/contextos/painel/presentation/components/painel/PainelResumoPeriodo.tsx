import {Card, CardContent, Grid, Stack, Typography} from '@mui/material';
import ProjetoIcon from '@mui/icons-material/Assignment';
import EventoIcon from '@mui/icons-material/CalendarMonth';
import FielIcon from '@mui/icons-material/PersonOutlined';
import AprovacaoIcon from '@mui/icons-material/PendingActions';
import type {GeneralSummary} from '@/shared/domain/types/types';

interface PainelResumoPeriodoProps {
  resumo?: GeneralSummary;
}

/**
 * Indicadores consolidados de topo do Painel com métricas essenciais e simétricas.
 */
const PainelResumoPeriodo = ({resumo}: PainelResumoPeriodoProps) => {
  const indicadores = [
    ['Eventos no período', resumo?.totalEventosPeriodo, EventoIcon, 'primary.main'],
    ['Projetos ativos', resumo?.projetosAtivos, ProjetoIcon, 'success.main'],
    ['Aprovações pendentes', resumo?.solicitacoesPendentes, AprovacaoIcon, 'warning.main'],
    ['Fiéis ativos', resumo?.totalFieisAtivos, FielIcon, 'info.main'],
  ] as const;

  return (
    <Grid container spacing={2}>
      {indicadores.map(([titulo, valor, Icon, color]) => (
        <Grid key={titulo} size={{xs: 12, sm: 6, lg: 3}}>
          <Card variant="outlined" sx={{ borderRadius: 2, height: '100%' }}>
            <CardContent sx={{py: 2, '&:last-child': {pb: 2}}}>
              <Stack direction="row" sx={{justifyContent: 'space-between', alignItems: 'flex-start'}}>
                <Stack spacing={0.5}>
                  <Typography color="text.secondary" variant="body2">{titulo}</Typography>
                  <Typography variant="h4" sx={{fontWeight: 700}}>{valor ?? '--'}</Typography>
                  {titulo === 'Fiéis ativos' && (
                    <Typography color="text.secondary" variant="caption">
                      {resumo ? `+${resumo.novosFieisMes ?? 0} novos no mês` : 'Sem dados'}
                    </Typography>
                  )}
                </Stack>
                <Icon sx={{color, fontSize: 24}} />
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

export default PainelResumoPeriodo;
