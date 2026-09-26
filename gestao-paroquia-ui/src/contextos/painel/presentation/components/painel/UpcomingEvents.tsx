import React from 'react';
import { Box, Chip, Paper, Skeleton, Stack, Typography } from '@mui/material';
import EventBusyIcon from '@mui/icons-material/EventBusy';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import { useNavigate } from 'react-router-dom';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { getCategoriaEventoMeta } from '@/shared/utils/statusMeta/categoriaEventoStatus';

interface Evento {
  id: string;
  titulo: string;
  inicio: string;
  categoria: string;
}

interface UpcomingEventsProps {
  events?: Evento[];
  isLoading?: boolean;
}

const UpcomingEvents: React.FC<UpcomingEventsProps> = ({ events, isLoading }) => {
  const navigate = useNavigate();

  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <CalendarMonthIcon color="primary" fontSize="small" />
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
            Próximos Eventos
          </Typography>
        </Stack>
        {events && events.length > 0 && (
          <Chip label={`${events.length}`} size="small" variant="outlined" sx={{ fontWeight: 600 }} />
        )}
      </Box>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Celebrações e compromissos confirmados
      </Typography>

      {isLoading ? (
        <Stack spacing={2}>
          {[1, 2, 3].map((i) => (
            <Stack key={i} direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
              <Skeleton variant="rounded" width={44} height={44} />
              <Box sx={{ flexGrow: 1 }}>
                <Skeleton variant="text" width="70%" />
                <Skeleton variant="text" width="40%" />
              </Box>
            </Stack>
          ))}
        </Stack>
      ) : events && events.length > 0 ? (
        <Stack spacing={0.5}>
          {events.map((event) => {
            const start = new Date(event.inicio);
            const meta = getCategoriaEventoMeta(event.categoria);
            return (
              <Box
                key={event.id}
                onClick={() => navigate('/calendario')}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1.5,
                  py: 1,
                  px: 1,
                  mx: -1,
                  borderRadius: 1,
                  cursor: 'pointer',
                  transition: 'background-color 0.15s',
                  '&:hover': { bgcolor: 'action.hover' },
                }}
              >
                <Box
                  sx={{
                    width: 44,
                    height: 44,
                    flexShrink: 0,
                    borderRadius: 1,
                    bgcolor: 'action.hover',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    lineHeight: 1,
                  }}
                >
                  <Typography variant="caption" sx={{ fontWeight: 700, fontSize: '0.65rem', color: 'text.secondary', textTransform: 'uppercase' }}>
                    {start.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')}
                  </Typography>
                  <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.1 }}>
                    {start.getDate()}
                  </Typography>
                </Box>

                <Box sx={{ minWidth: 0, flexGrow: 1 }}>
                  <Typography variant="body2" noWrap sx={{ fontWeight: 600 }}>
                    {event.titulo}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {start.toLocaleDateString('pt-BR', { weekday: 'long' })} · {start.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                  </Typography>
                </Box>

                <StatusIcon {...meta} size="small" />
              </Box>
            );
          })}
        </Stack>
      ) : (
        <Box sx={{ py: 6, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
          <EventBusyIcon sx={{ fontSize: 40, color: 'action.disabled' }} />
          <Typography color="text.secondary" variant="body2">
            Não há eventos próximos para mostrar.
          </Typography>
        </Box>
      )}
    </Paper>
  );
};

export default UpcomingEvents;
