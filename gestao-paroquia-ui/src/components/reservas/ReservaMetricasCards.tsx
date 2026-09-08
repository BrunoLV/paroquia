import React from 'react';
import { Card, CardContent, Grid, Stack, Typography, Box } from '@mui/material';
import TodayIcon from '@mui/icons-material/Today';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import OutboxIcon from '@mui/icons-material/Outbox';
import ShieldIcon from '@mui/icons-material/Shield';
import { ReservaMetricas } from '@/api/types';

interface ReservaMetricasCardsProps {
  metricas?: ReservaMetricas;
  isLoading?: boolean;
}

export const ReservaMetricasCards: React.FC<ReservaMetricasCardsProps> = ({
  metricas,
  isLoading,
}) => {
  const cards = [
    {
      titulo: 'Reservas Hoje',
      valor: metricas?.totalHoje ?? 0,
      icone: <TodayIcon sx={{ fontSize: 28, color: 'primary.main' }} />,
    },
    {
      titulo: 'Aprovações Pendentes',
      valor: metricas?.totalPendentes ?? 0,
      icone: <PendingActionsIcon sx={{ fontSize: 28, color: 'warning.main' }} />,
    },
    {
      titulo: 'Bens em Uso / Custódia',
      valor: metricas?.totalEmUso ?? 0,
      icone: <OutboxIcon sx={{ fontSize: 28, color: 'info.main' }} />,
    },
    {
      titulo: 'Conflitos Prevenidos',
      valor: metricas?.totalConflitosPrevenidos ?? 0,
      icone: <ShieldIcon sx={{ fontSize: 28, color: 'success.main' }} />,
    },
  ];

  return (
    <Grid container spacing={2}>
      {cards.map((card, idx) => (
        <Grid size={{ xs: 12, sm: 6, md: 3 }} key={idx}>
          <Card
            elevation={0}
            sx={{
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: 2,
              transition: 'transform 0.2s, box-shadow 0.2s',
              '&:hover': {
                transform: 'translateY(-2px)',
                boxShadow: 2,
              },
            }}
          >
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
                <Stack spacing={0.5}>
                  <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                    {card.titulo}
                  </Typography>
                  <Typography variant="h5" color="text.primary" sx={{ fontWeight: 700 }}>
                    {isLoading ? '...' : card.valor}
                  </Typography>
                </Stack>
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: 48,
                    height: 48,
                    borderRadius: 2,
                    bgcolor: (theme) =>
                      theme.palette.mode === 'dark' ? 'action.hover' : 'grey.100',
                  }}
                >
                  {card.icone}
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};
