import { Box, Card, Paper, Typography } from '@mui/material';
import { Business as BusinessIcon, CalendarMonth as CalendarIcon, Schedule as ScheduleIcon } from '@mui/icons-material';
import StatusIcon from '../StatusIcon';
import { getEventoStatusMeta } from '../../utils/statusMeta/eventoStatus';
import { getCategoriaEventoMeta } from '../../utils/statusMeta/categoriaEventoStatus';
import { getCategoryColor, MONTH_NAMES } from '../../utils/calendarHelpers';
import { Evento } from '../../api/types';

interface CalendarAgendaListProps {
  agendaEvents: Evento[];
  onEventClick: (event: Evento) => void;
  getOrgName: (orgId: string, igrejaVinculoId?: string | null) => string;
}

/** Visualização em lista (agenda) dos próximos eventos do período visível, ordenados por data de
 * início — usada como alternativa à grade mensal (ver `viewMode` em `useCalendarGrid`). */
const CalendarAgendaList = ({ agendaEvents, onEventClick, getOrgName }: CalendarAgendaListProps) => {
  if (agendaEvents.length === 0) {
    return (
      <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
        <CalendarIcon sx={{ fontSize: 48, color: 'action.disabled' }} />
        <Typography variant="body2" color="text.secondary">
          Nenhum evento agendado para o período visível.
        </Typography>
      </Card>
    );
  }

  return (
    <Paper elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
      {agendaEvents.map((evt, idx) => {
        const styleColors = getCategoryColor(evt.categoria);
        const start = new Date(evt.inicio);
        const end = new Date(evt.fim);
        const isCancelado = evt.status?.toUpperCase() === 'CANCELADO';

        return (
          <Box
            key={evt.id}
            onClick={() => onEventClick(evt)}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 2,
              p: 2,
              cursor: 'pointer',
              borderBottom: idx < agendaEvents.length - 1 ? '1px solid' : 'none',
              borderColor: 'divider',
              borderLeft: '4px solid',
              borderLeftColor: isCancelado ? 'error.main' : styleColors.border,
              opacity: evt.aprovacaoPendenteId ? 0.6 : (isCancelado ? 0.8 : 1),
              '&:hover': { bgcolor: 'action.hover' },
            }}
          >
            {/* Date badge */}
            <Box
              sx={{
                width: 56,
                height: 56,
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
              <Typography variant="caption" sx={{ fontWeight: 700, fontSize: '0.65rem', color: isCancelado ? 'error.main' : 'text.secondary', textTransform: 'uppercase' }}>
                {MONTH_NAMES[start.getMonth()].substring(0, 3)}
              </Typography>
              <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.1, color: isCancelado ? 'error.dark' : 'text.primary' }}>
                {start.getDate()}
              </Typography>
            </Box>

            {/* Title, description & badges */}
            <Box sx={{ minWidth: 0, flexGrow: 1 }}>
              <Typography
                variant="body2"
                sx={{
                  fontWeight: 700,
                  color: isCancelado ? 'error.dark' : 'text.primary',
                  textDecoration: isCancelado ? 'line-through' : 'none',
                }}
              >
                {evt.aprovacaoPendenteId ? `[Pendente] ${evt.titulo}` : evt.titulo}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ lineClamp: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', mb: 0.5 }}>
                {evt.descricao || 'Sem descrição.'}
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
                <StatusIcon {...getCategoriaEventoMeta(evt.categoria)} />
                <StatusIcon
                  label={getOrgName(evt.grupoParoquialResponsavelId, evt.igrejaVinculoId)}
                  color="default"
                  icon={<BusinessIcon />}
                  tooltip="Organização responsável pelo evento"
                />
                <StatusIcon {...getEventoStatusMeta(evt.status)} />
              </Box>
            </Box>

            {/* Event time range */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'text.secondary', flexShrink: 0 }}>
              <ScheduleIcon sx={{ fontSize: 18 }} />
              <Typography variant="body2" sx={{ fontWeight: 500, whiteSpace: 'nowrap' }}>
                {start.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })} -{' '}
                {end.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
              </Typography>
            </Box>
          </Box>
        );
      })}
    </Paper>
  );
};

export default CalendarAgendaList;
