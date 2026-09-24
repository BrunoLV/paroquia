import {
  alpha,
  Box,
  Card,
  Chip,
  Grid,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import IconActionButton from '@/shared/presentation/components/IconActionButton';
import { CalendarDay } from '@/contextos/calendario/presentation/hooks/useCalendarGrid';
import { Evento } from '@/shared/domain/types/types';
import { getCategoryColor } from '@/shared/utils/calendarHelpers';
import { getLiturgicalSeason, getLiturgicalSeasonStyle } from '@/shared/utils/liturgicalCalendar';
import { formatarHorarioEvento } from '@/contextos/calendario/presentation/utils/horarioEventoUtils';
import { getFormatoEventoIcon } from '@/contextos/calendario/presentation/utils/formatoEventoUtils';

const WEEKDAY_NAMES = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

export interface VisualizacaoSemanaCalendarioProps {
  weekDays: CalendarDay[];
  getEventsForDay: (day: Date) => Evento[];
  onDayClick: (date: Date) => void;
  onDaySelect?: (date: Date) => void;
  onEventClick: (event: Evento) => void;
}

/**
 * Visualização semanal (SEMANA) do calendário paroquial.
 * Apresenta grade de 7 colunas (Domingo a Sábado), cabeçalhos litúrgicos padronizados,
 * destaque circular para o dia atual e cartões verticais estilizados para cada celebração.
 */
export const VisualizacaoSemanaCalendario = ({
  weekDays,
  getEventsForDay,
  onDayClick,
  onDaySelect,
  onEventClick,
}: VisualizacaoSemanaCalendarioProps) => {
  return (
    <Card
      elevation={0}
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        p: 1.5,
      }}
    >
      <Box sx={{ overflowX: 'auto', minWidth: 800 }}>
        {/* Cabeçalho dos 7 dias da semana */}
        <Grid container columns={7} spacing={1} sx={{ mb: 1 }}>
          {weekDays.map(({ date, isToday }, idx) => {
            const season = getLiturgicalSeason(date);
            const seasonStyle = getLiturgicalSeasonStyle(season);

            return (
              <Grid
                size={{ xs: 1 }}
                key={idx}
                sx={{
                  p: 1.5,
                  textAlign: 'center',
                  bgcolor: isToday ? (theme) => alpha(theme.palette.primary.main, 0.08) : 'action.hover',
                  border: '1px solid',
                  borderColor: isToday ? 'primary.main' : 'divider',
                  borderRadius: 1.5,
                  position: 'relative',
                  '&:hover .add-week-btn': { opacity: 1 },
                }}
              >
                <Typography
                  variant="caption"
                  sx={{
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    color: isToday ? 'primary.main' : 'text.secondary',
                    letterSpacing: 0.5,
                  }}
                >
                  {WEEKDAY_NAMES[date.getDay()]}
                </Typography>

                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', my: 0.5 }}>
                  <Typography
                    variant="body2"
                    sx={{
                      fontWeight: isToday ? 800 : 600,
                      width: 28,
                      height: 28,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      borderRadius: '50%',
                      bgcolor: isToday ? 'primary.main' : 'transparent',
                      color: isToday ? 'primary.contrastText' : 'text.primary',
                      lineHeight: 1,
                    }}
                  >
                    {date.getDate()}
                  </Typography>
                </Box>

                <Typography
                  variant="caption"
                  sx={{
                    fontSize: '0.625rem',
                    fontWeight: 700,
                    color: seasonStyle.textColor,
                    textTransform: 'uppercase',
                    display: 'block',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}
                >
                  {seasonStyle.label}
                </Typography>

                <Box
                  className="add-week-btn"
                  sx={{
                    position: 'absolute',
                    top: 4,
                    right: 4,
                    opacity: 0,
                    transition: 'opacity 0.15s ease',
                  }}
                >
                  <IconActionButton
                    title="Agendar evento neste dia"
                    size="small"
                    icon={<AddIcon sx={{ fontSize: 16 }} />}
                    onClick={(e) => {
                      e.stopPropagation();
                      onDayClick(date);
                    }}
                    sx={{
                      bgcolor: (theme) => alpha(theme.palette.primary.main, 0.1),
                      p: 0.5,
                      '&:hover': { bgcolor: 'primary.main', color: 'primary.contrastText' },
                    }}
                  />
                </Box>
              </Grid>
            );
          })}
        </Grid>

        {/* Colunas dos 7 dias com eventos */}
        <Grid container columns={7} spacing={1}>
          {weekDays.map(({ date, isToday }, idx) => {
            const dayEvents = getEventsForDay(date);
            const season = getLiturgicalSeason(date);
            const seasonStyle = getLiturgicalSeasonStyle(season);

            return (
              <Grid
                size={{ xs: 1 }}
                key={idx}
                onClick={() => (onDaySelect ? onDaySelect(date) : onDayClick(date))}
                sx={{
                  minHeight: 520,
                  p: 1.25,
                  border: '1px solid',
                  borderColor: isToday ? 'primary.main' : 'divider',
                  borderRadius: 1.5,
                  bgcolor: isToday
                    ? (theme) => alpha(theme.palette.primary.main, 0.04)
                    : seasonStyle.backgroundColor,
                  cursor: 'pointer',
                  transition: 'all 0.15s ease',
                  '&:hover': {
                    borderColor: 'primary.main',
                    boxShadow: (theme) => theme.shadows[1],
                    bgcolor: (theme) => alpha(theme.palette.action.selected, 0.08),
                  },
                }}
              >
                <Stack spacing={1}>
                  {dayEvents.map((evt) => {
                    const styleColors = getCategoryColor(evt.categoria);
                    const isCancelado = evt.status?.toUpperCase() === 'CANCELADO';
                    const isFalhaExecucao = evt.status?.toUpperCase() === 'FALHA_EXECUCAO';
                    const isConflito = evt.estadoConflito?.toUpperCase() === 'PENDENTE';
                    const isPendenteAprovacao = Boolean(evt.aprovacaoPendenteId);

                    let borderLeftColor = styleColors.border;
                    if (isCancelado || isFalhaExecucao) {
                      borderLeftColor = 'error.main';
                    } else if (isConflito) {
                      borderLeftColor = 'warning.main';
                    }

                    const formatoIcon = getFormatoEventoIcon(evt.formato, 'small');
                    const horario = formatarHorarioEvento(evt);

                    return (
                      <Box
                        key={evt.id}
                        onClick={(e) => {
                          e.stopPropagation();
                          onEventClick(evt);
                        }}
                        sx={{
                          p: 1.25,
                          borderRadius: 1.5,
                          bgcolor: 'background.paper',
                          border: '1px solid',
                          borderColor: 'divider',
                          borderLeft: '4px solid',
                          borderLeftColor: borderLeftColor,
                          boxShadow: (theme) => theme.shadows[1],
                          opacity: isPendenteAprovacao ? 0.75 : isCancelado ? 0.75 : 1,
                          transition: 'all 0.15s ease',
                          cursor: 'pointer',
                          '&:hover': {
                            transform: 'translateY(-1px)',
                            boxShadow: (theme) => theme.shadows[2],
                          },
                        }}
                      >
                        {/* Horário e Formato */}
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 0.5 }}>
                          <Typography
                            variant="caption"
                            sx={{ fontWeight: 700, fontSize: '0.68rem', color: 'primary.main' }}
                          >
                            {horario}
                          </Typography>
                          <Box sx={{ display: 'flex', color: 'text.secondary', '& svg': { fontSize: '0.85rem' } }}>
                            {formatoIcon}
                          </Box>
                        </Box>

                        {/* Título do Evento */}
                        <Typography
                          variant="body2"
                          sx={{
                            fontWeight: 600,
                            fontSize: '0.8rem',
                            color: isCancelado ? 'text.disabled' : 'text.primary',
                            textDecoration: isCancelado ? 'line-through' : 'none',
                            lineHeight: 1.3,
                            display: '-webkit-box',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical',
                            overflow: 'hidden',
                          }}
                        >
                          {isPendenteAprovacao ? `[Pendente] ${evt.titulo}` : evt.titulo}
                        </Typography>

                        {/* Badges de Conflito / Categoria */}
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.75, flexWrap: 'wrap' }}>
                          <Chip
                            label={evt.categoria}
                            size="small"
                            sx={{
                              height: 18,
                              bgcolor: styleColors.bg,
                              color: styleColors.text,
                              fontSize: '0.62rem',
                              fontWeight: 700,
                              border: '1px solid',
                              borderColor: styleColors.border,
                              '& .MuiChip-label': { px: 0.75 },
                            }}
                          />

                          {isConflito && (
                            <Tooltip title="Conflito de recursos" arrow>
                              <WarningAmberIcon sx={{ fontSize: '0.95rem', color: 'warning.main' }} />
                            </Tooltip>
                          )}
                        </Box>
                      </Box>
                    );
                  })}
                </Stack>
              </Grid>
            );
          })}
        </Grid>
      </Box>
    </Card>
  );
};

export default VisualizacaoSemanaCalendario;
