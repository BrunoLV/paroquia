import React from 'react';
import { Card, CardContent, Typography, Grid } from '@mui/material';

interface SummaryCardsProps {
  metrics?: {
    monthlyEvents: number;
    pendingApprovals: number;
  };
}

const SummaryCards: React.FC<SummaryCardsProps> = ({ metrics }) => {
  return (
    <Grid container spacing={2}>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <Card sx={{ borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
          <CardContent>
            <Typography color="textSecondary" variant="subtitle2" gutterBottom>Eventos este Mês</Typography>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>
              {metrics ? metrics.monthlyEvents : '--'}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <Card sx={{ borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
          <CardContent>
            <Typography color="textSecondary" variant="subtitle2" gutterBottom>Pendentes</Typography>
            <Typography variant="h4" sx={{ fontWeight: 700, color: 'warning.main' }}>
              {metrics ? metrics.pendingApprovals : '--'}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};

export default SummaryCards;
