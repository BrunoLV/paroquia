import React from 'react';
import { Paper, Stack, Typography } from '@mui/material';
import { Add as AddIcon, CalendarMonth as CalendarIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import ActionButton from '../ActionButton';

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
      <Stack direction="row" spacing={2} sx={{ justifyContent: 'flex-start' }}>
        <ActionButton
          title="Novo Evento"
          icon={<AddIcon />}
          color="primary"
          sx={{ border: '1px solid', borderColor: 'primary.main' }}
          onClick={onNewEvent}
        />
        <ActionButton
          title="Ver Calendário"
          icon={<CalendarIcon />}
          color="primary"
          sx={{ border: '1px solid', borderColor: 'primary.main' }}
          onClick={() => navigate('/calendar')}
        />
      </Stack>
    </Paper>
  );
};

export default QuickActions;
