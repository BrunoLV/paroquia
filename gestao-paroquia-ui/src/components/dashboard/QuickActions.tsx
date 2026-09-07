import React from 'react';
import {Card, CardContent, Stack, Typography} from '@mui/material';
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
    <Card variant="outlined">
      <CardContent sx={{py: 1.5, '&:last-child': {pb: 1.5}}}>
        <Stack direction={{xs: 'column', md: 'row'}} sx={{alignItems: {md: 'center'}, justifyContent: 'space-between', gap: 1.5}}>
          <Typography variant="h6" sx={{fontWeight: 700}}>Ações Rápidas</Typography>
          <Stack direction="row" sx={{flexWrap: 'wrap', gap: 1}}>
            <ActionButton title="Novo Evento" icon={<AddIcon />} color="primary" onClick={onNewEvent} />
            <ActionButton title="Novo Fiel" icon={<MemberIcon />} color="primary" onClick={() => navigate('/fieis?novo=1')} />
            <ActionButton title="Novo Projeto" icon={<ProjectIcon />} color="primary" onClick={() => navigate('/projects?novo=1')} />
            <ActionButton title="Ver Calendário" icon={<CalendarIcon />} color="primary" onClick={() => navigate('/calendar')} />
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
};

export default QuickActions;
