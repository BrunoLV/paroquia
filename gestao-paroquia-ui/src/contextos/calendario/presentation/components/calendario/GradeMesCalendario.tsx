import { alpha, Box, Card, Grid, Typography } from '@mui/material';
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
  onDaySelect?: (date: Date) => void;
  onEventClick: (event: Evento) => void;
}

export type CalendarMonthGridProps = GradeMesCalendarioProps;

/**
 * Grade mensal (6 semanas x 7 dias) do calendário paroquial.
 * Apresenta harmonização visual com a visualização semanal e os cartões de eventos,
 * integrando cores litúrgicas e pílulas semânticas padronizadas.
 */
export const GradeMesCalendario = ({
  calendarDays,
  getEventsForDay,
  onDayClick,
  onDaySelect,
  onEventClick,
}: GradeMesCalendarioProps) => {
  return (
    <Card
      elevation={0}
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        overflow: 'hidden',
      }}
    >
      {/* Cabeçalho dos dias da semana */}
      <Grid
        container
        columns={7}
        sx={{
          bgcolor: 'action.hover',
          borderBottom: '1px solid',
          borderColor: 'divider',
          textAlign: 'center',
          py: 1.5,
        }}
      >
        {WEEKDAY_LABELS.map((day) => (
          <Grid size={{ xs: 1 }} key={day}>
            <Typography
              variant="caption"
              sx={{
                fontWeight: 700,
                color: 'text.secondary',
                textTransform: 'uppercase',
                letterSpacing: 0.5,
              }}
            >
              {day}
            </Typography>
          </Grid>
        ))}
      </Grid>

      {/* Células da Grade Mensal */}
      <Grid container columns={7}>
        {calendarDays.map(({ date, isCurrentMonth, isToday }, idx) => {
          const dayEvents = getEventsForDay(date);
          const season = getLiturgicalSeason(date);
          const seasonStyle = getLiturgicalSeasonStyle(season);

          return (
            <Grid
              size={{ xs: 1 }}
              key={idx}
              onClick={() => (onDaySelect ? onDaySelect(date) : onDayClick(date))}
              sx={{
                minHeight: 130,
                borderRight: (idx + 1) % 7 === 0 ? 'none' : '1px solid',
                borderBottom: idx < 35 ? '1px solid' : 'none',
                borderColor: 'divider',
                bgcolor: isToday
                  ? (theme) => alpha(theme.palette.primary.main, 0.05)
                  : isCurrentMonth
                  ? seasonStyle.backgroundColor
                  : 'action.hover',
                p: 1,
                cursor: 'pointer',
                transition: 'all 0.15s ease',
                position: 'relative',
                '&:hover': {
                  bgcolor: isToday
                    ? (theme) => alpha(theme.palette.primary.main, 0.12)
                    : (theme) => alpha(theme.palette.action.selected, 0.1),
                  '& .add-day-btn': { opacity: 1 },
                },
              }}
            >
              {/* Número do dia e rótulo litúrgico */}
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: isToday ? 800 : isCurrentMonth ? 600 : 400,
                    color: isToday ? 'primary.contrastText' : isCurrentMonth ? 'text.primary' : 'text.disabled',
                    bgcolor: isToday ? 'primary.main' : 'transparent',
                    borderRadius: '50%',
                    width: 28,
                    height: 28,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    lineHeight: 1,
                  }}
                >
                  {date.getDate()}
                </Typography>

                {/* Rótulo da estação litúrgica */}
                <Typography
                  variant="caption"
                  sx={{
                    fontSize: '0.625rem',
                    fontWeight: 700,
                    color: seasonStyle.textColor,
                    opacity: isCurrentMonth ? 0.9 : 0.4,
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

                {/* Ícone de adição no hover */}
                <AddIcon
                  className="add-day-btn"
                  onClick={(e) => {
                    e.stopPropagation();
                    onDayClick(date);
                  }}
                  sx={{
                    fontSize: '1rem',
                    color: 'primary.main',
                    opacity: 0,
                    transition: 'opacity 0.2s',
                  }}
                />
              </Box>

              {/* Lista de Eventos na Célula */}
              <Box sx={{ height: 90, overflowY: 'auto', pr: 0.5 }}>
                {dayEvents.map((evt) => {
                  const styleColors = getCategoryColor(evt.categoria);
                  const isCancelado = evt.status?.toUpperCase() === 'CANCELADO';
                  const isFalhaExecucao = evt.status?.toUpperCase() === 'FALHA_EXECUCAO';
                  const isConflito = evt.estadoConflito?.toUpperCase() === 'PENDENTE';
                  const isPendenteAprovacao = Boolean(evt.aprovacaoPendenteId);

                  let prefix = '';
                  if (isFalhaExecucao) {
                    prefix = '[Falha] ';
                  } else if (isConflito) {
                    prefix = '[Conflito] ';
                  } else if (isPendenteAprovacao) {
                    prefix = '[Pendente] ';
                  }

                  const displayTitle = `${prefix}${evt.titulo}`;

                  let borderLeftColor = styleColors.border;
                  if (isCancelado || isFalhaExecucao) {
                    borderLeftColor = 'error.main';
                  } else if (isConflito) {
                    borderLeftColor = 'warning.main';
                  }

                  const formatoIcon = getFormatoEventoIcon(evt.formato, 'inherit');
                  const horario = formatarHorarioEvento(evt);

                  return (
                    <Box
                      key={evt.id}
                      onClick={(e) => {
                        e.stopPropagation();
                        onEventClick(evt);
                      }}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 0.5,
                        px: 0.75,
                        py: 0.25,
                        mb: 0.5,
                        borderRadius: 1,
                        bgcolor: 'background.paper',
                        border: '1px solid',
                        borderColor: 'divider',
                        borderLeft: '3px solid',
                        borderLeftColor: borderLeftColor,
                        fontSize: '0.72rem',
                        fontWeight: 600,
                        color: isCancelado ? 'text.disabled' : 'text.primary',
                        textDecoration: isCancelado ? 'line-through' : 'none',
                        opacity: isPendenteAprovacao ? 0.75 : isCancelado ? 0.75 : 1,
                        cursor: 'pointer',
                        transition: 'all 0.12s ease',
                        boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
                        '&:hover': {
                          bgcolor: 'action.hover',
                          boxShadow: (theme) => theme.shadows[1],
                        },
                      }}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', color: 'primary.main', fontSize: '0.75rem' }}>
                        {formatoIcon}
                      </Box>
                      <Typography
                        variant="caption"
                        sx={{
                          fontSize: '0.65rem',
                          fontWeight: 700,
                          color: 'primary.main',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {horario}
                      </Typography>
                      <Typography
                        variant="caption"
                        sx={{
                          fontSize: '0.7rem',
                          fontWeight: 600,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {displayTitle}
                      </Typography>
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

export default GradeMesCalendario;
