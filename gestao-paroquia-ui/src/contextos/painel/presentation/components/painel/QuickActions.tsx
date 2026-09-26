import React from 'react';
import {Card, CardContent, Stack, Typography} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CalendarIcon from '@mui/icons-material/CalendarMonth';
import MemberIcon from '@mui/icons-material/Groups';
import ProjectIcon from '@mui/icons-material/Assignment';
import { useNavigate } from 'react-router-dom';
import ActionButton from '@/shared/presentation/components/ActionButton';

import BoltIcon from '@mui/icons-material/Bolt';

interface QuickActionsProps {
  onNewEvent?: () => void;
}

/**
 * Barra de atalhos operacionais rápidos do Painel com hierarquia visual refinada.
 */
const QuickActions: React.FC<QuickActionsProps> = ({ onNewEvent }) => {
  const navigate = useNavigate();

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
        <Stack direction={{ xs: 'column', md: 'row' }} sx={{ alignItems: { md: 'center' }, justifyContent: 'space-between', gap: 1.5 }}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <BoltIcon color="primary" fontSize="small" />
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
              Ações Rápidas
            </Typography>
          </Stack>
          <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1 }}>
            <ActionButton title="Novo Evento" icon={<AddIcon />} color="primary" variant="contained" onClick={onNewEvent} />
            <ActionButton title="Novo Fiel" icon={<MemberIcon />} color="primary" variant="outlined" onClick={() => navigate('/fieis?novo=1')} />
            <ActionButton title="Novo Projeto" icon={<ProjectIcon />} color="primary" variant="outlined" onClick={() => navigate('/projetos?novo=1')} />
            <ActionButton title="Ver Calendário" icon={<CalendarIcon />} color="primary" variant="outlined" onClick={() => navigate('/calendario')} />
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
};

export default QuickActions;
