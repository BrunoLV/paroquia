import React from 'react';
import { Paper, Stack, Typography } from '@mui/material';
import {
  Add as AddIcon,
  CalendarMonth as CalendarIcon,
  Groups as MemberIcon,
  Assignment as ProjectIcon,
} from '@mui/icons-material';
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
      <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap', gap: 1, justifyContent: 'flex-start' }}>
        <ActionButton
          title="Novo Evento"
          icon={<AddIcon />}
          color="primary"
          onClick={onNewEvent}
        />
        <ActionButton
          title="Novo Fiel"
          icon={<MemberIcon />}
          color="primary"
          onClick={() => navigate('/fieis?novo=1')}
        />
        <ActionButton
          title="Novo Projeto"
          icon={<ProjectIcon />}
          color="primary"
          onClick={() => navigate('/projects?novo=1')}
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
