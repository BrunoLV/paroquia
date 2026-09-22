import { useState } from 'react';
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
import TodayIcon from '@mui/icons-material/Today';
import ViewListIcon from '@mui/icons-material/ViewList';
import ViewTimelineIcon from '@mui/icons-material/ViewTimeline';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { Evento } from '@/shared/domain/types/types';
import { MONTH_NAMES } from '@/shared/utils/calendarHelpers';
import { getLiturgicalSeason, getLiturgicalSeasonStyle } from '@/shared/utils/liturgicalCalendar';
import CartaoEventoCalendario from '@/contextos/calendario/presentation/components/calendario/CartaoEventoCalendario';
import TimelineDiaCalendario from '@/contextos/calendario/presentation/components/calendario/TimelineDiaCalendario';

const WEEKDAY_FULL_NAMES = [
  'Domingo',
  'Segunda-feira',
  'Terça-feira',
  'Quarta-feira',
  'Quinta-feira',
  'Sexta-feira',
  'Sábado',
];

export type ModoExibicaoDia = 'TIMELINE' | 'LISTA';

export interface VisualizacaoHojeCalendarioProps {
  currentDate: Date;
  events: Evento[];
  onDayClick: (date: Date) => void;
  onEventClick: (event: Evento) => void;
  getOrgName: (orgId: string, igrejaVinculoId?: string | null) => string;
  getLocalName?: (localId: string | null) => string | null;
}

/**
 * Visualização diária (HOJE) do calendário paroquial.
 * Apresenta banner litúrgico contextual do dia selecionado, resumo quantitativo de atividades
 * e alternador dinâmico entre Linha do Tempo (Timeline) e Lista de cartões uniformizados.
 */
export const VisualizacaoHojeCalendario = ({
  currentDate,
  events,
  onDayClick,
  onEventClick,
  getOrgName,
  getLocalName,
}: VisualizacaoHojeCalendarioProps) => {
  const [modoExibicao, setModoExibicao] = useState<ModoExibicaoDia>('TIMELINE');

  const dayOfWeek = WEEKDAY_FULL_NAMES[currentDate.getDay()];
  const dayOfMonth = currentDate.getDate();
  const monthName = MONTH_NAMES[currentDate.getMonth()];
  const year = currentDate.getFullYear();

  const season = getLiturgicalSeason(currentDate);
  const seasonStyle = getLiturgicalSeasonStyle(season);

  // Ordenação cronológica dos eventos do dia
  const sortedEvents = [...events].sort(
    (a, b) => new Date(a.inicio).getTime() - new Date(b.inicio).getTime()
  );

  return (
    <Stack spacing={2.5}>
      {/* Banner Litúrgico e Resumo do Dia */}
      <Card
        elevation={0}
        sx={{
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: 2,
          bgcolor: seasonStyle.backgroundColor,
          borderLeft: '5px solid',
          borderLeftColor: seasonStyle.textColor,
          transition: 'all 0.2s ease',
        }}
      >
        <CardContent sx={{ p: { xs: 2.5, md: 3 }, '&:last-child': { pb: { xs: 2.5, md: 3 } } }}>
          <Box
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', md: 'row' },
              justifyContent: 'space-between',
              alignItems: { xs: 'flex-start', md: 'center' },
              gap: 2,
            }}
          >
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.5, flexWrap: 'wrap' }}>
                <Typography variant="h5" sx={{ fontWeight: 700, color: 'text.primary' }}>
                  {dayOfWeek}, {dayOfMonth} de {monthName} de {year}
                </Typography>
                <Chip
                  label={seasonStyle.label}
                  size="small"
                  sx={{
                    bgcolor: seasonStyle.backgroundColor,
                    color: seasonStyle.textColor,
                    fontWeight: 700,
                    fontSize: '0.7rem',
                    border: '1px solid',
                    borderColor: alpha(seasonStyle.textColor, 0.3),
                  }}
                />
              </Box>
              <Typography variant="body2" color="text.secondary">
                {sortedEvents.length === 0
                  ? 'Nenhuma celebração ou atividade agendada para esta data. Selecione um horário na linha do tempo abaixo para agendar.'
                  : `${sortedEvents.length} atividade(s) pastoral(is) e celebrativa(s) agendada(s).`}
              </Typography>
            </Box>

            {/* Ações: Alternador de Visualização (Timeline / Lista) + Novo Evento */}
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
                  ariaLabel="Exibir Linha do Tempo"
                  icon={<ViewTimelineIcon sx={{ fontSize: 18 }} />}
                  color="primary"
                  variant="text"
                  size="small"
                  onClick={() => setModoExibicao('TIMELINE')}
                  sx={{
                    border: 'none',
                    borderRadius: 0,
                    bgcolor: modoExibicao === 'TIMELINE' ? (theme) => alpha(theme.palette.primary.main, 0.12) : 'transparent',
                    fontWeight: modoExibicao === 'TIMELINE' ? 700 : 500,
                  }}
                />

                <ActionButton
                  label="Lista"
                  title="Exibir como lista de cartões"
                  ariaLabel="Exibir Lista de Cartões"
                  icon={<ViewListIcon sx={{ fontSize: 18 }} />}
                  color="primary"
                  variant="text"
                  size="small"
                  onClick={() => setModoExibicao('LISTA')}
                  sx={{
                    border: 'none',
                    borderRadius: 0,
                    bgcolor: modoExibicao === 'LISTA' ? (theme) => alpha(theme.palette.primary.main, 0.12) : 'transparent',
                    fontWeight: modoExibicao === 'LISTA' ? 700 : 500,
                  }}
                />
              </Box>

              <ActionButton
                label="Novo Evento neste Dia"
                title="Novo Evento neste Dia"
                icon={<AddIcon />}
                color="primary"
                onClick={() => onDayClick(currentDate)}
              />
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Renderização da Linha do Tempo ou da Lista */}
      {modoExibicao === 'TIMELINE' ? (
        <TimelineDiaCalendario
          date={currentDate}
          events={sortedEvents}
          onEventClick={onEventClick}
          onDayClick={onDayClick}
          getOrgName={getOrgName}
          getLocalName={getLocalName}
        />
      ) : sortedEvents.length === 0 ? (
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
          <TodayIcon sx={{ fontSize: 56, color: 'action.disabled' }} />
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 600, color: 'text.primary', mb: 0.5 }}>
              Dia livre de compromissos oficiais
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 460 }}>
              Não constam missas, reuniões pastorais ou reservas de espaço agendadas para esta data na paróquia.
            </Typography>
          </Box>
          <ActionButton
            label="Agendar Atividade"
            title="Criar evento nesta data"
            icon={<AddIcon />}
            color="primary"
            onClick={() => onDayClick(currentDate)}
            sx={{ mt: 1 }}
          />
        </Card>
      ) : (
        <Stack spacing={1.5}>
          {sortedEvents.map((evt) => (
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
    </Stack>
  );
};

export default VisualizacaoHojeCalendario;
