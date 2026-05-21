import React from 'react';
import { Paper, Typography } from '@mui/material';

const PendingApprovals = () => {
  return (
    <Paper sx={{ p: 3, borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.05)' }}>
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Aprovações Pendentes
      </Typography>
      <Typography color="textSecondary">Não há aprovações no momento.</Typography>
    </Paper>
  );
};

export default PendingApprovals;
