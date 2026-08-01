import React from 'react';
import {Button, Paper, Stack, Typography} from '@mui/material';
import {Add as AddIcon, CalendarMonth as CalendarIcon} from '@mui/icons-material';
import {useNavigate} from 'react-router-dom';

interface QuickActionsProps {
  onNewEvent?: () => void;
}

const QuickActions: React.FC<QuickActionsProps> = ({ onNewEvent }) => {
  const navigate = useNavigate();

  return (
    <Paper sx={{ p: 3, boxShadow: '0 4px 20px rgba(0,0,0,0.05)' }}>
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Ações Rápidas
      </Typography>
      <Stack spacing={2}>
        <Button
          variant="contained"
          fullWidth
          startIcon={<AddIcon />}
          sx={{ textTransform: 'none', py: 1 }}
          onClick={onNewEvent}
        >
          Novo Evento
        </Button>
        <Button
          variant="outlined"
          fullWidth
          startIcon={<CalendarIcon />}
          sx={{ textTransform: 'none', py: 1 }}
          onClick={() => navigate('/calendar')}
        >
          Ver Calendário
        </Button>
      </Stack>
    </Paper>
  );
};

export default QuickActions;
