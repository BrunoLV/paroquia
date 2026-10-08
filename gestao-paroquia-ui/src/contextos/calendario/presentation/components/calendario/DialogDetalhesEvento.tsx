import React from 'react';
import {
  Alert,
  Avatar,
  Box,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import BusinessIcon from '@mui/icons-material/Business';
import CancelIcon from '@mui/icons-material/Cancel';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CloseIcon from '@mui/icons-material/Close';
import DescriptionIcon from '@mui/icons-material/Description';
import EditIcon from '@mui/icons-material/Edit';
import EventIcon from '@mui/icons-material/Event';
import PeopleIcon from '@mui/icons-material/Groups';
import LocationIcon from '@mui/icons-material/LocationOn';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import RepeatIcon from '@mui/icons-material/Repeat';
import ScheduleIcon from '@mui/icons-material/Schedule';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import ObservacoesEvento from '@/contextos/calendario/presentation/components/ObservacoesEvento';
import AtivosAlocadosSection from '@/contextos/patrimonio/presentation/components/AtivosAlocadosSection';
import { getEventoStatusMeta } from '@/shared/utils/statusMeta/eventoStatus';
import { getCategoriaEventoMeta } from '@/shared/utils/statusMeta/categoriaEventoStatus';
import { RecurrenceRule } from '@/contextos/calendario/presentation/components/FormularioRecorrencia';
import { Evento, SetorPastoral } from '@/shared/domain/types/types';
import { formatarHorarioEvento } from '@/contextos/calendario/presentation/utils/horarioEventoUtils';
import { getFormatoEventoIcon, getFormatoEventoLabel } from '@/contextos/calendario/presentation/utils/formatoEventoUtils';
import { formatarResumoRecorrencia } from '@/contextos/calendario/presentation/utils/recorrenciaFormatUtils';

/** 1=Solenidade, 2=Preceito, 3=Pastoral, 4=Movimento — ver `Evento.nivelPrioridade` em api/types.ts. */
const NIVEL_PRIORIDADE_LABELS: Record<number, string> = {
  1: 'Nível 1 — Solenidade',
  2: 'Nível 2 — Preceito',
  3: 'Nível 3 — Pastoral',
  4: 'Nível 4 — Movimento',
};

const SETOR_PASTORAL_LABELS: Record<SetorPastoral, string> = {
  SANTIFICACAO_LITURGIA: 'Santificação / Liturgia',
  FORMACAO_ENSINAR: 'Formação / Ensino',
  SOCIAL_CARIDADE: 'Social / Caridade',
};

/**
 * Campo de exibição rotulado usado na ficha de detalhes do evento — mesmo padrão visual
 * de `InfoField` em `fieis/fielConstants.tsx` (label + ícone + valor em Paper outlined),
 * seguindo o conceito visual de referência da página Fiéis.
 */
const InfoField = ({ label, value, icon }: { label: string; value?: React.ReactNode; icon?: React.ReactNode }) => (
  <Paper
    variant="outlined"
    sx={{
      p: 2,
      borderRadius: 2,
      bgcolor: 'background.paper',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
    }}
  >
    <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 0.75 }}>
      {icon && <Box sx={{ color: 'primary.main', display: 'flex', fontSize: 18 }}>{icon}</Box>}
      <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
        {label}
      </Typography>
    </Stack>
    <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.4 }}>
      {value ?? '-'}
    </Typography>
  </Paper>
);

export interface DialogDetalhesEventoProps {
  open: boolean;
  onClose: () => void;
  event: Evento | null;
  recorrencia: RecurrenceRule | null | undefined;
  canEdit: boolean;
  onEdit: (event: Evento) => void;
  onCancel: (event: Evento) => void;
  getOrgName: (orgId: string, igrejaVinculoId?: string | null) => string;
  getLocalName: (localId: string | null) => string | null;
}

export type EventoDetailsDialogProps = DialogDetalhesEventoProps;

/**
 * Ficha de detalhes de um evento do calendário: agenda, organização/vínculos, ativos alocados,
 * classificação/prioridade, recorrência (quando aplicável) e observações. Editar/Cancelar ficam
 * ocultos enquanto há uma solicitação pendente para o evento (`aprovacaoPendenteId`), para evitar
 * empilhar uma segunda solicitação de mudança sobre a mesma entidade antes que a primeira seja
 * decidida pelo aprovador.
 */
const DialogDetalhesEvento = ({
  open,
  onClose,
  event,
  recorrencia,
  canEdit,
  onEdit,
  onCancel,
  getOrgName,
  getLocalName,
}: EventoDetailsDialogProps) => (
  <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
    {event && (
      <>
        <DialogTitle sx={{ py: 2.5, px: { xs: 2.5, sm: 3.5 }, fontWeight: 700 }}>
          <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography variant="h6" component="div" sx={{ fontWeight: 700 }}>
              Detalhes do evento
            </Typography>
            <StatusIcon {...getEventoStatusMeta(event.status)} />
          </Stack>
        </DialogTitle>
        <DialogContent dividers sx={{ ...DIALOG_CONTENT_SCROLL_SX, p: { xs: 2.5, sm: 3.5 } }}>
          <Stack spacing={3.5}>
            {/* Quando este evento tem uma solicitação de aprovação em aberto
                (ver merge em useCalendarData/combinedEvents), avisamos qual tipo de mudança está
                pendente — o conteúdo abaixo ainda reflete o estado atual/salvo do
                evento, não a mudança proposta ainda não aprovada. */}
            {event.tipoSolicitacaoPendente && (
              <Alert severity="warning">
                {event.tipoSolicitacaoPendente === 'CRIACAO_EVENTO'
                  ? 'Solicitação de Criação Pendente'
                  : event.tipoSolicitacaoPendente === 'CANCELAMENTO'
                  ? 'Solicitação de Cancelamento Pendente'
                  : event.tipoSolicitacaoPendente === 'EDICAO_EVENTO'
                  ? 'Solicitação de Edição Pendente'
                  : 'Solicitação Pendente'}
              </Alert>
            )}

            {event.motivoConflito && (
              <Alert severity="warning">Conflito detectado: {event.motivoConflito}</Alert>
            )}

            {/* Header com Avatar e Título — mesmo padrão do cabeçalho da Ficha do Fiel */}
            <Paper
              variant="outlined"
              sx={{
                p: 2.5,
                borderRadius: 2,
                bgcolor: 'background.default',
                display: 'flex',
                flexDirection: { xs: 'column', sm: 'row' },
                alignItems: { xs: 'flex-start', sm: 'center' },
                gap: 2,
              }}
            >
              <Avatar sx={{ bgcolor: 'primary.main', color: 'primary.contrastText', width: 56, height: 56 }}>
                <EventIcon />
              </Avatar>
              <Box sx={{ flexGrow: 1 }}>
                <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main', lineHeight: 1.2 }}>
                  {event.titulo}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  {getOrgName(event.organizacaoResponsavelId, event.igrejaVinculoId)}
                </Typography>
                <Stack direction="row" spacing={1.5} sx={{ mt: 1, flexWrap: 'wrap', gap: 0.5 }}>
                  <StatusIcon
                    label={getFormatoEventoLabel(event.formato)}
                    color="default"
                    icon={getFormatoEventoIcon(event.formato, 'small')}
                    tooltip={`Formato de realização: ${getFormatoEventoLabel(event.formato)}`}
                  />
                  <StatusIcon {...getCategoriaEventoMeta(event.categoria)} />
                  {event.recorrenciaId && (
                    <StatusIcon
                      label="Recorrente"
                      color="info"
                      icon={<RepeatIcon />}
                      tooltip="Este evento pertence a uma série recorrente"
                    />
                  )}
                </Stack>
              </Box>
            </Paper>

            {/* Agenda */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <ScheduleIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Agenda
                  </Typography>
                </Stack>
                <Grid container spacing={2.5}>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <InfoField
                      label="Formato"
                      icon={getFormatoEventoIcon(event.formato, 'inherit')}
                      value={getFormatoEventoLabel(event.formato)}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <InfoField
                      label="Horário"
                      icon={<ScheduleIcon fontSize="inherit" />}
                      value={formatarHorarioEvento(event)}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 5 }}>
                    <InfoField
                      label={event.localIds && event.localIds.length > 1 ? 'Locais' : 'Local'}
                      icon={<LocationIcon fontSize="inherit" />}
                      value={
                        event.localIds && event.localIds.length > 0 ? (
                          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 0.5 }}>
                            {event.localIds.map((id) => (
                              <Chip key={id} label={getLocalName(id) || id} size="small" variant="outlined" />
                            ))}
                          </Box>
                        ) : (
                          getLocalName(event.localId) || 'Não especificado'
                        )
                      }
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Organização e vínculos */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <BusinessIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Organização e vínculos
                  </Typography>
                </Stack>
                <Grid container spacing={2.5}>
                  <Grid size={{ xs: 12, sm: event.nomeProjeto ? 6 : 12 }}>
                    <InfoField
                      label="Organização responsável"
                      icon={<BusinessIcon fontSize="inherit" />}
                      value={getOrgName(event.organizacaoResponsavelId, event.igrejaVinculoId)}
                    />
                  </Grid>
                  {event.nomeProjeto && (
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <InfoField label="Projeto" icon={<BusinessIcon fontSize="inherit" />} value={event.nomeProjeto} />
                    </Grid>
                  )}
                  <Grid size={{ xs: 12 }}>
                    <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
                      <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center', mb: 1.25 }}>
                        <PeopleIcon fontSize="small" sx={{ color: 'primary.main' }} />
                        <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                          Organizações de apoio
                        </Typography>
                      </Stack>
                      {event.participantes && event.participantes.length > 0 ? (
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                          {event.participantes.map((id) => (
                            <StatusIcon key={id} color="default" icon={<BusinessIcon />} label={getOrgName(id)} tooltip="Organização de apoio" />
                          ))}
                        </Box>
                      ) : (
                        <Typography variant="body2" color="text.secondary">Nenhuma organização de apoio vinculada.</Typography>
                      )}
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <AtivosAlocadosSection eventoId={event.id} />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Classificação e prioridade */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <PriorityHighIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Classificação e prioridade
                  </Typography>
                </Stack>
                <Grid container spacing={2.5}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <InfoField
                      label="Prioridade da agenda"
                      value={event.nivelPrioridade ? NIVEL_PRIORIDADE_LABELS[event.nivelPrioridade] : '-'}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <InfoField
                      label="Setor pastoral"
                      value={event.setorPastoral ? SETOR_PASTORAL_LABELS[event.setorPastoral] : '-'}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <InfoField
                      label="Autorizado pelo CPP"
                      value={
                        <StatusIcon
                          label={event.autorizadoPeloCpp ? 'Sim' : 'Não'}
                          color={event.autorizadoPeloCpp ? 'success' : 'default'}
                          icon={event.autorizadoPeloCpp ? <CheckCircleIcon /> : <CancelIcon />}
                          tooltip="Liberação do Conselho Pastoral Paroquial para conflito de agenda"
                        />
                      }
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Recorrência — só aparece quando o evento pertence a uma série */}
            {event.recorrenciaId && (
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1.5 }}>
                    <RepeatIcon color="primary" fontSize="small" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      Recorrência
                    </Typography>
                  </Stack>
                  <Typography variant="body2" color="text.secondary">
                    {recorrencia ? formatarResumoRecorrencia(recorrencia, event.inicio) : 'Carregando regra de recorrência...'}
                  </Typography>
                </CardContent>
              </Card>
            )}

            {/* Descrição */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1.5 }}>
                  <DescriptionIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Descrição
                  </Typography>
                </Stack>
                <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'pre-wrap' }}>
                  {event.descricao || 'Nenhuma descrição fornecida.'}
                </Typography>
              </CardContent>
            </Card>

            {/* Event Observations */}
            <ObservacoesEvento eventoId={event.id} />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: { xs: 2.5, sm: 3.5 }, py: 2, flexWrap: 'wrap', gap: 1.25 }}>
          <ActionButton title="Fechar" icon={<CloseIcon />} onClick={onClose} color="secondary" />
          {event.status !== 'CANCELADO' && !event.aprovacaoPendenteId && canEdit && (
            <>
              <ActionButton title="Cancelar Evento" icon={<BlockIcon />} onClick={() => onCancel(event)} color="error" />
              <ActionButton title="Editar Evento" icon={<EditIcon />} onClick={() => onEdit(event)} color="primary" />
            </>
          )}
        </DialogActions>
      </>
    )}
  </Dialog>
);

export const EventoDetailsDialog = DialogDetalhesEvento;
export default DialogDetalhesEvento;
