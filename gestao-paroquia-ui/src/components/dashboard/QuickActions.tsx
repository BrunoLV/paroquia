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
    <Paper elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider' }}>
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>
        Ações Rápidas
      </Typography>
      <Stack direction="row" spacing={2} sx={{ justifyContent: 'flex-start' }}>
        <ActionButton
          title="Novo Evento"
          icon={<AddIcon />}
          color="primary"
          onClick={onNewEvent}
        />
        <ActionButton
          title="Ver Calendário"
          icon={<CalendarIcon />}
          color="primary"
          onClick={() => navigate('/calendar')}
        />
      </Stack>
    </Paper>
  );
};

export default QuickActions;
