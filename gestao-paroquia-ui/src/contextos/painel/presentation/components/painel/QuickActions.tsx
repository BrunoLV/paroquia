import React from 'react';
import {Card, CardContent, Stack, Typography} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CalendarIcon from '@mui/icons-material/CalendarMonth';
import MemberIcon from '@mui/icons-material/Groups';
import ProjectIcon from '@mui/icons-material/Assignment';
import { useNavigate } from 'react-router-dom';
import ActionButton from '@/shared/presentation/components/ActionButton';

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
            <ActionButton title="Novo Projeto" icon={<ProjectIcon />} color="primary" onClick={() => navigate('/projetos?novo=1')} />
            <ActionButton title="Ver Calendário" icon={<CalendarIcon />} color="primary" onClick={() => navigate('/calendario')} />
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
};

export default QuickActions;
