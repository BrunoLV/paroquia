import React from 'react';
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Typography,
  Box,
  Chip,
  Alert,
} from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import EventBusyIcon from '@mui/icons-material/EventBusy';
import EditCalendarIcon from '@mui/icons-material/EditCalendar';
import HowToVoteIcon from '@mui/icons-material/HowToVote';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { ReservaConflitoDetalhe } from '@/shared/domain/types/types';

interface EventoConflitoRecursoDialogProps {
  open: boolean;
  onClose: () => void;
  detalhes: ReservaConflitoDetalhe[];
  mensagem?: string;
  onConfirmarDiscussao?: () => void;
  isSubmitting?: boolean;
}

export const EventoConflitoRecursoDialog: React.FC<EventoConflitoRecursoDialogProps> = ({
  open,
  onClose,
  detalhes,
  mensagem,
  onConfirmarDiscussao,
  isSubmitting = false,
}) => {
  const formatarData = (iso?: string) => {
    if (!iso) return 'Não informado';
    try {
      const d = new Date(iso);
      if (isNaN(d.getTime())) return iso;
      return d.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return iso;
    }
  };

  const getTipoLabel = (c: ReservaConflitoDetalhe) => {
    const tipo = c.tipoItem || c.tipoConflito;
    if (tipo === 'LOCAL' || tipo === 'ESPACO_FISICO') return 'Espaço Físico';
    if (tipo === 'ATIVO' || tipo === 'ITEM_PATRIMONIAL') return 'Ativo Patrimonial';
    if (tipo === 'AGENDA_ECLESIAL' || tipo === 'PRIORIDADE_AGENDA') return 'Agenda Eclesial';
    return 'Recurso / Espaço';
  };

  const getTipoColor = (c: ReservaConflitoDetalhe): 'warning' | 'error' | 'info' | 'default' => {
    const tipo = c.tipoItem || c.tipoConflito;
    if (tipo === 'LOCAL' || tipo === 'ESPACO_FISICO') return 'warning';
    if (tipo === 'ATIVO' || tipo === 'ITEM_PATRIMONIAL') return 'info';
    return 'error';
  };

  return (
    <Dialog open={open} onClose={() => !isSubmitting && onClose()} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1.5, color: 'warning.main' }}>
        <WarningAmberIcon color="warning" sx={{ fontSize: 28 }} />
        <Typography variant="h6" component="span" sx={{ fontWeight: 700 }}>
          Conflitos Identificados com Eventos/Reservas Aprovados
        </Typography>
      </DialogTitle>
      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        <Alert severity="warning" sx={{ mb: 2.5 }}>
          {mensagem ||
            'Foram identificadas sobreposições de horário ou de intervalo de preparação e desocupação com eventos ou reservas já confirmados. Você pode retornar para ajustar os horários ou submeter o evento para discussão e deliberação do Conselho Paroquial.'}
        </Alert>

        <Stack spacing={2}>
          {detalhes.map((c, idx) => {
            const dataInicio = c.dataHoraInicio || c.inicioUtc || c.inicioOcupado;
            const dataFim = c.fimComBuffer || c.fimUtc || c.fimOcupadoComBuffer;
            const nomeRecurso = c.itemNome || c.recursoNome || 'Recurso / Espaço';
            const eventoConcorrente = c.eventoConcorrenteTitulo || c.motivo || c.descricaoReserva || c.detalhe;
            const responsavel = c.responsavelNome || c.solicitanteOuResponsavel || c.solicitanteNome;

            return (
              <Box
                key={idx}
                sx={{
                  p: 2,
                  borderRadius: 2,
                  bgcolor: 'background.paper',
                  border: '1px solid',
                  borderColor: 'warning.light',
                  boxShadow: 1,
                }}
              >
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1, justifyContent: 'space-between', flexWrap: 'wrap' }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                    <EventBusyIcon fontSize="small" color="warning" />
                    <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                      {nomeRecurso}
                    </Typography>
                  </Stack>
                  <Chip label={getTipoLabel(c)} color={getTipoColor(c)} size="small" variant="outlined" />
                </Stack>

                <Typography variant="body2" color="text.primary" sx={{ mb: 0.5 }}>
                  <strong>Período Ocupado (com intervalo de preparação):</strong>{' '}
                  {formatarData(dataInicio)} às {formatarData(dataFim)}
                </Typography>

                {eventoConcorrente && (
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                    <strong>Evento ou Reserva Concorrente:</strong> {eventoConcorrente}
                  </Typography>
                )}

                {responsavel && (
                  <Typography variant="body2" color="text.secondary">
                    <strong>Responsável / Solicitante:</strong> {responsavel}
                  </Typography>
                )}
              </Box>
            );
          })}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ p: 2, justifyContent: 'space-between', gap: 1 }}>
        <ActionButton
          title="Ajustar Horário ou Espaço"
          icon={<EditCalendarIcon />}
          color="inherit"
          variant="outlined"
          onClick={onClose}
          disabled={isSubmitting}
        />
        {onConfirmarDiscussao && (
          <ActionButton
            title="Cadastrar para Discussão com Conselho"
            icon={<HowToVoteIcon />}
            color="primary"
            variant="contained"
            onClick={onConfirmarDiscussao}
            disabled={isSubmitting}
          />
        )}
      </DialogActions>
    </Dialog>
  );
};
