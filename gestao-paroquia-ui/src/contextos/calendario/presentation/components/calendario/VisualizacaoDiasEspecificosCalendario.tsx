import { useMemo, useState } from 'react';
import {
  alpha,
  Box,
  Card,
  CardContent,
  Chip,
  Stack,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import DateRangeIcon from '@mui/icons-material/DateRange';
import ViewListIcon from '@mui/icons-material/ViewList';
import ViewTimelineIcon from '@mui/icons-material/ViewTimeline';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { Evento } from '@/shared/domain/types/types';
import { isSameDay, MONTH_NAMES } from '@/shared/utils/calendarHelpers';
import { getLiturgicalSeason, getLiturgicalSeasonStyle } from '@/shared/utils/liturgicalCalendar';
import CartaoEventoCalendario from '@/contextos/calendario/presentation/components/calendario/CartaoEventoCalendario';
import TimelineDiaCalendario from '@/contextos/calendario/presentation/components/calendario/TimelineDiaCalendario';

export type ModoExibicaoPeriodo = 'TIMELINE' | 'LISTA';

const WEEKDAY_NAMES = [
  'Domingo',
  'Segunda-feira',
  'Terça-feira',
  'Quarta-feira',
  'Quinta-feira',
  'Sexta-feira',
  'Sábado',
];

export interface VisualizacaoDiasEspecificosCalendarioProps {
  startDate: Date;
  endDate: Date;
  agendaEvents: Evento[];
  onEventClick: (event: Evento) => void;
  onDayClick: (date: Date) => void;
  getOrgName: (orgId: string, igrejaVinculoId?: string | null) => string;
  getLocalName?: (localId: string | null) => string | null;
}

interface GrupoDiaEventos {
  date: Date;
  dateKey: string;
  events: Evento[];
}

/**
 * Visualização de dias específicos / intervalo customizado (DIAS_ESPECIFICOS) do calendário.
 * Agrupa cronologicamente os eventos do período solicitado com cabeçalhos litúrgicos padronizados
 * e cartões de evento canônicos uniformes com a visualização diária.
 */
export const VisualizacaoDiasEspecificosCalendario = ({
  startDate,
  endDate,
  agendaEvents,
  onEventClick,
  onDayClick,
  getOrgName,
  getLocalName,
}: VisualizacaoDiasEspecificosCalendarioProps) => {
  const [modoExibicao, setModoExibicao] = useState<ModoExibicaoPeriodo>('TIMELINE');

  // Agrupar eventos por dia
  const groupedEvents = useMemo(() => {
    const groups: GrupoDiaEventos[] = [];

    agendaEvents.forEach((evt) => {
      const evtDate = new Date(evt.inicio);
      const existingGroup = groups.find((g) => isSameDay(g.date, evtDate));

      if (existingGroup) {
        existingGroup.events.push(evt);
      } else {
        groups.push({
          date: new Date(evtDate.getFullYear(), evtDate.getMonth(), evtDate.getDate()),
          dateKey: `${evtDate.getFullYear()}-${evtDate.getMonth()}-${evtDate.getDate()}`,
          events: [evt],
        });
      }
    });

    return groups.sort((a, b) => a.date.getTime() - b.date.getTime());
  }, [agendaEvents]);

  const totalDias = Math.max(
    1,
    Math.round((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)) + 1
  );

  return (
    <Stack spacing={2.5}>
      {/* Banner Informativo do Intervalo */}
      <Card
        elevation={0}
        sx={{
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: 2,
          bgcolor: 'action.hover',
        }}
      >
        <CardContent sx={{ p: { xs: 2, sm: 2.5 }, '&:last-child': { pb: { xs: 2, sm: 2.5 } } }}>
          <Box
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', md: 'row' },
              justifyContent: 'space-between',
              alignItems: { xs: 'flex-start', md: 'center' },
              gap: 2,
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <DateRangeIcon sx={{ fontSize: 32, color: 'primary.main' }} />
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary' }}>
                  Período: {startDate.toLocaleDateString('pt-BR')} até {endDate.toLocaleDateString('pt-BR')}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {totalDias} dia(s) selecionado(s) • {agendaEvents.length} celebração(ões) e evento(s) no intervalo
                </Typography>
              </Box>
            </Box>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
              <Box
                sx={{
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 2,
                  overflow: 'hidden',
                  display: 'flex',
                  bgcolor: 'background.paper',
                }}
              >
                <ActionButton
                  label="Linha do Tempo"
                  title="Exibir como linha do tempo cronológica"
                  ariaLabel="Exibir Linha do Tempo no Período"
                  icon={<ViewTimelineIcon sx={{ fontSize: 18 }} />}
                  color="primary"
                  variant="text"
                  size="small"
                  onClick={() => setModoExibicao('TIMELINE')}
                  sx={{
                    border: 'none',
                    borderRadius: 0,
                    bgcolor: modoExibicao === 'TIMELINE' ? (theme) => theme.custom.navigationSelectedBg : 'transparent',
                    fontWeight: modoExibicao === 'TIMELINE' ? 700 : 500,
                  }}
                />

                <ActionButton
                  label="Lista"
                  title="Exibir como lista de cartões"
                  ariaLabel="Exibir Lista no Período"
                  icon={<ViewListIcon sx={{ fontSize: 18 }} />}
                  color="primary"
                  variant="text"
                  size="small"
                  onClick={() => setModoExibicao('LISTA')}
                  sx={{
                    border: 'none',
                    borderRadius: 0,
                    bgcolor: modoExibicao === 'LISTA' ? (theme) => theme.custom.navigationSelectedBg : 'transparent',
                    fontWeight: modoExibicao === 'LISTA' ? 700 : 500,
                  }}
                />
              </Box>

              <ActionButton
                label="Novo Evento no Período"
                title="Novo Evento no Período"
                ariaLabel="Novo Evento no Período"
                icon={<AddIcon />}
                color="primary"
                onClick={() => onDayClick(startDate)}
              />
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Lista de Grupos por Dia */}
      {groupedEvents.length === 0 ? (
        <Card
          elevation={0}
          sx={{
            p: 6,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 2,
            border: '1px dashed',
            borderColor: 'divider',
            borderRadius: 2,
            bgcolor: 'action.hover',
            textAlign: 'center',
          }}
        >
          <CalendarMonthIcon sx={{ fontSize: 56, color: 'action.disabled' }} />
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 600, color: 'text.primary', mb: 0.5 }}>
              Nenhum evento agendado para o período especificado
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 460 }}>
              Não foram localizadas celebrações, reuniões ou atividades entre {startDate.toLocaleDateString('pt-BR')} e {endDate.toLocaleDateString('pt-BR')}.
            </Typography>
          </Box>
          <ActionButton
            label="Agendar Evento"
            title="Criar evento para o início deste período"
            icon={<AddIcon />}
            color="primary"
            onClick={() => onDayClick(startDate)}
            sx={{ mt: 1 }}
          />
        </Card>
      ) : (
        <Stack spacing={2.5}>
          {groupedEvents.map((group) => {
            const season = getLiturgicalSeason(group.date);
            const seasonStyle = getLiturgicalSeasonStyle(season);
            const dayOfWeek = WEEKDAY_NAMES[group.date.getDay()];
            const dayOfMonth = group.date.getDate();
            const monthName = MONTH_NAMES[group.date.getMonth()];

            return (
              <Box key={group.dateKey}>
                {/* Cabeçalho do Dia */}
                <Card
                  elevation={0}
                  sx={{
                    mb: 1.5,
                    border: '1px solid',
                    borderColor: 'divider',
                    borderRadius: 2,
                    bgcolor: seasonStyle.backgroundColor,
                    borderLeft: '4px solid',
                    borderLeftColor: seasonStyle.textColor,
                  }}
                >
                  <Box
                    sx={{
                      p: 1.75,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      flexWrap: 'wrap',
                      gap: 1.5,
                    }}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                      <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
                        {dayOfWeek}, {dayOfMonth} de {monthName}
                      </Typography>
                      <Chip
                        label={seasonStyle.label}
                        size="small"
                        sx={{
                          bgcolor: seasonStyle.backgroundColor,
                          color: seasonStyle.textColor,
                          fontWeight: 700,
                          fontSize: '0.68rem',
                          border: '1px solid',
                          borderColor: alpha(seasonStyle.textColor, 0.3),
                        }}
                      />
                    </Box>

                    <ActionButton
                      label="+ Adicionar neste dia"
                      title="Adicionar evento nesta data"
                      icon={<AddIcon sx={{ fontSize: 16 }} />}
                      variant="text"
                      size="small"
                      color="primary"
                      onClick={() => onDayClick(group.date)}
                    />
                  </Box>
                </Card>

                {/* Renderização da Linha do Tempo ou Lista por Dia */}
                {modoExibicao === 'TIMELINE' ? (
                  <TimelineDiaCalendario
                    date={group.date}
                    events={group.events}
                    onEventClick={onEventClick}
                    onDayClick={onDayClick}
                    getOrgName={getOrgName}
                    getLocalName={getLocalName}
                  />
                ) : (
                  <Stack spacing={1.5} sx={{ pl: { xs: 0, sm: 1 } }}>
                    {group.events.map((evt) => (
                      <CartaoEventoCalendario
                        key={evt.id}
                        evento={evt}
                        onClick={() => onEventClick(evt)}
                        getOrgName={getOrgName}
                        getLocalName={getLocalName}
                      />
                    ))}
                  </Stack>
                )}
              </Box>
            );
          })}
        </Stack>
      )}
    </Stack>
  );
};

export default VisualizacaoDiasEspecificosCalendario;
