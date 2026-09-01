import React from 'react';
import {alpha, Box, Card, CardContent, Grid, Typography} from '@mui/material';
import {
    Assignment as ProjectIcon,
    CalendarMonth as CalendarIcon,
    HourglassEmpty as PendingIcon
} from '@mui/icons-material';

interface SummaryCardsProps {
  metrics?: {
    monthlyEvents: number;
    aprovacoesPendentes: number;
    activeProjects?: number;
  };
}

const SummaryCards: React.FC<SummaryCardsProps> = ({ metrics }) => {
  const cards = [
    {
      title: 'Eventos este Mês',
      value: metrics ? metrics.monthlyEvents : '--',
      paletteKey: 'primary' as const,
      icon: CalendarIcon,
    },
    {
      title: 'Projetos Ativos',
      value: metrics && metrics.activeProjects !== undefined ? metrics.activeProjects : '--',
      paletteKey: 'success' as const,
      icon: ProjectIcon,
    },
    {
      title: 'Pendentes',
      value: metrics ? metrics.aprovacoesPendentes : '--',
      paletteKey: 'warning' as const,
      icon: PendingIcon,
    },
  ];

  return (
    <Grid container spacing={3}>
      {cards.map((card, idx) => {
        const IconComponent = card.icon;
        return (
          <Grid key={idx} size={{ xs: 12, sm: 4 }}>
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
