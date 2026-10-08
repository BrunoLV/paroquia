import {
  Box,
  Card,
  CardContent,
  Chip,
  Typography,
} from '@mui/material';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import BusinessIcon from '@mui/icons-material/Business';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { Evento } from '@/shared/domain/types/types';
import { getCategoryColor } from '@/shared/utils/calendarHelpers';
import { formatarHorarioEvento } from '@/contextos/calendario/presentation/utils/horarioEventoUtils';
import { getFormatoEventoIcon, getFormatoEventoLabel } from '@/contextos/calendario/presentation/utils/formatoEventoUtils';

export interface CartaoEventoCalendarioProps {
  evento: Evento;
  onClick: () => void;
  getOrgName: (orgId: string, igrejaVinculoId?: string | null) => string;
  getLocalName?: (localId: string | null) => string | null;
}

/**
 * Componente canônico de cartão de evento para visualizações em lista e timeline
 * (compartilhado entre HOJE e DIAS ESPECÍFICOS).
 * Garante uniformidade visual estrita em conformidade com o DESIGN_SYSTEM.md.
 */
export const CartaoEventoCalendario = ({
  evento,
  onClick,
  getOrgName,
  getLocalName,
}: CartaoEventoCalendarioProps) => {
  const styleColors = getCategoryColor(evento.categoria);
  const isCancelado = evento.status?.toUpperCase() === 'CANCELADO';
  const isFalhaExecucao = evento.status?.toUpperCase() === 'FALHA_EXECUCAO';
  const isConflito = evento.estadoConflito?.toUpperCase() === 'PENDENTE';
  const isPendenteAprovacao = Boolean(evento.aprovacaoPendenteId);

  let borderLeftColor = styleColors.border;
  if (isCancelado || isFalhaExecucao) {
    borderLeftColor = 'error.main';
  } else if (isConflito) {
    borderLeftColor = 'warning.main';
  }

  const localNome = getLocalName ? getLocalName(evento.localId) : null;
  const orgNome = getOrgName(evento.organizacaoResponsavelId);
  const horarioTexto = formatarHorarioEvento(evento);
  const formatoIcon = getFormatoEventoIcon(evento.formato, 'small');
  const formatoLabel = getFormatoEventoLabel(evento.formato);

  return (
    <Card
      elevation={0}
      onClick={onClick}
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderLeft: '4px solid',
        borderLeftColor: borderLeftColor,
        borderRadius: 2,
        cursor: 'pointer',
        opacity: isPendenteAprovacao ? 0.75 : isCancelado ? 0.75 : 1,
        transition: 'all 0.15s ease-in-out',
        bgcolor: 'background.paper',
        '&:hover': {
          bgcolor: 'action.hover',
          boxShadow: (theme) => theme.shadows[2],
          transform: 'translateY(-1px)',
        },
      }}
    >
      <CardContent sx={{ p: { xs: 2, sm: 2.25 }, '&:last-child': { pb: { xs: 2, sm: 2.25 } } }}>
        <Box
          sx={{
            display: 'flex',
            flexDirection: { xs: 'column', md: 'row' },
            justifyContent: 'space-between',
            alignItems: { xs: 'flex-start', md: 'center' },
            gap: 2,
          }}
        >
          {/* Informações Principais do Evento */}
          <Box sx={{ flexGrow: 1, minWidth: 0 }}>
            {/* Linha de Badges / Chips */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.75, flexWrap: 'wrap' }}>
              <Chip
                size="small"
                label={evento.categoria}
                sx={{
                  bgcolor: styleColors.bg,
                  color: styleColors.text,
                  fontWeight: 700,
                  fontSize: '0.7rem',
                  border: '1px solid',
                  borderColor: styleColors.border,
                }}
              />

              <Chip
                size="small"
                icon={formatoIcon}
                label={formatoLabel}
                variant="outlined"
                sx={{ fontSize: '0.7rem' }}
              />

              {isPendenteAprovacao && (
                <StatusIcon
                  label="Aprovação Pendente"
                  color="warning"
                  icon={<AccessTimeIcon sx={{ fontSize: 14 }} />}
                  tooltip="Aguardando confirmação pastoral"
                  size="small"
                />
              )}

              {isConflito && (
                <Chip
                  size="small"
                  icon={<WarningAmberIcon />}
                  label="Conflito de Recursos"
                  color="warning"
                  sx={{ fontWeight: 600, fontSize: '0.7rem' }}
                />
              )}

              {isCancelado && (
                <Chip
                  size="small"
                  label="Cancelado"
                  color="error"
                  variant="outlined"
                  sx={{ fontWeight: 600, fontSize: '0.7rem' }}
                />
              )}
            </Box>

            {/* Título */}
            <Typography
              variant="subtitle1"
              sx={{
                fontWeight: 600,
                color: isCancelado ? 'text.disabled' : 'text.primary',
                textDecoration: isCancelado ? 'line-through' : 'none',
                mb: evento.descricao ? 0.5 : 1,
                lineHeight: 1.3,
              }}
            >
              {isPendenteAprovacao ? `[Pendente] ${evento.titulo}` : evento.titulo}
            </Typography>

            {/* Descrição */}
            {evento.descricao && (
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{
                  mb: 1.25,
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden',
                  lineHeight: 1.4,
                }}
              >
                {evento.descricao}
              </Typography>
            )}

            {/* Metadados: Horário, Organização e Local */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2.5, flexWrap: 'wrap' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                <AccessTimeIcon sx={{ fontSize: 16, color: 'primary.main' }} />
                <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary' }}>
                  {horarioTexto}
                </Typography>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                <BusinessIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
                <Typography variant="body2" color="text.secondary">
                  {orgNome}
                </Typography>
              </Box>

              {localNome && (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                  <LocationOnIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
                  <Typography variant="body2" color="text.secondary">
                    {localNome}
                  </Typography>
                </Box>
              )}
            </Box>
          </Box>

          {/* Botão de Ação */}
          <Box sx={{ alignSelf: { xs: 'flex-end', md: 'center' }, flexShrink: 0 }}>
            <ActionButton
              label="Detalhes"
              title="Ver detalhes do evento"
              ariaLabel="Detalhes"
              icon={<CalendarMonthIcon sx={{ fontSize: 16 }} />}
              variant="outlined"
              size="small"
              color="primary"
              onClick={(e) => {
                e.stopPropagation();
                onClick();
              }}
            />
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export default CartaoEventoCalendario;
