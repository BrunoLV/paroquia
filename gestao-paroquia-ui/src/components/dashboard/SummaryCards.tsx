import React from 'react';
import {alpha, Box, Card, CardContent, Grid, Typography} from '@mui/material';
import {
    Assignment as ProjectIcon,
    CalendarMonth as CalendarIcon,
    Groups as PeopleIcon,
    HourglassEmpty as PendingIcon
} from '@mui/icons-material';
import {GeneralSummary} from '../../api/types';

interface SummaryCardsProps {
  resumo?: GeneralSummary;
}

type PaletteKey = 'primary' | 'success' | 'warning' | 'info';

const SummaryCards: React.FC<SummaryCardsProps> = ({ resumo }) => {
  const fmt = (n?: number) => (n === undefined ? '--' : n);
  const cards: Array<{ title: string; value: number | string; paletteKey: PaletteKey; icon: typeof CalendarIcon; hint?: string }> = [
    { title: 'Eventos no Período', value: fmt(resumo?.totalEventosPeriodo), paletteKey: 'primary', icon: CalendarIcon },
    { title: 'Projetos Ativos', value: fmt(resumo?.projetosAtivos), paletteKey: 'success', icon: ProjectIcon },
    { title: 'Aprovações Pendentes', value: fmt(resumo?.solicitacoesPendentes), paletteKey: 'warning', icon: PendingIcon },
    {
      title: 'Fiéis Ativos',
      value: fmt(resumo?.totalFieisAtivos),
      paletteKey: 'info',
      icon: PeopleIcon,
      hint: resumo ? `+${resumo.novosFieisMes} no mês` : undefined,
    },
  ];

  return (
    <Grid container spacing={3}>
      {cards.map((card) => {
        const IconComponent = card.icon;
        return (
          <Grid key={card.title} size={{ xs: 12, sm: 6, md: 3 }}>
            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
              <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box>
                    <Typography color="text.secondary" variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
                      {card.title}
                    </Typography>
                    <Typography variant="h3" sx={{ fontWeight: 800, color: 'text.primary' }}>
                      {card.value}
                    </Typography>
                    {card.hint && (
                      <Typography variant="caption" color="text.secondary">{card.hint}</Typography>
                    )}
                  </Box>
                  <Box
                    sx={{
                      p: 1.5,
                      borderRadius: 2,
                      bgcolor: (theme) => alpha(theme.palette[card.paletteKey].main, 0.1),
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <IconComponent sx={{ fontSize: 24, color: `${card.paletteKey}.main` }} />
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        );
      })}
    </Grid>
  );
};

export default SummaryCards;
