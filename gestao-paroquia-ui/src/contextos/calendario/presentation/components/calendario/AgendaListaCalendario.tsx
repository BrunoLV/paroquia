import { Box, Card, Paper, Typography } from '@mui/material';
import BusinessIcon from '@mui/icons-material/Business';
import CalendarIcon from '@mui/icons-material/CalendarMonth';
import ScheduleIcon from '@mui/icons-material/Schedule';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { getEventoStatusMeta } from '@/shared/utils/statusMeta/eventoStatus';
import { getCategoriaEventoMeta } from '@/shared/utils/statusMeta/categoriaEventoStatus';
import { getCategoryColor, MONTH_NAMES } from '@/shared/utils/calendarHelpers';
import { Evento } from '@/shared/domain/types/types';
import { formatarHorarioEvento } from '@/contextos/calendario/presentation/utils/horarioEventoUtils';
import { getFormatoEventoIcon, getFormatoEventoLabel } from '@/contextos/calendario/presentation/utils/formatoEventoUtils';

export interface AgendaListaCalendarioProps {
  agendaEvents: Evento[];
  onEventClick: (event: Evento) => void;
  getOrgName: (orgId: string, igrejaVinculoId?: string | null) => string;
}

export type CalendarAgendaListProps = AgendaListaCalendarioProps;

/** Visualização em lista (agenda) dos próximos eventos do período visível, ordenados por data de
 * início — usada como alternativa à grade mensal (ver `viewMode` em `useCalendarGrid`). */
const AgendaListaCalendario = ({ agendaEvents, onEventClick, getOrgName }: AgendaListaCalendarioProps) => {
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

        let borderLeftColor = styleColors.border;
        if (isCancelado || isFalhaExecucao) {
          borderLeftColor = 'error.main';
        } else if (isConflito) {
          borderLeftColor = 'warning.main';
        }

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
              borderLeftColor: borderLeftColor,
              opacity: isPendenteAprovacao ? 0.6 : (isCancelado ? 0.8 : 1),
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
                  color: isCancelado || isFalhaExecucao ? 'error.dark' : isConflito ? 'warning.dark' : 'text.primary',
                  textDecoration: isCancelado ? 'line-through' : 'none',
                }}
              >
                {displayTitle}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ lineClamp: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', mb: 0.5 }}>
                {evt.descricao || 'Sem descrição.'}
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
                <StatusIcon
                  label={getFormatoEventoLabel(evt.formato)}
                  color="default"
                  icon={getFormatoEventoIcon(evt.formato, 'small')}
                  tooltip={`Formato de realização: ${getFormatoEventoLabel(evt.formato)}`}
                />
                <StatusIcon {...getCategoriaEventoMeta(evt.categoria)} />
                <StatusIcon
                  label={getOrgName(evt.grupoParoquialResponsavelId, evt.igrejaVinculoId)}
                  color="default"
                  icon={<BusinessIcon />}
                  tooltip="Organização responsável pelo evento"
                />
                <StatusIcon {...getEventoStatusMeta(evt.status)} />
                {isConflito && (
                  <StatusIcon
                    label="Conflito Pendente"
                    color="warning"
                    icon={<WarningAmberIcon />}
                    tooltip={evt.motivoConflito || 'Conflito de agenda ou recursos pendente de deliberação'}
                  />
                )}
              </Box>
            </Box>

            {/* Event time range */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'text.secondary', flexShrink: 0 }}>
              <ScheduleIcon sx={{ fontSize: 18 }} />
              <Typography variant="body2" sx={{ fontWeight: 500, whiteSpace: 'nowrap' }}>
                {formatarHorarioEvento(evt)}
              </Typography>
            </Box>
          </Box>
        );
      })}
    </Paper>
  );
};

export const CalendarAgendaList = AgendaListaCalendario;
export default AgendaListaCalendario;
