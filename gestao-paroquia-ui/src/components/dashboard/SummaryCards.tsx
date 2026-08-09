import React from 'react';
import {Box, Card, CardContent, Grid, Typography} from '@mui/material';
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
      accentColor: '#1a237e',
      bgGradient: 'linear-gradient(135deg, #1a237e 0%, #3f51b5 100%)',
      icon: CalendarIcon,
    },
    {
      title: 'Projetos Ativos',
      value: metrics && metrics.activeProjects !== undefined ? metrics.activeProjects : '--',
      accentColor: '#00796b',
      bgGradient: 'linear-gradient(135deg, #00796b 0%, #00bfa5 100%)',
      icon: ProjectIcon,
    },
    {
      title: 'Pendentes',
      value: metrics ? metrics.aprovacoesPendentes : '--',
      accentColor: '#e65100',
      bgGradient: 'linear-gradient(135deg, #e65100 0%, #ff9800 100%)',
      icon: PendingIcon,
    },
  ];

  return (
    <Grid container spacing={3}>
      {cards.map((card, idx) => {
        const IconComponent = card.icon;
        return (
          <Grid key={idx} size={{ xs: 12, sm: 4 }}>
            <Card
              elevation={0}
              sx={{
                border: '1px solid',
                borderColor: 'divider',
                borderTop: `4px solid ${card.accentColor}`,
                background: 'linear-gradient(to bottom, #ffffff, #fcfcfc)',
                boxShadow: '0 4px 14px rgba(0,0,0,0.03)',
                position: 'relative',
                overflow: 'hidden',
                transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                '&:hover': {
                  transform: 'translateY(-3px)',
                  boxShadow: '0 10px 25px rgba(0,0,0,0.06)',
                },
              }}
            >
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
                      p: 1.75,
                      borderRadius: 2,
                      background: card.bgGradient,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      boxShadow: '0 4px 10px rgba(0,0,0,0.15)',
                    }}
                  >
                    <IconComponent sx={{ fontSize: 28, color: '#ffffff' }} />
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
