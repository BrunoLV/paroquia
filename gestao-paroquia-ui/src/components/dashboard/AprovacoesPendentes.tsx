import PendingActionsIcon from '@mui/icons-material/PendingActions';
import { Box, Paper, Typography } from '@mui/material';

const AprovacoesPendentes = () => {
  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        border: '1px solid',
        borderColor: 'divider',
        borderTop: '4px solid #e65100',
        background: 'linear-gradient(to bottom, #ffffff, #fcfcfc)',
        boxShadow: '0 4px 20px rgba(0,0,0,0.02)',
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <PendingActionsIcon sx={{ color: '#e65100' }} />
        <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary' }}>
          Aprovações Pendentes
        </Typography>
      </Box>
      <Typography variant="body2" color="text.secondary">
        Não há aprovações no momento.
      </Typography>
    </Paper>
  );
};

export default AprovacoesPendentes;
