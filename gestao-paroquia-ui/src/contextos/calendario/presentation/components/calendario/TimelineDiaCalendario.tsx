import { useMemo } from 'react';
import {
  alpha,
  Box,
  Card,
  Chip,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import AddIcon from '@mui/icons-material/Add';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import { Evento } from '@/shared/domain/types/types';
import { getCategoryColor } from '@/shared/utils/calendarHelpers';
import CartaoEventoCalendario from '@/contextos/calendario/presentation/components/calendario/CartaoEventoCalendario';

export interface TimelineDiaCalendarioProps {
  date: Date;
  events: Evento[];
  onEventClick: (event: Evento) => void;
  onDayClick: (date: Date) => void;
  getOrgName: (orgId: string, igrejaVinculoId?: string | null) => string;
  getLocalName?: (localId: string | null) => string | null;
}

const DEFAULT_START_HOUR = 7;
const DEFAULT_END_HOUR = 22;

/**
 * Visualização em Linha do Tempo (Timeline View) para os eventos de um dia.
 * Apresenta grade horária contínua (07:00 às 22:00+), nós temáticos por categoria litúrgica,
 * indicador em tempo real "Agora" com linha destacada e slots horários interativos.
 */
export const TimelineDiaCalendario = ({
  date,
  events,
  onEventClick,
  onDayClick,
  getOrgName,
  getLocalName,
}: TimelineDiaCalendarioProps) => {
  const isToday = useMemo(() => {
    const today = new Date();
    return (
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    );
  }, [date]);

  const now = new Date();
  const currentHour = now.getHours();
  const currentMinutes = now.getMinutes();
  const agoraTexto = now.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

  // Segregação entre eventos de Dia Inteiro e eventos com horário marcado
  const { allDayEvents, timedEvents } = useMemo(() => {
    const allDay: Evento[] = [];
    const timed: Evento[] = [];

    events.forEach((evt) => {
      const isExplicitAllDay = (evt as unknown as { diaInteiro?: boolean }).diaInteiro;
      const start = new Date(evt.inicio);
      const end = new Date(evt.fim);
      const durationHours = (end.getTime() - start.getTime()) / (1000 * 60 * 60);

      if (isExplicitAllDay || durationHours >= 23) {
        allDay.push(evt);
      } else {
        timed.push(evt);
      }
    });

    timed.sort((a, b) => new Date(a.inicio).getTime() - new Date(b.inicio).getTime());

    return { allDayEvents: allDay, timedEvents: timed };
  }, [events]);

  // Cálculo da faixa de horas a exibir (07:00 às 22:00, expandindo se houver eventos mais cedo ou tarde)
  const { startHour, endHour, hoursList } = useMemo(() => {
    let minH = DEFAULT_START_HOUR;
    let maxH = DEFAULT_END_HOUR;

    if (isToday) {
      minH = Math.min(minH, currentHour);
      maxH = Math.max(maxH, currentHour);
    }

    timedEvents.forEach((evt) => {
      const sH = new Date(evt.inicio).getHours();
      const eH = new Date(evt.fim).getHours();
      if (sH < minH) minH = sH;
      if (eH > maxH) maxH = Math.min(23, eH + 1);
    });

    const list: number[] = [];
    for (let h = minH; h <= maxH; h++) {
      list.push(h);
    }

    return { startHour: minH, endHour: maxH, hoursList: list };
  }, [isToday, currentHour, timedEvents]);

  // Agrupamento de eventos por hora de início
  const eventsByStartHour = useMemo(() => {
    const map = new Map<number, Evento[]>();
    timedEvents.forEach((evt) => {
      const h = new Date(evt.inicio).getHours();
      const list = map.get(h) || [];
      list.push(evt);
      map.set(h, list);
    });
    return map;
  }, [timedEvents]);

  return (
    <Stack spacing={2.5}>
      {/* Seção de Eventos de Dia Inteiro */}
      {allDayEvents.length > 0 && (
        <Card
          elevation={0}
          sx={{
            p: 2,
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 2,
            bgcolor: 'action.hover',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
            <WbSunnyIcon sx={{ fontSize: 20, color: 'warning.main' }} />
            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'text.primary' }}>
              Celebrações e Eventos de Dia Inteiro ({allDayEvents.length})
            </Typography>
          </Box>
          <Stack spacing={1.5}>
            {allDayEvents.map((evt) => (
              <CartaoEventoCalendario
                key={evt.id}
                evento={evt}
                onClick={() => onEventClick(evt)}
                getOrgName={getOrgName}
                getLocalName={getLocalName}
              />
            ))}
          </Stack>
        </Card>
      )}

      {/* Grade Horária da Linha do Tempo */}
      <Card
        elevation={0}
        sx={{
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: 2,
          bgcolor: 'background.paper',
          p: { xs: 1.5, sm: 2.5 },
        }}
      >
        <Box sx={{ position: 'relative' }}>
          {hoursList.map((hour, idx) => {
            const hourLabel = `${String(hour).padStart(2, '0')}:00`;
            const hourEvents = eventsByStartHour.get(hour) || [];
            const hasEvents = hourEvents.length > 0;
            const isCurrent = isToday && currentHour === hour;
            const isLastHour = idx === hoursList.length - 1;

            return (
              <Box key={hour} sx={{ position: 'relative' }}>
                {/* Linha Indicadora do Momento Atual ("Agora") */}
                {isCurrent && (
                  <Box
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.5,
                      my: 1,
                      zIndex: 3,
                    }}
                  >
                    <Box sx={{ minWidth: { xs: 50, sm: 65 }, textAlign: 'right' }}>
                      <Typography variant="caption" sx={{ fontWeight: 800, color: 'error.main', fontSize: '0.75rem' }}>
                        {agoraTexto}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1, gap: 1 }}>
                      <Box
                        sx={{
                          width: 10,
                          height: 10,
                          borderRadius: '50%',
                          bgcolor: 'error.main',
                          boxShadow: (theme) => `0 0 0 3px ${alpha(theme.palette.error.main, 0.25)}`,
                        }}
                      />
                      <Box sx={{ height: 2, flexGrow: 1, bgcolor: 'error.main', borderRadius: 1 }} />
                      <Chip
                        label="Agora"
                        size="small"
                        sx={{
                          height: 18,
                          fontSize: '0.625rem',
                          fontWeight: 700,
                          bgcolor: 'error.main',
                          color: 'error.contrastText',
                        }}
                      />
                    </Box>
                  </Box>
                )}

                {/* Linha do Slot Horário */}
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: { xs: 1.5, sm: 2 },
                    minHeight: hasEvents ? 'auto' : 52,
                    py: 1,
                    position: 'relative',
                    '&:hover .add-hour-btn': { opacity: 1 },
                  }}
                >
                  {/* Rótulo de Hora à Esquerda */}
                  <Box sx={{ minWidth: { xs: 50, sm: 65 }, textAlign: 'right', pt: 0.5, flexShrink: 0 }}>
                    <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary', fontSize: '0.8rem' }}>
                      {hourLabel}
                    </Typography>
                  </Box>

                  {/* Eixo / Trilho Vertical */}
                  <Box
                    sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      flexShrink: 0,
                      width: 20,
                      pt: 0.75,
                    }}
                  >
                    {/* Marcador do Slot */}
                    <Box
                      sx={{
                        width: hasEvents ? 14 : 8,
                        height: hasEvents ? 14 : 8,
                        borderRadius: '50%',
                        bgcolor: hasEvents
                          ? getCategoryColor(hourEvents[0].categoria).bg
                          : 'action.disabled',
                        border: '2px solid',
                        borderColor: hasEvents
                          ? getCategoryColor(hourEvents[0].categoria).border
                          : 'divider',
                        transition: 'all 0.15s ease',
                      }}
                    />

                    {/* Linha Vertical Conectora */}
                    {!isLastHour && (
                      <Box
                        sx={{
                          width: 2,
                          flexGrow: 1,
                          minHeight: hasEvents ? 20 : 40,
                          bgcolor: 'divider',
                          my: 0.5,
                        }}
                      />
                    )}
                  </Box>

                  {/* Conteúdo: Eventos no Horário ou Linha de Slot Vazio */}
                  <Box sx={{ flexGrow: 1, minWidth: 0, pt: 0.25 }}>
                    {hasEvents ? (
                      <Stack spacing={1.5} sx={{ mb: 1.5 }}>
                        {hourEvents.map((evt) => (
                          <CartaoEventoCalendario
                            key={evt.id}
                            evento={evt}
                            onClick={() => onEventClick(evt)}
                            getOrgName={getOrgName}
                            getLocalName={getLocalName}
                          />
                        ))}
                      </Stack>
                    ) : (
                      <Box
                        sx={{
                          height: 36,
                          borderTop: '1px dashed',
                          borderColor: 'divider',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'flex-start',
                          px: 1,
                          cursor: 'pointer',
                          borderRadius: 1,
                          transition: 'background-color 0.15s',
                          '&:hover': {
                            bgcolor: 'action.hover',
                          },
                        }}
                        onClick={() => {
                          const slotDate = new Date(date);
                          slotDate.setHours(hour, 0, 0, 0);
                          onDayClick(slotDate);
                        }}
                      >
                        <Tooltip title={`Agendar às ${hourLabel}`} arrow>
                          <Typography
                            className="add-hour-btn"
                            variant="caption"
                            sx={{
                              color: 'primary.main',
                              fontWeight: 600,
                              opacity: 0,
                              transition: 'opacity 0.2s',
                              display: 'flex',
                              alignItems: 'center',
                              gap: 0.5,
                            }}
                          >
                            <AddIcon sx={{ fontSize: 14 }} /> Agendar às {hourLabel}
                          </Typography>
                        </Tooltip>
                      </Box>
                    )}
                  </Box>
                </Box>
              </Box>
            );
          })}
        </Box>
      </Card>
    </Stack>
  );
};

export default TimelineDiaCalendario;
