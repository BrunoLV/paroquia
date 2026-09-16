import {Card, CardContent, Grid, Stack, Typography} from '@mui/material';
import ProjetoIcon from '@mui/icons-material/Assignment';
import EventoIcon from '@mui/icons-material/CalendarMonth';
import PastoralIcon from '@mui/icons-material/Groups';
import AprovacaoIcon from '@mui/icons-material/PendingActions';
import type {GeneralSummary} from '@/api/types';

interface PainelResumoPeriodoProps {
  resumo?: GeneralSummary;
}

const PainelResumoPeriodo = ({resumo}: PainelResumoPeriodoProps) => {
  const totalVinculos = (resumo?.fieisEngajadosPastoral ?? 0) + (resumo?.fieisSemVinculoPastoral ?? 0);
  const indicadores = [
    ['Eventos no período', resumo?.totalEventosPeriodo, EventoIcon, 'primary.main'],
    ['Projetos ativos', resumo?.projetosAtivos, ProjetoIcon, 'success.main'],
    ['Aprovações pendentes', resumo?.solicitacoesPendentes, AprovacaoIcon, 'warning.main'],
    ['Engajamento pastoral', resumo?.fieisEngajadosPastoral, PastoralIcon, 'secondary.main'],
  ] as const;

  return (
    <Grid container spacing={2}>
      {indicadores.map(([titulo, valor, Icon, color]) => (
        <Grid key={titulo} size={{xs: 12, sm: 6, lg: 3}}>
          <Card variant="outlined">
            <CardContent sx={{py: 2, '&:last-child': {pb: 2}}}>
              <Stack direction="row" sx={{justifyContent: 'space-between', alignItems: 'flex-start'}}>
                <Stack spacing={0.5}>
                  <Typography color="text.secondary" variant="body2">{titulo}</Typography>
                  <Typography variant="h4" sx={{fontWeight: 700}}>{valor ?? '--'}</Typography>
                  {titulo === 'Engajamento pastoral' && <Typography color="text.secondary" variant="caption">{resumo ? `${resumo.fieisEngajadosPastoral} de ${totalVinculos} fiéis vinculados` : 'Sem dados de vínculos'}</Typography>}
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
