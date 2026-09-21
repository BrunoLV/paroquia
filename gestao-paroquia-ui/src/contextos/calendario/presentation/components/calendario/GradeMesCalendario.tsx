import { alpha, Box, Card, Grid, Stack, Typography, useTheme } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { CalendarDay } from '@/contextos/calendario/presentation/hooks/useCalendarGrid';
import { getCategoryColor } from '@/shared/utils/calendarHelpers';
import { getLiturgicalSeason, getLiturgicalSeasonStyle } from '@/shared/utils/liturgicalCalendar';
import { Evento } from '@/shared/domain/types/types';
import { formatarHorarioEvento } from '@/contextos/calendario/presentation/utils/horarioEventoUtils';
import { getFormatoEventoIcon } from '@/contextos/calendario/presentation/utils/formatoEventoUtils';

const WEEKDAY_LABELS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

export interface GradeMesCalendarioProps {
  calendarDays: CalendarDay[];
  getEventsForDay: (day: Date) => Evento[];
  onDayClick: (date: Date) => void;
  onEventClick: (event: Evento) => void;
}

export type CalendarMonthGridProps = GradeMesCalendarioProps;

/** Grade mensal (6 semanas x 7 dias) do calendário, com cor litúrgica de fundo por dia e
 * pílulas de evento coloridas por categoria. Clicar num dia vazio abre a criação de evento
 * pré-preenchida com a data; clicar num evento abre os detalhes (propagação interrompida para
 * não também abrir a criação). */
const GradeMesCalendario = ({ calendarDays, getEventsForDay, onDayClick, onEventClick }: GradeMesCalendarioProps) => {
  const theme = useTheme();
  return (
  <Card sx={{ overflow: 'hidden' }}>
    {/* Days of week header */}
    <Grid container columns={7} sx={{ bgcolor: 'action.hover', borderBottom: '1px solid', borderColor: 'divider', textAlign: 'center', py: 1.5 }}>
      {WEEKDAY_LABELS.map((day) => (
        <Grid size={{ xs: 1 }} key={day}>
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'text.secondary' }}>
            {day}
          </Typography>
        </Grid>
      ))}
    </Grid>

    {/* Calendario Grid Cells */}
    <Grid container columns={7}>
      {calendarDays.map(({ date, isCurrentMonth, isToday }, idx) => {
        const dayEvents = getEventsForDay(date);
        const season = getLiturgicalSeason(date);
        const seasonStyle = getLiturgicalSeasonStyle(season);
        return (
          <Grid
            size={{ xs: 1 }}
            key={idx}
            onClick={() => onDayClick(date)}
            sx={{
              minHeight: 130,
              borderRight: '1px solid',
              borderBottom: '1px solid',
              borderColor: 'divider',
              bgcolor: isToday
                ? (theme) => alpha(theme.palette.success.main, 0.08)
                : isCurrentMonth ? seasonStyle.backgroundColor : 'action.hover',
              p: 1,
              cursor: 'pointer',
              transition: 'all 0.15s ease',
              position: 'relative',
              '&:hover': {
                bgcolor: isToday
                  ? (theme) => alpha(theme.palette.success.main, 0.16)
                  : 'action.selected',
                '& .add-day-btn': { opacity: 1 },
              },
            }}
          >
            {/* Day number, liturgical season label (desktop) and indicator */}
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
              <Typography
                variant="body2"
                sx={{
                  fontWeight: isToday ? 700 : 500,
                  color: isToday ? 'success.dark' : isCurrentMonth ? 'text.primary' : 'text.disabled',
                  bgcolor: isToday ? (theme) => alpha(theme.palette.success.main, 0.2) : 'transparent',
                  borderRadius: '50%',
                  width: 24,
                  height: 24,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                {date.getDate()}
              </Typography>

              {/* Liturgical Season Label for Desktop */}
              <Typography
                variant="caption"
                sx={{
                  fontSize: '0.625rem',
                  fontWeight: 700,
                  color: seasonStyle.textColor,
                  opacity: isCurrentMonth ? 0.85 : 0.45,
                  textTransform: 'uppercase',
                  lineHeight: 1,
                  display: { xs: 'none', md: 'block' },
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                }}
              >
                {seasonStyle.label}
              </Typography>

              {/* Plus icon on hover */}
              <AddIcon
                className="add-day-btn"
                color="action"
                sx={{
                  fontSize: '1rem',
                  opacity: 0,
                  transition: 'opacity 0.2s',
                  '&:hover': { color: 'success.dark' },
                }}
              />
            </Box>

            {/* Events list in the day cell */}
            <Box sx={{ height: 90, overflowY: 'auto', pr: 0.5 }}>
              {dayEvents.map((evt) => {
                const styleColors = getCategoryColor(evt.categoria);
                const isCancelado = evt.status?.toUpperCase() === 'CANCELADO';
                const isFalhaExecucao = evt.status?.toUpperCase() === 'FALHA_EXECUCAO';
                const isConflito = evt.estadoConflito?.toUpperCase() === 'PENDENTE';
                const isPendenteAprovacao = Boolean(evt.aprovacaoPendenteId);

                let prefix = '';
                if (isFalhaExecucao) {
                  prefix = '[Falha de Execução] ';
                } else if (isConflito) {
                  prefix = '[Conflito] ';
                } else if (isPendenteAprovacao) {
                  prefix = '[Pendente] ';
                }

                const displayTitle = `${prefix}${evt.titulo}`;

                let bgColor = styleColors.bg;
                let textColor = styleColors.text;
                let borderColor = styleColors.border;

                if (isFalhaExecucao) {
                  bgColor = alpha(theme.palette.error.main, 0.16);
                  textColor = 'error.dark';
                  borderColor = 'error.main';
                } else if (isConflito) {
                  bgColor = alpha(theme.palette.warning.main, 0.16);
                  textColor = 'warning.dark';
                  borderColor = 'warning.main';
                } else if (isCancelado) {
                  bgColor = alpha(theme.palette.error.main, 0.12);
                  textColor = 'error.dark';
                  borderColor = 'error.main';
                }

                return (
                  <Box
                    key={evt.id}
                    onClick={(e) => {
                      e.stopPropagation(); // prevent opening create modal
                      onEventClick(evt);
                    }}
                    sx={{
                      bgcolor: bgColor,
                      color: textColor,
                      borderLeft: '3px solid',
                      borderLeftColor: borderColor,
                      px: 1,
                      py: 0.5,
                      mb: 0.5,
                      fontSize: '0.72rem',
                      fontWeight: 600,
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      transition: 'all 0.2s',
                      textDecoration: isCancelado ? 'line-through' : 'none',
                      opacity: isPendenteAprovacao ? 0.6 : (isCancelado ? 0.75 : 1),
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.5,
                      '&:hover': {
                        filter: 'brightness(0.93)',
                        transform: 'translateX(2px)',
                      },
                      '& svg': {
                        fontSize: '0.85rem',
                        flexShrink: 0,
                      },
                    }}
                    title={`${displayTitle} (${formatarHorarioEvento(evt)})`}
                  >
                    {getFormatoEventoIcon(evt.formato, 'inherit')}
                    <Box component="span" sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {displayTitle}
                    </Box>
                  </Box>
                );
              })}
            </Box>
          </Grid>
        );
      })}
    </Grid>
  </Card>
  );
};

export const CalendarMonthGrid = GradeMesCalendario;
export default GradeMesCalendario;
