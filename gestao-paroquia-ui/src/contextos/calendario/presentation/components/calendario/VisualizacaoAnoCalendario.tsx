import { useMemo } from 'react';
import {
  alpha,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import EventIcon from '@mui/icons-material/Event';
import { Evento } from '@/shared/domain/types/types';
import { isSameDay, MONTH_NAMES } from '@/shared/utils/calendarHelpers';
import { CALENDAR_CATEGORIES } from '@/contextos/calendario/presentation/hooks/useCalendarFilterState';

const MINI_WEEKDAY_LABELS = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'];

export interface VisualizacaoAnoCalendarioProps {
  currentYear: number;
  getEventsForDay: (day: Date) => Evento[];
  combinedEvents: Evento[];
  onDayClick: (date: Date) => void;
  onEventClick: (event: Evento) => void;
  onMonthSelect: (monthIndex: number) => void;
}

/**
 * Visualização anual (ANO) do calendário paroquial.
 * Apresenta 12 mini-calendários mensais com mapeamento de densidade de eventos e
 * métricas pastorais consolidadas do ano corrente.
 */
export const VisualizacaoAnoCalendario = ({
  currentYear,
  getEventsForDay,
  combinedEvents,
  onDayClick,
  onMonthSelect,
}: VisualizacaoAnoCalendarioProps) => {
  // Contadores por categoria no ano
  const statsPorCategoria = useMemo(() => {
    const counts: Record<string, number> = {};
    CALENDAR_CATEGORIES.forEach((cat) => {
      counts[cat] = 0;
    });
    combinedEvents.forEach((evt) => {
      if (counts[evt.categoria] !== undefined) {
        counts[evt.categoria]++;
      } else {
        counts[evt.categoria] = (counts[evt.categoria] || 0) + 1;
      }
    });
    return counts;
  }, [combinedEvents]);

  // Estrutura de dias de cada mês
  const monthsData = useMemo(() => {
    return Array.from({ length: 12 }, (_, monthIdx) => {
      const firstDay = new Date(currentYear, monthIdx, 1);
      const startDayOfWeek = firstDay.getDay(); // 0 a 6
      const daysInMonth = new Date(currentYear, monthIdx + 1, 0).getDate();

      const cells: (Date | null)[] = [];
      for (let i = 0; i < startDayOfWeek; i++) {
        cells.push(null);
      }
      for (let d = 1; d <= daysInMonth; d++) {
        cells.push(new Date(currentYear, monthIdx, d));
      }

      return {
        monthIndex: monthIdx,
        monthName: MONTH_NAMES[monthIdx],
        cells,
      };
    });
  }, [currentYear]);

  const today = new Date();

  return (
    <Stack spacing={3}>
      {/* Painel Consolidado de Métricas do Ano */}
      <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, bgcolor: 'action.hover' }}>
        <CardContent sx={{ p: { xs: 2, sm: 2.5 } }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2, flexWrap: 'wrap', gap: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <EventIcon sx={{ color: 'primary.main' }} />
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary' }}>
                Visão Geral do Ano {currentYear}
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.secondary' }}>
              {combinedEvents.length} celebração(ões) e evento(s) no período
            </Typography>
          </Box>

          <Grid container spacing={1.5}>
            {CALENDAR_CATEGORIES.map((cat) => (
              <Grid size={{ xs: 6, sm: 4, md: 3, lg: 1.7 }} key={cat}>
                <Box
                  sx={{
                    p: 1.25,
                    borderRadius: 1.5,
                    border: '1px solid',
                    borderColor: 'divider',
                    bgcolor: 'background.paper',
                    textAlign: 'center',
                  }}
                >
                  <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block' }}>
                    {cat}
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    {statsPorCategoria[cat] || 0}
                  </Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        </CardContent>
      </Card>

      {/* Grade com os 12 Mini-Meses */}
      <Grid container spacing={2.5}>
        {monthsData.map(({ monthIndex, monthName, cells }) => (
          <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={monthIndex}>
            <Card
              elevation={0}
              sx={{
                border: '1px solid',
                borderColor: 'divider',
                borderRadius: 2,
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                transition: 'all 0.15s ease',
                '&:hover': {
                  borderColor: 'primary.main',
                  boxShadow: (theme) => theme.shadows[2],
                },
              }}
            >
              {/* Cabeçalho do Mini-Mês */}
              <Box
                sx={{
                  p: 1.5,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  borderBottom: '1px solid',
                  borderColor: 'divider',
                  bgcolor: 'action.hover',
                }}
              >
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'text.primary' }}>
                  {monthName}
                </Typography>
                <Button
                  size="small"
                  variant="text"
                  endIcon={<CalendarMonthIcon sx={{ fontSize: '1rem !important' }} />}
                  onClick={() => onMonthSelect(monthIndex)}
                  sx={{ fontSize: '0.72rem', py: 0.25, px: 0.75 }}
                >
                  Ver Mês
                </Button>
              </Box>

              {/* Tabela do Mini-Mês */}
              <CardContent sx={{ p: 1.5, flexGrow: 1 }}>
                <Grid container columns={7} sx={{ textAlign: 'center', mb: 0.5 }}>
                  {MINI_WEEKDAY_LABELS.map((day, i) => (
                    <Grid size={{ xs: 1 }} key={i}>
                      <Typography
                        variant="caption"
                        sx={{ fontSize: '0.65rem', fontWeight: 700, color: 'text.secondary' }}
                      >
                        {day}
                      </Typography>
                    </Grid>
                  ))}
                </Grid>

                <Grid container columns={7} sx={{ textAlign: 'center' }}>
                  {cells.map((date, cellIdx) => {
                    if (!date) {
                      return <Grid size={{ xs: 1 }} key={`empty-${cellIdx}`} sx={{ height: 26 }} />;
                    }

                    const dayEvents = getEventsForDay(date);
                    const hasEvents = dayEvents.length > 0;
                    const isToday = isSameDay(date, today);

                    const dayTooltip = hasEvents
                      ? `${dayEvents.length} evento(s): ${dayEvents.map((e) => e.titulo).join(', ')}`
                      : `${date.getDate()} de ${monthName}`;

                    return (
                      <Grid size={{ xs: 1 }} key={date.toISOString()}>
                        <Tooltip title={dayTooltip} arrow placement="top">
                          <Box
                            onClick={() => onDayClick(date)}
                            sx={{
                              height: 26,
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              borderRadius: '50%',
                              cursor: 'pointer',
                              position: 'relative',
                              fontSize: '0.72rem',
                              fontWeight: isToday ? 800 : hasEvents ? 700 : 400,
                              bgcolor: isToday
                                ? 'primary.main'
                                : hasEvents
                                ? (theme) => alpha(theme.palette.primary.main, 0.18)
                                : 'transparent',
                              color: isToday
                                ? 'primary.contrastText'
                                : hasEvents
                                ? 'primary.dark'
                                : 'text.primary',
                              '&:hover': {
                                bgcolor: isToday
                                  ? 'primary.dark'
                                  : (theme) => alpha(theme.palette.primary.main, 0.3),
                              },
                            }}
                          >
                            {date.getDate()}
                            {hasEvents && !isToday && (
                              <Box
                                sx={{
                                  position: 'absolute',
                                  bottom: 2,
                                  width: 4,
                                  height: 4,
                                  borderRadius: '50%',
                                  bgcolor: 'primary.main',
                                }}
                              />
                            )}
                          </Box>
                        </Tooltip>
                      </Grid>
                    );
                  })}
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  );
};

export default VisualizacaoAnoCalendario;
