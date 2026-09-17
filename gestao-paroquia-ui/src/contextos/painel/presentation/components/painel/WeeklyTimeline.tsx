import React from 'react';
import {Box, Paper, Stack, Typography} from '@mui/material';
import OrgIcon from '@mui/icons-material/Business';
import PlaceIcon from '@mui/icons-material/Place';
import {WeeklyEvent} from '@/shared/domain/types/types';

interface WeeklyTimelineProps {
  events: WeeklyEvent[];
}

const WeeklyTimeline: React.FC<WeeklyTimelineProps> = ({ events }) => {

  const formatEventDate = (dateStr: string) => {
    try {
      const date = new Date(dateStr);
      const weekday = date.toLocaleDateString('pt-BR', { weekday: 'short' });
      const dayMonth = date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
      const time = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
      return {
        weekday: weekday.charAt(0).toUpperCase() + weekday.slice(1).replace('.', ''),
        dayMonth,
        time,
      };
    } catch (e) {
      return { weekday: '---', dayMonth: '--/--', time: '--:--' };
    }
  };

  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        border: '1px solid',
        borderColor: 'divider',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary', mb: 1 }}>
        Agenda da Semana
      </Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 3 }}>
        Eventos e celebrações agendados para os próximos dias
      </Typography>

      <Box sx={{ flexGrow: 1, position: 'relative' }}>
        {events.length > 0 ? (
          <Stack spacing={0.5}>
            {events.map((event, index) => {
              const { weekday, dayMonth, time } = formatEventDate(event.inicio);
              const isLast = index === events.length - 1;

              return (
                <Box key={index} sx={{ display: 'flex', minHeight: 70 }}>
                  {/* Left block: Date/Time */}
                  <Box sx={{ width: 65, pr: 2, textAlign: 'right', pt: 0.5 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main', fontSize: 13 }}>
                      {weekday}
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: 11, mt: -0.2 }}>
                      {dayMonth}
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: 11, mt: -0.2, fontWeight: 500 }}>
                      {time}
                    </Typography>
                  </Box>

                  {/* Connector block */}
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', px: 1, position: 'relative' }}>
                    <Box
                      sx={{
                        width: 10,
                        height: 10,
                        borderRadius: '50%',
                        bgcolor: 'primary.main',
                        border: '2px solid',
                        borderColor: 'background.paper',
                        zIndex: 1,
                        mt: 1,
                      }}
                    />
                    {!isLast && (
                      <Box
                        sx={{
                          width: 2,
                          flexGrow: 1,
                          bgcolor: 'divider',
                          my: 0.5,
                        }}
                      />
                    )}
                  </Box>

                  {/* Right block: Content */}
                  <Box sx={{ flexGrow: 1, pl: 2, pb: isLast ? 0 : 3, pt: 0.5 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'text.primary', lineHeight: 1.3 }}>
                      {event.titulo}
                    </Typography>
                    <Stack direction="row" spacing={1.5} sx={{ mt: 0.5, color: 'text.secondary', display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                      {event.localNome && (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.3 }}>
                          <PlaceIcon sx={{ fontSize: 12 }} />
                          <Typography variant="caption">{event.localNome}</Typography>
                        </Box>
                      )}
                      {event.grupoParoquialNome && (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.3 }}>
                          <OrgIcon sx={{ fontSize: 12 }} />
                          <Typography variant="caption">{event.grupoParoquialNome}</Typography>
                        </Box>
                      )}
                    </Stack>
                  </Box>
                </Box>
              );
            })}
          </Stack>
        ) : (
          <Box sx={{ py: 6, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Nenhum evento agendado para esta semana.
            </Typography>
          </Box>
        )}
      </Box>
    </Paper>
  );
};

export default WeeklyTimeline;
