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
      color: '#0288d1',
      bgGradient: 'linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%)',
      icon: CalendarIcon,
    },
    {
      title: 'Projetos Ativos',
      value: metrics && metrics.activeProjects !== undefined ? metrics.activeProjects : '--',
      color: '#00796b',
      bgGradient: 'linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%)',
      icon: ProjectIcon,
    },
    {
      title: 'Pendentes',
      value: metrics ? metrics.aprovacoesPendentes : '--',
      color: '#f57c00',
      bgGradient: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)',
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
                background: 'linear-gradient(to bottom, #ffffff, #fcfcfc)',
                boxShadow: '0 4px 12px rgba(0,0,0,0.02)',
                position: 'relative',
                overflow: 'hidden',
                transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                '&:hover': {
                  transform: 'translateY(-3px)',
                  boxShadow: '0 10px 25px rgba(0,0,0,0.05)',
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
                      p: 1.5,
                      
                      background: card.bgGradient,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <IconComponent sx={{ fontSize: 28, color: card.color }} />
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
